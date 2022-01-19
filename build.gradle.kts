import com.avast.gradle.dockercompose.ComposeSettings

plugins {
    java
    application
    `maven-publish`
    kotlin("jvm") version "1.6.0"
    id("com.avast.gradle.docker-compose") version "0.14.9"
    id("com.github.johnrengelman.shadow") version "7.1.2"
    id("uk.co.lukestevens.plugins.release-helper") version "0.1.0"
}

fun RepositoryHandler.githubPackages() = maven {
    name = "GitHubPackages"
    url = uri("https://maven.pkg.github.com/lukecmstevens/packages")
    credentials {
        username = System.getenv("GH_USER")
        password = System.getenv("GH_TOKEN")
    }
}

repositories {
    mavenLocal()
    mavenCentral()
    githubPackages()
}

group = "uk.co.lukestevens"

application {
    mainClass.set("uk.co.lukestevens.geoguessr.GeoGuessrBotMain")
}

sourceSets {
    create("integrationTest") {
        compileClasspath += sourceSets.main.get().output + sourceSets.test.get().output
        runtimeClasspath += sourceSets.main.get().output + sourceSets.test.get().output
    }
}

val integrationTestImplementation: Configuration by configurations.getting {
    extendsFrom(configurations.testImplementation.get())
}

dependencies {
    compileOnly("uk.co.lukestevens:base-lib:2.1.0")
    implementation("uk.co.lukestevens:db-lib:2.0.0")
    implementation("uk.co.lukestevens:config-lib:2.0.0")
    implementation("uk.co.lukestevens:logging-lib:2.0.0")

    implementation("com.google.code.gson:gson:2.8.5")
    implementation("com.google.inject:guice:4.2.2")
    implementation("com.squareup.okhttp3:okhttp:4.4.1")
    implementation("org.postgresql:postgresql:42.2.6")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.7.0")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:5.7.0")
    testImplementation("io.mockk:mockk:1.12.2")

    integrationTestImplementation("com.github.tomakehurst:wiremock-jre8:2.31.0")
}

/**
 *  Tasks
 */
configure<ComposeSettings> {
    startedServices.set(listOf("db-schemas"))
    forceRecreate.set(true)
}

tasks.test {
    useJUnitPlatform()
}

tasks.jar {
    manifest {
        attributes("Main-Class" to application.mainClass.get())
    }
}

val integrationTest = task<Test>("integrationTest") {
    useJUnitPlatform()
    description = "Runs integration tests."
    group = "verification"

    testClassesDirs = sourceSets["integrationTest"].output.classesDirs
    classpath = sourceSets["integrationTest"].runtimeClasspath
    outputs.upToDateWhen { false }
    mustRunAfter(tasks.composeUp)
}

/*val integrationTestWithDockerCompose = task("integrationTestWithDockerCompose") {
    dependsOn(integrationTest)
    integrationTest.mustRunAfter(tasks.composeUp)
}*/

dockerCompose.isRequiredBy(integrationTest)
