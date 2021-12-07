package it.nexera.ris.common.helpers;

public class EnumHelper {
    public static String toStringFormatter(Enum obj) {
        if (obj == null) {
            return null;
        }

        StringBuilder sb = new StringBuilder();
        sb.append(obj.getClass().getSimpleName().substring(0, 1).toLowerCase());
        sb.append(obj.getClass().getSimpleName().substring(1));
        sb.append(obj.name());

        return sb.toString();
    }

    public static String toStringFormatterShort(Enum obj) {
        if (obj == null) {
            return null;
        }

        StringBuilder sb = new StringBuilder();
        sb.append(obj.getClass().getSimpleName().substring(0, 1).toLowerCase());
        sb.append(obj.getClass().getSimpleName().substring(1));
        sb.append(obj.name());
        sb.append("ShortValue");

        return sb.toString();
    }

}
