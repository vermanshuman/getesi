package it.nexera.ris.web.beans.pages;

import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.helpers.LogHelper;
import it.nexera.ris.common.helpers.MessageHelper;
import it.nexera.ris.common.helpers.ResourcesHelper;
import it.nexera.ris.common.helpers.ValidationHelper;
import it.nexera.ris.common.security.crypto.MD5;
import it.nexera.ris.persistence.UserHolder;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.domain.User;
import it.nexera.ris.web.beans.EntityEditPageBean;
import it.nexera.ris.web.beans.wrappers.logic.UserWrapper;
import org.hibernate.HibernateException;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import java.io.IOException;
import java.io.Serializable;

@ManagedBean(name = "userProfileViewBean")
@ViewScoped
public class UserProfileViewBean extends EntityEditPageBean<User>
        implements Serializable {

    private static final long serialVersionUID = 7377804605535599777L;

    private static final String PASSWORD_EXPIRED_PARAM = "password_expired";

    private String oldPwd;

    private String pwd;

    private String confirmPwd;

    private Boolean disableFunctionality;

    @Override
    public void onLoad() throws NumberFormatException, HibernateException,
            PersistenceBeanException, InstantiationException,
            IllegalAccessException {

        if (!this.isPostback()) {
            this.setDisableFunctionality(Boolean.FALSE);
        }

        try {
            this.setEntity(
                    DaoManager.get(User.class, this.getCurrentUser().getId()));
        } catch (Exception e) {
            LogHelper.log(log, e);
        }

        if (getRequestParameter(PASSWORD_EXPIRED_PARAM) != null) {
            this.setDisableFunctionality(Boolean.TRUE);
        }

        if (!this.isPostback() && this.getDisableFunctionality()) {
            addPasswordExpiredMessage();
        }
    }

    @Override
    public void goBack() {
        if (!this.getDisableFunctionality()) {
            this.getViewState().clear();
        } else {
            addPasswordExpiredMessage();
        }
    }

    private void addPasswordExpiredMessage() {
        MessageHelper.addGlobalMessage(FacesMessage.SEVERITY_ERROR,
                ResourcesHelper
                        .getValidation("profilePasswordExpiredMessageTitle"),
                ResourcesHelper
                        .getValidation("profilePasswordExpiredMessageBody"));
    }

    @Override
    public void onValidate() throws PersistenceBeanException {
        boolean bValid = true;
        if (!ValidationHelper.isNullOrEmpty(this.getOldPwd())
                || !ValidationHelper.isNullOrEmpty(this.getPwd())
                || !ValidationHelper.isNullOrEmpty(this.getConfirmPwd())) {
            if (ValidationHelper.isNullOrEmpty(this.getOldPwd())) {
                this.addFieldException("tabs:oldPassword",
                        "requiredOldPassword");
                bValid = false;
            } else if (!ValidationHelper.checkCriterions(User.class,
                    new Criterion[]
                            {
                                    Restrictions.eq("id", this.getEntity().getId()),
                                    Restrictions.eq("password",
                                            MD5.encodeString(this.getOldPwd(), null))
                            })) {
                this.addFieldException("tabs:oldPassword", "wrongPassword");
                bValid = false;
            }

            if (ValidationHelper.isNullOrEmpty(this.getPwd())) {
                this.addFieldException("tabs:newPassword",
                        "requiredNewPassword");
                bValid = false;
            }

            if (ValidationHelper.isNullOrEmpty(this.getConfirmPwd())) {
                this.addFieldException("tabs:confirmationPassword",
                        "requiredConfirmPassword");
                bValid = false;
            }

            if (!ValidationHelper.isNullOrEmpty(this.getConfirmPwd())
                    && !ValidationHelper.isNullOrEmpty(this.getPwd())
                    && !this.getConfirmPwd().equals(this.getPwd())) {
                this.addFieldException("tabs:newPassword", "passwordMissmatch");
                this.addFieldException("tabs:confirmationPassword",
                        "passwordMissmatch", Boolean.FALSE);
                bValid = false;
            }

            if (!ValidationHelper.isNullOrEmpty(this.getConfirmPwd())
                    && !ValidationHelper.isNullOrEmpty(this.getPwd())
                    && this.getConfirmPwd().equals(this.getPwd())) {
                if (!ValidationHelper.checkFieldLength(this.getPwd(), 8, 14)
                        && !ValidationHelper
                        .checkFieldLength(this.getConfirmPwd(), 8, 14)) {
                    this.addFieldException("tabs:newPassword",
                            "passwordFormatError");
                    this.addFieldException("tabs:confirmationPassword",
                            "passwordFormatError", Boolean.FALSE);
                    bValid = false;
                } else if (!ValidationHelper.checkCorrectFormatByExpression(
                        "^[A-Za-z0-9_-]{8,14}$", this.getPwd().trim())) {
                    this.addFieldException("tabs:newPassword",
                            "passContainsIllegalSymbols");
                    this.addFieldException("tabs:confirmationPassword",
                            "passContainsIllegalSymbols", Boolean.FALSE);
                    bValid = false;
                }
            }

            if (bValid) {
                if (!ValidationHelper.isNullOrEmpty(this.getOldPwd())
                        && !ValidationHelper.isNullOrEmpty(this.getPwd())) {
                    if (this.getOldPwd().equals(this.getPwd())) {
                        this.addFieldException("tabs:newPassword",
                                "passwordChangePasswordShouldBeDifferent");
                        this.addFieldException("tabs:confirmationPassword",
                                "passwordChangePasswordShouldBeDifferent",
                                Boolean.FALSE);
                        bValid = false;
                    }
                }
            }

            if (!bValid) {
                addPasswordExpiredMessage();
            }
        }
    }

    @Override
    public void onSave() throws HibernateException, PersistenceBeanException,
            NumberFormatException, IOException, InstantiationException,
            IllegalAccessException {
        if (!ValidationHelper.isNullOrEmpty(this.getPwd())) {
            this.getEntity().setPassword(MD5.encodeString(this.getPwd(), null));
            this.getContext().getExternalContext().getApplicationMap()
                    .put("reload_users", Boolean.TRUE);
        }

        DaoManager.save(this.getEntity());

        this.setDisableFunctionality(Boolean.FALSE);
        if (this.getEntity().getId().equals(this.getCurrentUser().getId())) {
            UserHolder.getInstance().setCurrentUser(
                    UserWrapper.wrap(getEntity(), DaoManager.getSession()));
        }
    }

    public String getOldPwd() {
        return oldPwd;
    }

    public void setOldPwd(String oldPwd) {
        this.oldPwd = oldPwd;
    }

    public String getPwd() {
        return pwd;
    }

    public void setPwd(String pwd) {
        this.pwd = pwd;
    }

    public String getConfirmPwd() {
        return confirmPwd;
    }

    public void setConfirmPwd(String confirmPwd) {
        this.confirmPwd = confirmPwd;
    }

    public Boolean getDisableFunctionality() {
        return disableFunctionality == null ? Boolean.FALSE
                : disableFunctionality;
    }

    public void setDisableFunctionality(Boolean disableFunctionality) {
        this.disableFunctionality = disableFunctionality;
    }
}
