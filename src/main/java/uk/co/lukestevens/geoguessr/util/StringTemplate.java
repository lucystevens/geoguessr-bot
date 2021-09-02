package uk.co.lukestevens.geoguessr.util;

import java.util.HashMap;
import java.util.Map;

public class StringTemplate {

    private final String template;
    private final Map<String, String> variables = new HashMap<>();

    public static StringTemplate fromTemplate(String template){
        return new StringTemplate(template);
    }

    StringTemplate(String template) {
        this.template = template;
    }

    public StringTemplate withVariable(String variable, String value){
        this.variables.put(variable, value);
        return this;
    }
    public StringTemplate withVariable(String variable, int value){
        this.variables.put(variable, String.valueOf(value));
        return this;
    }


    public StringTemplate withVariables(Map<String, String> variables){
        this.variables.putAll(variables);
        return this;
    }

    public String build(){
        String value = template;
        for(Map.Entry<String, String> variable : variables.entrySet()){
            value = value.replace("{{" + variable.getKey() + "}}", variable.getValue());
        }
        return value;
    }
}
