package it.nexera.ris.web.filters;

import it.nexera.ris.common.enums.PageTypes;
import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.helpers.*;
import it.nexera.ris.persistence.UserHolder;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.domain.Request;
import it.nexera.ris.web.beans.base.AccessBean;
import it.nexera.ris.web.beans.session.SessionBean;
import it.nexera.ris.web.beans.wrappers.logic.PermissionWrapper;
import it.nexera.ris.web.beans.wrappers.logic.UserWrapper;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;

import javax.faces.context.FacesContext;
import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

public class RoleRightsFilter extends BaseFilter implements Filter {

    /* (non-Javadoc)
     * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse, javax.servlet.FilterChain)
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
                         FilterChain chain) throws IOException, ServletException {
        if (!(request instanceof HttpServletRequest)
                && !(response instanceof HttpServletResponse)) {
            return;
        }

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        if (httpRequest.getRequestURI().contains(".css.jsf")
                || httpRequest.getRequestURI().contains(".png.jsf")
                || httpRequest.getRequestURI().contains(".gif.jsf")
                || httpRequest.getRequestURI().contains(".ico.jsf")
                || httpRequest.getRequestURI().endsWith(".css")
                || httpRequest.getRequestURI().endsWith(".png")
                || httpRequest.getRequestURI().endsWith(".gif")
                || httpRequest.getRequestURI().endsWith(".ico")
                || this.getFacesContext(httpRequest, httpResponse).isPostback()) {
            chain.doFilter(httpRequest, httpResponse);
            return;
        }

        if (UserHolder.getInstance().getCurrentUser(httpRequest) == null) {
            RedirectHelper.goTo(PageTypes.LOGIN, httpRequest, httpResponse);
            return;
        }
        SessionHelper.removeObject("loadMailFilters");
        SessionHelper.removeObject("loadRequestFilters");
        if(!ValidationHelper.isNullOrEmpty(SessionHelper.get("currentPageURL"))) {
            String previousURL = SessionHelper.get("currentPageURL").toString();
            if(previousURL.endsWith(PageTypes.REQUEST_EDIT.getPage()) ||
                    previousURL.endsWith(PageTypes.REQUEST_ESTATE_SITUATION_LIST.getPage()) ||
                    previousURL.endsWith(PageTypes.REQUEST_ESTATE_SITUATION_VIEW.getPage()) ||
                    previousURL.endsWith(PageTypes.REQUEST_TEXT_EDIT.getPage()) ||
                    previousURL.endsWith(PageTypes.SUBJECT.getPage())){
                if(httpRequest.getRequestURI().endsWith(PageTypes.REQUEST_LIST.getPage())){
                    SessionHelper.put("loadRequestFilters", "true");
                }
            }else if(previousURL.endsWith(PageTypes.MAIL_MANAGER_LIST.getPage()) ||
                    previousURL.endsWith(PageTypes.MAIL_MANAGER_EDIT.getPage()) ||
                    previousURL.endsWith(PageTypes.MAIL_MANAGER_FOLDER.getPage()) ||
                    previousURL.endsWith(PageTypes.MAIL_MANAGER_VIEW.getPage())){
                if(httpRequest.getRequestURI().endsWith(PageTypes.MAIL_MANAGER_LIST.getPage())){
                    SessionHelper.put("loadMailFilters", "true");
                }
            }
        }
        SessionHelper.removeObject("currentPageURL");
        SessionHelper.put("currentPageURL", httpRequest.getRequestURI());
        UserWrapper currentUser = getCurrentUser(httpRequest, httpResponse);

        int result = doCustomHandle(httpRequest, httpResponse);

        if (result == 1) {
            chain.doFilter(request, response);
            return;
        } else if (result == -1) {
            this.doRedirectToIndexPage(httpRequest, httpResponse);
            return;
        }

        if (currentUser != null) {
            String context = httpRequest.getContextPath();
            String path = httpRequest.getRequestURI();

            // Find PageType
            if (path.contains(context)) {
                int index = path.indexOf(context);
                path = path.substring(index + context.length(), path.length());

                PageTypes pageType = PageTypes.getPageTypeByPath(path);
                if (pageType != null) {
                    if (PageTypes.REQUEST_ESTATE_SITUATION_LIST.equals(pageType)
                            || PageTypes.REQUEST_ESTATE_SITUATION_EDIT.equals(pageType)
                            || PageTypes.REQUEST_ESTATE_SITUATION_VIEW.equals(pageType)
                            || PageTypes.REQUEST_ESTATE_FORMALITY.equals(pageType)
                            || PageTypes.REQUEST_FORMALITY_CREATE.equals(pageType)
                            || PageTypes.REQUEST_FORMALITY_EDIT.equals(pageType)) {
                        pageType = PageTypes.REQUEST_EDIT;
                    }
                    if (PageTypes.REAL_ESTATE_VIEW.equals(pageType)
                            || PageTypes.REAL_ESTATE_EDIT.equals(pageType)) {
                        pageType = PageTypes.REAL_ESTATE;
                    }
                    try {
                        if (!PageTypes.USER_PROFILE_VIEW.equals(pageType)) {

                            if (!handleRequest(httpRequest, httpResponse,
                                    pageType, currentUser)) {
                                return;
                            }
                        }

                    } catch (Exception e) {

                    }
                }
            }
        } else {
            if (httpRequest.getRequestURI().contains("Pages")) {
                this.doRedirectToIndexPage(httpRequest, httpResponse);
                return;
            }
        }

        chain.doFilter(request, response);
    }

    private int doCustomHandle(HttpServletRequest httpRequest,
                               HttpServletResponse httpResponse) {
        if (httpRequest.getRequestURI().contains("RecoveryPassword")) {
            return 1;
        } else if (httpRequest.getRequestURI().contains("ChangePassword")) {
            UserWrapper user = getCurrentUser(httpRequest, httpResponse);

            if (user != null) {
                return 1;
            } else {
                return -1;
            }
        }

        // Add more rules here

        return 0;
    }

    private boolean handleRequest(HttpServletRequest httpRequest,
                                  HttpServletResponse httpResponse, PageTypes pageType,
                                  UserWrapper user) throws Exception {
        // Check what page type are in request
        Boolean canViewOnePageModule = PermissionsHelper
                .getPermissionByPage(pageType);
        if (canViewOnePageModule != null) {
            if (Boolean.FALSE.equals(canViewOnePageModule)) {
                this.doRedirectToIndexPage(httpRequest, httpResponse);
                return false;
            }
        } else if (pageType.getPagesContext().contains("List")) {
            if (!AccessBean.canListPage(pageType, user) && !AccessBean.canListCreatedByUserPage(pageType, user)) {
                this.doRedirectToIndexPage(httpRequest, httpResponse);
                return false;
            }
        } else if (pageType.getPagesContext().contains("Edit")) {
            String id = httpRequest.getParameter(RedirectHelper.ID_PARAMETER);
            if (id == null || id.isEmpty()) {
                if (!AccessBean.canCreateInPage(pageType, user)) {
                    if (checkCertificationNotarialPermission(pageType,user,id)) return true;
                    this.doRedirectToIndexPage(httpRequest, httpResponse);
                    return false;
                }
            } else {
                if (!AccessBean.canEditInPage(pageType, user)) {
                    if (checkCertificationNotarialPermission(pageType,user,id)) return true;
                    this.doRedirectToIndexPage(httpRequest, httpResponse);
                    return false;
                }
            }
        } else if (pageType.getPagesContext().contains("View")) {
            String pageName = pageType.getCode().replace("VIEW", "LIST");
            for (PageTypes page : PageTypes.values()) {
                if (page.getCode().equals(pageName)) {
                    List<PermissionWrapper> permissions = AccessBean
                            .getRightsForPage(page, user);
                    for (PermissionWrapper p : permissions) {
                        if (p.isCanView()) {
                            return true;
                        }
                    }
                }
            }
            this.doRedirectToIndexPage(httpRequest, httpResponse);
            return false;
        }

        // Add custom handling here
        // When request should be redirected, return false

        return true;
    }

    private boolean checkCertificationNotarialPermission(PageTypes pageType, UserWrapper user, String entityId)
            throws IllegalAccessException, PersistenceBeanException, InstantiationException {
        if (pageType.equals(PageTypes.REQUEST_EDIT)) {
            if (ValidationHelper.isNullOrEmpty(entityId)) {
                return AccessBean.canCreateInPage(PageTypes.NOTARIAL_CERTIFICATION_LIST, user);
            } else if (RequestHelper.checkIfRequestHasDistraintFormality(Long.valueOf(entityId))) {
                if (AccessBean.canEditInPage(PageTypes.NOTARIAL_CERTIFICATION_LIST, user)) {
                    return AccessBean.canListPage(PageTypes.NOTARIAL_CERTIFICATION_LIST, user) ||
                            (AccessBean.canListCreatedByUserPage(PageTypes.NOTARIAL_CERTIFICATION_LIST, user)
                                    && RequestHelper.checkIfRequestWasCreatedByUser(Long.valueOf(entityId), user.getId()));
                }
            }
        }
        return false;
    }

    private void doRedirectToIndexPage(HttpServletRequest httpRequest,
                                       HttpServletResponse response) {
        if (response == null) {
            return;
        }

        try {
            StringBuilder sb = new StringBuilder("");
            sb.append(httpRequest.getRequestURL().substring(
                    0,
                    httpRequest.getRequestURL().indexOf(
                            FacesContext.getCurrentInstance()
                                    .getExternalContext()
                                    .getRequestContextPath())));
            sb.append(FacesContext.getCurrentInstance().getExternalContext()
                    .getRequestContextPath());
            if (!PageTypes.HOME.getPagesContext().startsWith("/")) {
                sb.append("/");
            }
            sb.append(PageTypes.HOME.getPagesContext());
            response.sendRedirect(sb.toString());
        } catch (IOException e) {
            LogHelper.log(log, e);
        }
    }
}
