package it.nexera.ris.persistence.beans.entities.domain;

import it.nexera.ris.common.annotations.MailTag;
import it.nexera.ris.common.enums.*;
import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.helpers.DateTimeHelper;
import it.nexera.ris.common.helpers.LogHelper;
import it.nexera.ris.common.helpers.MailHelper;
import it.nexera.ris.common.helpers.ValidationHelper;
import it.nexera.ris.persistence.UserHolder;
import it.nexera.ris.persistence.beans.dao.CriteriaAlias;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.IndexedEntity;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.Office;
import it.nexera.ris.settings.ApplicationSettingsHolder;
import it.nexera.ris.web.beans.wrappers.logic.MailManagerPriorityWrapper;
import j2html.tags.ContainerTag;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Hibernate;
import org.hibernate.ScrollMode;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;
import org.hibernate.sql.JoinType;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static j2html.TagCreator.*;

/**
 * if you edit WLGInbox do not forgot make changes in
 * {@link it.nexera.ris.persistence.beans.entities.domain.readonly.WLGInboxShort}
 */
@Entity
@Table(name = "wlg_inbox")
public class WLGInbox extends IndexedEntity {

    public transient final Log log = LogFactory.getLog(IndexedEntity.class);

    private static final long serialVersionUID = 863893897426501718L;

    @Column(name = "server_id")
    private Long serverId;

    /**
     * {@link it.nexera.ris.common.enums.MailManagerStatuses}
     */
    @Column(name = "state_id", columnDefinition = "INT DEFAULT 1", nullable = false)
    private Long state;

    /**
     * {@link it.nexera.ris.common.enums.MailManagerStatuses}
     */
    @Column(name = "previous_state_id")
    private Long previousState;

    @Column(name = "message_uid", length = 200)
    private String messageUid;

    @Column(name = "receive_date", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP", nullable = false)
    private Date receiveDate;

    @Column(name = "send_date", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP", nullable = false)
    private Date sendDate;

    @Column(name = "process_date", columnDefinition = "TIMESTAMP NULL ON UPDATE CURRENT_TIMESTAMP")
    private Date processDate;

    @Column(name = "path", length = 500)
    private String path;

    @Column(name = "email_from", length = 200)
    private String emailFrom;

    @Column(name = "email_to", length = 400)
    private String emailTo;

    @Column(name = "email_cc", length = 400)
    private String emailCC;

    @Column(name = "email_bcc", length = 400)
    private String emailBCC;

    @Column(name = "email_subject", length = 500)
    private String emailSubject;

    @Column(name = "email_body", columnDefinition = "TEXT")
    private String emailBody;

    @Column(name = "email_body_html", columnDefinition = "LONGTEXT")
    private String emailBodyHtml;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "disposition_notification_to")
    private Boolean dispositionNotificationTo;

    @Column(name = "delivered_notification")
    private Boolean deliveredNotification;

    @Column(name = "requested_read_confirm")
    private Boolean requestedReadConfirm;

    @Column(name = "confirm_sent")
    private Boolean confirmSent;

    @Column(name = "email_disposition_notification", length = 400)
    private String emailDispositionNotification;

    @Column(name = "xpriority")
    private Integer xpriority;

    @Column
    private Boolean response;

    @Column
    private Boolean forwarded;

    @OneToMany(mappedBy = "mail", fetch = FetchType.LAZY)
    private List<Request> requests;

    @ManyToOne
    @JoinColumn(name = "folder_id")
    private WLGFolder folder;

    @ManyToOne
    @JoinColumn(name = "change_state_user_id")
    private User userChangedState;

    @ManyToOne
    @JoinColumn(name = "change_folder_user_id")
    private User userChangedFolder;

    @OneToMany(mappedBy = "mail", fetch = FetchType.LAZY)
    private List<Document> documents;

    @Column(name = "reference_request")
    private String referenceRequest;

    @Column(name = "ndg")
    private String ndg;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_invoice_id")
    private Client clientInvoice;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_fiduciary_id")
    private Client clientFiduciary;

    @ManyToMany
    @JoinTable(name = "inbox_manager", joinColumns = {
            @JoinColumn(name = "inbox_id", table = "wlg_inbox")
    }, inverseJoinColumns = {
            @JoinColumn(name = "client_id", table = "client")
    })
    private List<Client> managers;

    @OneToMany(mappedBy = "inbox")
    private List<WLGExport> wlgExports;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "office_id")
    private Office office;

