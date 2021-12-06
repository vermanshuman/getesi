package it.nexera.ris.web.filters;

import it.nexera.ris.common.enums.PageTypes;
import it.nexera.ris.common.helpers.LogHelper;
import it.nexera.ris.common.helpers.PermissionsHelper;
import it.nexera.ris.common.helpers.RedirectHelper;
import it.nexera.ris.persistence.UserHolder;
import it.nexera.ris.web.beans.base.AccessBean;
import it.nexera.ris.web.beans.wrappers.logic.PermissionWrapper;
import it.nexera.ris.web.beans.wrappers.logic.UserWrapper;

import javax.faces.context.FacesContext;
import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@WebFilter(urlPatterns = {"/Pages/ManagementGroup/MailManagerView.jsf", "/Pages/ManagementGroup/RequestTextEdit.jsf"})
public class NoCacheFilter implements javax.servlet.Filter {

    @Override
    public void init(final FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(final ServletRequest servletRequest, final ServletResponse servletResponse, final FilterChain filterChain) throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        response.setHeader("Cache-Control", "no-cache, no-store");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0);

        filterChain.doFilter(request, response);
    }

    @Override
    public void destroy() {
    }
}