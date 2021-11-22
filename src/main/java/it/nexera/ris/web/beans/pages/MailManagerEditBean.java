package it.nexera.ris.web.beans.pages;

import static j2html.TagCreator.div;
import static j2html.TagCreator.each;
import static j2html.TagCreator.table;
import static j2html.TagCreator.tbody;
import static j2html.TagCreator.td;
import static j2html.TagCreator.tr;
import static org.apache.commons.collections4.CollectionUtils.emptyIfNull;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.StringJoiner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;

import org.apache.commons.lang.WordUtils;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.hibernate.sql.JoinType;
import org.primefaces.event.FileUploadEvent;

import it.nexera.ris.common.enums.ApplicationSettingsKeys;
import it.nexera.ris.common.enums.DocumentType;
import it.nexera.ris.common.enums.EmailType;
import it.nexera.ris.common.enums.MailEditType;
import it.nexera.ris.common.enums.MailManagerPriority;
import it.nexera.ris.common.enums.MailManagerStatuses;
import it.nexera.ris.common.enums.PageTypes;
import it.nexera.ris.common.enums.RequestState;
import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.helpers.ComboboxHelper;
import it.nexera.ris.common.helpers.DateTimeHelper;
import it.nexera.ris.common.helpers.DocumentTypeRequestReportComparator;
import it.nexera.ris.common.helpers.FileHelper;
import it.nexera.ris.common.helpers.LogHelper;
import it.nexera.ris.common.helpers.MailEmlReader;
import it.nexera.ris.common.helpers.MailHelper;
import it.nexera.ris.common.helpers.MailManagerHelper;
import it.nexera.ris.common.helpers.MessageHelper;
import it.nexera.ris.common.helpers.PrintPDFHelper;
import it.nexera.ris.common.helpers.RedirectHelper;
import it.nexera.ris.common.helpers.ResourcesHelper;
import it.nexera.ris.common.helpers.SaveRequestDocumentsHelper;
import it.nexera.ris.common.helpers.SessionHelper;
import it.nexera.ris.common.helpers.ValidationHelper;
import it.nexera.ris.common.helpers.create.pdf.CreatePDFReportHelper;
import it.nexera.ris.common.helpers.create.xls.CreateExcelRequestsReportHelper;
import it.nexera.ris.common.helpers.create.xls.CreateExternalRequestReportHelper;
import it.nexera.ris.persistence.UserHolder;
import it.nexera.ris.persistence.beans.dao.CriteriaAlias;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.IndexedEntity;
import it.nexera.ris.persistence.beans.entities.domain.Client;
import it.nexera.ris.persistence.beans.entities.domain.ClientEmail;
import it.nexera.ris.persistence.beans.entities.domain.Document;
import it.nexera.ris.persistence.beans.entities.domain.EmailRemove;
import it.nexera.ris.persistence.beans.entities.domain.Referent;
import it.nexera.ris.persistence.beans.entities.domain.Request;
import it.nexera.ris.persistence.beans.entities.domain.User;
import it.nexera.ris.persistence.beans.entities.domain.WLGExport;
import it.nexera.ris.persistence.beans.entities.domain.WLGFolder;
import it.nexera.ris.persistence.beans.entities.domain.WLGInbox;
import it.nexera.ris.persistence.beans.entities.domain.WLGServer;
import it.nexera.ris.settings.ApplicationSettingsHolder;
import it.nexera.ris.web.beans.EntityViewPageBean;
import it.nexera.ris.web.beans.wrappers.logic.ClientEmailWrapper;
import it.nexera.ris.web.beans.wrappers.logic.ClientWrapper;
import it.nexera.ris.web.beans.wrappers.logic.FileWrapper;
import it.nexera.ris.web.beans.wrappers.logic.MailManagerPriorityWrapper;
import it.nexera.ris.web.beans.wrappers.logic.ReferentEmailWrapper;

@ManagedBean(name = "mailManagerEditBean")
@ViewScoped
public class MailManagerEditBean extends EntityViewPageBean<WLGInbox> implements Serializable {

    private static final long serialVersionUID = 7445534474549204586L;

    public static final String MAIL_REPLY_SUBJECT_RESOURCE_PREFIX = "mailManagerRepeatSubjectPrefix";

    public static final String MAIL_FORWARD_SUBJECT_RESOURCE_PREFIX = "mailManagerForwardSubjectPrefix";

    public static final String SUBJECT_DELIM = ": ";

    private static final String DELIM = ", ";

    private static final String MAIL_FOOTER = ResourcesHelper.getString("emailFooter");

    private static final String MAIL_RERLY_FOOTER = ResourcesHelper.getString("emailReplyFooter");

    private static final String defaultSendFromName = "GETESI SRL";

    private MailEditType mailType;

    private List<SelectItem> clients;

    private List<String> selectedClients;

    private List<MailManagerPriorityWrapper> priorityWrappers;

    private Integer selectedPriorityWrapper;

    private Long downloadFileIndex;

    private List<FileWrapper> attachedFiles;

    private List<FileWrapper> copiedImages;

    private Long deleteFileId;

    private String forwardAddress;

    private ClientEmailWrapper newClientEmail;

    private ReferentEmailWrapper newReferentEmail;

    private Long editClientId;

    private List<ClientWrapper> clientEmails;

    private List<String> sendTo;

    private List<String> sendCC;

    private List<String> sendBCC;

    private String mailPdf;

    private boolean printPdf;

    private boolean senderServerNotSpecified;

    private String downloadFilePath;

    private String url;

    private String changeVar;

    private Long baseMailId;

    private String emailToDelete;

    private List<SelectItem> userFolders;

    private Long selectedFolderId;

    private MailEmlReader baseMailReader;

    private Long requestId;

    private Boolean fromView;

    private List<Long> selectedIds;

    private WLGExport excelFornitori;

    private List<Request> selectedRequests;

    private List<Long> fullfillRequestIds;

    private Boolean evadiRichesta;

    @Override
    protected void preLoad() throws PersistenceBeanException, IllegalAccessException, InstantiationException {
        MailEditType type = MailEditType.findByName(getRequestParameter(RedirectHelper.MAIL));
        Long maliId = 0L;
        if (!ValidationHelper.isNullOrEmpty(getRequestParameter(RedirectHelper.MAIL_ID))) {
            maliId = Long.valueOf(getRequestParameter(RedirectHelper.MAIL_ID));
        }
        setMailType(type == null ? MailEditType.NEW : type);
        String requestId = getRequestParameter("request_id");
        if (!ValidationHelper.isNullOrEmpty(requestId)) {
            setRequestId(Long.parseLong(requestId));
        }
        if (!ValidationHelper.isNullOrEmpty(maliId)) {
            setEntity(DaoManager.get(WLGInbox.class, maliId));
            setFromView(true);
        } else {
            setFromView(false);
        }
        setSelectedIds(null);
        if(!ValidationHelper.isNullOrEmpty(getMailType()) &&
                getMailType().equals(MailEditType.SEND_TO_MANAGER)){

            if(!ValidationHelper.isNullOrEmpty(SessionHelper.get("selectedIds"))) {
                List<Long> selectedIds =(List<Long>)SessionHelper.get("selectedIds");
                SessionHelper.removeObject("selectedIds");
                setSelectedIds(selectedIds);
            }
        }


    }

    @Override
    public void onLoad() throws NumberFormatException, HibernateException, PersistenceBeanException,
            InstantiationException, IllegalAccessException, IOException {


        setCopiedImages(new LinkedList<>());
        setBaseMailId(getEntityId());
        startEdit();
        if (getEntity().getEmailBody().isEmpty() && !getMailType().equals(MailEditType.SEND_TO_MANAGER)) {
            appendMailFooter();
        }
        if (!getMailType().equals(MailEditType.SEND_TO_MANAGER)) {
            fillAttachedFiles();
        }else {
            attachFornitoriExcel();
        }

        setClientEmails(new ArrayList<>());

        List<Client> clients = DaoManager.load(Client.class);

        if (!ValidationHelper.isNullOrEmpty(clients)) {
            for (Client client : clients) {
                getClientEmails().add(new ClientWrapper(client));
            }
        }

        installPriority();
        fillWrappers();
    }

    private String createSpecialSubject(String prefixLiteral, String delim, String subject) {
        StringBuilder builder = new StringBuilder();
        builder.append(ResourcesHelper.getString(prefixLiteral)).append(delim).append(subject);
        return builder.toString();
    }

