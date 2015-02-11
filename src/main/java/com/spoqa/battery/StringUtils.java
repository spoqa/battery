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

        boolean isLowercase = false;
        int startIndex = 0;

        for (int i = 0; i < input.length(); ++i) {
            char c = input.charAt(i);
            boolean currentLowercase = Character.isLowerCase(c);
            if (i == 0)
                isLowercase = currentLowercase;

            if (isLowercase != currentLowercase) {
                output.add(input.substring(startIndex, i));
                startIndex = i;
            }
        }

        output.add(input.substring(startIndex));

        return output;
    }

}
