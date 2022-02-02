package it.nexera.ris.web.beans.wrappers.logic;

import it.nexera.ris.common.enums.MailManagerPriority;
import it.nexera.ris.common.helpers.ResourcesHelper;
import it.nexera.ris.common.helpers.ValidationHelper;
import it.nexera.ris.persistence.beans.entities.domain.WLGInbox;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

public class MailEditWrapper {

    public static final String MAIL_REPLY_SUBJECT_RESOURCE_PREFIX = "mailManagerRepeatSubjectPrefix";

    public static final String MAIL_FORWARD_SUBJECT_RESOURCE_PREFIX = "mailManagerForwardSubjectPrefix";

    public static final String SUBJECT_DELIM = ": ";

    private static final String DELIM = ", ";

    private static final String MAIL_FOOTER = ResourcesHelper.getString("emailFooter");

    private List<FileWrapper> attachedFiles;

    private List<ClientWrapper> clientEmails;

    private String forwardAddress;

    private boolean isSenderServerNotSpecified;

    private Long editMailId;

    private Long emailDeleteId;

    private Long editClientId;

    private Integer selectedPriorityWrapper;

    private AtomicLong fileIndex;

    private Long deleteFileId;

    private WLGInbox wlgInboxEdit;

    private List<String> sendTo;

    public MailEditWrapper(WLGInbox wlgInboxEdit) {
        this.wlgInboxEdit = wlgInboxEdit;
        fillDestination(wlgInboxEdit.getEmailTo());
    }

    public MailEditWrapper() {
        this(new WLGInbox());
    }

    public List<FileWrapper> getAttachedFiles() {
        return attachedFiles;
    }

    public void setAttachedFiles(List<FileWrapper> attachedFiles) {
        this.attachedFiles = attachedFiles;
    }

    public List<ClientWrapper> getClientEmails() {
        return clientEmails;
    }

    public List<ClientWrapper> getAvailableClientEmails() {
        return clientEmails.stream().filter(ce -> ce.getHide() == null || !ce.getHide()).collect(Collectors.toList());
    }

    public void setClientEmails(List<ClientWrapper> clientEmails) {
        this.clientEmails = clientEmails;
    }

    public String getForwardAddress() {
        return forwardAddress;
    }

    public void setForwardAddress(String forwardAddress) {
        this.forwardAddress = forwardAddress;
    }

    public boolean isSenderServerNotSpecified() {
        return isSenderServerNotSpecified;
    }

    public void setSenderServerNotSpecified(boolean senderServerNotSpecified) {
        isSenderServerNotSpecified = senderServerNotSpecified;
    }

    public Long getEditMailId() {
        return editMailId;
    }

    public void setEditMailId(Long editMailId) {
        this.editMailId = editMailId;
    }

    public Long getEmailDeleteId() {
        return emailDeleteId;
    }

    public void setEmailDeleteId(Long emailDeleteId) {
        this.emailDeleteId = emailDeleteId;
    }

    public Long getEditClientId() {
        return editClientId;
    }

    public void setEditClientId(Long editClientId) {
        this.editClientId = editClientId;
    }

    public Integer getPriorityWrapper() {
        return selectedPriorityWrapper;
    }

    public void setPriorityWrapper(Integer selectedPriorityWrapper) {
        this.selectedPriorityWrapper = selectedPriorityWrapper;
    }

    public AtomicLong getFileIndex() {
        return fileIndex;
    }

    public void setFileIndex(AtomicLong fileIndex) {
        this.fileIndex = fileIndex;
    }

    public Long getDeleteFileId() {
        return deleteFileId;
    }

    public void setDeleteFileId(Long deleteFileId) {
        this.deleteFileId = deleteFileId;
    }

    public WLGInbox getWlgInboxEdit() {
        return wlgInboxEdit;
    }

    public void setWlgInboxEdit(WLGInbox wlgInboxEdit) {
        this.wlgInboxEdit = wlgInboxEdit;
        fillDestination(this.wlgInboxEdit.getEmailTo());
    }

    public void installPriority() {
        if (this.getPriorityWrapper() == null) {
            if (this.getWlgInboxEdit() == null || this.getWlgInboxEdit().getPriorityWrapper() == null) {
                this.setPriorityWrapper(MailManagerPriority.NORMAL.getId());
            } else {
                this.setPriorityWrapper(this.getWlgInboxEdit().getPriorityWrapper().getId().intValue());
            }
        }
    }

    public String getSendToInString() {
        StringBuilder builder = new StringBuilder();
        for (String s : this.sendTo) {
            builder.append(s).append(" ");
        }
        return builder.toString();
    }

    public List<String> getSendTo() {
        return sendTo;
    }

    public void setSendTo(List<String> sendTo) {
        this.sendTo = sendTo;
    }

    public void setSubjectForRepeat(String subject) {
        wlgInboxEdit.setEmailSubject(createSpecialSubject(MAIL_REPLY_SUBJECT_RESOURCE_PREFIX, SUBJECT_DELIM, subject));
    }

    public void setSubjectForForward(String subject) {
        wlgInboxEdit.setEmailSubject(createSpecialSubject(MAIL_FORWARD_SUBJECT_RESOURCE_PREFIX, SUBJECT_DELIM, subject));
    }

    public void updateDestination() {
        if (!ValidationHelper.isNullOrEmpty(sendTo)) {
            StringBuilder builder = new StringBuilder();
            sendTo.stream().forEach(item -> {
                builder.append(item).append(DELIM);
            });
            builder.delete(builder.length() - DELIM.length(), builder.length());
            getWlgInboxEdit().setEmailTo(builder.toString());
        }
    }

    public void deleteFile() {
        if (!ValidationHelper.isNull(getDeleteFileId())
                && !ValidationHelper.isNullOrEmpty(getAttachedFiles())) {
            for (FileWrapper file : getAttachedFiles()) {
                if (getDeleteFileId().equals(file.getId())) {
                    getAttachedFiles().remove(file);

                    break;
                }
            }
        }
    }

    public void appendMailFooter() {
        if (wlgInboxEdit != null) {
            if (wlgInboxEdit.getEmailBody() != null) {
                wlgInboxEdit.setEmailBody(wlgInboxEdit.getEmailBody().concat(MAIL_FOOTER));
            } else {
                wlgInboxEdit.setEmailBody(MAIL_FOOTER);
            }
        }
    }

    private String createSpecialSubject(String prefixLiteral, String delim, String subject) {
        StringBuilder builder = new StringBuilder();
        builder.append(ResourcesHelper.getString(prefixLiteral)).append(delim).append(subject);
        return builder.toString();
    }

    public void fillDestination(String fromData) {
        sendTo = new ArrayList<>();
        if (!ValidationHelper.isNullOrEmpty(fromData)) {
            String[] values = fromData.split(DELIM);
            Arrays.stream(values).forEach(sendTo::add);
        }
    }
}
