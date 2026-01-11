package io.github.itokagimaru.artifact.artifact.artifacts.data.effect.value;

public class Calculator {
    public enum calculateType{
        ADD("add"),
        MULTIPLY("multiply");
        private String type;
        calculateType(String type){
            this.type = type;
        }

        public static calculateType fromText(String text){
            for(calculateType type : calculateType.values()){
                if(type.type.equalsIgnoreCase(text)){
                    return type;
                }
            }
            return null;
        }
    }
}
