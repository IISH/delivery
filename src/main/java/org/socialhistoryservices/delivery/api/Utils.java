package org.socialhistoryservices.delivery.api;

public class Utils {
    public static String[] getParentPidAndItem(String pid) {
        //String itemSeparator = deliveryProperties.getItemSeparator();
        String itemSeparator = ".";
        if (pid.contains(itemSeparator)) {
            int idx = pid.indexOf(itemSeparator);
            String parentPid = pid.substring(0, idx);
            String item = pid.substring(idx + 1);
            return new String[]{parentPid, item};
        }
        return new String[]{pid, null};
    }
}
