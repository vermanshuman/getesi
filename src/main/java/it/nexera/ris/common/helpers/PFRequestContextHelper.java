package it.nexera.ris.common.helpers;

import org.primefaces.context.RequestContext;

public class PFRequestContextHelper {
    public static void executeJS(String str) {
        RequestContext context = RequestContext.getCurrentInstance();
        if (context != null) {
            context.execute(str);
        }
    }
}
