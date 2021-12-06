package it.nexera.ris.common.security.beans;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.rememberme.TokenBasedRememberMeServices;
import org.springframework.util.ReflectionUtils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;


public class TokenBasedRememberMeServicesEx extends
        TokenBasedRememberMeServices {

    protected transient final Log log = LogFactory.getLog(TokenBasedRememberMeServicesEx.class);

    @SuppressWarnings("deprecation")
    public TokenBasedRememberMeServicesEx() {
    }

    public TokenBasedRememberMeServicesEx(String key,
                                          UserDetailsService userDetailsService) {
        super(key, userDetailsService);
    }

    protected void setCookie(String[] tokens, int maxAge,
                             HttpServletRequest request, HttpServletResponse response) {
        String cookieValue = encodeCookie(tokens);
        Cookie cookie = new Cookie(getCookieName(), cookieValue);
        cookie.setMaxAge(maxAge);
        cookie.setPath(getCookiePath(request));

        cookie.setSecure(request.isSecure());

        Method method = ReflectionUtils.findMethod(Cookie.class, "setHttpOnly",
                boolean.class);
        if (method != null) {
            ReflectionUtils.invokeMethod(method, cookie, Boolean.TRUE);
        }

        response.addCookie(cookie);
    }

    private String getCookiePath(HttpServletRequest request) {
        String contextPath = request.getContextPath();
        return contextPath.length() > 0 ? contextPath : "/";
    }
}
