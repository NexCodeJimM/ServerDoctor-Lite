package com.jimm.serverdoctor;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Compares plugin version strings (including pre-release tags like {@code 0.9.0-BETA}).
 */
public final class VersionUtil {

    private VersionUtil() {
    }

    /**
     * @return positive if {@code remote} is newer than {@code local}, negative if older, 0 if equal
     */
    public static int compare(String remote, String local) {
        if (remote == null || remote.isBlank()) {
            return -1;
        }
        if (local == null || local.isBlank()) {
            return 1;
        }
        List<VersionPart> remoteParts = parse(remote.trim());
        List<VersionPart> localParts = parse(local.trim());
        int max = Math.max(remoteParts.size(), localParts.size());
        for (int index = 0; index < max; index++) {
            VersionPart remotePart = index < remoteParts.size() ? remoteParts.get(index) : null;
            VersionPart localPart = index < localParts.size() ? localParts.get(index) : null;
            int result = compareParts(remotePart, localPart);
            if (result != 0) {
                return result;
            }
        }
        return 0;
    }

    public static boolean isNewer(String remote, String local) {
        return compare(remote, local) > 0;
    }

    private static int compareParts(VersionPart remotePart, VersionPart localPart) {
        if (remotePart == null && localPart == null) {
            return 0;
        }
        if (remotePart == null) {
            return -1;
        }
        if (localPart == null) {
            return 1;
        }
        if (remotePart.numeric && localPart.numeric) {
            return Long.compare(remotePart.number, localPart.number);
        }
        if (remotePart.numeric && !localPart.numeric) {
            return 1;
        }
        if (!remotePart.numeric && localPart.numeric) {
            return -1;
        }
        return remotePart.text.compareToIgnoreCase(localPart.text);
    }

    private static List<VersionPart> parse(String version) {
        String[] tokens = version.toLowerCase(Locale.ROOT).split("[.\\-_]+");
        List<VersionPart> parts = new ArrayList<>();
        for (String token : tokens) {
            if (token.isEmpty()) {
                continue;
            }
            String digitsOnly = token.replaceAll("[^0-9]", "");
            if (!digitsOnly.isEmpty() && digitsOnly.equals(token)) {
                try {
                    parts.add(VersionPart.numeric(Long.parseLong(digitsOnly)));
                    continue;
                } catch (NumberFormatException ignored) {
                    // fall through to text
                }
            }
            int firstDigit = -1;
            for (int index = 0; index < token.length(); index++) {
                if (Character.isDigit(token.charAt(index))) {
                    firstDigit = index;
                    break;
                }
            }
            if (firstDigit > 0) {
                String prefix = token.substring(0, firstDigit);
                if (!prefix.isEmpty()) {
                    parts.add(VersionPart.text(prefix));
                }
                String remainder = token.substring(firstDigit);
                String remainderDigits = remainder.replaceAll("[^0-9]", "");
                if (!remainderDigits.isEmpty() && remainderDigits.equals(remainder)) {
                    parts.add(VersionPart.numeric(Long.parseLong(remainderDigits)));
                } else {
                    parts.add(VersionPart.text(remainder));
                }
            } else {
                parts.add(VersionPart.text(token));
            }
        }
        return parts;
    }

    private record VersionPart(boolean numeric, long number, String text) {
        static VersionPart numeric(long number) {
            return new VersionPart(true, number, "");
        }

        static VersionPart text(String text) {
            return new VersionPart(false, 0L, text);
        }
    }
}
