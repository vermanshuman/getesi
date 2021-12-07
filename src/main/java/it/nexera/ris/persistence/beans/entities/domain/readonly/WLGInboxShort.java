package it.nexera.ris.persistence.beans.entities.domain.readonly;

import it.nexera.ris.common.enums.ApplicationSettingsKeys;
import it.nexera.ris.common.enums.MailManagerPriority;
import it.nexera.ris.common.enums.MailManagerStatuses;
import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.helpers.DateTimeHelper;
import it.nexera.ris.common.helpers.LogHelper;
import it.nexera.ris.common.helpers.MailHelper;
import it.nexera.ris.common.helpers.ValidationHelper;
import it.nexera.ris.persistence.UserHolder;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.IndexedEntity;
import it.nexera.ris.persistence.beans.entities.domain.User;
import it.nexera.ris.persistence.beans.entities.domain.WLGExport;
import it.nexera.ris.persistence.beans.entities.domain.WLGFolder;
import it.nexera.ris.settings.ApplicationSettingsHolder;
import it.nexera.ris.web.beans.wrappers.logic.MailManagerPriorityWrapper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.ScrollMode;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;

import javax.persistence.*;
import java.math.BigInteger;
import java.util.Date;
import java.util.List;

/**
 * if you edit WLGInboxShort do not forgot make changes in
 * {@link it.nexera.ris.persistence.beans.entities.domain.WLGInbox}
 */
@Entity
@Table(name = "wlg_inbox")
public class WLGInboxShort extends IndexedEntity {

    public transient final Log log = LogFactory.getLog(IndexedEntity.class);

    /**
     * {@link it.nexera.ris.common.enums.MailManagerStatuses}
     */
    @Column(name = "state_id", columnDefinition = "INT DEFAULT 1", nullable = false)
    private Long state;

    @Column(name = "xpriority")
    private Integer xpriority;

    @Column(name = "send_date", columnDefinition = "TIMESTAMP", nullable = false)
    private Date sendDate;

    @Column(name = "email_from", length = 200)
    private String emailFrom;

    @Column(name = "email_to", length = 400)
    private String emailTo;

    @Column(name = "email_subject", length = 500)
    private String emailSubject;

    @Column
    private Boolean response;

    @Column
    private Boolean forwarded;

    @Column(name = "server_id")
    private Long serverId;

    @ManyToOne
    @JoinColumn(name = "folder_id")
    private WLGFolder folder;

    @Column(name = "email_body", columnDefinition = "TEXT")
    private String emailBody;

    @ManyToOne
    @JoinColumn(name = "change_state_user_id")
    private User userChangedState;

    @Transient
    private String[] emailFromStr;

    @Transient
    private String[] emailToStr;

    @Transient
    private Boolean files;

    @Transient
    private Boolean received;

    public boolean getRead() {
        boolean isRead = false;
        try {
            isRead = DaoManager.getSession()
                    .createQuery("select 1 from ReadWLGInbox as inbox where inbox.mailId= :mailId and inbox.userId = :userId")
                    .setLong("mailId", getId()).setLong("userId", UserHolder.getInstance().getCurrentUser().getId())
                    .setFetchSize(1).scroll(ScrollMode.FORWARD_ONLY).next();
        } catch (PersistenceBeanException | IllegalAccessException e) {
            LogHelper.log(log, e);
        }
        return isRead;
    }

    public String getStateStr() {
        if (!ValidationHelper.isNullOrEmpty(getState())) {
            MailManagerStatuses state = MailManagerStatuses.findById(this.getState());
            return state.toString();
        }
        return "";
    }

    public String getStateName() {
        if (!ValidationHelper.isNullOrEmpty(this.getState())) {
            MailManagerStatuses state = MailManagerStatuses.findById(this.getState());
            if (state != null) {
                return state.name();
            }
        }
        return "";
    }

    public Boolean getFiles() {
        if (this.files == null) {
            try {
                BigInteger count = DaoManager.countQuery("SELECT count(*) FROM wlg_export "
                        + "WHERE wlg_inbox_id = " + this.getId() + " and (image_selected = true or "
                        + "(destination_path not like '%image0%.jpg' and destination_path not like '%image0%.jpeg'"
                        + "and destination_path not like '%image0%.gif' and destination_path not like '%image0%.png'))");

                this.files = !ValidationHelper.isNullOrEmpty(count) && !count.equals(BigInteger.ZERO);
            } catch (Exception e) {
                LogHelper.log(log, e);
            }
        }

        return files;
    }