    @Column(name = "cdr")
    private String cdr;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id")
    private Client client;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "received_inbox_id")
    private WLGInbox recievedInbox;

    private String fiduciary;

    @Transient
    private List<WLGExport> files;

    @Transient
    private List<WLGExport> imgFiles;

    @Transient
    private List<Long> requestIds;

    @Transient
    private String[] emailFromStr;

    @Transient
    private String[] emailToStr;

    @Transient
    private String emailPrefix;

    @Transient
    private String emailPostfix;

    @Transient
    private String emailBodyToEditor;

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

    public boolean getNotRead() {
        return !getRead();
    }

    public String[] getEmailFromStr() {
        if (emailFromStr == null) {
            emailFromStr = MailHelper.parseEmail(emailFrom);
        }
        return emailFromStr;
    }

    public void setEmailFromStr(String[] emailFromStr) {
        this.emailFromStr = emailFromStr;
    }

    public String[] getEmailToStr() {
        if (emailToStr == null) {
            emailToStr = MailHelper.parseEmail(emailTo);
        }
        return emailToStr;
    }

    public String getCreateUser() throws PersistenceBeanException, IllegalAccessException, InstantiationException {
        return DaoManager.get(User.class, getCreateUserId()).getFullname();
    }

    public void setEmailToStr(String[] emailToStr) {
        this.emailToStr = emailToStr;
    }

    public boolean isCanEdit() {
        return MailManagerTypes.DRAFT.equals(getMailType());
    }

    public List<Long> getRequestIds() {
        if (requestIds == null) {
            try {
                requestIds = DaoManager.loadIds(Request.class, new Criterion[]
                        {
                                Restrictions.eq("mail.id", getId()),
                                Restrictions.or(
                                        Restrictions.eq("isDeleted", Boolean.FALSE),
                                        Restrictions.isNull("isDeleted"))
                        });
            } catch (Exception e) {
                LogHelper.log(log, e);
            }
        }

        return requestIds;
    }

    public MailManagerTypes getMailType() {
        if (ValidationHelper.isNullOrEmpty(this.getServerId())) {
            return MailManagerTypes.DRAFT;
        } else if (this.getServerId().equals(Long.parseLong(ApplicationSettingsHolder.getInstance()
                .getByKey(ApplicationSettingsKeys.SENT_SERVER_ID).getValue()))) {
            return MailManagerTypes.SENT;
        } else {
            return MailManagerTypes.RECEIVED;
        }
    }

    public boolean isDraft() {
        return this.getMailType() == MailManagerTypes.DRAFT;
    }

    public boolean isReceived() {
        return this.getMailType() == MailManagerTypes.RECEIVED;
    }

    public String getMailTypeStr() {
        return getMailType().toString();
    }

    public String getSendDateStr() {
        if (getReceived()) {
            return DateTimeHelper.toFormatedStringLocal(getSendDate(),
                    DateTimeHelper.getDatePatternWithMinutes(), null);
        } else {
            return DateTimeHelper.toStringWithMinutes(getSendDate());
        }
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

    public String getStyleClass() {
        String result = "";

        if (!ValidationHelper.isNullOrEmpty(this.getState())) {
            MailManagerStatuses state = MailManagerStatuses
                    .findById(this.getState());

            if (state != null) {
                switch (state) {
                    case ARCHIVED:
                        result = "style_row_archived_or_deleted_mail";
                        break;
                    case ASSIGNED:
                        result = "style_row_assigned_mail";
                        break;
                    case CANCELED:
                        result = "style_row_canceled_mail";
                        break;
                    case CLOSED:
                        result = "style_row_closed_mail";
                        break;
                    case MANAGED:
                        result = "style_row_managed_mail";
                        break;
                    case NEW:
                    case READ:
                        if (!getRead()) {
                            result = "style_row_new_mail";
                        } else {
                            result = "style_row_read_mail";
                        }
                        break;
                    case SUSPENDED:
                        result = "style_row_suspended_mail";
                        break;
                    case DELETED:
                        result = "style_row_archived_or_deleted_mail";
                        break;
                    default:
                        break;

                }
            }
        }

        return result;
    }

    public List<WLGExport> getFiles() {
        if (this.files == null) {
            try {
                files = (List<WLGExport>) DaoManager.getSession().createSQLQuery("SELECT * FROM wlg_export "
                        + "WHERE wlg_inbox_id = " + this.getId() + " and (image_selected = true or "
                        + "(destination_path not like '%image0%.jpg' and destination_path not like '%image0%.jpeg'"
                        + "and destination_path not like '%image0%.gif' and destination_path not like '%image0%.png'))")
                        .addEntity(WLGExport.class).list();

            } catch (Exception e) {
                LogHelper.log(log, e);
            }

            if (files == null) {
                files = new ArrayList<>();
            }
        }

        return files;
    }

    public List<WLGExport> getImgFiles() throws PersistenceBeanException, IllegalAccessException {
        if (this.imgFiles == null) {
            imgFiles = (List<WLGExport>) DaoManager.getSession().createSQLQuery("SELECT * FROM wlg_export "
                    + "WHERE wlg_inbox_id = " + getId() + " AND (image_selected IS NULL OR image_selected = FALSE) AND "
                    + "(destination_path LIKE '%image0%.jpeg' OR destination_path LIKE '%image0%.jpg' OR "
                    + "destination_path LIKE '%image0%.gif' OR destination_path LIKE '%image0%.png'" +
                    " OR destination_path LIKE '%OutlookEmoji%.png'" +
                    " OR destination_path LIKE '%ATT00%.png'" +
                    ")")
                    .addEntity(WLGExport.class).list();
            if (imgFiles == null) {
                imgFiles = new ArrayList<>();
            }
        }
        return imgFiles;
    }

    public void setImgFiles(List<WLGExport> imgFiles) {
        this.imgFiles = imgFiles;
    }

    public String getReceiveDateStr() {
        return DateTimeHelper.toFormatedString(receiveDate, DateTimeHelper.getDateTimePdfPattern());
    }

    public void setFiles(List<WLGExport> files) {
        this.files = files;
    }

    public boolean getCanActivateProcedure() {
        return !MailManagerStatuses.ARCHIVED.getId().equals(this.getState());
    }

    public Long getServerId() {
        return serverId;
    }

    public void setServerId(Long serverId) {
        this.serverId = serverId;
    }

    public String getMessageUid() {
        return messageUid;
    }

    public void setMessageUid(String messageUid) {
        this.messageUid = messageUid;
    }

    public Date getReceiveDate() {
        return receiveDate;
    }

    public void setReceiveDate(Date receiveDate) {
        this.receiveDate = receiveDate;
    }

    public Date getSendDate() {
        return sendDate;
    }

    public void setSendDate(Date sendDate) {
        this.sendDate = sendDate;
    }

    public Date getProcessDate() {
        return processDate;
    }

    public void setProcessDate(Date processDate) {
        this.processDate = processDate;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    @MailTag(emailTag = EmailPDFTags.MAIL_DATE)
    public String getMailDateStr() {
        return getSendDateStr();
    }

    @MailTag(emailTag = EmailPDFTags.MAIL_ATTACHED)
    public String getStringFiles() {
        StringBuilder stringBuilder = new StringBuilder();
        List<WLGExport> list = getFiles();
        for (int i = 0; i < list.size(); i++) {
            stringBuilder.append(list.get(i).getFileName());
            if (list.size() - 1 != i) {
                stringBuilder.append(", ");
            }
        }
        return stringBuilder.toString();
    }

    @MailTag(emailTag = EmailPDFTags.MAIL_FROM)
    public String getEmailFromPdf() {
        return emailFrom == null ? "" : MailHelper.prepareEmailToPdf(emailFrom);
    }

    public String getEmailFrom() {
        return emailFrom == null ? "" : emailFrom;
    }

    public void setEmailFrom(String emailFrom) {
        this.emailFrom = emailFrom;
    }

    @MailTag(emailTag = EmailPDFTags.MAIL_TO)
    public String getEmailToPdf() {
        return emailTo == null ? "" : MailHelper.prepareEmailToPdf(emailTo);
    }

    public String getEmailTo() {
        return emailTo == null ? "" : emailTo;
    }

    public void setEmailTo(String emailTo) {
        this.emailTo = emailTo;
    }

    @MailTag(emailTag = EmailPDFTags.MAIL_COPY)
    public String getEmailCCPdf() {
        return emailCC == null ? "" : MailHelper.prepareEmailToPdf(emailCC);
    }

    @MailTag(emailTag = EmailPDFTags.MAIL_REQUESTS)
    public String getEmailRequests() {
        final String tdCenterStyle = "border: 1px solid black; text-align: center; font-size: 10px;";
        final String tableStyle = "border: 2px solid black; border-collapse: collapse; width: 100%; align: center; table-layout: fixed;";
        if (!ValidationHelper.isNullOrEmpty(this.getRequests())) {
            ContainerTag tbody = tbody();
            List<Request> filteredRequests =  this.getRequests().stream().filter(Request::isDeletedRequest).collect(Collectors.toList());
            for (Request request : filteredRequests) {
                    ContainerTag tr = tr();
                    tr = tr.with(td(request.getMailViewSubject()).withStyle(tdCenterStyle));
                    tr = tr.with(td(request.getRequestTypeName()).withStyle(tdCenterStyle));
                    tr = tr.with(td(request.getServiceName()).withStyle(tdCenterStyle));
                    tr = tr.with(td(request.getAggregationLandChargesRegistryName()).withStyle(tdCenterStyle));
                    tr = tr.with(td(request.getStateDescription()).withStyle(tdCenterStyle));
                    tbody.with(tr);
            }
            return table(thead(
                    tr(
                            th("Soggetto").withStyle("border: 1px solid black;"),
                            th("Servizio").withStyle("border: 1px solid black;"),
                            th("Tipo Richiesta").withStyle("border: 1px solid black;"),
                            th("Conservatoria").withStyle("border: 1px solid black;"),
                            th("Stato").withStyle("border: 1px solid black;"))), tbody).withStyle(tableStyle).toString();

        }
        return "";
    }

    public String getEmailCC() {
        return emailCC == null ? "" : emailCC;
    }

    public void setEmailCC(String emailCC) {
        this.emailCC = emailCC;
    }

    @MailTag(emailTag = EmailPDFTags.MAIL_SUBJECT)
    public String getEmailSubject() {
        return emailSubject == null ? "" : emailSubject;
    }

    public void setEmailSubject(String emailSubject) {
        this.emailSubject = emailSubject;
    }

    public String getEmailTextBody() {
        return emailBody;
    }

    @MailTag(emailTag = EmailPDFTags.MAIL_BODY)
    public String getEmailBody() {
        if (ValidationHelper.isNullOrEmpty(path)
                && !ValidationHelper.isNullOrEmpty(emailBodyHtml)) {
            return emailBodyHtml.replaceAll("\\$", "&dollar;").replaceAll("\r\n", "");
        }
        if (getMailType() == MailManagerTypes.RECEIVED && !ValidationHelper.isNullOrEmpty(path)) {
            String eml = getEmailBodyEml();
            if (eml != null) {
                return eml.replaceAll("\\$", "&dollar;");
            }
        }
        if (getMailType() == MailManagerTypes.RECEIVED) {
            return emailBody == null ? "" : emailBody.replaceAll("\r\n", "<br/>")
                    .replaceAll("\\$", "&dollar;");
        }
        return emailBody == null ? "" : emailBody.replaceAll("\\$", "&dollar;");
    }

    @MailTag(emailTag = EmailPDFTags.MAIL_BLIND_COPY)
    public String getEmailBCCPdf() {
        return emailBCC == null ? "" : MailHelper.prepareEmailToPdf(emailBCC);
    }

    public String getEmailBCC() {
        return emailBCC == null ? "" : emailBCC;
    }

    public void setEmailBody(String emailBody) {
        if (getMailType() == MailManagerTypes.RECEIVED) {
            this.emailBodyHtml = emailBody;
        } else {
            this.emailBody = emailBody;
        }
    }

    public String getEmailBodyHtml() {
        return emailBodyHtml;
    }

    @Transient
    public String getEmailBodyEml() {
        String html = MailHelper.getHtmlFromEml(path);
        return html != null ? html : getEmailBodyHtml();
    }

    @Transient
    public String getEmailBodyToEditor() {
        if (emailBodyToEditor == null) {
            StringBuilder html = new StringBuilder();
            html.append(getEmailPrefix());
            html.append(getEmailBody());
            html.append(getEmailPostfix());
            emailBodyToEditor = html.toString();
        }
        return emailBodyToEditor;
    }

    public List<Request> getValidRequests() throws PersistenceBeanException, IllegalAccessException {
        if (!Hibernate.isInitialized(getRequests())) {
           reloadRequests();
        }
        if (!ValidationHelper.isNullOrEmpty(getRequests())) {
            return getRequests().stream().filter(Request::isDeletedRequest).sorted(Comparator.comparing(Request::getRequestTypeName)).collect(Collectors.toList());
        }
        return null;
    }

    public void reloadRequests() throws PersistenceBeanException, IllegalAccessException {
        this.setRequests(DaoManager.load(Request.class, new CriteriaAlias[]{
                new CriteriaAlias("mail", "m", JoinType.INNER_JOIN)
        }, new Criterion[]{
                Restrictions.eq("m.id", this.getId())
        }));
        if(!ValidationHelper.isNullOrEmpty(this.getRequests())) {
            this.getRequests().stream().forEach(r -> {
                Hibernate.initialize(r.getSubject());
                Hibernate.initialize(r.getRequestType());
                Hibernate.initialize(r.getService());
                Hibernate.initialize(r.getMultipleServices());
                Hibernate.initialize(r.getAggregationLandChargesRegistry());
                Hibernate.initialize(r.getCity());
                Hibernate.initialize(r.getProvince());
                Hibernate.initialize(r.getCountry());
                Hibernate.initialize(r.getResidence());
                Hibernate.initialize(r.getDomicile());
                Hibernate.initialize(r.getDocuments());
            });
        }
    }

    public void setEmailBodyHtml(String emailBodyHtml) {
        this.emailBodyHtml = emailBodyHtml;
    }

    public Long getState() {
        return state;
    }

    public void setState(Long state) {
        this.state = state;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void setEmailBCC(String emailBCC) {
        this.emailBCC = emailBCC;
    }

    public List<Request> getRequests() {
        return requests;
    }

    public void setRequests(List<Request> requests) {
        this.requests = requests;
    }

    public Boolean getDispositionNotificationTo() {
        return dispositionNotificationTo;
    }

    public void setDispositionNotificationTo(Boolean dispositionNotificationTo) {
        this.dispositionNotificationTo = dispositionNotificationTo;
    }

    public Boolean getRequestedReadConfirm() {
        return requestedReadConfirm;
    }

    public void setRequestedReadConfirm(Boolean requestedReadConfirm) {
        this.requestedReadConfirm = requestedReadConfirm;
    }

    public Boolean getConfirmSent() {
        return confirmSent;
    }

    public void setConfirmSent(Boolean confirmSent) {
        this.confirmSent = confirmSent;
    }

    public String getEmailDispositionNotification() {
        return emailDispositionNotification;
    }

    public void setEmailDispositionNotification(
            String emailDispositionNotification) {
        this.emailDispositionNotification = emailDispositionNotification;
    }

    public Integer getXpriority() {
        return xpriority;
    }

    public void setXpriority(Integer xpriority) {
        this.xpriority = xpriority;
    }

    @Transient
    public MailManagerPriorityWrapper getPriorityWrapper() {
        return getXpriority() != null ? new MailManagerPriorityWrapper(MailManagerPriority.findById(getXpriority())) : null;
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

    public Boolean getRealResponse() {
        return (!ValidationHelper.isNullOrEmpty(getResponse()) && getResponse());
    }

    public Boolean getRealForward() {
        return (!ValidationHelper.isNullOrEmpty(getForwarded()) && getForwarded());
    }

    public Boolean getDeliveredNotification() {
        return deliveredNotification;
    }

    public void setDeliveredNotification(Boolean deliveredNotification) {
        this.deliveredNotification = deliveredNotification;
    }

    public String getEmailPrefix() {
        if (emailPrefix == null) {
            return "";
        }
        return emailPrefix;
    }

    public void setEmailPrefix(String emailPrefix) {
        this.emailPrefix = emailPrefix;
    }

    public String getEmailPostfix() {
        if (emailPostfix == null) {
            return "";
        }
        return emailPostfix;
    }

    public void setEmailPostfix(String emailPostfix) {
        this.emailPostfix = emailPostfix;
    }

    public void setEmailBodyToEditor(String emailBodyToEditor) {
        this.emailBodyToEditor = emailBodyToEditor;
    }

    public WLGFolder getFolder() {
        return folder;
    }

    public void setFolder(WLGFolder folder) {
        this.folder = folder;
    }

    public Boolean getReceived() {
        if (received == null) {
            received = getServerId() != null && getServerId().equals(Long.parseLong(ApplicationSettingsHolder
                    .getInstance().getByKey(ApplicationSettingsKeys.RECEIVED_SERVER_ID).getValue()));
        }
        return received;
    }

    public void setReceived(Boolean received) {
        this.received = received;
    }

    public Long getPreviousState() {
        return previousState;
    }

    public void setPreviousState(Long previousState) {
        this.previousState = previousState;
    }

    public User getUserChangedState() {
        return userChangedState;
    }

    public void setUserChangedState(User userChangedState) {
        this.userChangedState = userChangedState;
    }

    public User getUserChangedFolder() {
        return userChangedFolder;
    }

    public void setUserChangedFolder(User userChangedFolder) {
        this.userChangedFolder = userChangedFolder;
    }

    public List<Document> getDocuments() {
        return documents;
    }

    public void setDocuments(List<Document> documents) {
        this.documents = documents;
    }

    public String getReferenceRequest() {
        return referenceRequest;
    }

    public void setReferenceRequest(String referenceRequest) {
        this.referenceRequest = referenceRequest;
    }

    public String getNdg() { return ndg; }

    public void setNdg(String ndg) { this.ndg = ndg; }

    public Client getClientInvoice() {
        return clientInvoice;
    }

    public void setClientInvoice(Client clientInvoice) {
        this.clientInvoice = clientInvoice;
    }

    public Client getClientFiduciary() {
        return clientFiduciary;
    }

    public void setClientFiduciary(Client clientFiduciary) {
        this.clientFiduciary = clientFiduciary;
    }

    public List<Client> getManagers() {
        return managers;
    }

    public void setManagers(List<Client> managers) {
        this.managers = managers;
    }

    public List<WLGExport> getWlgExports() {
        return wlgExports;
    }

    public void setWlgExports(List<WLGExport> wlgExports) {
        this.wlgExports = wlgExports;
    }

    public String getCdr() {
		return cdr;
	}

	public void setCdr(String cdr) {
		this.cdr = cdr;
	}

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    public Office getOffice() {
        return office;
    }

    public void setOffice(Office office) {
        this.office = office;
    }

    public WLGInbox getRecievedInbox() {
        return recievedInbox;
    }

    public void setRecievedInbox(WLGInbox recievedInbox) {
        this.recievedInbox = recievedInbox;
    }

    public List<Request> getToBeSentRequests() throws PersistenceBeanException, IllegalAccessException {
        if (!Hibernate.isInitialized(getRequests())) {
            reloadRequests();
        }
        if (!ValidationHelper.isNullOrEmpty(getRequests())) {
            return getRequests().stream().filter(
                    request -> (request.isDeletedRequest() && RequestState.TO_BE_SENT.getId() == request.getStateId()))
                    .sorted(Comparator.comparing(Request::getRequestTypeName))
                    .collect(Collectors.toList());
        }
        return null;
    }

    public String getFiduciary() {
        return fiduciary;
    }

    public void setFiduciary(String fiduciary) {
        this.fiduciary = fiduciary;
    }
}
