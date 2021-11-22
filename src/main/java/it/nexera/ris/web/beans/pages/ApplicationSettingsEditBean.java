package it.nexera.ris.web.beans.pages;

import it.nexera.ris.common.enums.ApplicationSettingsKeys;
import it.nexera.ris.common.enums.PageTypes;
import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.helpers.LogHelper;
import it.nexera.ris.common.helpers.RedirectHelper;
import it.nexera.ris.common.helpers.ValidationHelper;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.domain.ApplicationSettingsValue;
import it.nexera.ris.persistence.beans.entities.domain.WLGServer;
import it.nexera.ris.settings.ApplicationSettingsHolder;
import it.nexera.ris.web.beans.EntityEditPageBean;
import it.nexera.ris.web.beans.wrappers.logic.ApplicationSettingsValueWrapper;
import org.hibernate.HibernateException;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import java.io.IOException;
import java.io.Serializable;
import java.util.List;

@ManagedBean(name = "appSettingsBean")
@ViewScoped
public class ApplicationSettingsEditBean extends
        EntityEditPageBean<ApplicationSettingsValue> implements Serializable {
    private static final Long SERVER_TYPE_15 = 15l;

    private static final Long SERVER_TYPE_14 = 14l;

    private static final long serialVersionUID = 2780565527350538548L;

    // Common settings
    private ApplicationSettingsValueWrapper settingPasswordPeriodExpiration;

    // FILE ENTITY

    private ApplicationSettingsValueWrapper settingFileEntityPath;

    // SESSION TIMEOUT

    private ApplicationSettingsValueWrapper settingSessionTimeout;

    private ApplicationSettingsValueWrapper settingSessionCheckTimeout;

    private WLGServer mailServerReceive;

    private WLGServer mailServerSend;

    // Fattura24 Settings
    private ApplicationSettingsValueWrapper settingsCloudApiURL;

    private ApplicationSettingsValueWrapper settingsCloudApiKey;

    /*
     * (non-Javadoc)
     *
     * @see it.nexera.web.beans.EntityEditPageBean#pageLoad()
     */
    @Override
    public void onLoad() throws NumberFormatException, HibernateException,
            PersistenceBeanException, InstantiationException,
            IllegalAccessException {
        fillSettingsValues();
        fillMailServer();
    }

    private void fillMailServer() {
        try {
            List<WLGServer> wlgServers = DaoManager.load(WLGServer.class);

            if (ValidationHelper.isNullOrEmpty(wlgServers)) {
                this.setMailServerReceive(new WLGServer());
                this.setMailServerSend(new WLGServer());
            } else {
                if (wlgServers.size() > 2) {
                    LogHelper.log(log,
                            "In table [wlg_server] present more than two rows! Check it!");
                }

                for (WLGServer server : wlgServers) {
                    if (SERVER_TYPE_15.equals(server.getType())
                            && this.getMailServerSend() == null) {
                        this.setMailServerSend(server);
                    } else if (SERVER_TYPE_14.equals(server.getType())
                            && this.getMailServerReceive() == null) {
                        this.setMailServerReceive(server);
                    } else {
                        continue;
                    }
                }
            }
        } catch (Exception e) {
            LogHelper.log(log, e);
        }
    }

    private void fillSettingsValues() {
        setSettingPasswordPeriodExpiration(
                ApplicationSettingsHolder.getInstance().getByKey(
                        ApplicationSettingsKeys.PASSWORD_EXPIRATION_PERIOD));

        setSettingFileEntityPath(ApplicationSettingsHolder.getInstance()
                .getByKey(ApplicationSettingsKeys.FILE_ENTITY_PATH));

        setSettingSessionCheckTimeout(ApplicationSettingsHolder.getInstance()
                .getByKey(ApplicationSettingsKeys.SESSION_CHECK_TIMEOUT));
        setSettingSessionTimeout(ApplicationSettingsHolder.getInstance()
                .getByKey(ApplicationSettingsKeys.SESSION_TIMEOUT));

        setSettingsCloudApiKey(ApplicationSettingsHolder.getInstance().getByKey(
                        ApplicationSettingsKeys.CLOUD_API_KEY));

        setSettingsCloudApiURL(ApplicationSettingsHolder.getInstance().getByKey(
                ApplicationSettingsKeys.CLOUD_API_URL));
    }

    /*
     * (non-Javadoc)
     *
     * @see it.nexera.web.beans.EntityEditPageBean#validate()
     */
    @Override
    public void onValidate() throws PersistenceBeanException {

    }

    /*
     * (non-Javadoc)
     *
     * @see it.nexera.web.beans.EntityEditPageBean#save()
     */
    @Override
    public void onSave() throws HibernateException, PersistenceBeanException,
            NumberFormatException, IOException, InstantiationException,
            IllegalAccessException {
        if (ValidationHelper
                .isNullOrEmpty(getSettingPasswordPeriodExpiration())) {
            getSettingPasswordPeriodExpiration().setValue("0");
        }

        Integer value = Integer
                .parseInt(getSettingPasswordPeriodExpiration().getValue());
        getSettingPasswordPeriodExpiration().setValue(String.valueOf(value));

        ApplicationSettingsHolder.getInstance()
                .applyNewValue(getSettingPasswordPeriodExpiration());

        // FILE ENTITY

        if (getSettingFileEntityPath() != null
                && (getSettingFileEntityPath().getValue().endsWith("\\")
                || getSettingFileEntityPath().getValue().endsWith("/"))) {
            getSettingFileEntityPath()
                    .setValue(getSettingFileEntityPath().getValue().substring(0,
                            getSettingFileEntityPath().getValue().length()
                                    - 1));
        }

        ApplicationSettingsHolder.getInstance()
                .applyNewValue(getSettingFileEntityPath());

        ApplicationSettingsHolder.getInstance()
                .applyNewValue(getSettingSessionCheckTimeout());
        ApplicationSettingsHolder.getInstance()
                .applyNewValue(getSettingSessionTimeout());

        ApplicationSettingsHolder.getInstance()
                .applyNewValue(getSettingsCloudApiKey());

        ApplicationSettingsHolder.getInstance()
                .applyNewValue(getSettingsCloudApiURL());

        if (!ValidationHelper.isNullOrEmpty(this.getMailServerReceive())
                && !ValidationHelper
                .isNullOrEmpty(this.getMailServerReceive().getHost())) {
            DaoManager.save(this.getMailServerReceive());
        }
    }

    public ApplicationSettingsValueWrapper getSettingPasswordPeriodExpiration() {
        return settingPasswordPeriodExpiration;
    }

    public void setSettingPasswordPeriodExpiration(
            ApplicationSettingsValueWrapper settingPasswordPeriodExpiration) {
        this.settingPasswordPeriodExpiration = settingPasswordPeriodExpiration;
    }

    @Override
    public void goBack() {
        RedirectHelper.goTo(PageTypes.HOME);
    }

    public ApplicationSettingsValueWrapper getSettingFileEntityPath() {
        return settingFileEntityPath;
    }

    public void setSettingFileEntityPath(
            ApplicationSettingsValueWrapper settingFileEntityPath) {
        this.settingFileEntityPath = settingFileEntityPath;
    }

    public ApplicationSettingsValueWrapper getSettingSessionTimeout() {
        return settingSessionTimeout;
    }

    public void setSettingSessionTimeout(
            ApplicationSettingsValueWrapper settingSessionTimeout) {
        this.settingSessionTimeout = settingSessionTimeout;
    }

    public ApplicationSettingsValueWrapper getSettingSessionCheckTimeout() {
        return settingSessionCheckTimeout;
    }

    public void setSettingSessionCheckTimeout(
            ApplicationSettingsValueWrapper settingSessionCheckTimeout) {
        this.settingSessionCheckTimeout = settingSessionCheckTimeout;
    }

	public WLGServer getMailServerReceive() {
        return mailServerReceive;
    }

    public void setMailServerReceive(WLGServer mailServerReceive) {
        this.mailServerReceive = mailServerReceive;
    }

    public WLGServer getMailServerSend() {
        return mailServerSend;
    }

    public void setMailServerSend(WLGServer mailServerSend) {
        this.mailServerSend = mailServerSend;
    }

    public ApplicationSettingsValueWrapper getSettingsCloudApiURL() {
        return settingsCloudApiURL;
    }

    public void setSettingsCloudApiURL(ApplicationSettingsValueWrapper settingsCloudApiURL) {
        this.settingsCloudApiURL = settingsCloudApiURL;
    }

    public ApplicationSettingsValueWrapper getSettingsCloudApiKey() {
        return settingsCloudApiKey;
    }

    public void setSettingsCloudApiKey(ApplicationSettingsValueWrapper settingsCloudApiKey) {
        this.settingsCloudApiKey = settingsCloudApiKey;
    }
}