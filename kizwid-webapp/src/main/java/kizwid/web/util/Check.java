package kizwid.web.util;

/**
 * User: kizwid
 * Date: 2012-02-22
 */
public class Check {
    public static void that(boolean isTrue, String messageTemplate, String... params) {
        if(!isTrue){
            throw new IllegalArgumentException(String.format(messageTemplate,params));
        }
    }
}