    public Boolean getRealForward() {
        return (!ValidationHelper.isNullOrEmpty(getForwarded()) && getForwarded());
    }

    public Boolean getRealResponse() {
        return (!ValidationHelper.isNullOrEmpty(getResponse()) && getResponse());
    }

    public boolean getCanActivateProcedure() {
        return !MailManagerStatuses.ARCHIVED.getId().equals(this.getState());
    }

    @Transient
    public MailManagerPriorityWrapper getPriorityWrapper() {
        return getXpriority() != null ? new MailManagerPriorityWrapper(MailManagerPriority.findById(getXpriority())) : null;
    }

    public String getSendDateStr() {
        if (getReceived()) {
            return DateTimeHelper.toFormatedStringLocal(getSendDate(),
                    DateTimeHelper.getDatePatternWithMinutes(), null);
        } else {
            return DateTimeHelper.toStringWithMinutes(getSendDate());
        }
    }

    public String[] getEmailFromStr() {
        if (emailFromStr == null) {
            emailFromStr = MailHelper.parseEmail(emailFrom);
        }
        return emailFromStr;
    }

    public String[] getEmailToStr() {
        if (emailToStr == null) {
            emailToStr = MailHelper.parseEmail(emailTo);
        }
        return emailToStr;
    }

    public String getCreateUser() throws PersistenceBeanException, IllegalAccessException, InstantiationException {
        return DaoManager.get(User.class, getCreateUserId()).getFirstName();
    }

    public String getChangeStateUser() {
        return getUserChangedState() == null ? "" : getUserChangedState().getFirstName();
    }

    public Boolean getReceived() {
        if (received == null) {
            received = getServerId() != null
                    && getServerId().equals(Long.parseLong(ApplicationSettingsHolder.getInstance()
                    .getByKey(ApplicationSettingsKeys.RECEIVED_SERVER_ID).getValue()));
        }
        return received;
    }

    public void setReceived(Boolean received) {
        this.received = received;
    }

    public void setEmailToStr(String[] emailToStr) {
        this.emailToStr = emailToStr;
    }

    public void setEmailFromStr(String[] emailFromStr) {
        this.emailFromStr = emailFromStr;
    }

    public Long getState() {
        return state;
    }

    public void setState(Long state) {
        this.state = state;
    }

    public Integer getXpriority() {
        return xpriority;
    }

    public void setXpriority(Integer xpriority) {
        this.xpriority = xpriority;
    }

    public Date getSendDate() {
        return sendDate;
    }

    public void setSendDate(Date sendDate) {
        this.sendDate = sendDate;
    }

    public String getEmailFrom() {
        return emailFrom;
    }

    public void setEmailFrom(String emailFrom) {
        this.emailFrom = emailFrom;
    }

    public String getEmailTo() {
        return emailTo;
    }

    public void setEmailTo(String emailTo) {
        this.emailTo = emailTo;
    }

    public String getEmailSubject() {
        return emailSubject;
    }

    public void setEmailSubject(String emailSubject) {
        this.emailSubject = emailSubject;
    }

    public Boolean getResponse() {
        return response;
    }

    public void setResponse(Boolean response) {
        this.response = response;
    }

    public Boolean getForwarded() {
        return forwarded;
    }

    public void setForwarded(Boolean forwarded) {
        this.forwarded = forwarded;
    }

    public void setFiles(Boolean files) {
        this.files = files;
    }

    public Long getServerId() {
        return serverId;
    }

    public void setServerId(Long serverId) {
        this.serverId = serverId;
    }

    public WLGFolder getFolder() {
        return folder;
    }

    public void setFolder(WLGFolder folder) {
        this.folder = folder;
    }

    public String getEmailBody() {
        return emailBody;
    }

    public void setEmailBody(String emailBody) {
        this.emailBody = emailBody;
    }

    public User getUserChangedState() {
        return userChangedState;
    }

    public void setUserChangedState(User userChangedState) {
        this.userChangedState = userChangedState;
    }
}
