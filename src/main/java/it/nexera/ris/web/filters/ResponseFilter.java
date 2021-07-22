package it.nexera.ris.web.filters;

import org.jdom.filter.AbstractFilter;

import javax.servlet.*;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class ResponseFilter extends AbstractFilter implements Filter {

    private static final long serialVersionUID = 1810893113609027126L;

    public void doFilter(final ServletRequest request,
                         final ServletResponse response, final FilterChain chain)
            throws IOException, ServletException {
        HttpServletResponse resp = (HttpServletResponse) response;

        resp.addHeader("p3p",
                "CP=\"IDC DSP COR ADM DEVi TAIi PSA PSD IVAi IVDi CONi HIS OUR IND CNT\"");

        chain.doFilter(request, resp);
    }

    /* (non-Javadoc)
     * @see javax.servlet.Filter#destroy()
     */
    @Override
    public void destroy() {
    }

    /* (non-Javadoc)
     * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
     */
    @Override
    public void init(FilterConfig arg0) throws ServletException {
    }

    /* (non-Javadoc)
     * @see org.jdom.filter.AbstractFilter#matches(java.lang.Object)
     */
    @Override
    public boolean matches(Object arg0) {
        return false;
    }

}
