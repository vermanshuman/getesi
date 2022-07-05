package it.nexera.ris.common.security.beans;

import it.nexera.ris.common.enums.PageTypes;
import it.nexera.ris.common.enums.RoleTypes;
import it.nexera.ris.common.enums.UserStatuses;
import it.nexera.ris.common.helpers.*;
import it.nexera.ris.common.security.crypto.MD5;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.domain.Role;
import it.nexera.ris.persistence.beans.entities.domain.User;
import it.nexera.ris.persistence.beans.entities.domain.WLGInbox;
import it.nexera.ris.web.beans.BaseValidationPageBean;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.IOException;
import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;

@ManagedBean(name = "loginBean")
@ViewScoped
public class LoginBean extends BaseValidationPageBean implements Serializable {
    
	private static final long serialVersionUID = 387873920390676270L;

	protected transient final Log log = LogFactory.getLog(LoginBean.class);

    private String password = null;

    private String username = null;

    private String usermail;

    private static final String REMEMBER_ME = "rememberme";

    private String passwordSecond;

    private String passwordFirst;

    private String errorMessage;

    private String registrationLogin;

    private String registrationPassword;

    private Boolean resetPassword;

    private Boolean error;

    private User user;

    @Override
    protected void onConstruct() {
        if (!this.isPostback() && this.getRememberMe() == null) {
            this.setRememberMe(true);
        }
        Map<String, String> params = FacesContext.getCurrentInstance().
                getExternalContext().getRequestParameterMap();
        if (!params.isEmpty()) {
            String confirmCode = params.get("confirmCode");
            try {
                setUser(DaoManager.get(
                        User.class,
                        new Criterion[]
                                {Restrictions.eq("confirmCode", confirmCode)}));
                if (!ValidationHelper.isNullOrEmpty(getUser())) {
                    setResetPassword(true);
                }
            } catch (Exception e) {
                LogHelper.log(log, e);
            }
        }

    }

    public void resetError() {
        setUsermail(null);
        setErrorMessage(null);
        setError(false);
    }

    public void saveNewPassword() {
        cleanValidation();
        if (ValidationHelper.isNullOrEmpty(getPasswordFirst())) {
            this.addFieldException("password_2", "differendPassword");
        } else if (!getPasswordFirst().equals(getPasswordSecond())) {
            this.addFieldException("password", "passwordIsRequired");
        } else if (!ValidationHelper.checkCorrectFormatByExpression(
                "^[A-Za-z0-9_-]{8,14}$", this.getPasswordFirst().trim())) {
            this.addFieldException("password", "passContainsIllegalSymbols");
        } else {
            getUser().setPassword(MD5.encodeString(getPasswordFirst(), null));
            getUser().setConfirmCode(null);
            try {
                DaoManager.save(getUser(), true);
            } catch (Exception e) {
                LogHelper.log(log, e);
            }
            RedirectHelper.goTo(PageTypes.LOGOUT);
        }
    }

    public void doLogin() throws IOException, ServletException {
    	
        this.cleanValidation();
        
       

        this.validate(this.getUsername(), "j_username", this.getPassword(), "password");
        loginValidate(this.getUsername(), "j_username", this.getPassword(), "password");
        if (this.getValidationFailed()) {
            return;
        }

        ExternalContext context = FacesContext.getCurrentInstance()
                .getExternalContext();

        RequestDispatcher dispatcher = ((ServletRequest) context.getRequest())
                .getRequestDispatcher("/j_spring_security_check");

        dispatcher.forward((ServletRequest) context.getRequest(),
                (ServletResponse) context.getResponse());

        String oldSessionId = (String) SessionHelper.get("my_session_id");
        StringBuffer sb = new StringBuffer(oldSessionId);
        sb.append("__");
        sb.append(getCurrentUser().getId());
        SessionHelper.removeObject("my_session_id");
        SessionHelper.put(sb.toString(), "my_session_id");

        FacesContext.getCurrentInstance().responseComplete();
    }

    public void validate(String username, String userFieldId, String password, String passwordFieldId) {
        cleanValidation();
        if (ValidationHelper.isNullOrEmpty(username)) {
            this.addFieldException(userFieldId, "usernameIsRequired");
        } else if (!ValidationHelper.checkUserNameFormat(username
                .trim())) {
            this.addFieldException(userFieldId, "loginFormatError");
        } else if (!ValidationHelper.checkCorrectFormatByExpression(
                "^[A-Za-z0-9_-]{3,20}$", username.trim())) {
            this.addFieldException(userFieldId, "loginContainsIllegalSymbols");
        }

        if (ValidationHelper.isNullOrEmpty(password)) {
            this.addFieldException(passwordFieldId, "passwordIsRequired");
        } else if (!ValidationHelper.checkCorrectFormatByExpression(
                "^[A-Za-z0-9_-]{8,14}$", password.trim())) {
            this.addFieldException(passwordFieldId, "passContainsIllegalSymbols");
        }

    }

    public void loginValidate(String username, String userFieldId, String password, String passwordFieldId){
        if (!ValidationHelper.isNullOrEmpty(username)
                && !ValidationHelper.isNullOrEmpty(password)) {
            User user = null;
            try {
                user = DaoManager.get(
                        User.class,
                        new Criterion[]
                                {
                                        Restrictions.eq("login", username),
                                        Restrictions.eq("password", MD5.encodeString(
                                                password, null))});
            } catch (Exception e) {
                LogHelper.log(log, e);
            }
            if (user == null) {
                this.addFieldException(userFieldId, "wrongLoginInfo");
                this.markInvalid(passwordFieldId, "wrongLoginInfo");
            }

            if (user != null
                    && (user.getStatus() == null || user.getStatus().equals(
                    UserStatuses.INACTIVE))) {
                this.addException("wrongLoginStatus");
            }
        }
    }

