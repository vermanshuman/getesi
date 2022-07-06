package it.nexera.ris.common.helpers;

import javax.faces.context.FacesContext;
import javax.servlet.http.HttpSession;

public class HttpSessionHelper {
    public static void put(String key, Object value) {
        if (getSession() != null) {
            getSession().setAttribute(key, value);
        }
    }

    public static Object get(String key) {
        if (getSession() != null) {
            return getSession().getAttribute(key);
        } else {
            return null;
        }
    }

    private static HttpSession getSession() {
        return (HttpSession) FacesContext.getCurrentInstance()
                .getExternalContext().getSession(false);
    }
}
