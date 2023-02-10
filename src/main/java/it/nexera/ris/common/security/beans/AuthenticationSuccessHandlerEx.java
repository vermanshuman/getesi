package it.nexera.ris.common.security.beans;

import it.nexera.ris.common.enums.PageTypes;
import it.nexera.ris.common.helpers.LogHelper;
import it.nexera.ris.common.helpers.RedirectHelper;
import it.nexera.ris.common.helpers.UserHelper;
import it.nexera.ris.common.security.api.UserDetailsImpl;
import it.nexera.ris.persistence.PersistenceSession;
import it.nexera.ris.persistence.UserHolder;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.domain.User;
import it.nexera.ris.web.beans.wrappers.logic.UserWrapper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import javax.faces.context.FacesContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class AuthenticationSuccessHandlerEx implements
        AuthenticationSuccessHandler {

    public transient final Log log = LogFactory.getLog(getClass());

    /* (non-Javadoc)
     * @see org.springframework.security.web.authentication.AuthenticationSuccessHandler#onAuthenticationSuccess(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, org.springframework.security.core.Authentication)
     */
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response, Authentication authentication)
            throws IOException, ServletException {
        Object principal = SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();

        if (principal instanceof UserDetailsImpl) {
            UserDetailsImpl userDetails = (UserDetailsImpl) principal;
            try {
                User user = null;
                if (FacesContext.getCurrentInstance() != null) {
                	user = UserHelper.getUser(userDetails);

                  /*  if (user != null) {
                        HttpSessionCollector.checkUniqueUser(user.getId());
                    }*/

                    UserHolder.getInstance().setCurrentUser(
                            new UserWrapper(user, DaoManager.getSession()),
                            request);
                } else {
                    Session session = null;
                    try {
                        session = PersistenceSession.createSession();
                        user = UserHelper.getUser(userDetails, session);

                        /*if (user != null) {
                            HttpSessionCollector.checkUniqueUser(user.getId());
                        }*/

                        UserHolder.getInstance().setCurrentUser(
                                new UserWrapper(user, session), request);
                    } catch (Exception e) {
                        throw e;
                    } finally {
                        if (session != null) {
                            session.clear();
                            session.close();
                        }
                    }
                }
            } catch (Exception e) {
                LogHelper.log(log, e);
            }
        }

        PageTypes page = PageTypes.getPageTypeByPath(request.getRequestURI());
        RedirectHelper.goTo(page == null ? PageTypes.HOME : page, request, response);
    }

}
