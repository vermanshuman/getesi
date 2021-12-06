package it.nexera.ris.common.helpers;

import it.nexera.ris.common.enums.LocaleType;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class ResourcesHelper extends BaseHelper {

    private static final String PROJECT_PROPERTIES_FILE_NAME = "project.properties";

    private static String getString(String bundle, String resourceId,
                                    Object[] params) {
        Locale locale = new Locale(LocaleType.IT.getValue());
        ClassLoader loader = getClassLoader();
        return getString(bundle, resourceId, locale, loader, params);
    }

    public static String getString(String resourceId) {
        String str = getString("resources", resourceId, null);
        if (str != null && !str.isEmpty()) {
            return str;
        } else {
            return String.format("??%s??", resourceId);
        }
    }

    public static String getEnum(String resourceId) {
        String str = getString("enums", resourceId, null);
        if (str != null && !str.isEmpty()) {
            return str;
        } else {
            LogHelper
                    .log(log, String.format(
                            "%s - is not present in enums resource bundle",
                            resourceId));
            return getString(resourceId);
        }
    }

    public static String getValidation(String resourceId) {
        String str = getString("validation", resourceId, null);
        if (str != null && !str.isEmpty()) {
            return str;
        } else {
            return getString(resourceId);
        }
    }

    private static String getString(String bundleStr, String resourceId,
                                    Locale locale, ClassLoader loader, Object[] params) {
        String resource = null;

        ResourceBundle bundle = ResourceBundle.getBundle(bundleStr, locale,
                loader);
        if (bundle != null) {
            try {
                resource = bundle.getString(resourceId);
            } catch (MissingResourceException ex) {
                log.warn("ResourcesHelper.getString : " + ex);
            }
        }

        if (resource == null) {
            return null; // no match
        }
        if (params == null) {
            return resource;
        }

        MessageFormat formatter = new MessageFormat(resource, locale);
        return formatter.format(params);
    }

    private static ClassLoader getClassLoader() {
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        if (loader == null) {
            loader = ClassLoader.getSystemClassLoader();
        }
        return loader;
    }

}
