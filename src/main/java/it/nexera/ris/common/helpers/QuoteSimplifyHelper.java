package it.nexera.ris.common.helpers;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

public class QuoteSimplifyHelper {

    public static long gcm(long a, long b) {
        return b == 0 ? a : gcm(b, a % b);
    }

    public static String simplify(String quota) {
        long firstNumber = Long.parseLong(quota.substring(0, quota.indexOf("/")));
        long secondNumber = Long.parseLong(quota.substring(quota.indexOf("/") + 1, quota.length()));

        long gcm = gcm(firstNumber, secondNumber);
        return (firstNumber / gcm) + "/" + (secondNumber / gcm);
    }

    public static boolean checkQuote(String s) {
        if (s.contains("/")) {
            String firstPart = s.substring(0, s.indexOf("/"));
            String secondPart = s.substring(s.indexOf("/") + 1, s.length());
            if (NumberUtils.isParsable(firstPart) && !StringUtils.contains(firstPart, ".")
                    && NumberUtils.isParsable(secondPart) && !StringUtils.contains(secondPart, ".")) {
                return true;
            }
            return false;
        } else {
            return false;
        }
    }
}
