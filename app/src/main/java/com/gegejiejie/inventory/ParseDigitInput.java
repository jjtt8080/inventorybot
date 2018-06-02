package com.gegejiejie.inventory;
import android.util.Log;

import java.util.HashMap;

public class ParseDigitInput {
    private static HashMap<String, Integer> numberLiterals = null;

    public static void Init() {
        if (numberLiterals == null) {
            numberLiterals = new HashMap<String, Integer>();
            numberLiterals.put("one", 1);
            numberLiterals.put("two", 2);
            numberLiterals.put("three", 3);
            numberLiterals.put("four", 4);
            numberLiterals.put("five", 5);
            numberLiterals.put("six", 6);
            numberLiterals.put("seven", 7);
            numberLiterals.put("eight", 8);
            numberLiterals.put("nine", 9);
            numberLiterals.put("ten", 10);
            numberLiterals.put("eleven", 11);
            numberLiterals.put("twelve", 12);
        }
    }
    public static int parseInt(String text) {
        Init();
        text = text.replaceAll("\\s", "");
        text = text.replaceAll(":", "");
        String r = "";
        for (String k: numberLiterals.keySet())
        {
            r = text.replaceAll(k, Integer.toString(numberLiterals.get(k)));
        }
        Log.e("ParseDigitInput:parseInt", r);
        return Integer.parseInt(r);
    }
}

