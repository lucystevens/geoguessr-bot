package uk.co.lukestevens.geoguessr.util;

public class SystemExit {

    private static boolean exitEnabled = true;

    public static void forceExit(int exitCode){
        if(exitEnabled){
            System.exit(exitCode);
        }
    }

    public static void disableExit(){
        exitEnabled = false;
    }
}
