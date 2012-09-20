package kizwid.web.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * User: kizwid
 * Date: 2012-02-10
 */
public class PropCalcErrorParser {
    private static final Pattern pattern = Pattern.compile("([^:]*):([^:]*):([^:]*):([^:]*):(.*)");

    public static PropCalcError parse(String error, String riskGroup) {
        PropCalcError propCalcError;
        Matcher matcher = pattern.matcher(error);
        if (matcher.find()) {
            String dictionary = matcher.group(1);
            String marketData = matcher.group(2);
            String riskGroupSplit = matcher.group(3);
            String errorMessage = matcher.group(5);
            propCalcError = new PropCalcError(riskGroup, dictionary, marketData, riskGroupSplit, errorMessage);
        } else {
            // unable to parse so will transmit whole error
            propCalcError = new PropCalcError(riskGroup, error);
        }
        return propCalcError;
    }

}
