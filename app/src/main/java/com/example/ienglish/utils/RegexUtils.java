package com.example.ienglish.utils;

import java.util.ArrayList;
import java.util.regex.Pattern;

/**
 * 没用上，懒得删
 */

public class RegexUtils {

    public static ArrayList<String> getNumbs(ArrayList<String> wordList){

        ArrayList<String> numbs = new ArrayList<>();
        boolean hasFound = false;

        String regEx1 = "[a-zA-Z0-9]";
        //String regEx2 = "^[①|②|③|④|⑤][0-9]{10}\\+[0-9]{4}$";

        for (String word : wordList) {

            if( !hasFound && Pattern.matches(regEx1,word)){
                hasFound = true;
                numbs.add(word);
            }
        }

        return  numbs;
    }


}