    public void registerValidate(String username, String userFieldId, String password, String passwordFieldId){
        if (!ValidationHelper.isNullOrEmpty(username)
                && !ValidationHelper.isNullOrEmpty(password)) {
            User user = null;
            try {
                user = DaoManager.get(
                        User.class,
                        new Criterion[]
                                {
                                        Restrictions.eq("login", username),
                                        Restrictions.eq("password", MD5.encodeString(
                                                password, null))});
            } catch (Exception e) {
                LogHelper.log(log, e);
            }
            if (user != null) {
                this.addException("userAlreadyPresent");
            }

        }
    }

    public String doLogout() throws ServletException, IOException {
        try {
            this.getSession().clear();
        } catch (Exception e) {
            LogHelper.log(log, e);
        }
        try {
            ExternalContext context = FacesContext.getCurrentInstance()
                    .getExternalContext();

            RequestDispatcher dispatcher = ((ServletRequest) context
                    .getRequest())
                    .getRequestDispatcher("/j_spring_security_logout");

            dispatcher.forward((ServletRequest) context.getRequest(),
                    (ServletResponse) context.getResponse());
        } catch (Exception e) {
            LogHelper.log(log, e);
        }

        return null;
    }

    public void sendMail() {
        if (ValidationHelper.isNullOrEmpty(this.getUsermail())) {
            setError(true);
            setErrorMessage(ResourcesHelper.getValidation("noMail"));
        }
        try {
            setUser(DaoManager.get(
                    User.class,
                    new Criterion[]
                            {
                                    Restrictions.eq("login", getUsername()),
                                    Restrictions.eq("email", getUsermail())
                            }));
            if (ValidationHelper.isNullOrEmpty(user) || !user.getEmail().equals(getUsermail()) ||
                    !user.getLogin().equals(getUsername())) {
                setError(true);
                setErrorMessage(ResourcesHelper.getValidation("noMail"));
            } else {
                UUID uuid = UUID.randomUUID();
                String randomUUIDString = uuid.toString();
                String link = PageTypes.LOGIN.getPagesContext() + "?" + "confirmCode" + "=" + randomUUIDString;
                URL reconstructedURL = new URL(getRequest().getScheme(),
                        getRequest().getServerName(), getRequest().getServerPort(),
                        FacesContext.getCurrentInstance().getExternalContext().getRequestContextPath()
                                + link);
                String body = String.format(
                        ResourcesHelper.getString("emailResetPasswordBody"),
                        "<br/><a href=\"" + reconstructedURL.toString() + "\">"
                                + reconstructedURL.toString() + "</a>").replaceAll("\\?\\?", "");
                WLGInbox confirmMail = new WLGInbox();
                confirmMail.setXpriority(3);
                confirmMail.setEmailTo(getUsermail());
                confirmMail.setEmailBody(body);
                MailHelper.sendMail(confirmMail, null);
                getUser().setConfirmCode(randomUUIDString);
                DaoManager.save(getUser(), true);
            }

        } catch (Exception e) {
            LogHelper.log(log, e);
        }
    }

    public void registration() {
        try {
            validate(getRegistrationLogin(), "username", getRegistrationPassword(), "registrPass");
            registerValidate(getRegistrationLogin(), "username", getRegistrationPassword(), "registrPass");
            if (this.getValidationFailed()) {
                return;
            }
            User user = new User();
            user.setPassword(MD5.encodeString(
                    getRegistrationPassword(), null));
            user.setLogin(getRegistrationLogin());
            user.setStatus(UserStatuses.ACTIVE);
            user.setRoles(new ArrayList<Role>());
            user.getRoles().add(DaoManager.get(Role.class,
                    Restrictions.eq("type", RoleTypes.EXTERNAL)));
            DaoManager.save(user, true);
            if (user.getId() != null) {
                executeJS("PF('dlg3').hide()");
            } else {
                this.addException("userErrorSave");
            }
        } catch (Exception e) {
            LogHelper.log(log, e);
        }

    }

    public String getUsername() {
        return this.username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUsermail() {
        return usermail;
    }

    public void setUsermail(String usermail) {
        this.usermail = usermail;
    }

    public Boolean getRememberMe() {
        return (Boolean) this.getViewState().get(REMEMBER_ME);
    }

    public void setRememberMe(Boolean rememberMe) {
        this.getViewState().put(REMEMBER_ME, rememberMe);
    }

    public String getPasswordSecond() {
        return passwordSecond;
    }

    public void setPasswordSecond(String passwordSecond) {
        this.passwordSecond = passwordSecond;
    }

    public String getPasswordFirst() {
        return passwordFirst;
    }

    public void setPasswordFirst(String passwordFirst) {
        this.passwordFirst = passwordFirst;
    }

    public Boolean getResetPassword() {
        return resetPassword;
    }

    public void setResetPassword(Boolean resetPassword) {
        this.resetPassword = resetPassword;
    }


    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Boolean getError() {
        return error;
    }

    public void setError(Boolean error) {
        this.error = error;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getRegistrationLogin() {
        return registrationLogin;
    }

    public void setRegistrationLogin(String registrationLogin) {
        this.registrationLogin = registrationLogin;
    }

    public String getRegistrationPassword() {
        return registrationPassword;
    }

    public void setRegistrationPassword(String registrationPassword) {
        this.registrationPassword = registrationPassword;
    }
}