    private void startEdit() throws PersistenceBeanException, IllegalAccessException, InstantiationException {
        if (getMailType() == MailEditType.REQUEST_REPLY_ALL) {
            setEntity(DaoManager.get(Request.class, getRequestId()).getMail());
        }
        if (getMailType() == MailEditType.EDIT) {
            if (getEntity().getServerId() == null) {
                if (!ValidationHelper.isNullOrEmpty(getEntity().getEmailTo())) {
                    setSendTo(Arrays.asList(getEntity().getEmailTo().split(",")));
                }
                if (!ValidationHelper.isNullOrEmpty(getEntity().getEmailCC())) {
                    setSendCC(Arrays.asList(getEntity().getEmailCC().split(",")));
                }
                if (!ValidationHelper.isNullOrEmpty(getEntity().getEmailBCC())) {
                    setSendBCC(Arrays.asList(getEntity().getEmailBCC().split(",")));
                }
            } else {
                setSendTo(MailHelper.parseMailAddress(getEntity().getEmailTo()));
                setSendCC(MailHelper.parseMailAddress(getEntity().getEmailCC()));
                setSendBCC(MailHelper.parseMailAddress(getEntity().getEmailBCC()));
            }
        }
        if (getMailType() == MailEditType.FORWARD || getMailType() == MailEditType.REPLY
                || getMailType() == MailEditType.REPLY_TO_ALL || getMailType() == MailEditType.REQUEST_REPLY_ALL) {
            appendReplyFooter();
        }
        if (getMailType() != MailEditType.EDIT && getMailType() != MailEditType.NEW && getMailType() != MailEditType.REQUEST) {
            WLGInbox inbox = new WLGInbox();

            inbox.setEmailFrom(getEntity().getEmailFrom());
            inbox.setEmailTo(getEntity().getEmailTo());
            inbox.setEmailCC(getEntity().getEmailCC());
            inbox.setEmailBCC(getEntity().getEmailBCC());
            inbox.setEmailSubject(getEntity().getEmailSubject());
            inbox.setEmailBody(getEntity().getEmailBody());
            inbox.setEmailPostfix(getEntity().getEmailPostfix());
            inbox.setEmailPrefix(getEntity().getEmailPrefix());
            inbox.setFiles(getEntity().getFiles());
            inbox.setClient(getEntity().getClient());
            inbox.setClientInvoice(getEntity().getClientInvoice());
            inbox.setClientFiduciary(getEntity().getClientFiduciary());
            inbox.setManagers(getEntity().getManagers());
            inbox.setOffice(getEntity().getOffice());
            inbox.setNdg(getEntity().getNdg());
            inbox.setCdr(getEntity().getCdr());
            inbox.setReferenceRequest(getEntity().getReferenceRequest());
            if (getFromView()) {
                inbox.setRequests(getEntity().getRequests());
            }
            setEntity(inbox);
        }
        if (getMailType() == MailEditType.FORWARD) {
            getEntity().setEmailCC(null);
            getEntity().setEmailBCC(null);
            getEntity().setEmailSubject(createSpecialSubject(MAIL_FORWARD_SUBJECT_RESOURCE_PREFIX, SUBJECT_DELIM, getEntity().getEmailSubject()));
        } else if (getMailType() == MailEditType.REPLY) {
            getEntity().setEmailCC(null);
            getEntity().setEmailBCC(null);
            fillDestination(MailHelper.parseMailAddress(getEntity().getEmailFrom()).get(0));
            getEntity().setEmailSubject(createSpecialSubject(MAIL_REPLY_SUBJECT_RESOURCE_PREFIX, SUBJECT_DELIM, getEntity().getEmailSubject()));
        } else if (getMailType() == MailEditType.REPLY_TO_ALL || getMailType() == MailEditType.REQUEST_REPLY_ALL) {
            String emailsFrom = DaoManager.loadField(WLGServer.class, "login",
                    String.class, new Criterion[]{Restrictions.eq("id", Long.parseLong(
                            ApplicationSettingsHolder.getInstance().getByKey(ApplicationSettingsKeys.SENT_SERVER_ID)
                                    .getValue()))}).get(0);
            if (getEntity().getEmailCC().contains(emailsFrom)) {
                sendTo = new LinkedList<>();
                sendTo.addAll(MailHelper.parseMailAddress(getEntity().getEmailFrom()));
                sendTo.addAll(MailHelper.parseMailAddress(getEntity().getEmailTo()));
                sendCC = new LinkedList<>();
                sendCC = MailHelper.parseMailAddress(getEntity().getEmailCC()).stream()
                        .filter(m -> !m.contains(emailsFrom)).collect(Collectors.toList());
                sendBCC = MailHelper.parseMailAddress(getEntity().getEmailBCC());
            } else {
                sendTo = MailHelper.parseMailAddress(getEntity().getEmailFrom());
                sendCC = new LinkedList<>();
                sendCC.addAll(MailHelper.parseMailAddress(getEntity().getEmailTo()).stream()
                        .filter(m -> !m.contains(emailsFrom)).collect(Collectors.toList()));
                sendCC.addAll(MailHelper.parseMailAddress(getEntity().getEmailCC()));
                sendBCC = MailHelper.parseMailAddress(getEntity().getEmailBCC());
            }
            getEntity().setEmailSubject(createSpecialSubject(MAIL_REPLY_SUBJECT_RESOURCE_PREFIX, SUBJECT_DELIM, getEntity().getEmailSubject()));
        }else if (getMailType() == MailEditType.SEND_TO_MANAGER) {
            List<Request> selectedRequests = new ArrayList<Request>();
            if(!ValidationHelper.isNullOrEmpty(getSelectedIds())) {
                selectedRequests = DaoManager.load(Request.class, new Criterion[]{
                        Restrictions.in("id", getSelectedIds())
                });
            }
            List<Request> distictRequests =
                    selectedRequests.stream().filter(distinctByKey(Request::getRequestType)).collect(Collectors.toList());
            setSelectedRequests(selectedRequests);
            StringBuilder subject = new StringBuilder(ResourcesHelper.getString("permissionRequest"));
            String delim = " ";
            for(Request request: distictRequests) {
                if(!ValidationHelper.isNullOrEmpty(request.getRequestTypeName())) {
                    subject.append(delim);
                    subject.append(request.getRequestTypeName());
                    delim = " - ";
                }
            }

            getEntity().setEmailSubject(subject.toString());
            StringJoiner joiner = new StringJoiner("<br/>");
            Calendar cal = Calendar.getInstance();

            cal.set(Calendar.HOUR_OF_DAY, 13);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);

            boolean afterOne = Calendar.getInstance().after(cal);

            if (afterOne) {
                joiner.add(ResourcesHelper.getString("mailManagerEditGoodEvening"));
            } else {
                joiner.add(ResourcesHelper.getString("mailManagerEditGoodMorning"));
            }
            joiner.add(ResourcesHelper.getString("mailManagerManagerMailHeader"));

            if(!ValidationHelper.isNullOrEmpty(selectedRequests)) {
                StringJoiner requestJoiner = new StringJoiner("");
                selectedRequests.stream().forEach(x->
                        {
                            StringJoiner subjectJoiner = new StringJoiner("<br/>");
                            if(ValidationHelper.isNullOrEmpty(x.getSubjectList())) {
                                if(!ValidationHelper.isNullOrEmpty(x.getSubject())) {
                                    if(x.getSubject().getTypeIsPhysicalPerson()) {
                                        subjectJoiner.add("<li>" + x.getSubject().getFullNameCapitalize()+" CF. "+ x.getSubject().getFiscalCode() + "</li>");
                                    }else {
                                        subjectJoiner.add("<li>" + x.getSubject().getBusinessName().toUpperCase()+" P.IVA "+ x.getSubject().getNumberVAT() + "</li>");
                                    }
                                }
                            }else {

                                x.getSubjectList().stream().forEach(s ->
                                {
                                    if(s.getTypeIsPhysicalPerson()) {
                                        subjectJoiner.add("<li>" + s.getFullNameCapitalize()+" CF. "+ s.getFiscalCode()+ "</li>");
                                    }else {
                                        subjectJoiner.add("<li>" + s.getBusinessName().toUpperCase()+" P.IVA "+ s.getNumberVAT()+ "</li>");
                                    }
                                });

                            }
                            if(subjectJoiner.length() > 0) {
                                requestJoiner.add("<ul>" + subjectJoiner.toString()+"</ul>");
                            }
                            StringJoiner serviceJoiner = new StringJoiner(", ");
                            if(!ValidationHelper.isNullOrEmpty(x.getService())){
                                serviceJoiner.add(x.getServiceName());
                            }else {
                                emptyIfNull(x.getMultipleServices()).stream().forEach(s ->
                                        serviceJoiner.add(s.getName()));
                            }
                            if(serviceJoiner.toString().length() > 0)
                                requestJoiner.add(serviceJoiner.toString( ));
                        }
                );
                joiner.add(requestJoiner.toString());
            }

            joiner.add("<br/>" + ResourcesHelper.getString("mailManagerManagerMailRegards"));
            getEntity().setEmailPrefix(joiner.toString() + MAIL_FOOTER);
            getEntity().setEmailBody(null);
            getEntity().setEmailPostfix(null);

            WLGInbox inbox = new WLGInbox();

            inbox.setEmailFrom(getEntity().getEmailFrom());
            inbox.setEmailTo(getEntity().getEmailTo());
            inbox.setEmailCC(getEntity().getEmailCC());
            inbox.setEmailBCC(getEntity().getEmailBCC());
            inbox.setEmailSubject(getEntity().getEmailSubject());
            inbox.setEmailBody(getEntity().getEmailBody());
            inbox.setEmailPostfix(getEntity().getEmailPostfix());
            inbox.setEmailPrefix(getEntity().getEmailPrefix());
            inbox.setClient(getEntity().getClient());
            inbox.setClientInvoice(getEntity().getClientInvoice());
            inbox.setClientFiduciary(getEntity().getClientFiduciary());
            inbox.setManagers(getEntity().getManagers());
            inbox.setOffice(getEntity().getOffice());
            inbox.setNdg(getEntity().getNdg());
            inbox.setCdr(getEntity().getCdr());
            inbox.setReferenceRequest(getEntity().getReferenceRequest());
            if (getFromView()) {
                inbox.setRequests(getEntity().getRequests());
            }
            setEntity(inbox);

        }
        if ((getMailType() == MailEditType.FORWARD || getMailType() == MailEditType.REPLY || getMailType() == MailEditType.REQUEST_REPLY_ALL
                || getMailType() == MailEditType.REPLY_TO_ALL) && getEntity().getEmailBodyToEditor().contains("img")) {
            getEntity().getEmailBodyToEditor();
            MailEmlReader reader = new MailEmlReader(DaoManager.get(WLGInbox.class, getBaseMailId()));
            reader.readInbox();
            if (!ValidationHelper.isNullOrEmpty(reader.getImageNames())) {
                for (MailEmlReader.MailImageWrapper wrapper : reader.getImageNames()) {
                    WLGExport targetExport = null;
                    List<WLGExport> exportBase = DaoManager.load(WLGExport.class, new Criterion[]{
                            Restrictions.eq("inbox.id", getBaseMailId()),
                            Restrictions.like("destinationPath", wrapper.getImageName(), MatchMode.ANYWHERE)
                    });
                    if (exportBase != null) {
                        for (WLGExport export : exportBase) {
                            if (!ValidationHelper.isNullOrEmpty(export.getDestinationPath())
                                    && new File(export.getDestinationPath()).exists()) {
                                WLGExport copy = export.copy();
                                if (copy != null) {
                                    getCopiedImages().add(new FileWrapper(copy.getId(), copy.getFileName(), copy.getDestinationPath()));
                                    targetExport = copy;
                                    break;
                                }
                            }
                        }

                    }
                    if (targetExport == null) {
                        WLGExport newFile = new WLGExport();
                        DaoManager.save(newFile, true);

                        File filePath = new File(newFile.generateDestinationPath(wrapper.getImageName()));
                        try {
                            if (wrapper.getImageName() != null) {
                                FileHelper.writeFileToFolder(wrapper.getImageName(), filePath,
                                        FileHelper.loadContentByPath(reader.getInbox().getPath()
                                                .replaceAll("message.eml", wrapper.getImageName())));
                            }
                        } catch (IOException e) {
                            LogHelper.log(log, e);
                        }
                        DaoManager.save(newFile, true);

                        getCopiedImages().add(new FileWrapper(newFile.getId(), newFile.getFileName(), newFile.getDestinationPath()));
                        targetExport = newFile;
                    }
                    if (targetExport != null) {
                        getEntity().setEmailBodyToEditor(getEntity().getEmailBodyToEditor().replaceAll(
                                "id=\"" + wrapper.getTagId() + "\"",
                                "id=\"" + wrapper.getTagId() + "\" imageID=\"" + targetExport.getId() + "\""));
                    }
                }
                setBaseMailReader(reader);
            }
        }
    }

    private void fillWrappers() {
        setPriorityWrappers(new ArrayList<>());
        Arrays.stream(MailManagerPriority.values())
                .forEach(item -> getPriorityWrappers().add(new MailManagerPriorityWrapper(item)));
    }

    public void installPriority() {
        setSelectedPriorityWrapper(getEntity().getXpriority() == null
                ? Integer.valueOf(MailManagerPriority.NORMAL.getId())
                : getEntity().getXpriority());
    }


    public void fillDestination(String fromData) {
        sendTo = new ArrayList<>();
        if (!ValidationHelper.isNullOrEmpty(fromData)) {
            String[] values = fromData.split(DELIM);
            sendTo.addAll(Arrays.asList(values));
        }
    }

    public void fillAttachedFiles() throws PersistenceBeanException, IOException, InstantiationException, IllegalAccessException {
        setAttachedFiles(new ArrayList<>());

        if (getFromView()) {
            List<Document> documentList = new ArrayList<>();
            if(!ValidationHelper.isNullOrEmpty(getEntity().getRequests())) {
                for (Request request : getEntity().getRequests()) {
                    boolean addAttachment = true;
                    if(isEvadiRichesta() != null && isEvadiRichesta()){
                        if(!ValidationHelper.isNullOrEmpty(getFullfillRequestIds()) &&
                                !getFullfillRequestIds().contains(request.getId())){
                            addAttachment = false;
                        }
                    }
                    if (addAttachment && RequestState.TO_BE_SENT.getId().equals(request.getStateId()) && request.isDeletedRequest()) {
                        documentList.addAll(getDocumentsEvade(request));
                    }
                }
            }
            documentList = documentList.stream().distinct().sorted(new DocumentTypeRequestReportComparator()).collect(Collectors.toList());

            convertDocumentsToExport(documentList);
        }

        if (getEntity().getFiles() != null && MailEditType.FORWARD.equals(mailType)) {
            for (WLGExport file : getEntity().getFiles()) {
                WLGExport copy = file.copy();
                addAttachedFile(copy);
            }
        }

        if (getEntity().getFiles() != null && MailEditType.EDIT.equals(mailType)) {
            for (WLGExport file : getEntity().getFiles()) {
                addAttachedFile(file);
            }
        }

        if (!getFromView() && (MailEditType.REQUEST.equals(mailType) || MailEditType.REQUEST_REPLY_ALL.equals(mailType))) {
            try {
                Request requestRelated = DaoManager.get(Request.class, requestId);
                List<Request> requests = new ArrayList<>();
                if (!ValidationHelper.isNullOrEmpty(requestRelated.getMail())) {
                    requests = DaoManager.load(Request.class, new Criterion[]{
                            Restrictions.eq("mail", requestRelated.getMail())});
                    requests = requests.stream().filter(Request::isDeletedRequest).collect(Collectors.toList());
                }

                List<Document> documents = new ArrayList<>();

                for (Request request : requests) {
                    documents.addAll(getDocumentsEvade(request));
                }

                documents = documents.stream().distinct().sorted(new DocumentTypeRequestReportComparator()).collect(Collectors.toList());

                convertDocumentsToExport(documents);
            } catch (Exception e) {
                LogHelper.log(log, e);
            }
        }
    }

    private List<Document> getDocumentsEvade(Request request) throws PersistenceBeanException, IllegalAccessException, InstantiationException {
        List<Document> documents = new ArrayList<>();
        documents = DaoManager.load(Document.class, new Criterion[]{
                Restrictions.eq("request.id", request.getId()),
                Restrictions.eq("selectedForEmail", true)
        });

        List<Document> otherDocs = new ArrayList<>();

        if (!ValidationHelper.isNullOrEmpty(request.getMail())) {

            otherDocs = DaoManager.load(Document.class, new CriteriaAlias[]{
                    new CriteriaAlias("formality", "f", JoinType.INNER_JOIN),
                    new CriteriaAlias("f.requestList", "r_f", JoinType.INNER_JOIN)
            }, new Criterion[]{
                    Restrictions.eq("r_f.id", request.getId()),
                    Restrictions.eq("selectedForEmail", true)
            });

            if (!ValidationHelper.isNullOrEmpty(otherDocs)) {
                for (Document doc : otherDocs) {
                    if (!documents.contains(doc)) {
                        documents.add(doc);
                    }
                }
            }
        }
        return documents;
    }

    private void convertDocumentsToExport(List<Document> documents) throws IOException, PersistenceBeanException {
        for (Document doc : documents) {
            if (!new File(doc.getPath()).exists()) {
                LogHelper.log(log, "WARNING failed to read document file | no file on server: " + doc.getPath());
                continue;
            }
            WLGExport newFile = new WLGExport();

            String fileName = doc.getTitle();
            fileName += DocumentType.OTHER.getId().equals(doc.getTypeId()) ?
                    doc.getPath().substring(doc.getPath().lastIndexOf(".")) : ".pdf";

            byte[] content;
            switch (DocumentType.getById(doc.getTypeId())) {
                case CADASTRAL:
                case ESTATE_FORMALITY: {
                    content = PrintPDFHelper.generateAndGetPDFOnDocument(doc.getId());
                    if (content == null) continue;
                }
                break;
                default:
                    content = Files.readAllBytes(Paths.get(doc.getPath()));
            }
            newFile.setExportDate(new Date());
            DaoManager.save(newFile, true);
            File filePath = new File(newFile.generateDestinationPath(fileName));
            try {
                String str = FileHelper.writeFileToFolder(fileName, filePath, content);
                if (!new File(str).exists())
                    continue;
                LogHelper.log(log, newFile.getId() + " " + str);
            } catch (IOException e) {
                LogHelper.log(log, e);
            }
            DaoManager.save(newFile, true);
            addAttachedFile(newFile);
        }
    }

    public void downloadFile() {
        FileWrapper wrapper = getAttachedFiles().stream().filter(w -> w.getId().equals(getDownloadFileIndex()))
                .findAny().orElse(null);
        if (!ValidationHelper.isNullOrEmpty(wrapper)) {
            if (ValidationHelper.isNullOrEmpty(wrapper.getFilePath())) {
                log.warn("File download error: Document is null");
                return;
            }

            File file = new File(wrapper.getFilePath());
            try {
                FileHelper.sendFile(wrapper.getFileName(), new FileInputStream(file), (int) file.length());
            } catch (FileNotFoundException e) {
                MessageHelper.addGlobalMessage(FacesMessage.SEVERITY_ERROR,
                        ResourcesHelper.getValidation("noDocumentOnServer"), "");
            }
        }
    }

    private void appendReplyFooter() throws PersistenceBeanException, IllegalAccessException, InstantiationException {
        WLGInbox baseMail = DaoManager.get(WLGInbox.class, getBaseMailId());
        String sendDate;
        if (getEntity().getSendDate() == null) {
            sendDate = "";
        } else if (baseMail.getReceived()) {
            sendDate = DateTimeHelper.toFormatedStringLocal(getEntity().getSendDate(),
                    DateTimeHelper.getMySQLDateTimePattern(), null);
        } else {
            sendDate = DateTimeHelper.ToMySqlStringWithSeconds(getEntity().getSendDate());
        }
        getEntity().setEmailPrefix(appendRequestsInfo() + String.format(MAIL_RERLY_FOOTER,
                prepareEmailAddress(getEntity().getEmailFrom()),
                sendDate,
                prepareEmailAddress(getEntity().getEmailTo()),
                getEntity().getEmailCC(),
                getEntity().getEmailSubject()));
    }

    private String prepareEmailAddress(String email) {
        return email.replace("<", "&lt;").replace(">", "&gt;");
    }

    public String appendRequestsInfo() throws IllegalAccessException,
            PersistenceBeanException, InstantiationException {

        List<Long> selectedIds = ValidationHelper.isNullOrEmpty(getRequestParameter(RedirectHelper.SELECTED)) ? new ArrayList<>() :
                (Stream.of(getRequestParameter(RedirectHelper.SELECTED).split(",")).map(Long::valueOf).collect(Collectors.toList()));
        StringJoiner joiner = new StringJoiner(" ");
        Request request = DaoManager.get(Request.class, getRequestId());
        List<Request> requestList;
        if(!ValidationHelper.isNullOrEmpty(selectedIds)){
            setEvadiRichesta(Boolean.TRUE);
            setFullfillRequestIds(selectedIds);
        }

        if (getFromView() && !ValidationHelper.isNullOrEmpty(getEntity().getRequests())) {
            requestList = getEntity().getRequests().stream().filter(x -> !ValidationHelper.isNullOrEmpty(x.getStateId()) &&
                    RequestState.TO_BE_SENT.getId().equals(x.getStateId()) && selectedIds.contains(x.getId())).collect(Collectors.toList());
        } else {
            requestList = DaoManager.load(Request.class, new Criterion[]{
                    Restrictions.eq("mail.id", request.getMail() == null ? null : request.getMail().getId()),
                    Restrictions.eq("stateId", RequestState.TO_BE_SENT.getId())
            });
        }
        if (!ValidationHelper.isNullOrEmpty(requestList)) {
            requestList = requestList.stream().filter(Request::isDeletedRequest).collect(Collectors.toList());


            Calendar cal = Calendar.getInstance();

            cal.set(Calendar.HOUR_OF_DAY, 13);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);

            boolean afterOne = Calendar.getInstance().after(cal);

            if (afterOne) {
                joiner.add(ResourcesHelper.getString("mailManagerEditGoodEvening"));
            } else {
                joiner.add(ResourcesHelper.getString("mailManagerEditGoodMorning"));
            }

            if (requestList.size() == 1) {
                joiner.add(ResourcesHelper.getString("mailManagerEditAttachedOneRequest"));
            } else {
                joiner.add(ResourcesHelper.getString("mailManagerEditAttachedManyRequests"));
            }

            joiner.add("</br></br>" + (table
                    (tbody(each(requestList, i -> tr(
                            td(div().withStyle(" border-radius: 50%; width: 5px; height: 5px; background: #000;"))
                                    .withStyle("border: none; padding-left: 50px; padding-right: 5px;"),
                            td().withStyle("border: none; width:15px;"),
                            td(i.getSubject().getFullNameCapitalize()).withStyle("border: none;"),
                            td().withStyle("border: none; width:15px;"),
                            td(i.getFiscalCodeVATNamber()).withStyle("border: none;"),
                            td().withStyle("border: none; width:15px;"),
                            td(i.getServiceName()).withStyle("border: none;"),
                            td((ValidationHelper.isNullOrEmpty(i.getService()) ? ( !ValidationHelper.isNullOrEmpty(i.getMultipleServices()) ? i.getMultipleServices().stream().map( m -> m.getName()).collect(Collectors.joining( "-" )) : ""): "")).withStyle("border: none;"),
                            td().withStyle("border: none; width:15px;"),
                            td(
                                    (!ValidationHelper.isNullOrEmpty(i.getService()) ? i.getService().getEmailTextCamelCase() : "")
                                            + " " + ((i.getAggregationLandChargesRegistryName() != "") ?
                                            i.getAggregationLandChargesRegistryName() : i.getCityDescription())).withStyle("border: none;")
                            )))
                    ).withStyle("border: none;")
            ).toString());

            joiner.add(ResourcesHelper.getString("mailManagerEditSupportDocumentation"));
            joiner.add(ResourcesHelper.getString("mailManagerEditCordiality"));
            joiner.add(WordUtils.capitalizeFully(defaultSendFromName));
        }
        return joiner.toString();
    }

    private int getPadding(List<Request> requestList) {
        int result = 0;
        for (Request request : requestList) {
            if (request.getSubject().getFullNameCapitalize().length() > result) {
                result = request.getSubject().getFullNameCapitalize().length();
            }
        }
        return result;
    }

    public void appendMailFooter() throws IllegalAccessException, PersistenceBeanException, InstantiationException {
        if (getEntity() != null) {
            StringBuilder builder = new StringBuilder();
            builder.append(appendRequestsInfo()).append(MAIL_FOOTER);
            getEntity().setEmailPostfix(builder.toString());
        }
    }

    public List<String> completeDestinations(String query) {
        return completeField(query, "email_to");
    }

    private String convertToCorrectDestination(String destination) {
        Pattern pattern = Pattern.compile("(?<=<).*(?=>)");
        Matcher matcher = pattern.matcher(destination);
        return matcher.find() ? matcher.group() : destination;
    }

    private List<String> completeField(String query, String field) {
        try {
            List<String> filterList = new ArrayList<>();
            Session session = DaoManager.getSession();
            ((List<String>) session.createSQLQuery("SELECT DISTINCT " + field + " FROM wlg_inbox wlg " +
                    "WHERE " + field + " LIKE '%" + query + "%' AND " + "(" + field + " LIKE '%,%' " +
                    "OR NOT EXISTS(SELECT 1 FROM email_remove WHERE wlg." + field + " LIKE CONCAT('%', email, '%')))")
                    .list()).stream()
                    .map(MailHelper::parseMailAddress)
                    .flatMap(List::stream)
                    .filter(item -> item.toLowerCase().contains(query.toLowerCase()))
                    .filter(item -> !filterList.contains(item))
                    .filter(MailHelper::checkRemoveMailAddress)
                    .forEach(filterList::add);
            return filterList;
        } catch (PersistenceBeanException | IllegalAccessException e) {
            LogHelper.log(log, e.getMessage());
        }
        return Collections.emptyList();
    }

    public List<String> completeMailCC(String query) {
        return completeField(query, "email_cc");
    }

    public List<String> completeMailBCC(String query) {
        return completeField(query, "email_bcc");
    }

    public void deleteEmailTo() throws PersistenceBeanException {
        String email = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("param");
        if (!ValidationHelper.isNullOrEmpty(email)) {
            deleteEmail(MailHelper.prepareEmailToSend(email));
            getSendTo().remove(getSendTo().size() - 1);
        }
    }

    public void deleteEmailCC() throws PersistenceBeanException {
        String email = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("param");
        if (!ValidationHelper.isNullOrEmpty(email)) {
            deleteEmail(MailHelper.prepareEmailToSend(email));
            getSendCC().remove(getSendCC().size() - 1);
        }
    }

    public void deleteEmailBCC() throws PersistenceBeanException {
        String email = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("param");
        if (!ValidationHelper.isNullOrEmpty(email)) {
            deleteEmail(MailHelper.prepareEmailToSend(email));
            getSendBCC().remove(getSendBCC().size() - 1);
        }
    }

    public void deleteEmail(String email) throws PersistenceBeanException {
        EmailRemove remove = new EmailRemove();
        remove.setEmail(email);
        DaoManager.save(remove, true);
    }

    public void loadClients() throws PersistenceBeanException, IllegalAccessException {
        setClients(ComboboxHelper.fillList(Client.class, Order.asc("nameOfTheCompany"),
                new Criterion[]{Restrictions.or(Restrictions.eq("deleted", Boolean.FALSE),
                        Restrictions.isNull("deleted"))}, false));
        List<Client> clients = DaoManager.load(Client.class, new Criterion[]{Restrictions.or(Restrictions.eq("deleted", Boolean.FALSE),
                Restrictions.isNull("deleted"))}, Order.asc("nameOfTheCompany"));
        setClientEmails(clients.stream().map(ClientWrapper::new).collect(Collectors.toList()));
    }

    public void selectClientHandler() {
        if (!ValidationHelper.isNullOrEmpty(getSelectedClients())) {
            getClientEmails()
                    .forEach(wrapper -> wrapper.setHide(!getSelectedClients().contains(wrapper.getClient().getStrId())));
        }
    }

    public void handleFileUpload(FileUploadEvent event) throws PersistenceBeanException {

        WLGExport newFile = new WLGExport();
        newFile.setExportDate(new Date());
        DaoManager.save(newFile, true);

        File filePath = new File(newFile.generateDestinationPath(event.getFile().getFileName()));

        try {
            String str = FileHelper.writeFileToFolder(event.getFile().getFileName(),
                    filePath, event.getFile().getContents());
            if (!new File(str).exists()) {
                return;
            }
            LogHelper.log(log, newFile.getId() + " " + str);
        } catch (IOException e) {
            LogHelper.log(log, e);
        }
        DaoManager.save(newFile, true);

        addAttachedFile(newFile);
    }

    private void addAttachedFile(WLGExport export) {
        if (export == null) {
            return;
        }
        if (getAttachedFiles() == null) {
            setAttachedFiles(new ArrayList<>());
        }
        if (new File(export.getDestinationPath()).exists()) {
            getAttachedFiles().add(new FileWrapper(export.getId(), export.getFileName(), export.getDestinationPath()));
        } else {
            LogHelper.log(log, "WARNING failed to attach file | no file on server: " + export.getDestinationPath());
        }

        attachedFiles = getAttachedFiles().stream().distinct().collect(Collectors.toList());
    }

    public void deleteFile() throws PersistenceBeanException, IllegalAccessException, InstantiationException {
        if (!ValidationHelper.isNullOrEmpty(getDeleteFileId())
                && !ValidationHelper.isNullOrEmpty(getAttachedFiles())) {
            WLGExport export = DaoManager.get(WLGExport.class, getDeleteFileId());
            if (FileHelper.delete(export.getDestinationPath())) {
                DaoManager.remove(export, true);
                getAttachedFiles().removeAll(getAttachedFiles().stream()
                        .filter(f -> f.getId().equals(getDeleteFileId())).collect(Collectors.toList()));
            }
        }
    }

    public void addSelectedAddresses() {
        Set<String> selectedEmails = MailManagerHelper.loadAllSelectedEmails(getClientEmails());
        if (getSendTo() == null) {
            setSendTo(new LinkedList<>(selectedEmails));
        } else {
            getSendTo().addAll(selectedEmails.stream()
                    .filter(s -> !getSendTo().contains(s))
                    .collect(Collectors.toList()));
        }

        getEntity().setEmailTo(MailManagerHelper
                .generateNewEmailToStr(getEntity().getEmailTo(), selectedEmails));

        MailManagerHelper.saveClientMailChanges(getClientEmails());
    }

    public List<ClientWrapper> getAvailableClientEmails() {
        if (!ValidationHelper.isNullOrEmpty(getClientEmails())) {
            List<ClientWrapper> list = new ArrayList<>();
            for (ClientWrapper ce : clientEmails) {
                if (ce.getHide() == null || !ce.getHide()) {
                    list.add(ce);
                }
            }
            return list;
        } else {
            return new LinkedList<>();
        }
    }

    public void openAddNewEmail() {
        ClientEmail mail = new ClientEmail();
        mail.setTypeId(EmailType.PERSONAL.getId());
        setNewClientEmail(new ClientEmailWrapper(mail));
    }

    public void saveNewMail() {
        if (getEditClientId() != null) {
            for (ClientWrapper client : getClientEmails()) {
                if (client.getClient().getId().equals(getEditClientId())) {
                    client.getPersonalEmails().add(getNewClientEmail());
                }
            }
        }
    }

    public void openAddNewReferent() {
        setNewReferentEmail(new ReferentEmailWrapper(new Referent()));
    }

    public void saveNewReferent() {
        if (getEditClientId() != null) {
            for (ClientWrapper client : getClientEmails()) {
                if (client.getClient().getId().equals(getEditClientId())) {
                    client.getReferents().add(getNewReferentEmail());
                }
            }
        }
    }

    public void updateDestination() {
        if (!ValidationHelper.isNullOrEmpty(getSendTo())) {
            getEntity().setEmailTo(getSendTo().stream()
                    .map(MailHelper::prepareEmailToSend).collect(Collectors.joining(DELIM)));
        } else {
            getEntity().setEmailTo(null);
        }
    }

    public void updateCC() {
        if (!ValidationHelper.isNullOrEmpty(getSendCC())) {
            getEntity().setEmailCC(getSendCC().stream()
                    .map(MailHelper::prepareEmailToSend).collect(Collectors.joining(DELIM)));
        } else {
            getEntity().setEmailCC(null);
        }
    }

    public void updateBCC() {
        if (!ValidationHelper.isNullOrEmpty(getSendBCC())) {
            getEntity().setEmailBCC(getSendBCC().stream()
                    .map(MailHelper::prepareEmailToSend).collect(Collectors.joining(DELIM)));
        } else {
            getEntity().setEmailBCC(null);
        }
    }

    public void generateMailPdf() {
        updateDestination();
        updateCC();
        updateBCC();
        setMailPdf(PrintPDFHelper.generatePDFOnEmail(getEntity(), "pdf"));
    }

    public String getNewMailPdf() {
        if (isPrintPdf()) {
            setPrintPdf(false);
            return getMailPdf();
        } else {
            return null;
        }
    }

    public boolean isValidate() {
        if (ValidationHelper.isNullOrEmpty(MailHelper.parseMailAddress(getEntity().getEmailTo()))) {
            addRequiredFieldException("form:mailTo");
        } else {
            List<String> emails = Arrays.asList(getEntity().getEmailTo().split(","));

            Set<String> emailsSet = new HashSet<>();

            for (String email : emails) {
                Matcher m = Pattern.compile("[a-zA-Z0-9_.+-]+@[a-zA-Z0-9-]+\\.[a-zA-Z0-9-.]+").matcher(email);

                while (m.find()) {
                    emailsSet.add(m.group().toLowerCase());
                }
            }

            emailsSet.stream().filter(email -> !ValidationHelper.checkMailCorrectFormat(email.trim()))
                    .forEach(email -> addFieldException("form:mailTo", "emailWrongFormat"));
        }
        return !getValidationFailed();
    }

    public void doSaveWithoutValidation() throws PersistenceBeanException, IllegalAccessException, InstantiationException {
        doSave(false);
    }

    public void doSave() {
        doSave(true);
    }

    private void doSave(boolean needValidate) {
        updateDestination();
        updateCC();
        updateBCC();
        if (!needValidate || isValidate()) {
            if (getMailType().equals(MailEditType.SEND_TO_MANAGER)) {
                try {
                    DaoManager.save(excelFornitori, true);
                } catch (HibernateException | PersistenceBeanException e1) {
                    LogHelper.log(log, e1);
                }
            }

            List<String> emailsFrom = null;
            try {
                emailsFrom = DaoManager.loadField(WLGServer.class, "login", String.class, new Criterion[]{
                        Restrictions.eq("id", Long.parseLong(ApplicationSettingsHolder.getInstance()
                                .getByKey(ApplicationSettingsKeys.SENT_SERVER_ID).getValue()))
                });
            } catch (PersistenceBeanException | IllegalAccessException e) {
                LogHelper.log(log, e);
            }

            if (!ValidationHelper.isNullOrEmpty(emailsFrom)) {
                String preparedStr = String.format("\"%s\" <%s>", defaultSendFromName, emailsFrom.get(0));
                getEntity().setEmailFrom(preparedStr);
            } else {
                setSenderServerNotSpecified(true);
                return;
            }

            getEntity().setEmailBody(MailHelper.htmlToText(getEntity().getEmailBodyToEditor()));
            try {
                String preparedBody = String.format(ResourcesHelper.getString("emailStructure"),
                        getEntity().getEmailBodyToEditor());
                if (getBaseMailReader() != null) {
                    preparedBody = getBaseMailReader().prepareBodyToSend(preparedBody);
                }
                getEntity().setEmailBodyHtml(preparedBody);
            } catch (PersistenceBeanException | IllegalAccessException | InstantiationException e) {
                LogHelper.log(log, e);
            }
            getEntity().setEmailSubject(WordUtils.capitalizeFully(getEntity().getEmailSubject()));

            if (ValidationHelper.isNullOrEmpty(getEntity().getServerId())) {
                getEntity().setServerId(0L);
            }

            if (getEntity().isNew()) {
                getEntity().setState(MailManagerStatuses.NEW.getId());
                getEntity().setSendDate(new Date());
                getEntity().setReceiveDate(new Date());
            }
            getEntity().setProcessDate(new Date());
            try {
                DaoManager.save(getEntity(), !needValidate);

                if (getMailType() != MailEditType.FORWARD && getMailType() != MailEditType.REPLY
                        && getMailType() != MailEditType.REPLY_TO_ALL
                        && !ValidationHelper.isNullOrEmpty(getEntity().getFiles())) {
                    if(isEvadiRichesta() != null && isEvadiRichesta()){
                        getEntity().setFiles(null);
                    }else {
                        for (WLGExport file : getEntity().getFiles()) {
                            DaoManager.remove(file);
                        }
                    }
                }
            } catch (PersistenceBeanException e) {
                LogHelper.log(log, e);
            }

            if (isSenderServerNotSpecified()) {
                MessageHelper.addGlobalMessage(FacesMessage.SEVERITY_ERROR,
                        ResourcesHelper.getValidation("senderServerNotSpecified"), null);
            }
        }
    }

    public void saveDraft() throws IllegalAccessException, PersistenceBeanException, InstantiationException {
        updateDestination();
        updateCC();
        updateBCC();
        getEntity().setServerId(null);
        getEntity().setReceiveDate(new Date());
        getEntity().setSendDate(new Date());
        getEntity().setState(MailManagerStatuses.NEW.getId());
        getEntity().setEmailBody(MailHelper.htmlToText(getEntity().getEmailBodyToEditor()));
        getEntity().setEmailBodyHtml(getEntity().getEmailBodyToEditor());
        getEntity().setXpriority(getSelectedPriorityWrapper());
        DaoManager.save(getEntity(), true);

        saveFiles(true);
    }

    private void saveFiles(boolean transaction) throws PersistenceBeanException, IllegalAccessException, InstantiationException {
        if (!ValidationHelper.isNullOrEmpty(getAttachedFiles())) {
            for (FileWrapper wrapper : getAttachedFiles()) {
                WLGExport export = DaoManager.get(WLGExport.class, new Criterion[]{
                        Restrictions.eq("id", wrapper.getId())
                });
                export.setExportDate(new Date());
                export.setSourcePath(String.format("\\%s", getEntity().getId()));
                export.setInbox(getEntity());
                DaoManager.save(export, transaction);
            }
        }
        if (!ValidationHelper.isNullOrEmpty(getCopiedImages())) {
            for (FileWrapper wrapper : getCopiedImages()) {
                WLGExport export = DaoManager.get(WLGExport.class, new Criterion[]{
                        Restrictions.eq("id", wrapper.getId())
                });
                export.setExportDate(new Date());
                export.setSourcePath(String.format("\\%s", getEntity().getId()));
                export.setInbox(getEntity());
                DaoManager.save(export, transaction);
            }
        }
    }

    public void sendMail() throws PersistenceBeanException, IllegalAccessException {

        cleanValidation();
        doSave();

        if (getValidationFailed()) {
            return;
        }

        getEntity().setXpriority(getSelectedPriorityWrapper());

        if (!ValidationHelper.isNullOrEmpty(getEntity().getEmailFrom())) {
            List<String> emailsFrom = null;
            try {
                emailsFrom = DaoManager.loadField(WLGServer.class, "login", String.class, new Criterion[]{
                        Restrictions.eq("id", Long.parseLong(ApplicationSettingsHolder.getInstance()
                                .getByKey(ApplicationSettingsKeys.SENT_SERVER_ID).getValue()))
                });
            } catch (PersistenceBeanException | IllegalAccessException e) {
                LogHelper.log(log, e);
            }
            if (emailsFrom != null) {
                getEntity().setEmailFrom(emailsFrom.get(0));
            }
        }

        try {
            MailHelper.sendMail(getEntity(), getAttachedFiles(), getCopiedImages());
            log.info("Mail is sent");
            if(!ValidationHelper.isNullOrEmpty(getBaseMailId()))
                getEntity().setRecievedInbox(DaoManager.get(WLGInbox.class, getBaseMailId()));
            if(!ValidationHelper.isNullOrEmpty(getRequestId())
                    && (ValidationHelper.isNullOrEmpty(isEvadiRichesta()) || !isEvadiRichesta())){
                Request request = DaoManager.get(Request.class, getRequestId());
                request.setStateId(RequestState.TO_BE_SENT.getId());
                DaoManager.save(request, true);
            }

        } catch (Exception e) {
            log.info("Mail is not sent");
            LogHelper.log(log, e);
            executeJS("showNotSendMsg();");
            return;
        } finally {
            executeJS("setIsSend(true);");
        }
        List<Request> requestList = null;

        if (!ValidationHelper.isNullOrEmpty(getRequestId())) {
            try {
                Request request = DaoManager.get(Request.class, getRequestId());
                requestList = DaoManager.load(Request.class, new Criterion[]{
                        Restrictions.eq("mail.id", request.getMail() == null ? null : request.getMail().getId()),
                        Restrictions.eq("stateId", RequestState.TO_BE_SENT.getId())
                });

                log.info("State should be changed for request ids " + requestList.stream().map(IndexedEntity::getId)
                        .collect(Collectors.toList()));
                for (Request req : requestList) {
                    boolean updateState = true;

                    if(isEvadiRichesta() != null && isEvadiRichesta()){
                        if(!ValidationHelper.isNullOrEmpty(getFullfillRequestIds()) &&
                                !getFullfillRequestIds().contains(req.getId())){
                            updateState = false;
                        }
                    }
                    if(updateState){
                        req.setStateId(RequestState.EVADED.getId());
                        request.setUser(DaoManager.get(User.class, UserHolder.getInstance().getCurrentUser().getId()));
                        DaoManager.save(req, true);
                    }
                }
            } catch (PersistenceBeanException | IllegalAccessException | InstantiationException e) {
                LogHelper.log(log, e);
            }
        }
        getEntity().setServerId(Long.parseLong(ApplicationSettingsHolder.getInstance()
                .getByKey(ApplicationSettingsKeys.SENT_SERVER_ID).getValue()));
        try {

            saveFiles(true);
            WLGInbox inbox = DaoManager.get(WLGInbox.class, getBaseMailId());
            DaoManager.save(getEntity(), true);
            if(!ValidationHelper.isNullOrEmpty(getEntity().getManagers())) {
                inbox.setManagers(new ArrayList<>(getEntity().getManagers()));
            }
            if (getMailType() == MailEditType.REPLY || getMailType() == MailEditType.REPLY_TO_ALL || getMailType() == MailEditType.FORWARD) {
                inbox.setForwarded(getMailType() == MailEditType.FORWARD);
                inbox.setResponse(getMailType() == MailEditType.REPLY || getMailType() == MailEditType.REPLY_TO_ALL);
            }
            DaoManager.save(inbox, true);
        } catch (PersistenceBeanException | IllegalAccessException | InstantiationException e) {
            LogHelper.log(log, e);
        }
        if ((MailEditType.REPLY == getMailType() || MailEditType.REPLY_TO_ALL == getMailType()
                || MailEditType.REQUEST == getMailType() || MailEditType.REQUEST_REPLY_ALL == getMailType())
                && ValidationHelper.isNullOrEmpty(getEntity().getFolder())) {

            // generatePdfRequestCost();
            generateXlsRequestCost(requestList);

            List<WLGFolder> folders = null;
            try {
                WLGInbox inbox = DaoManager.get(WLGInbox.class, getBaseMailId());

                folders = DaoManager.load(WLGFolder.class, new CriteriaAlias[]{
                        new CriteriaAlias("emails", "email", JoinType.INNER_JOIN)
                }, new Criterion[]{
                        Restrictions.eq("email.emailFrom", inbox.getEmailFrom())
                });
            } catch (PersistenceBeanException | IllegalAccessException | InstantiationException e) {
                LogHelper.log(log, e);
            }
            if (!ValidationHelper.isNullOrEmpty(folders)) {
                setUserFolders(ComboboxHelper.fillList(folders, false, false));
                setSelectedFolderId(folders.get(0).getId());
                executeJS("PF('selectFolderWV').show();");
                return;
            }
        }

        if(!ValidationHelper.isNullOrEmpty(getFullfillRequestIds())){
            List<Request> fullfillRequests = DaoManager.load(Request.class, new Criterion[]{
                    Restrictions.in("id", getFullfillRequestIds())
            });
            try {
                User currentUser = DaoManager.get(User.class, getCurrentUser().getId());
                fullfillRequests.stream().forEach(r -> {
                    r.setUser(currentUser);
                    try {
                        DaoManager.save(r, true);
                    } catch (Exception e) {
                        e.printStackTrace();
                        LogHelper.log(log, e);
                    }
                });;
            } catch (Exception e) {
                e.printStackTrace();
                LogHelper.log(log, e);
            }
        }
        RedirectHelper.goTo(PageTypes.MAIL_MANAGER_LIST);
    }

    private void generateXlsRequestCost(List<Request> requestList) {


        try {
            List<Request> requestsAttached = getRelatedRequests();

            if (!ValidationHelper.isNullOrEmpty(requestsAttached)) {
                CreateExcelRequestsReportHelper helper = new CreateExcelRequestsReportHelper(true);
                String path = FileHelper.writeFileToFolder("costs-" + getRequestId() + ".xls",
                        new File(FileHelper.getApplicationProperties().getProperty("requestReportSavePath")
                                + File.separator + getRequestId()),
                        helper.convertMailDataToExcel(requestsAttached, null));

                Document document = DaoManager.get(Document.class, new Criterion[]{
                        Restrictions.eq("mail", getEntity())});

                if (ValidationHelper.isNullOrEmpty(document)) {
                    document = new Document();
                }

                Request request = DaoManager.get(Request.class, getRequestId());
                DaoManager.save(request, true);
                document.setRequest(request);
                document.setTitle("costs-" + getRequestId());
                document.setTypeId(DocumentType.INVOICE_REPORT.getId());

                if(document.getInvoiceRequests() != null && !ValidationHelper.isNullOrEmpty(requestList))
                    document.getInvoiceRequests().addAll(requestList);
                document.setDate(new Date());
                document.setPath(path);
                document.setMail(getEntity());
                document.setCost(SaveRequestDocumentsHelper.generateCostByRequests(requestsAttached));
                document.setInvoiceNumber(SaveRequestDocumentsHelper.getLastInvoiceNumber() + 1);

                DaoManager.save(document, true);
            }
        } catch (Exception e) {
            LogHelper.log(log, e);
        }
    }

    public void generatePdfRequestCost() {

        try {
            List<Request> requestsAttached = getRelatedRequests();

            if (!ValidationHelper.isNullOrEmpty(requestsAttached)) {
                CreatePDFReportHelper createPDFReportHelper = new CreatePDFReportHelper();
                String bodyTable = createPDFReportHelper.getPdfRequestBodyTable(requestsAttached);

                String path = FileHelper.writeFileToFolder("costs-" + getRequestId() + ".pdf",
                        new File(FileHelper.getApplicationProperties().getProperty("requestReportSavePath")
                                + File.separator + getRequestId()),
                        PrintPDFHelper.convertToPDF(null, bodyTable, null, DocumentType.INVOICE_REPORT));


                Document document = new Document();
                document.setRequest(DaoManager.get(Request.class, getRequestId()));
                document.setTitle("costs-" + getRequestId());
                document.setTypeId(DocumentType.INVOICE_REPORT.getId());
                document.getInvoiceRequests().addAll(getEntity().getRequests().stream().filter(x -> RequestState.TO_BE_SENT.getId()
                        .equals(x.getStateId())).collect(Collectors.toList()));
                document.setDate(new Date());
                document.setPath(path);
                document.setMail(getEntity());
                document.setCost(SaveRequestDocumentsHelper.generateCostByRequests(requestsAttached));
                document.setInvoiceNumber(SaveRequestDocumentsHelper.getLastInvoiceNumber() + 1);

                DaoManager.save(document, true);
            }
        } catch (Exception e) {
            LogHelper.log(log, e);
        }

    }

    private List<Request> getRelatedRequests() throws PersistenceBeanException, IllegalAccessException, InstantiationException {

        List<Request> requestsAttached = new ArrayList<>();
        requestsAttached = DaoManager.load(Request.class, new CriteriaAlias[]{
                        new CriteriaAlias("documentsRequest", "docs", JoinType.INNER_JOIN),
                        new CriteriaAlias("docs.mail", "m", JoinType.INNER_JOIN)},
                new Criterion[]{Restrictions.eq("m.id", getEntity().getId())});

        if (!ValidationHelper.isNullOrEmpty(getEntity().getRequests())) {
            for (Request request : getEntity().getRequests()) {
                if (!requestsAttached.contains(request)) {
                    requestsAttached.add(request);
                }
            }
        }

        Request request = DaoManager.get(Request.class, getRequestId());
        if (!requestsAttached.contains(request)) {
            requestsAttached.add(request);
        }

        if (!ValidationHelper.isNullOrEmpty(request.getMail())) {
            List<Request> requestsWithSameMail = DaoManager.load(Request.class,
                    new Criterion[]{Restrictions.eq("mail.id", request.getMail().getId())});

            if (!ValidationHelper.isNullOrEmpty(requestsWithSameMail)) {
                for (Request req : requestsWithSameMail) {
                    if (!requestsAttached.contains(req)) {
                        requestsAttached.add(req);
                    }
                }
            }
        }
        return requestsAttached;
    }


    public void confirmSelectFolder(Boolean needFolder) throws PersistenceBeanException, IllegalAccessException, InstantiationException {
        if (needFolder) {
            if (!ValidationHelper.isNullOrEmpty(getSelectedFolderId())) {
                WLGInbox inbox = DaoManager.get(WLGInbox.class, getBaseMailId());
                inbox.setFolder(DaoManager.get(WLGFolder.class, getSelectedFolderId()));
                inbox.setUserChangedFolder(DaoManager.get(User.class, getCurrentUser().getId()));
                DaoManager.save(inbox, true);
            }
        }
        RedirectHelper.goTo(PageTypes.MAIL_MANAGER_LIST);
    }

    public void confirmRedirect(Boolean save) {
        try {
            if (save) {
                saveDraft();
            }
            List<WLGExport> exports = DaoManager.load(WLGExport.class, new Criterion[]{
                    Restrictions.isNull("sourcePath")
            });
            if (!ValidationHelper.isNullOrEmpty(exports)) {
                for (WLGExport export : exports) {
                    FileHelper.delete(export.getDestinationPath());
                    DaoManager.remove(export, true);
                }
            }
            if (!getUrl().equals("#")) {
                RedirectHelper.sendRedirect(getUrl());
            }
        } catch (Exception e) {
            LogHelper.log(log, e);
        }
    }

    public void goMain() {
        if (ValidationHelper.isNullOrEmpty(getChangeVar())) {
            RedirectHelper.goTo(PageTypes.MAIL_MANAGER_LIST);
        } else {
            setUrl(PageTypes.MAIL_MANAGER_LIST.getPagesContext());
            executeJS("PF('confirmRedirect').show();");
        }
    }

    public void goList() {
        RedirectHelper.goTo(PageTypes.MAIL_MANAGER_LIST);
    }

    private void attachFornitoriExcel() throws HibernateException, IllegalAccessException, PersistenceBeanException{
        excelFornitori = new WLGExport();
        Date currentDate = new Date();
        excelFornitori.setExportDate(currentDate);
        DaoManager.save(excelFornitori, true);
        String fileName = "Richieste_esterne_"+DateTimeHelper.toFileDateWithMinutes(currentDate)+".xls";
        String sb =  excelFornitori.generateDestinationPath(fileName);//MailHelper.getDestinationPath()+File.separator+getEntityId();
        File filePath = new File(sb);
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            new CreateExternalRequestReportHelper().createReport(currentDate, getSelectedRequests(), baos);

            String str = FileHelper.writeFileToFolder(fileName,
                    filePath, baos.toByteArray());
            if (!new File(str).exists()) {
                return;
            }
            LogHelper.log(log, excelFornitori.getId() + " " + str);
        } catch (Exception e) {
            LogHelper.log(log, e);
        }
        DaoManager.save(excelFornitori, true);
        addAttachedFile(excelFornitori);
    }

    public String getMailPdf() {
        return mailPdf;
    }

    public void setMailPdf(String mailPdf) {
        this.mailPdf = mailPdf;
    }

    public boolean isPrintPdf() {
        return printPdf;
    }

    public void setPrintPdf(boolean printPdf) {
        this.printPdf = printPdf;
    }

    public List<SelectItem> getClients() {
        return clients;
    }

    public void setClients(List<SelectItem> clients) {
        this.clients = clients;
    }

    public List<String> getSelectedClients() {
        return selectedClients;
    }

    public void setSelectedClients(List<String> selectedClients) {
        this.selectedClients = selectedClients;
    }

    public List<MailManagerPriorityWrapper> getPriorityWrappers() {
        return priorityWrappers;
    }

    public void setPriorityWrappers(List<MailManagerPriorityWrapper> priorityWrappers) {
        this.priorityWrappers = priorityWrappers;
    }

    public Integer getSelectedPriorityWrapper() {
        return selectedPriorityWrapper;
    }

    public void setSelectedPriorityWrapper(Integer selectedPriorityWrapper) {
        this.selectedPriorityWrapper = selectedPriorityWrapper;
    }

    public String getDownloadFilePath() {
        return downloadFilePath;
    }

    public void setDownloadFilePath(String downloadFilePath) {
        this.downloadFilePath = downloadFilePath;
    }

    public String getForwardAddress() {
        return forwardAddress;
    }

    public void setForwardAddress(String forwardAddress) {
        this.forwardAddress = forwardAddress;
    }

    public ClientEmailWrapper getNewClientEmail() {
        return newClientEmail;
    }

    public void setNewClientEmail(ClientEmailWrapper newClientEmail) {
        this.newClientEmail = newClientEmail;
    }

    public ReferentEmailWrapper getNewReferentEmail() {
        return newReferentEmail;
    }

    public void setNewReferentEmail(ReferentEmailWrapper newReferentEmail) {
        this.newReferentEmail = newReferentEmail;
    }

    public Long getEditClientId() {
        return editClientId;
    }

    public void setEditClientId(Long editClientId) {
        this.editClientId = editClientId;
    }

    public List<ClientWrapper> getClientEmails() {
        return clientEmails;
    }

    public void setClientEmails(List<ClientWrapper> clientEmails) {
        this.clientEmails = clientEmails;
    }

    public List<String> getSendTo() {
        return sendTo;
    }

    public void setSendTo(List<String> sendTo) {
        this.sendTo = sendTo;
    }

    public MailEditType getMailType() {
        return mailType;
    }

    public void setMailType(MailEditType mailType) {
        this.mailType = mailType;
    }

    public boolean isSenderServerNotSpecified() {
        return senderServerNotSpecified;
    }

    public void setSenderServerNotSpecified(boolean senderServerNotSpecified) {
        this.senderServerNotSpecified = senderServerNotSpecified;
    }

    public Long getDeleteFileId() {
        return deleteFileId;
    }

    public void setDeleteFileId(Long deleteFileId) {
        this.deleteFileId = deleteFileId;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getChangeVar() {
        return changeVar;
    }

    public void setChangeVar(String changeVar) {
        this.changeVar = changeVar;
    }

    public Long getBaseMailId() {
        return baseMailId;
    }

    public void setBaseMailId(Long baseMailId) {
        this.baseMailId = baseMailId;
    }

    public List<String> getSendCC() {
        return sendCC;
    }

    public void setSendCC(List<String> sendCC) {
        this.sendCC = sendCC;
    }

    public List<String> getSendBCC() {
        return sendBCC;
    }

    public void setSendBCC(List<String> sendBCC) {
        this.sendBCC = sendBCC;
    }

    public String getEmailToDelete() {
        return emailToDelete;
    }

    public void setEmailToDelete(String emailToDelete) {
        this.emailToDelete = emailToDelete;
    }

    public List<SelectItem> getUserFolders() {
        return userFolders;
    }

    public void setUserFolders(List<SelectItem> userFolders) {
        this.userFolders = userFolders;
    }

    public Long getSelectedFolderId() {
        return selectedFolderId;
    }

    public void setSelectedFolderId(Long selectedFolderId) {
        this.selectedFolderId = selectedFolderId;
    }

    public List<FileWrapper> getAttachedFiles() {
        return attachedFiles;
    }

    public void setAttachedFiles(List<FileWrapper> attachedFiles) {
        this.attachedFiles = attachedFiles;
    }

    public Long getDownloadFileIndex() {
        return downloadFileIndex;
    }

    public void setDownloadFileIndex(Long downloadFileIndex) {
        this.downloadFileIndex = downloadFileIndex;
    }

    public List<FileWrapper> getCopiedImages() {
        return copiedImages;
    }

    public void setCopiedImages(List<FileWrapper> copiedImages) {
        this.copiedImages = copiedImages;
    }

    public MailEmlReader getBaseMailReader() {
        return baseMailReader;
    }

    public void setBaseMailReader(MailEmlReader baseMailReader) {
        this.baseMailReader = baseMailReader;
    }

    public Long getRequestId() {
        return requestId;
    }

    public void setRequestId(Long requestId) {
        this.requestId = requestId;
    }

    public Boolean getFromView() {
        return fromView != null ? Boolean.TRUE : Boolean.FALSE;
    }

    public void setFromView(Boolean fromView) {
        this.fromView = fromView;
    }

    public List<Long> getSelectedIds() {
        return selectedIds;
    }

    public void setSelectedIds(List<Long> selectedIds) {
        this.selectedIds = selectedIds;
    }

    public WLGExport getExcelFornitori() {
        return excelFornitori;
    }

    public void setExcelFornitori(WLGExport excelFornitori) {
        this.excelFornitori = excelFornitori;
    }

    public List<Request> getSelectedRequests() {
        return selectedRequests;
    }

    public void setSelectedRequests(List<Request> selectedRequests) {
        this.selectedRequests = selectedRequests;
    }

    public List<Long> getFullfillRequestIds() {
        return fullfillRequestIds;
    }

    public void setFullfillRequestIds(List<Long> fullfillRequestIds) {
        this.fullfillRequestIds = fullfillRequestIds;
    }

    public Boolean isEvadiRichesta() {
        return evadiRichesta;
    }

    public void setEvadiRichesta(Boolean evadiRichesta) {
        this.evadiRichesta = evadiRichesta;
    }
}