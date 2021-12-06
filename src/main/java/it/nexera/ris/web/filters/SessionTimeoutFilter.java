package it.nexera.ris.web.filters;

import it.nexera.ris.common.enums.PageTypes;
import it.nexera.ris.common.helpers.RedirectHelper;
import it.nexera.ris.persistence.UserHolder;
import it.nexera.ris.web.beans.session.SessionBean;
import it.nexera.ris.web.beans.wrappers.logic.UserWrapper;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class SessionTimeoutFilter extends BaseFilter implements Filter {

    public void init(FilterConfig filterConfig) throws ServletException {
    }

    public void doFilter(ServletRequest request, ServletResponse response,
                         FilterChain filterChain) throws IOException, ServletException {
        if ((request instanceof HttpServletRequest)
                && (response instanceof HttpServletResponse)) {
            HttpServletRequest httpServletRequest = (HttpServletRequest) request;
            HttpServletResponse httpServletResponse = (HttpServletResponse) response;

            SessionBean sb = null;
            try {
                sb = getSessionBean(httpServletRequest);
            } catch (Exception e) {

            }

            if (sb != null) {
                UserWrapper user = (UserWrapper) httpServletRequest
                        .getSession().getAttribute(
                                UserHolder.USER_HOLDER_ATTRIBUTE);

                PageTypes page = PageTypes.getPageTypeByPath(httpServletRequest
                        .getRequestURI());

                if (httpServletRequest.getSession().getAttribute(
                        UserHolder.SESSION_TIME_ATTRIBUTE) != null) {
                    httpServletRequest.getSession()
                            .setMaxInactiveInterval((int) httpServletRequest
                                    .getSession().getAttribute(
                                            UserHolder.SESSION_TIME_ATTRIBUTE));
                    httpServletRequest.getSession().setAttribute(
                            "ON_DOCUMENT_GENERATION", Boolean.FALSE);
                }

                if (user != null && PageTypes.LOGIN.equals(page)) {
                    if (httpServletRequest.getSession()
                            .getAttribute("needLogout") != null
                            && ((Boolean) httpServletRequest.getSession()
                            .getAttribute("needLogout"))) {
                        Authentication auth = SecurityContextHolder.getContext()
                                .getAuthentication();
                        SecurityContextLogoutHandler ctxLogOut = new SecurityContextLogoutHandler();
                        ctxLogOut.logout(httpServletRequest,
                                httpServletResponse, auth);
                    } else {
                        /*StringBuffer strb = new StringBuffer();

                        strb.append(user.getId());
                        strb.append("__separator__");
                        strb.append("show_logout_dlg");

                        EventBus eventBus = EventBusFactory.getDefault()
                                .eventBus();
                        eventBus.publish("/notify", strb.toString());*/
                    }
                }

                if ((httpServletRequest.getParameter("reason") != null && httpServletRequest
                        .getParameter("reason").equalsIgnoreCase("expired"))) {
                    UserHolder.getInstance().setCurrentUser(null, request);
                    if (sb.getSession() != null && user != null) {
                        sb.getSession().clear();
                    }
                    httpServletRequest.getSession().invalidate();
                }
            }

            // is session invalid?
            if (this.isSessionInvalid(httpServletRequest)
                    && !httpServletRequest.getRequestURI().contains(".css.jsf")
                    && !httpServletRequest.getRequestURI().contains(".js.jsf")
                    && !httpServletRequest.getRequestURI().contains(".png.jsf")
                    && httpServletRequest.getRequestURI().contains(".jsf")) {
                RedirectHelper.goTo(PageTypes.LOGIN, httpServletRequest,
                        httpServletResponse);
                return;
            }
        }
        filterChain.doFilter(request, response);
    }

    private boolean isSessionInvalid(HttpServletRequest httpServletRequest) {
        boolean sessionInValid = (httpServletRequest.getRequestedSessionId() != null)
                && !httpServletRequest.isRequestedSessionIdValid();
        return sessionInValid;
    }

    private SessionBean getSessionBean(HttpServletRequest httpServletRequest) {
        return ((SessionBean) httpServletRequest.getSession().getAttribute(
                "sessionBean"));
    }

    public void destroy() {
    }

}
