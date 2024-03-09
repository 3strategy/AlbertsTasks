package com.example.tasks;

public class Utilities {
    public static String db2Dsiplay(String str){
        return str.substring(6,8)+"-"+str.substring(4,6)+"-"+str.substring(0,4);
    }
    public static String display2DB(String str){
        return str.substring(7,11)+str.substring(4,6)+str.substring(0,2);
    }
}
