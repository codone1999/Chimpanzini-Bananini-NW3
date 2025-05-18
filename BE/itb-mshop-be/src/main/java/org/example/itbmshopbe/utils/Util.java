package org.example.itbmshopbe.utils;

public class Util {
    public static String trimToLength(String input, int maxLength) {
        if (input == null) return null;
        return input.length() <= maxLength ? input : input.substring(0, maxLength);
    }
    public static String trimFirstAndLastSentence(String input) {
        if (input == null || input.isBlank()) {
            return input;
        }

        input = input.trim();
        int firstPeriodIndex = input.indexOf('.');
        int lastPeriodIndex = input.lastIndexOf('.');
        if (firstPeriodIndex == lastPeriodIndex) {
            return input;
        }
        String firstSentence = input.substring(0, firstPeriodIndex + 1).trim();
        String lastSentence = input.substring(lastPeriodIndex).trim();
        String middleContent = input.substring(firstPeriodIndex + 1, lastPeriodIndex + 1);
        return firstSentence + middleContent + lastSentence;
    }

    public static String checkWebsiteUrl(String input) {
        if (input == null || input.isBlank()) {
            return input;
        }
        input = input.replaceAll("\\.\\.+", ".");
        if (input.matches(".*\\.\\w{2,4}(\\.[a-z]{2,4})?$")) {
            int lastDotIndex = input.lastIndexOf('.');
            String tld = input.substring(lastDotIndex);
            String base = input.substring(0, lastDotIndex);

            base = base.replaceAll("\\.\\.", ".");
            tld = tld.replaceAll("\\.([a-z]{2,4}){2,}", ".$1");

            input = base + tld;
        }
        return input;
    }
}
