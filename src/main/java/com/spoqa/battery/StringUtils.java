package com.spoqa.battery;

import java.util.ArrayList;
import java.util.List;

public final class StringUtils {

    public static String join(List<String> array, String delimiter) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < array.size(); ++i) {
            if (i > 0)
                sb.append(delimiter);
            sb.append(array.get(i));
        }
        return sb.toString();
    }

    public static String uppercaseFirst(String input) {
        return input.substring(0, 1).toUpperCase() + input.substring(1).toLowerCase();
    }

    public static List<String> splitByCase(String input) {
        List<String> output = new ArrayList<String>();

        boolean isUppercase = false;
        boolean continuousUppercase = false;
        int startIndex = 0;

        for (int i = 0; i < input.length(); ++i) {
            char c = input.charAt(i);
            boolean currentUppercase = Character.isUpperCase(c);
            if (i == 0)
                isUppercase = currentUppercase;

            if (currentUppercase && !isUppercase) {
                output.add(input.substring(startIndex, i));
                startIndex = i;
            } else if (currentUppercase) {
                continuousUppercase = true;
            } else if (continuousUppercase) {
                output.add(input.substring(startIndex, i - 1));
                startIndex = i - 1;
                continuousUppercase = false;
            }

            isUppercase = currentUppercase;
        }

        output.add(input.substring(startIndex));

        return output;
    }
}
