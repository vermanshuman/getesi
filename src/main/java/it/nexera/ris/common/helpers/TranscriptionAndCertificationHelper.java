package it.nexera.ris.common.helpers;

import it.nexera.ris.common.enums.*;
import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.helpers.create.xls.CreateExcelRequestsReportHelper;
import it.nexera.ris.common.xml.wrappers.SelectItemWrapper;
import it.nexera.ris.persistence.UserHolder;
import it.nexera.ris.persistence.beans.dao.CriteriaAlias;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.domain.*;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.CostStampsOrServices;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.ImportF24Pdf;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.Province;
import it.nexera.ris.settings.ApplicationSettingsHolder;
import it.nexera.ris.web.beans.wrappers.logic.ExcelDataWrapper;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.HibernateException;
import org.hibernate.StaleObjectStateException;
import org.hibernate.Transaction;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Restrictions;
import org.hibernate.sql.JoinType;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import java.io.*;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

import static it.nexera.ris.common.helpers.GeneralFunctionsHelper.APPLICATION_PDF;

@Setter
@Getter
public class TranscriptionAndCertificationHelper {

    private StreamedContent pdfOfLetterFile;

    private CostManipulationHelper costManipulationHelper = new CostManipulationHelper();

    private transient final Log log = LogFactory.getLog(getClass());

    private Document document;

    private WLGInbox mail;

    private List<SelectItemWrapper<Client>> selectedClientManagers;

    private void saveExtraCost(Request transcriptionRequest, ExtraCost newCost, Double price, ExtraCostType extraCostType, String note)
            throws PersistenceBeanException {
        if (newCost == null) {
            newCost = new ExtraCost();
            newCost.setNote(note);
        }
        newCost.setTranscription(Boolean.TRUE);
        newCost.setPrice(price);
        newCost.setType(extraCostType);
        newCost.setRequestId(transcriptionRequest.getId());
        DaoManager.save(newCost);
    }

    public TranscriptionData saveTranscription(Request transcriptionRequest, TranscriptionData transcriptionData,
                                               Double ipotecarioCost, Double ipotecarioPostale, String courierFileName, Document courierDoc,
                                               byte[] courierDocumentContents, String entryFileName, Document entryDoc, byte[] entryDocumentContents) {
        Transaction tr = null;
        try {
            tr = DaoManager.getSession().beginTransaction();
            if (!ValidationHelper.isNullOrEmpty(transcriptionRequest)) {
                List<ExtraCost> extraCosts = DaoManager.load(ExtraCost.class, new Criterion[]{
                        Restrictions.eq("requestId", transcriptionRequest.getId()),
                        Restrictions.eq("type", ExtraCostType.IPOTECARIO)});

                if (!ValidationHelper.isNullOrEmpty(ipotecarioCost)) {
                    if (ValidationHelper.isNullOrEmpty(extraCosts)) {
                        saveExtraCost(transcriptionRequest, null, ipotecarioCost, ExtraCostType.IPOTECARIO, "Sintetico");
                    } else {
                        saveExtraCost(transcriptionRequest, extraCosts.get(0), ipotecarioCost, ExtraCostType.IPOTECARIO, "Sintetico");
                    }
                } else if (!ValidationHelper.isNullOrEmpty(extraCosts)) {
                    saveExtraCost(transcriptionRequest, null, ipotecarioCost, ExtraCostType.IPOTECARIO, "Sintetico");
                }

                extraCosts = DaoManager.load(ExtraCost.class, new Criterion[]{
                        Restrictions.eq("requestId", transcriptionRequest.getId()),
                        Restrictions.eq("type", ExtraCostType.POSTALE)});

                if (!ValidationHelper.isNullOrEmpty(ipotecarioPostale)) {
                    if (ValidationHelper.isNullOrEmpty(extraCosts)) {
                        saveExtraCost(transcriptionRequest, null, ipotecarioPostale, ExtraCostType.POSTALE, "Spese postali");
                    } else {
                        saveExtraCost(transcriptionRequest, extraCosts.get(0), ipotecarioPostale, ExtraCostType.POSTALE, "Spese postali");
                    }
                } else if (!ValidationHelper.isNullOrEmpty(extraCosts)) {
                    saveExtraCost(transcriptionRequest, null, ipotecarioPostale, ExtraCostType.POSTALE, "Spese postali");
                }
                extraCosts = DaoManager.load(ExtraCost.class, new Criterion[]{
                        Restrictions.eq("requestId", transcriptionRequest.getId()),
                        Restrictions.eq("type", ExtraCostType.MARCA),
                        Restrictions.eq("note", "Marca da bollo").ignoreCase()});

                double taxStampCost = 0.00d;
                double servicesStampCost = 0.00d;
                List<CostStampsOrServices> costStampsOrServicesList = DaoManager.load(CostStampsOrServices.class);
                if (costStampsOrServicesList != null && costStampsOrServicesList.size() > 0) {
                    CostStampsOrServices costStampsOrServices = costStampsOrServicesList.get(0);
                    taxStampCost = costStampsOrServices.getTaxStampCost();
                    servicesStampCost = costStampsOrServices.getServicesStampCost();
                }

                if (!ValidationHelper.isNullOrEmpty(transcriptionData.getNumberRevenueStamp())) {
                    if (ValidationHelper.isNullOrEmpty(extraCosts)) {
                        saveExtraCost(transcriptionRequest, null, transcriptionData.getNumberRevenueStamp() * taxStampCost, ExtraCostType.MARCA, "Marca da bollo");
                    } else {
                        saveExtraCost(transcriptionRequest, extraCosts.get(0), transcriptionData.getNumberRevenueStamp() * taxStampCost, ExtraCostType.MARCA, "Marca da bollo");
                    }
                } else if (!ValidationHelper.isNullOrEmpty(extraCosts)) {
                    saveExtraCost(transcriptionRequest, null, 0.0, ExtraCostType.MARCA, "Marca da bollo");
                }

                extraCosts = DaoManager.load(ExtraCost.class, new Criterion[]{
                        Restrictions.eq("requestId", transcriptionRequest.getId()),
                        Restrictions.eq("type", ExtraCostType.MARCA),
                        Restrictions.eq("note", "Marca servizio").ignoreCase()});

                if (!ValidationHelper.isNullOrEmpty(transcriptionData.getNumberServiceStamp())) {
                    if (ValidationHelper.isNullOrEmpty(extraCosts)) {
                        saveExtraCost(transcriptionRequest, null, transcriptionData.getNumberServiceStamp() * servicesStampCost, ExtraCostType.MARCA, "Marca servizio");
                    } else {
                        saveExtraCost(transcriptionRequest, extraCosts.get(0),
                                transcriptionData.getNumberRevenueStamp() * servicesStampCost, ExtraCostType.MARCA, "Marca servizio");
                    }
                } else if (!ValidationHelper.isNullOrEmpty(extraCosts)) {
                    saveExtraCost(transcriptionRequest, null, 0.0, ExtraCostType.MARCA, "Marca servizio");
                }
            }
            DaoManager.save(transcriptionData);
            if (!ValidationHelper.isNullOrEmpty(courierDocumentContents)) {
                Document courierDocument = handleDocument("courier", transcriptionRequest, transcriptionData,
                        courierFileName, courierDocumentContents, entryFileName, entryDocumentContents);
                transcriptionData.setCourierDocument(courierDocument);
                if (ValidationHelper.isNullOrEmpty(courierDoc)
                        && !ValidationHelper.isNullOrEmpty(courierDocument)
                        && StringUtils.isNotBlank(courierDocument.getFileExtension())
                        && APPLICATION_PDF.equalsIgnoreCase(courierDocument.getFileExtension().replaceAll("\\.", ""))) {
                    try {
                        invokeExternalTool(courierDocument, courierFileName);
                    } catch (Exception e) {
                        e.printStackTrace();
                        LogHelper.log(log, e);
                    }
                    List<Document> documentList = DaoManager.load(Document.class, new Criterion[]{
                            Restrictions.or(
                                    Restrictions.eq("title", courierDocument.getTitle()),
                                    Restrictions.like("path", courierDocument.getTitle(), MatchMode.ANYWHERE))
                    });
                    if (!ValidationHelper.isNullOrEmpty(documentList)) {
                        List<Formality> formalityList = documentList
                                .stream()
                                .filter(d -> !ValidationHelper.isNullOrEmpty(d.getFormality()))
                                .map(Document::getFormality)
                                .flatMap(List::stream).collect(Collectors.toList());

                        if (!ValidationHelper.isNullOrEmpty(formalityList)) {
                            Optional<Formality> formality = formalityList.stream()
                                    .max((o1, o2) -> o1.getDocument().getCreateDate().compareTo(o2.getCreateDate()));
                            if (formality.isPresent()) {
                                transcriptionRequest.setDistraintFormality(formality.get());
                                DaoManager.save(transcriptionRequest);
                            }
                        }
                    }
                }
            } else if (!ValidationHelper.isNullOrEmpty(courierDoc)) {
                File courierDocument = new File(courierDoc.getPath());
                if (StringUtils.isNotBlank(courierDoc.getPath()) && courierDocument.exists()) {
                    FileHelper.delete(courierDocument);
                }
            }
            if (!ValidationHelper.isNullOrEmpty(entryDocumentContents)) {
                Document entryDocument = handleDocument("entry", transcriptionRequest, transcriptionData,
                        courierFileName, courierDocumentContents, entryFileName, entryDocumentContents);
                transcriptionData.setEntryDocument(entryDocument);
            } else if (!ValidationHelper.isNullOrEmpty(entryDoc)) {
                File entryDocument = new File(entryDoc.getPath());
                if (StringUtils.isNotBlank(entryDoc.getPath()) && entryDocument.exists()) {
                    FileHelper.delete(entryDocument);
                }
            }
            if(ValidationHelper.isNullOrEmpty(transcriptionData.getRequest()))
                transcriptionData.setRequest(transcriptionRequest);
            DaoManager.save(transcriptionData);
        } catch (Exception e) {
            e.printStackTrace();
            if (tr != null) {
                tr.rollback();
            }
            LogHelper.log(log, e);
            MessageHelper.addGlobalMessage(FacesMessage.SEVERITY_ERROR, "",
                    ResourcesHelper.getValidation("objectEditedException"));
        } finally {
            if (tr != null && !tr.wasRolledBack() && tr.isActive()) {
                try {
                    tr.commit();
                } catch (StaleObjectStateException e) {
                    MessageHelper
                            .addGlobalMessage(
                                    FacesMessage.SEVERITY_ERROR,
                                    "",
                                    ResourcesHelper
                                            .getValidation("exceptionOccuredWhileSaving"));
                    LogHelper.log(log, e);
                } catch (Exception e) {
                    LogHelper.log(log, e);
                    e.printStackTrace();
                }
            }
        }
        try {
            getCostManipulationHelper().setCostNote(null);
            getCostManipulationHelper().performCostCalculation(transcriptionRequest, true);

        } catch (Exception e) {
            LogHelper.log(log, e);
            e.printStackTrace();
        }
        return transcriptionData;
    }

    private void invokeExternalTool(Document courierDocument, String courierFileName) {
        String path = Paths.get(courierDocument.getDocumentPath()).getParent().toString();
        if (path.charAt(path.length() - 1) != File.separatorChar) {
            path += File.separator;
        }
        StringBuilder sb = new StringBuilder();
        sb.append(path);
        GeneralFunctionsHelper.invokeExternalTool(courierDocument, null, courierFileName, sb, true);
    }

    public Document handleDocument(String directory, Request transcriptionRequest, TranscriptionData transcriptionData,
                                   String courierFileName, byte[] courierDocumentContents, String entryFileName, byte[] entryDocumentContents)
            throws IOException, HibernateException, PersistenceBeanException {
        Document document = null;
        try {
            String path = FileHelper.getTranscriptionDocumentSavePath(transcriptionData.getId(), directory, "transcription");
            File filePath = new File(path);
            if (directory.equalsIgnoreCase("courier")) {
                FileHelper.writeFileToFolder(courierFileName, filePath, courierDocumentContents);
                if (ValidationHelper.isNullOrEmpty(transcriptionData.getCourierDocument()))
                    document = new Document();
                else
                    document = transcriptionData.getCourierDocument();
                document.setTitle(FileHelper.getFileNameWOExtension(courierFileName));
                document.setPath(path + courierFileName);
                document.setTypeId(DocumentType.COURIER.getId());
            } else {
                FileHelper.writeFileToFolder(entryFileName, filePath, entryDocumentContents);
                if (ValidationHelper.isNullOrEmpty(transcriptionData.getEntryDocument()))
                    document = new Document();
                else
                    document = transcriptionData.getEntryDocument();
                document.setTitle(FileHelper.getFileNameWOExtension(entryFileName));
                document.setPath(path + entryFileName);
                document.setTypeId(DocumentType.ENTRY.getId());
            }
            document.setRequest(transcriptionRequest);

            document.setDate(new Date());
            DaoManager.save(document);
        } catch (Exception e) {
            LogHelper.log(log, e);
        }
        return document;
    }

    public String preparePdfOfLetter(Request transcriptionRequest, TranscriptionData transcriptionData) throws IOException, PersistenceBeanException, IllegalAccessException, InstantiationException {
        String body = getPdfRequestBody(transcriptionRequest, transcriptionData);
        log.info("Pdf of letter body" + body);
        byte[] fileContent = PrintPDFHelper.convertToPDF(null, body, null, DocumentType.OTHER, true);

        if (fileContent != null) {
            String fileName = "lettera_Conservatoria_COMO";
            String tempDir = FileHelper.getLocalTempDir();
            tempDir += File.separator + UUID.randomUUID();
            FileUtils.forceMkdir(new File(tempDir));
            String tempPdf = tempDir + File.separator + fileName + ".pdf";
            InputStream stream = new ByteArrayInputStream(fileContent);
            File targetFile = new File(tempPdf);
            OutputStream outStream = new FileOutputStream(targetFile);
            setPdfOfLetterFile(new DefaultStreamedContent(stream, FileHelper.getFileExtension(tempPdf),
                    fileName.toUpperCase() + ".pdf"));
            IOUtils.closeQuietly(stream);
            IOUtils.closeQuietly(outStream);
            log.info("Pdf of letter created : " + tempPdf);
            return tempPdf;
        }
        return "";
    }

    public String getPdfRequestBody(Request transcriptionRequest, TranscriptionData transcriptionData) throws HibernateException, IllegalAccessException, PersistenceBeanException, InstantiationException {
        String realEstateName = "";
        String reclameServiceName = "";
        String subjects = "";
        double importf24 = 0.00d;
        String transcriptionActType = "";
        Integer packetNumber = 0;
        String textInVisura = "";
        boolean isNumberRevenueStamp = false;
        Integer numberRevenueStamp = 0;
        boolean isNumberServiceStamp = false;
        Integer numberServiceStamp = 0;
        double taxStampCost = 0.00d;
        double servicesStampCost = 0.00d;
        List<CostStampsOrServices> costStampsOrServicesList = DaoManager.load(CostStampsOrServices.class);
        if (costStampsOrServicesList != null && costStampsOrServicesList.size() > 0) {
            CostStampsOrServices costStampsOrServices = costStampsOrServicesList.get(0);
            taxStampCost = costStampsOrServices.getTaxStampCost();
            servicesStampCost = costStampsOrServices.getServicesStampCost();
        }
        if (!ValidationHelper.isNullOrEmpty(transcriptionRequest)) {
            if (!ValidationHelper.isNullOrEmpty(transcriptionRequest.getTranscriptionActId())) {
                if (!ValidationHelper.isNullOrEmpty(transcriptionRequest.getTranscriptionActId().getProvincialOfficeName())) {
                    realEstateName = transcriptionRequest.getTranscriptionActId().getProvincialOfficeName();
                } else if (!ValidationHelper.isNullOrEmpty(transcriptionRequest.getTranscriptionActId().getReclamePropertyService()) &&
                        !ValidationHelper.isNullOrEmpty(transcriptionRequest.getTranscriptionActId().getReclamePropertyService().getName())) {
                    reclameServiceName = transcriptionRequest.getTranscriptionActId().getReclamePropertyService().getName();
                }
                if (!ValidationHelper.isNullOrEmpty(transcriptionRequest.getTranscriptionActId().getType())) {
                    transcriptionActType = transcriptionRequest.getTranscriptionActId().getType();
                }
                textInVisura = transcriptionRequest.getTranscriptionActId().getDicTypeFormalityAttachmentCText();
            } else {
                if (!ValidationHelper.isNullOrEmpty(transcriptionRequest.getSpecialFormality())
                        && !ValidationHelper.isNullOrEmpty(transcriptionRequest.getSpecialFormality().getTextInVisura()))
                    textInVisura = transcriptionRequest.getSpecialFormality().getTextInVisura().toLowerCase();
            }
            if (StringUtils.isBlank(realEstateName) && StringUtils.isBlank(reclameServiceName) &&
                    !ValidationHelper.isNullOrEmpty(transcriptionRequest.getAggregationLandChargesRegistryName())) {
                realEstateName = transcriptionRequest.getAggregationLandChargesRegistryName();
            }
            if (StringUtils.isBlank(transcriptionActType) && !ValidationHelper.isNullOrEmpty(transcriptionRequest.getActType())) {
                switch (transcriptionRequest.getActType()) {
                    case "I":
                        transcriptionActType = "iscrizione";
                        break;
                    case "T":
                        transcriptionActType = "trascrizione";
                        break;
                    case "A":
                        transcriptionActType = "annotamento";
                        break;
                    default:
                        transcriptionActType = "";
                        break;
                }
            }
            subjects = RequestHelper.getRequestSubjects(transcriptionRequest, true);
        }

        if (!ValidationHelper.isNullOrEmpty(transcriptionData.getTranscriptionAmount())) {
            importf24 = transcriptionData.getTranscriptionAmount();
        }
        if (transcriptionData.getAnticipatedF24() && !ValidationHelper.isNullOrEmpty(transcriptionData.getImportF24Anticipated())) {
            importf24 = transcriptionData.getImportF24Anticipated();
        }
        if (!ValidationHelper.isNullOrEmpty(transcriptionData.getPacketNumber())) {
            packetNumber = transcriptionData.getPacketNumber();
        }
        if (!ValidationHelper.isNullOrEmpty(transcriptionData.getNumberRevenueStamp())
                && transcriptionData.getNumberRevenueStamp().intValue() != 0) {
            isNumberRevenueStamp = true;
            numberRevenueStamp = transcriptionData.getNumberRevenueStamp();
        }
        if (!ValidationHelper.isNullOrEmpty(transcriptionData.getNumberServiceStamp())
                && transcriptionData.getNumberServiceStamp().intValue() != 0) {
            isNumberServiceStamp = true;
            numberServiceStamp = transcriptionData.getNumberServiceStamp();
        }

        String actTypeText = "";
        if (!ValidationHelper.isNullOrEmpty(transcriptionRequest.getActType()) && transcriptionRequest.getActType().equals("T")) {
            actTypeText = "la trascrizione";
        } else if (!ValidationHelper.isNullOrEmpty(transcriptionRequest.getTranscriptionActId())
                && !ValidationHelper.isNullOrEmpty(transcriptionRequest.getTranscriptionActId().getType())
                && transcriptionRequest.getTranscriptionActId().getType().equals("trascrizione")) {
            actTypeText = "la trascrizione";
        }
        if (!ValidationHelper.isNullOrEmpty(transcriptionRequest.getActType()) && transcriptionRequest.getActType().equals("I")) {
            actTypeText = "l&#39;iscrizione";
        } else if (!ValidationHelper.isNullOrEmpty(transcriptionRequest.getTranscriptionActId())
                && !ValidationHelper.isNullOrEmpty(transcriptionRequest.getTranscriptionActId().getType())
                && transcriptionRequest.getTranscriptionActId().getType().equals("iscrizione")) {
            actTypeText = "l&#39;iscrizione";
        }
        if (!ValidationHelper.isNullOrEmpty(transcriptionRequest.getActType()) && transcriptionRequest.getActType().equals("A")) {
            actTypeText = "l&#39;annotamento";
        } else if (!ValidationHelper.isNullOrEmpty(transcriptionRequest.getTranscriptionActId())
                && !ValidationHelper.isNullOrEmpty(transcriptionRequest.getTranscriptionActId().getType())
                && transcriptionRequest.getTranscriptionActId().getType().equals("annotamento")) {
            actTypeText = "l&#39;annotamento";
        }

        String formalityText = "";
        if (!ValidationHelper.isNullOrEmpty(transcriptionRequest.getSpecialFormality())) {
            if (!ValidationHelper.isNullOrEmpty(transcriptionRequest.getSpecialFormality().getPreposition()))
                formalityText = transcriptionRequest.getSpecialFormality().getPreposition();
            if (!ValidationHelper.isNullOrEmpty(transcriptionRequest.getSpecialFormality().getTextInVisura()))
                formalityText += " " + transcriptionRequest.getSpecialFormality().getTextInVisura();
        }

        String operationalHeadquarters = ApplicationSettingsHolder.getInstance().getByKey(ApplicationSettingsKeys.ADDRESS).getValue()
                + " - " + ApplicationSettingsHolder.getInstance().getByKey(ApplicationSettingsKeys.COMPANY_POSTAL_CODE).getValue()
                + " - " + ApplicationSettingsHolder.getInstance().getByKey(ApplicationSettingsKeys.COMPANY_CITY).getValue();

        String emailsFrom = MailHelper.getEmailFrom();

        String supportType = !ValidationHelper.isNullOrEmpty(transcriptionData.getSupportType()) ?
                SupportType.getById(transcriptionData.getSupportType()).toString() : "";

        /*String relativePath="/resources/images/";
        String absolutePath =   FacesContext.getCurrentInstance().getExternalContext().getRealPath(relativePath);
        String filePath = "file:" + absolutePath + "pdf_logo.png";*/

        StringBuilder result = new StringBuilder();
        result.append("<head>");
        result.append("<style>div{font-family: Verdana, sans-serif; font-size: 15px;}</style>");
        result.append("</head>");
        result.append("<div style=\"margin-top: 50px; margin-left: 40px; margin-right: 40px;\">");
        result.append("<div style=\"float: left;\">");
        result.append("<img src='http://operation.getesi.it/getesi_image/logo.png' style='width:130px;height:75px;'>");
        result.append("</div>");
        result.append("<br><br><br>");
        result.append("<div style=\"float: right; margin-top:60px;\">");
        result.append("<i>Spett.le</i><br><br>");
        result.append("Agenzia delle Entrate - Servizi di Pubblicit&#224;<br><br>");
        result.append("Immobiliare di ");
        if (StringUtils.isNotBlank(realEstateName))
            result.append(realEstateName);
        else if (StringUtils.isNotBlank(reclameServiceName)) {
            result.append("<b>");
            result.append(reclameServiceName);
            result.append("</b>");
        }
        result.append(",<br><br>");
        result.append("<u>Ufficio Trascrizione Atti</u>,<br><br>");
        result.append("</div>");
        result.append("<br><br>");
        result.append("<div style=\"margin-top: 50px; width: 100%;\">");
        result.append("<span style=\"line-height: 2;\">con la presente siamo a richiedervi " + actTypeText + " " + formalityText.toLowerCase() + " contro " + subjects
                + " per la quale si allegano i seguenti documenti:</span><br><br><br>");
        result.append("<ul>");
        result.append("<li style=\"line-height: 2.5;\">Modello F24 pagato (&#8364;.");
        if(transcriptionData.getUserPhone() != null && !transcriptionData.getUserPhone()){
            result.append(" ");
        }
        result.append(InvoiceHelper.format(importf24) + ");</li>");
        result.append("<li style=\"line-height: 2.5;\">Nota di " + transcriptionActType + " in formato cartaceo;</li>");
        result.append("<li style=\"line-height: 2.5;\">" + supportType + " con nota in formato sogei (plico " + packetNumber + ");</li>");
        result.append("<li style=\"line-height: 2.5;\">Copia uso " + transcriptionActType + " atto di " + textInVisura + ";</li>");
        if (isNumberRevenueStamp)
            result.append("<li style=\"line-height: 2.5;\">" + numberRevenueStamp + " marche da bollo da &#8364; " + InvoiceHelper.format(taxStampCost) + " ;</li>");
        if (isNumberServiceStamp)
            result.append("<li style=\"line-height: 2.5;\">" + numberServiceStamp + " marche servizi da &#8364; " + InvoiceHelper.format(servicesStampCost) + " ;</li>");
        if (!ValidationHelper.isNullOrEmpty(transcriptionData.getEnvelopStamp()) && transcriptionData.getEnvelopStamp())
            result.append("<li style=\"line-height: 2.5;\">Busta preaffrancata.</li>");
        result.append("</ul>");
        result.append("<br><br>");
        if(StringUtils.isNotBlank(transcriptionData.getNote())) {
            result.append("<b>");
            result.append(transcriptionData.getNote());
            result.append("</b>");
        }
        result.append("<br><br><br><br>");
        result.append("Per ogni eventuale chiarimento, lascio di seguito il contatto di riferimento della Societ&#224;");

        if(transcriptionData.getUserPhone() != null && transcriptionData.getUserPhone()){
            result.append(" ed il mio personale");
        }
        result.append(":<br><br>");
        result.append("<br><br>");
        result.append("Tel. Getesi Srl: " + ApplicationSettingsHolder.getInstance().getByKey(ApplicationSettingsKeys.TELEPHONE).getValue()
                + "<br style=\"content: ''; display: block;\">");
        if (!ValidationHelper.isNullOrEmpty(transcriptionData.getUserPhone()) && transcriptionData.getUserPhone()) {
            result.append("Cel. " + WordUtils.capitalizeFully(transcriptionRequest.getUser().getLastName()) + " " +
                    WordUtils.capitalizeFully(transcriptionRequest.getUser().getFirstName())
                    + " : " + (!ValidationHelper.isNullOrEmpty(transcriptionRequest.getUser().getPhone()) ?
                    transcriptionRequest.getUser().getPhone() : "") + "<br><br><br>");
        } else {
            result.append("<br>");
        }
        Date date = new Date();
        result.append("Napoli, " + DateTimeHelper.toFormatedString(date, DateTimeHelper.getDatePattern()) + "<br><br><br><br><br>");
        result.append("Getesi Srl<br style=\"content: ''; display: block;\">");
        result.append("Sede Operativa: " + operationalHeadquarters + "<br style=\"content: ''; display: block;\">");
        result.append("Tel. 081 " + ApplicationSettingsHolder.getInstance().getByKey(ApplicationSettingsKeys.TELEPHONE).getValue()
                + " - Fax 081 " + ApplicationSettingsHolder.getInstance().getByKey(ApplicationSettingsKeys.FAX).getValue()
                + "<br style=\"content: ''; display: block;\">");
        result.append(emailsFrom + "<br style=\"content: ''; display: block;\">");
        result.append("P.iva: " + ApplicationSettingsHolder.getInstance().getByKey(ApplicationSettingsKeys.FISCAL_CODE).getValue());
        result.append("</div>");
        result.append("</div>");
        return result.toString();
    }

    public String generatePdfPage(String footer, boolean addAuthorizationBlock, Request transcriptionRequest) throws PersistenceBeanException, IllegalAccessException, InstantiationException {
        String companyName = ApplicationSettingsHolder.getInstance().getByKey(ApplicationSettingsKeys.COMPANY_NAME).getValue();
        String fiscalCode = ApplicationSettingsHolder.getInstance().getByKey(ApplicationSettingsKeys.FISCAL_CODE).getValue();
        String provinceDescription = ApplicationSettingsHolder.getInstance().getByKey(ApplicationSettingsKeys.COMPANY_PROVINCE).getValue();
        String provinceCode = "";
        if (!ValidationHelper.isNullOrEmpty(provinceDescription)) {
            List<Province> provinces = DaoManager.load(Province.class,
                    new Criterion[]{
                            Restrictions.eq("description", provinceDescription).ignoreCase()
                    });
            if (!ValidationHelper.isNullOrEmpty(provinces)) {
                provinceCode = provinces.get(0).getCode();
                provinceCode = "<table style=\"width: 100%;\">\n" +
                        "                                    <tbody>\n" +
                        "                                        <tr style=\"background-color: #fff;padding-top: 10px;\">\n" +
                        "                                            <td style=\"padding: 0 10px;border-right: 1px solid #40B6D1;\">" + provinceCode.charAt(0) + "</td>\n" +
                        "                                            <td style=\"padding: 0 10px;\">" + provinceCode.charAt(1) + "</td>\n" +
                        "                                        </tr>\n" +
                        "                                    </tbody>\n" +
                        "                                </table>\n";
            }
        }
        String address = ApplicationSettingsHolder.getInstance().getByKey(ApplicationSettingsKeys.ADDRESS).getValue();
        List<ImportF24Pdf> importF24s = transcriptionRequest != null && transcriptionRequest.getImportF24PdfList() != null ?
                transcriptionRequest.getImportF24PdfList() : null;
        String requestSpecialFormalityRows = "";
        Double totalImport = 0.0d;
        if (importF24s != null) {
            for (ImportF24Pdf importF24 : importF24s) {
                requestSpecialFormalityRows += getRequestSpecialFormalityRow(importF24, transcriptionRequest);
                if (importF24.getF24Import() != null && Double.valueOf(importF24.getF24Import()) > 0)
                    totalImport += Double.valueOf(importF24.getF24Import());
            }
        }
        for (int i = 0; i < 16 - (importF24s != null ? importF24s.size() : 0); i++) {
            requestSpecialFormalityRows += getRequestSpecialFormalityRow(null, transcriptionRequest);
        }
        String fiscalCodeHtml = "";
        for (int i = 0; i < fiscalCode.length(); i++) {
            fiscalCodeHtml += "<td style=\"padding: 0 5px;border-right: 1px solid #40B6D1;\">" + fiscalCode.charAt(i) + "</td>";
        }
        String officeCode = transcriptionRequest != null && transcriptionRequest.getAggregationLandChargesRegistry() != null && transcriptionRequest.getAggregationLandChargesRegistry().getCodeOffice() != null
                ? transcriptionRequest.getAggregationLandChargesRegistry().getCodeOffice() : null;
        String officeCodeHtml = "";
        if (officeCode != null) {
            for (int i = 0; i < officeCode.length(); i++) {
                officeCodeHtml += "<td style=\"padding: 0 10px;border-right: 1px solid #40B6D1;\">" + officeCode.charAt(i) + "</td>";
            }
        } else {
            officeCodeHtml += "<td style=\"padding: 0 10px;border-right: 1px solid #40B6D1;\">&nbsp;</td>";
            officeCodeHtml += "<td style=\"padding: 0 10px;border-right: 1px solid #40B6D1;\">&nbsp;</td>";
            officeCodeHtml += "<td style=\"padding: 0 10px;\">&nbsp;</td>";
        }
        String relativePath = "/resources/images/";
        String absolutePath = FacesContext.getCurrentInstance().getExternalContext().getRealPath(relativePath);
        String filePath = "file:" + absolutePath + "/internal-revenue-service.png";

        String page = "<div style=\"padding: 20px;\">\n" +
                "            <table style=\"width: 100%;\">\n" +
                "                <tbody>\n" +
                "                    <tr>\n" +
                "                        <td width=\"30%\" style=\"vertical-align: baseline;\">\n" +
                "                            <div>\n" +
                "                                <div><img width=\"100%\" src='" + filePath + "'></div>\n" +
                "                            </div>\n" +
                "                        </td>\n" +
                "                        <td>\n" +
                "                            <table style=\"width: 100%;border-collapse: collapse;\">\n" +
                "                                <tbody>\n" +
                "                                    <tr>\n" +
                "                                        <td width=\"38%\"></td>\n" +
                "                                        <td width=\"10%\"></td>\n" +
                "                                        <td width=\"52%\">\n" +
                "                                            <div style=\"border: 2px solid #40B6D1;padding: 0px 3px;padding-top: 2px;font-size: 12px;\">Mod. <span style=\"color: #40B6D1;font-size: 16px;\">F24</span> Versamenti con element identificativi</div>\n" +
                "                                        </td>\n" +
                "                                    </tr>\n" +
                "                                    <tr>\n" +
                "                                        <td style=\"font-size: 13px;padding-top: 20px;text-align: right;padding-right: 10px;\">DELEGA IRREVOCABLE A:</td>\n" +
                "                                        <td style=\"background-color: #DAEEF6;min-height: 20px;\"></td>\n" +
                "                                        <td style=\"background-color: #DAEEF6;min-height: 20px;\"></td>\n" +
                "                                    </tr>\n" +
                "                                </tbody>\n" +
                "                            </table>\n" +
                "                            <table style=\"width: 100%;border-collapse: collapse;margin-top: 5px;\">\n" +
                "                                <tbody>\n" +
                "                                    <tr>\n" +
                "                                        <td width=\"38%\" style=\"font-size: 13px;padding-top: 20px;text-align: right;padding-right: 10px;\">AGENZIA</td>\n" +
                "                                        <td width=\"10%\" style=\"background-color: #DAEEF6;min-height: 20px;\"></td>\n" +
                "                                        <td width=\"31%\" style=\"background-color: #DAEEF6;min-height: 20px;\"></td>\n" +
                "                                        <td width=\"32%\" style=\"background-color: #DAEEF6;min-height: 20px;font-size: 13px;padding-top: 15px;\">PROV.</td>\n" +
                "                                    </tr>\n" +
                "                                </tbody>\n" +
                "                            </table>\n" +
                "                            <table style=\"width: 100%;border-collapse: collapse;\">\n" +
                "                                <tbody>\n" +
                "                                    <tr>\n" +
                "                                        <td style=\"font-size: 13px;\">PER L'ACCREDITO ALLA TESORERIA COMPETENTE</td>\n" +
                "                                    </tr>\n" +
                "                                </tbody>\n" +
                "                            </table>\n" +
                "                        </td>\n" +
                "                    </tr>\n" +
                "                </tbody>\n" +
                "            </table>\n" +
                "            <h5 style=\"margin-top: 3px;margin-bottom: 0px;color: #fff;background-color: #A3D6E7;padding: 2px 0px 0px 5px;\">CONTRIBUENTE</h5>\n" +
                "            <div style=\"background-color: #DAEEF6;min-height: 500px;\">\n" +
                "                <table style=\"width: 100%;\">\n" +
                "                    <tbody>\n" +
                "                        <tr>\n" +
                "                            <td width=\"20%\" style=\"font-size: 13px;padding-left: 5px;\"><h5>CODICE FISCALE</h5></td>\n" +
                "                            <td>\n" +
                "                                <table style=\"width: 55%;\">\n" +
                "                                    <tbody>\n" +
                "                                        <tr style=\"background-color: #fff;padding-top: 10px;\">\n" +
                fiscalCodeHtml +
                "                                        </tr>\n" +
                "                                    </tbody>\n" +
                "                                </table>\n" +
                "                            </td>\n" +
                "                        </tr>\n" +
                "                    </tbody>\n" +
                "                </table>\n" +
                "                <table style=\"width: 100%;\">\n" +
                "                    <tbody>\n" +
                "                        <tr>\n" +
                "                            <td width=\"20%\"></td>\n" +
                "                            <td width=\"50%\" style=\"font-size:12px\">cognome, denominazione o ragione sociale</td>\n" +
                "                            <td width=\"29%\" style=\"font-size:12px;border-left: 8px solid #DAEEF6;\">nome</td>\n" +
                "                            <td width=\"1%\"></td>\n" +
                "                        </tr>\n" +
                "                    </tbody>\n" +
                "                </table>\n" +
                "                <table style=\"width: 100%;\">\n" +
                "                    <tbody>\n" +
                "                        <tr>\n" +
                "                            <td width=\"20%\" style=\"font-size: 13px;height: 27px;padding-left: 5px;\"><h5>DATI ANAGRAFICI</h5></td>\n" +
                "                            <td width=\"50%\" style=\"background-color: #fff;height: 27px;\">" + companyName + "</td>\n" +
                "                            <td width=\"29%\" style=\"background-color: #fff;border-left: 8px solid #DAEEF6;height: 27px;\"></td>\n" +
                "                            <td width=\"1%\"></td>\n" +
                "                        </tr>\n" +
                "                    </tbody>\n" +
                "                </table>\n" +
                "                <table style=\"width: 100%;\">\n" +
                "                    <tbody>\n" +
                "                        <tr>\n" +
                "                            <td width=\"20%\" style=\"font-size:12px\"></td>\n" +
                "                            <td width=\"30%\" style=\"font-size:12px\">data di nascita</td>\n" +
                "                            <td width=\"14%\" style=\"font-size:12px;padding-left: 28px;\">sesso (M o F)</td>\n" +
                "                            <td width=\"29%\" style=\"font-size:12px\">comune (o Stato estero) di nascita</td>\n" +
                "                            <td width=\"6%\" style=\"font-size:12px\">prov.</td>\n" +
                "                            <td width=\"1%\"></td>\n" +
                "                        </tr>\n" +
                "                    </tbody>\n" +
                "                </table>\n" +
                "                <table style=\"width: 100%;\">\n" +
                "                    <tbody>\n" +
                "                        <tr>\n" +
                "                            <td width=\"20%\"></td>\n" +
                "                            <td width=\"22%\" style=\"background-color: #fff;\">\n" +
                "                                <table style=\"width: 100%;border-collapse: collapse;\">\n" +
                "                                    <thead>\n" +
                "                                        <tr>\n" +
                "                                            <th colspan=\"2\" width=\"25%\" style=\"text-align: center;font-size: 10px;border-right: 1px solid #40B6D1;\">giorno</th>\n" +
                "                                            <th colspan=\"2\" width=\"25%\" style=\"text-align: center;font-size: 10px;border-right: 1px solid #40B6D1;\">mese</th>\n" +
                "                                            <th colspan=\"4\" width=\"50%\" style=\"text-align: center;font-size: 10px;\">anno</th>\n" +
                "                                        </tr>\n" +
                "                                    </thead>\n" +
                "                                    <tbody>\n" +
                "                                        <tr>\n" +
                "                                            <td style=\"padding: 0 10px;border-right: 1px solid #40B6D1;\">&nbsp;</td>\n" +
                "                                            <td style=\"padding: 0 10px;border-right: 1px solid #40B6D1;\">&nbsp;</td>\n" +
                "                                            <td style=\"padding: 0 10px;border-right: 1px solid #40B6D1;\">&nbsp;</td>\n" +
                "                                            <td style=\"padding: 0 10px;border-right: 1px solid #40B6D1;\">&nbsp;</td>\n" +
                "                                            <td style=\"padding: 0 10px;border-right: 1px solid #40B6D1;\">&nbsp;</td>\n" +
                "                                            <td style=\"padding: 0 10px;border-right: 1px solid #40B6D1;\">&nbsp;</td>\n" +
                "                                            <td style=\"padding: 0 10px;border-right: 1px solid #40B6D1;\">&nbsp;</td>\n" +
                "                                            <td style=\"padding: 0 10px;\">&nbsp;</td>\n" +
                "                                        </tr>\n" +
                "                                    </tbody>\n" +
                "                                </table>\n" +
                "                            </td>\n" +
                "                            <td width=\"10%\" style=\"background-color: #fff;border-left: 16px solid #DAEEF6;border-right: 16px solid #DAEEF6;\"></td>\n" +
                "                            <td width=\"calc(41% - 16px)\" style=\"background-color: #fff;\"></td>\n" +
                "                            <td width=\"6%\" style=\"background-color: #fff;border-left: 8px solid #DAEEF6;\">\n" +
                "                                <table style=\"width: 100%;\">\n" +
                "                                    <tbody>\n" +
                "                                        <tr style=\"background-color: #fff;padding-top: 10px;\">\n" +
                "                                            <td style=\"padding: 0 10px;border-right: 1px solid #40B6D1;\">&nbsp;</td>\n" +
                "                                            <td style=\"padding: 0 10px;\">&nbsp;</td>\n" +
                "                                        </tr>\n" +
                "                                    </tbody>\n" +
                "                                </table>\n" +
                "                            </td>\n" +
                "                            <td width=\"1%\"></td>\n" +
                "                        </tr>\n" +
                "                    </tbody>\n" +
                "                </table>\n" +
                "                <table style=\"width: 100%;\">\n" +
                "                    <tbody>\n" +
                "                        <tr>\n" +
                "                            <td width=\"20%\"></td>\n" +
                "                            <td width=\"42%\" style=\"font-size:12px\">comune</td>\n" +
                "                            <td width=\"8%\" style=\"border-left: 8px solid #DAEEF6;font-size:12px;\">prov.</td>\n" +
                "                            <td width=\"30%\" style=\"border-left: 8px solid #DAEEF6;font-size:12px;\">via e numero civico</td>\n" +
                "                            <td width=\"1%\"></td>\n" +
                "                        </tr>\n" +
                "                    </tbody>\n" +
                "                </table>\n" +
                "                <table style=\"width: 100%;\">\n" +
                "                    <tbody>\n" +
                "                        <tr>\n" +
                "                            <td width=\"20%\" style=\"font-size: 13px;height: 27px;padding-left: 5px;\"><h5>DOMICILIO FISCALE</h5></td>\n" +
                "                            <td width=\"40%\" style=\"background-color: #fff;height: 27px;\"></td>\n" +
                "                            <td width=\"9%\" style=\"background-color: #fff;border-left: 8px solid #DAEEF6;height: 27px;\">" + provinceCode + "</td>\n" +
                "                            <td width=\"30%\" style=\"background-color: #fff;border-left: 8px solid #DAEEF6;height: 27px;font-size: 12px;\">" + address + "</td>\n" +
                "                            <td width=\"1%\"></td>\n" +
                "                        </tr>\n" +
                "                    </tbody>\n" +
                "                </table>\n" +
                "                <table style=\"width: 100%;margin-top: 15px;margin-bottom: 15px;\">\n" +
                "                    <tbody>\n" +
                "                        <tr>\n" +
                "                            <td width=\"35%\" style=\"font-size: 13px;padding-left: 5px;\"><h5>CODICE FISCALE del coobbligato, erede,<br/>genitore, tutore o curatore fallimentare</h5></td>\n" +
                "                            <td width=\"38%\" style=\"background-color: #fff;\">\n" +
                "                                <table style=\"width: 55%;\">\n" +
                "                                    <tbody>\n" +
                "                                        <tr style=\"background-color: #fff;padding-top: 10px;\">\n" +
                "                                            <td style=\"padding: 0 6px;border-right: 1px solid #40B6D1;\">&nbsp;</td>\n" +
                "                                            <td style=\"padding: 0 6px;border-right: 1px solid #40B6D1;\">&nbsp;</td>\n" +
                "                                            <td style=\"padding: 0 6px;border-right: 1px solid #40B6D1;\">&nbsp;</td>\n" +
                "                                            <td style=\"padding: 0 6px;border-right: 1px solid #40B6D1;\">&nbsp;</td>\n" +
                "                                            <td style=\"padding: 0 6px;border-right: 1px solid #40B6D1;\">&nbsp;</td>\n" +
                "                                            <td style=\"padding: 0 6px;border-right: 1px solid #40B6D1;\">&nbsp;</td>\n" +
                "                                            <td style=\"padding: 0 6px;border-right: 1px solid #40B6D1;\">&nbsp;</td>\n" +
                "                                            <td style=\"padding: 0 6px;border-right: 1px solid #40B6D1;\">&nbsp;</td>\n" +
                "                                            <td style=\"padding: 0 6px;border-right: 1px solid #40B6D1;\">&nbsp;</td>\n" +
                "                                            <td style=\"padding: 0 6px;border-right: 1px solid #40B6D1;\">&nbsp;</td>\n" +
                "                                            <td style=\"padding: 0 6px;border-right: 1px solid #40B6D1;\">&nbsp;</td>\n" +
                "                                            <td style=\"padding: 0 6px;border-right: 1px solid #40B6D1;\">&nbsp;</td>\n" +
                "                                            <td style=\"padding: 0 6px;border-right: 1px solid #40B6D1;\">&nbsp;</td>\n" +
                "                                            <td style=\"padding: 0 6px;border-right: 1px solid #40B6D1;\">&nbsp;</td>\n" +
                "                                            <td style=\"padding: 0 6px;border-right: 1px solid #40B6D1;\">&nbsp;</td>\n" +
                "                                            <td style=\"padding: 0 6px;\">&nbsp;</td>\n" +
                "                                        </tr>\n" +
                "                                    </tbody>\n" +
                "                                </table>\n" +
                "                            </td>\n" +
                "                            <td width=\"14%\" style=\"border-left: 8px solid #DAEEF6;font-size: 12px;\">codice identificativo</td>\n" +
                "                            <td width=\"6%\" style=\"background-color: #fff;border-left: 8px solid #DAEEF6;\">\n" +
                "                                <table style=\"width: 100%;\">\n" +
                "                                    <tbody>\n" +
                "                                        <tr style=\"background-color: #fff;padding-top: 10px;\">\n" +
                "                                            <td style=\"padding: 0 10px;border-right: 1px solid #40B6D1;\">&nbsp;</td>\n" +
                "                                            <td style=\"padding: 0 10px;\">&nbsp;</td>\n" +
                "                                        </tr>\n" +
                "                                    </tbody>\n" +
                "                                </table>\n" +
                "                            </td>\n" +
                "                            <td width=\"1%\"></td>\n" +
                "                        </tr>\n" +
                "                    </tbody>\n" +
                "                </table>\n" +
                "            </div>\n" +
                "            <h5 style=\"margin-bottom: 0px;color: #fff;background-color: #A3D6E7;padding: 2px 0px 0px 5px;\">SEZIONE ERARIO ED ALTRO</h5>\n" +
                "            <div style=\"background-color: #DAEEF6;min-height: 500px;padding-left: 15px;padding-bottom: 15px;\">\n" +
                "                <table style=\"width: 100%;\">\n" +
                "                    <tbody>\n" +
                "                        <tr>\n" +
                "                            <td width=\"6%\" style=\"font-size:12px;\">codice ufficio</td>\n" +
                "                            <td width=\"30%\" style=\"font-size:12px;\">codice atto</td>\n" +
                "                        </tr>\n" +
                "                    </tbody>\n" +
                "                </table>\n" +
                "                <table style=\"width: 100%;\">\n" +
                "                    <tbody>\n" +
                "                        <tr>\n" +
                "                            <td>\n" +
                "                                <table style=\"width: 3%;\">\n" +
                "                                    <tbody>\n" +
                "                                        <tr style=\"background-color: #fff;padding-top: 10px;\">\n" +
                officeCodeHtml +
                "                                        </tr>\n" +
                "                                    </tbody>\n" +
                "                                </table>\n" +
                "                            </td>\n" +
                "                            <td>\n" +
                "                                <table style=\"width: 30%;\">\n" +
                "                                    <tbody>\n" +
                "                                        <tr style=\"background-color: #fff;padding-top: 10px;\">\n" +
                "                                           <td style=\"padding: 0 10px;border-right: 1px solid #40B6D1;\">&nbsp;</td>\n" +
                "                                           <td style=\"padding: 0 10px;border-right: 1px solid #40B6D1;\">&nbsp;</td>\n" +
                "                                           <td style=\"padding: 0 10px;border-right: 1px solid #40B6D1;\">&nbsp;</td>\n" +
                "                                           <td style=\"padding: 0 10px;border-right: 1px solid #40B6D1;\">&nbsp;</td>\n" +
                "                                           <td style=\"padding: 0 10px;border-right: 1px solid #40B6D1;\">&nbsp;</td>\n" +
                "                                           <td style=\"padding: 0 10px;border-right: 1px solid #40B6D1;\">&nbsp;</td>\n" +
                "                                           <td style=\"padding: 0 10px;border-right: 1px solid #40B6D1;\">&nbsp;</td>\n" +
                "                                           <td style=\"padding: 0 10px;border-right: 1px solid #40B6D1;\">&nbsp;</td>\n" +
                "                                           <td style=\"padding: 0 10px;border-right: 1px solid #40B6D1;\">&nbsp;</td>\n" +
                "                                           <td style=\"padding: 0 10px;border-right: 1px solid #40B6D1;\">&nbsp;</td>\n" +
                "                                           <td style=\"padding: 0 10px;\">&nbsp;</td>\n" +
                "                                        </tr>\n" +
                "                                    </tbody>\n" +
                "                                </table>\n" +
                "                            </td>\n" +
                "                        </tr>\n" +
                "                    </tbody>\n" +
                "                </table>\n" +
                "                <table style=\"width: 100%;margin-top: 5px;\">\n" +
                "                    <tbody>\n" +
                "                        <tr>\n" +
                "                            <td width=\"4%\" style=\"font-size:12px;text-align: center;\">tipo</td>\n" +
                "                            <td width=\"48%\" style=\"font-size:12px;text-align: center;\">elementi identificativi</td>\n" +
                "                            <td width=\"10%\" style=\"font-size:12px;text-align: center;\">codice</td>\n" +
                "                            <td width=\"10%\" style=\"font-size:12px;text-align: center;\">anno di riferimento</td>\n" +
                "                            <td width=\"30%\" style=\"font-size:12px;text-align: center;\">importi a debito versati</td>\n" +
                "                        </tr>\n" +
                "                    </tbody>\n" +
                "                </table>\n" +
                "                <table style=\"width: 100%;\">\n" +
                "                    <tbody>\n" +
                requestSpecialFormalityRows +
                "                    </tbody>\n" +
                "                </table>\n" +
                "            </div>\n" +
                "            <table style=\"width: 100%;border-collapse: collapse;\">\n" +
                "               <tbody>\n" +
                "                   <tr>\n" +
                "                   <td width=\"60%\" style=\"border-right: 8px solid #DAEEF6;\">\n" +
                "                       <h5 style=\"width: 100%;margin-bottom: 0px;color: #fff;background-color: #A3D6E7;padding: 2px 0px 0px 5px;\">FIRMA</h5>\n" +
                "                       <table style=\"width: 100%;border-collapse: collapse;\">\n" +
                "                           <tbody>\n" +
                "                               <tr>\n" +
                "                                   <td width=\"100%\" style=\"background-color: #fff;border-left: 15px solid #DAEEF6;\">&nbsp;\n" +
                "                                   </td>\n" +
                "                               </tr>\n" +
                "                               <tr>\n" +
                "                                   <td width=\"100%\" style=\"background-color: #DAEEF6;border-left: 15px solid #DAEEF6;\">&nbsp;\n" +
                "                                   </td>\n" +
                "                               </tr>\n" +
                "                           </tbody>\n" +
                "                       </table>\n" +
                "                   </td>\n" +
                "                   <td width=\"40%\">\n" +
                "                       <h5 style=\"width: 100%;margin-bottom: 0px;color: #fff;background-color: #A3D6E7;padding: 2px 0px 0px 5px;\">SALDO FINALE</h5>\n" +
                "                       <table style=\"width: 100%;border-collapse: collapse;\">\n" +
                "                           <tbody>\n" +
                "                               <tr>\n" +
                "                                   <td width=\"30%\" style=\"background-color: #DAEEF6;text-align: right;padding-right:2px;\"><b>EURO</b></td>\n" +
                "                                   <td width=\"5%\" style=\"background-color: #fff;border-left: 1px solid #40B6D1;text-align: center;\">+</td>\n" +
                "                                   <td width=\"60%\" style=\"background-color: #fff;border-left: 1px solid #40B6D1;text-align: right;\">" + InvoiceHelper.format(totalImport) + "\n" +
                "                                   </td>\n" +
                "                               </tr>\n" +
                "                               <tr>\n" +
                "                                   <td width=\"30%\" style=\"background-color: #DAEEF6;text-align: right;padding-right:2px;\">&nbsp;</td>\n" +
                "                                   <td width=\"5%\" style=\"background-color: #DAEEF6;text-align: center;\">&nbsp;</td>\n" +
                "                                   <td width=\"60%\" style=\";background-color: #DAEEF6;text-align: right;\">&nbsp;\n" +
                "                                   </td>\n" +
                "                               </tr>\n" +
                "                           </tbody>\n" +
                "                       </table>\n" +
                "                   </td>\n" +
                "                   </tr>\n" +
                "               </tbody>\n" +
                "            </table>\n" +
                "            <h5 style=\"margin-bottom: 0px;margin-top: 0px;color: #fff;background-color: #A3D6E7;padding: 2px 0px 0px 5px;\">ESTREMI DEL VERSAMENTO <span style=\"font-size: 10px;\">(DA COMPILARE A CURA DI BANCA/POSTE/AGENTE DELLA RISCOSSIONE)</span></h5>\n" +
                "            <table style=\"width: 100%;border-collapse: collapse;font-size: 11px;border: 10px solid #A3D6E7;border-top-width: 0px;\">\n" +
                "               <tbody>\n" +
                "                   <tr>\n" +
                "                       <td width=\"60%\" style=\"border-right: 8px solid #DAEEF6;\">\n" +
                "                           <table style=\"width: 100%;border-collapse: collapse;font-size: 0.83em;\">\n" +
                "                               <thead>\n" +
                "                                   <tr>\n" +
                "                                       <th rowspan=\"2\" style=\"font-weight: 100;border-right: 1px solid #40B6D1;text-align: center;\">DATA</th>\n" +
                "                                       <th colspan=\"2\" style=\"font-weight: 100;text-align: center;\">CODICE BANCA/POSTE/AGENTE DELLA RISCOSSIONE</th>\n" +
                "                                   </tr>\n" +
                "                                   <tr>\n" +
                "                                       <th style=\"font-weight: 100;border: 1px solid #40B6D1;text-align: center;\">AZIENDA</th>\n" +
                "                                       <th style=\"font-weight: 100;border-top: 1px solid #40B6D1;border-bottom: 1px solid #40B6D1;text-align: center;\">CAB/SPORTELLO</th>\n" +
                "                                   </tr>\n" +
                "                               </thead>\n" +
                "                               <tbody>\n" +
                "                                   <tr>\n" +
                "                                       <td style=\"background-color: #fff;border-right: 1px solid #40B6D1;text-align: center;\">\n" +
                "                                <table style=\"width: 100%;border-collapse: collapse;\">\n" +
                "                                    <thead>\n" +
                "                                        <tr>\n" +
                "                                            <th colspan=\"2\" width=\"25%\" style=\"text-align: center;font-size: 10px;border-right: 1px solid #40B6D1;\">giorno</th>\n" +
                "                                            <th colspan=\"2\" width=\"25%\" style=\"text-align: center;font-size: 10px;border-right: 1px solid #40B6D1;\">mese</th>\n" +
                "                                            <th colspan=\"4\" width=\"50%\" style=\"text-align: center;font-size: 10px;\">anno</th>\n" +
                "                                        </tr>\n" +
                "                                    </thead>\n" +
                "                                    <tbody>\n" +
                "                                        <tr>\n" +
                "                                            <td style=\"padding: 0 10px;border-right: 1px solid #40B6D1;\">&nbsp;</td>\n" +
                "                                            <td style=\"padding: 0 10px;border-right: 1px solid #40B6D1;\">&nbsp;</td>\n" +
                "                                            <td style=\"padding: 0 10px;border-right: 1px solid #40B6D1;\">&nbsp;</td>\n" +
                "                                            <td style=\"padding: 0 10px;border-right: 1px solid #40B6D1;\">&nbsp;</td>\n" +
                "                                            <td style=\"padding: 0 10px;border-right: 1px solid #40B6D1;\">&nbsp;</td>\n" +
                "                                            <td style=\"padding: 0 10px;border-right: 1px solid #40B6D1;\">&nbsp;</td>\n" +
                "                                            <td style=\"padding: 0 10px;border-right: 1px solid #40B6D1;\">&nbsp;</td>\n" +
                "                                            <td style=\"padding: 0 10px;\">&nbsp;</td>\n" +
                "                                        </tr>\n" +
                "                                    </tbody>\n" +
                "                                </table>\n" +
                "                                       <td style=\"border-right: 1px solid #40B6D1;text-align: center;\">&nbsp;</td>\n" +
                "                                       <td style=\"text-align: center;\">&nbsp;</td>\n" +
                "                                   </tr>\n" +
                "                               </tbody>\n" +
                "                           </table>\n" +
                "                       </td>\n" +
                "                       <td width=\"40%\">\n" +
                "                           <table style=\"width: 100%;border-collapse: collapse;\">\n" +
                "                               <tbody>\n" +
                "                                   <tr>\n" +
                "                                       <td style=\"border-right: 5px solid #DAEEF6;text-align: center;\">Pagamento effettuato con assegno <div style=\"width: 16px;height: 14px;border: 1px solid #40B6D1;display: inline-block;margin-left: 5px;\">&nbsp;</div> bancario/postale</td>\n" +
                "                                   </tr>\n" +
                "                                   <tr>\n" +
                "                                       <td style=\"border-right: 5px solid #DAEEF6;text-align: center;\">n.ro <div style=\"width: 100px;height: 14px;border-bottom: 1px solid #000;display: inline-block;margin-left: 5px;\">&nbsp;</div><div style=\"width: 16px;height: 14px;border: 1px solid #40B6D1;display: inline-block;margin-left: 5px;\">&nbsp;</div> circolare/vaglia postale</td>\n" +
                "                                   </tr>\n" +
                "                                   <tr>\n" +
                "                                       <td style=\"border-right: 5px solid #DAEEF6;text-align: center;\">tratto / emesso su <div style=\"width: 90px;height: 14px;border-bottom: 1px solid #000;display: inline-block;margin-left: 5px;\">&nbsp;</div><div style=\"width: 90px;height: 14px;border-bottom: 1px solid #000;display: inline-block;margin-left: 5px;\">&nbsp;</div></td>\n" +
                "                                   </tr>\n" +
                "                                   <tr>\n" +
                "                                       <td style=\"border-right: 5px solid #DAEEF6;\"> <div style=\"width: 170px;height: 14px;display: inline-block;margin-left: 120px;\">cod. ABI</div><div style=\"width: 50px;height: 14px;display: inline-block;margin-left: 45px;\">CAB</div></td>\n" +
                "                                   </tr>\n" +
                "                               </tbody>\n" +
                "                           </table>\n" +
                "                       </td>\n" +
                "                   </tr>\n" +
                "               </tbody>\n" +
                "            </table>\n" +
                "            <table style=\"width: 100%;border-collapse: collapse;font-size: 10px;border: 1px solid #DAEEF6;border-top: none;\">\n" +
                "               <tbody>\n" +
                "                   <tr>\n" +
                (addAuthorizationBlock ? "\n" +
                        "                       <td style=\"text-align: center;width: 150px;\">Autorizzo addebito su conto corrente bancario n° <div style=\"width: 90px;height: 14px;border-bottom: 1px solid #000;display: inline-block;margin-left: 5px;\">&nbsp;</div></td>\n" +
                        "                       <td style=\"text-align: center;\">cod. ABI <div style=\"width: 90px;height: 14px;border-bottom: 1px solid #000;display: inline-block;margin-left: 5px;\">&nbsp;</div></td>\n" +
                        "                       <td style=\"text-align: center;\">CAB <div style=\"width: 90px;height: 14px;border-bottom: 1px solid #000;display: inline-block;margin-left: 5px;\">&nbsp;</div></td>\n" +
                        "                       <td style=\"text-align: center;\">firma <div style=\"width: 90px;height: 14px;border-bottom: 1px solid #000;display: inline-block;margin-left: 5px;\">&nbsp;</div></td>\n"
                        : "                     <td width=\"100%\" height=\"30px\">&nbsp;</td>\n") +
                "                   </tr>\n" +
                "               </tbody>\n" +
                "            </table>\n" +
                "        </div>\n" +
                "        <div style=\"text-align: center;font-size: 12px;\">" + footer + "</div>";
        return page;
    }

    private String getRequestSpecialFormalityRow(ImportF24Pdf importF24, Request transcriptionRequest) {
        StringBuilder sb = new StringBuilder();
        String f24_identification_number = "";
        String tdStart = "<td style=\"background-color: #fff;padding: 0 3px;border-right: 1px solid #40B6D1;\">";
        String tdEnd = "</td>\n";
        if (importF24 != null && importF24.getF24IdentificationNumber() != null)
            f24_identification_number = importF24.getF24IdentificationNumber();
        sb.append("<tr style=\"border-bottom: 1px solid #40B6D1;\">");
        sb.append("\n");
        sb.append("<td style=\"background-color: #fff;padding: 0 5px;border-right: 15px solid #DAEEF6;\">" + (importF24 != null && importF24.getType() != null ? importF24.getType() : "&nbsp;") + "</td>\n");

        char[] f24_identification_number_arr = f24_identification_number.toCharArray();
        for (int i = 0; i < 16; i++) {
            sb.append(tdStart);
            sb.append(i < f24_identification_number_arr.length ? f24_identification_number_arr[i] : "&nbsp;");
            sb.append(tdEnd);
        }

        sb.append("<td style=\"background-color: #fff;padding: 0 3px;\">&nbsp;</td>\n");
        sb.append("<td width=\"10%\" style=\"background-color: #fff;padding: 0 5px;border-left: 8px solid #DAEEF6;\">" + (importF24 != null ? importF24.getCode() : "&nbsp;") + "</td>\n");
        sb.append("<td width=\"10%\" style=\"background-color: #fff;padding: 0 5px;border-left: 8px solid #DAEEF6;\">\n");
        if (importF24 != null && importF24.getReferenceYear() != null) {
            sb.append(importF24.getReferenceYear());
        } else {
            sb.append("&nbsp;");
        }
        sb.append("</td>\n");
        sb.append("<td width=\"30%\" style=\"background-color: #fff;padding: 0 10px;border-left: 8px solid #DAEEF6;text-align: right;\">");
        if (importF24 != null && !ValidationHelper.isNullOrEmpty(importF24.getPercentage()) && importF24.getPercentage().doubleValue() > 0.0) {
            if (!ValidationHelper.isNullOrEmpty(transcriptionRequest.getMortagageImport()) && transcriptionRequest.getMortagageImport().doubleValue() > 0.0) {
                sb.append(InvoiceHelper.format(transcriptionRequest.getMortagageImport().doubleValue() * importF24.getPercentage().doubleValue() / 100));
            }
        } else if (importF24 != null && importF24.getF24Import() != null && Double.valueOf(importF24.getF24Import()) > 0) {
            sb.append(InvoiceHelper.format(Double.valueOf(importF24.getF24Import())));
        } else {
            sb.append("&nbsp;");
        }
        sb.append("</td>\n");
        sb.append("</tr>\n");
        return sb.toString();
//                "                        <tr style=\"border-bottom: 1px solid #40B6D1;\">\n" +
//                "                            <td style=\"background-color: #fff;padding: 0 5px;border-right: 15px solid #DAEEF6;\">R</td>\n" +
//                "                            <td style=\"background-color: #fff;padding: 0 3px;border-right: 1px solid #40B6D1;\">" + f24_identification_number + "</td>\n" +
//                "                            <td style=\"background-color: #fff;padding: 0 3px;border-right: 1px solid #40B6D1;\">&nbsp;</td>\n" +
//                "                            <td style=\"background-color: #fff;padding: 0 3px;border-right: 1px solid #40B6D1;\">&nbsp;</td>\n" +
//                "                            <td style=\"background-color: #fff;padding: 0 3px;border-right: 1px solid #40B6D1;\">&nbsp;</td>\n" +
//                "                            <td style=\"background-color: #fff;padding: 0 3px;border-right: 1px solid #40B6D1;\">&nbsp;</td>\n" +
//                "                            <td style=\"background-color: #fff;padding: 0 3px;border-right: 1px solid #40B6D1;\">&nbsp;</td>\n" +
//                "                            <td style=\"background-color: #fff;padding: 0 3px;border-right: 1px solid #40B6D1;\">&nbsp;</td>\n" +
//                "                            <td style=\"background-color: #fff;padding: 0 3px;border-right: 1px solid #40B6D1;\">&nbsp;</td>\n" +
//                "                            <td style=\"background-color: #fff;padding: 0 3px;border-right: 1px solid #40B6D1;\">&nbsp;</td>\n" +
//                "                            <td style=\"background-color: #fff;padding: 0 3px;border-right: 1px solid #40B6D1;\">&nbsp;</td>\n" +
//                "                            <td style=\"background-color: #fff;padding: 0 3px;border-right: 1px solid #40B6D1;\">&nbsp;</td>\n" +
//                "                            <td style=\"background-color: #fff;padding: 0 3px;border-right: 1px solid #40B6D1;\">&nbsp;</td>\n" +
//                "                            <td style=\"background-color: #fff;padding: 0 3px;border-right: 1px solid #40B6D1;\">&nbsp;</td>\n" +
//                "                            <td style=\"background-color: #fff;padding: 0 3px;border-right: 1px solid #40B6D1;\">&nbsp;</td>\n" +
//                "                            <td style=\"background-color: #fff;padding: 0 3px;border-right: 1px solid #40B6D1;\">&nbsp;</td>\n" +
//                "                            <td style=\"background-color: #fff;padding: 0 3px;border-right: 1px solid #40B6D1;\">&nbsp;</td>\n" +


//                "                            <td style=\"background-color: #fff;padding: 0 3px;\">&nbsp;</td>\n" +
//                "                            <td width=\"10%\" style=\"background-color: #fff;padding: 0 10px;border-left: 8px solid #DAEEF6;\">&nbsp;</td>\n" +
//                "                            <td width=\"10%\" style=\"background-color: #fff;padding: 0 10px;border-left: 8px solid #DAEEF6;\">&nbsp;</td>\n" +
//                "                            <td width=\"30%\" style=\"background-color: #fff;padding: 0 10px;border-left: 8px solid #DAEEF6;\">&nbsp;</td>\n" +
//                "                        </tr>\n";
    }


    private void saveExtraCost(Request certificationRequest, ExtraCost newCost, Double price, ExtraCostType extraCostType)
            throws PersistenceBeanException {
        if (newCost == null) {
            newCost = new ExtraCost();
            if (extraCostType.equals(ExtraCostType.IPOTECARIO))
                newCost.setNote("Sintetico");
            else if (extraCostType.equals(ExtraCostType.POSTALE))
                newCost.setNote("Spese postali");
        }
        newCost.setCertification(Boolean.TRUE);
        newCost.setPrice(price);
        newCost.setType(extraCostType);
        newCost.setRequestId(certificationRequest.getId());
        DaoManager.save(newCost);
    }

    public CertificationData saveCertification(Request certificationRequest, CertificationData certificationDataEntity, Double cadastralCost,
                                               Double mortgageCost, Double personalCost, Double postalCost, String cadastralFileName, Document cadastralDoc,
                                               byte[] cadastralDocumentContents, String mapFileName, Document mapDoc, byte[] mapDocumentContents,
                                               String signedCertificationFileName, Document signedCertificationDocument, byte[] signedCertificationDocumentContents) {
        Transaction tr = null;
        try {
            tr = DaoManager.getSession().beginTransaction();
            if (!ValidationHelper.isNullOrEmpty(certificationRequest)) {
                List<ExtraCost> extraCosts = DaoManager.load(ExtraCost.class, new Criterion[]{
                        Restrictions.eq("requestId", certificationRequest.getId()),
                        Restrictions.eq("type", ExtraCostType.CATASTO)});

                if (!ValidationHelper.isNullOrEmpty(cadastralCost)) {
                    if (ValidationHelper.isNullOrEmpty(extraCosts)) {
                        saveExtraCost(certificationRequest, null, cadastralCost, ExtraCostType.CATASTO);
                    } else {
                        saveExtraCost(certificationRequest, extraCosts.get(0), cadastralCost, ExtraCostType.CATASTO);
                    }
                } else if (!ValidationHelper.isNullOrEmpty(extraCosts)) {
                    saveExtraCost(certificationRequest, null, cadastralCost, ExtraCostType.CATASTO);
                }

                extraCosts = DaoManager.load(ExtraCost.class, new Criterion[]{
                        Restrictions.eq("requestId", certificationRequest.getId()),
                        Restrictions.eq("type", ExtraCostType.IPOTECARIO)});

                if (!ValidationHelper.isNullOrEmpty(mortgageCost)) {
                    if (ValidationHelper.isNullOrEmpty(extraCosts)) {
                        saveExtraCost(certificationRequest, null, mortgageCost, ExtraCostType.IPOTECARIO);
                    } else {
                        saveExtraCost(certificationRequest, extraCosts.get(0), mortgageCost, ExtraCostType.IPOTECARIO);
                    }
                } else if (!ValidationHelper.isNullOrEmpty(extraCosts)) {
                    saveExtraCost(certificationRequest, null, mortgageCost, ExtraCostType.IPOTECARIO);
                }

                extraCosts = DaoManager.load(ExtraCost.class, new Criterion[]{
                        Restrictions.eq("requestId", certificationRequest.getId()),
                        Restrictions.eq("type", ExtraCostType.ANAGRAFICO)});

                if (!ValidationHelper.isNullOrEmpty(personalCost)) {
                    if (ValidationHelper.isNullOrEmpty(extraCosts)) {
                        saveExtraCost(certificationRequest, null, personalCost, ExtraCostType.ANAGRAFICO);
                    } else {
                        saveExtraCost(certificationRequest, extraCosts.get(0), personalCost, ExtraCostType.ANAGRAFICO);
                    }
                } else if (!ValidationHelper.isNullOrEmpty(extraCosts)) {
                    saveExtraCost(certificationRequest, null, personalCost, ExtraCostType.ANAGRAFICO);
                }

                extraCosts = DaoManager.load(ExtraCost.class, new Criterion[]{
                        Restrictions.eq("requestId", certificationRequest.getId()),
                        Restrictions.eq("type", ExtraCostType.POSTALE)});

                if (!ValidationHelper.isNullOrEmpty(postalCost)) {
                    if (ValidationHelper.isNullOrEmpty(extraCosts)) {
                        saveExtraCost(certificationRequest, null, postalCost, ExtraCostType.POSTALE);
                    } else {
                        saveExtraCost(certificationRequest, extraCosts.get(0), postalCost, ExtraCostType.POSTALE);
                    }
                } else if (!ValidationHelper.isNullOrEmpty(extraCosts)) {
                    saveExtraCost(certificationRequest, null, postalCost, ExtraCostType.POSTALE);
                }
            }
            DaoManager.save(certificationDataEntity);
            if (!ValidationHelper.isNullOrEmpty(cadastralDocumentContents)) {
                Document cadastralDocument = handleDocumentCertification("cadastral", certificationRequest, certificationDataEntity,
                        cadastralFileName, cadastralDocumentContents, mapFileName,
                        mapDocumentContents, signedCertificationFileName, signedCertificationDocumentContents);
                certificationDataEntity.setCadastralDocument(cadastralDocument);
            } else if (!ValidationHelper.isNullOrEmpty(cadastralDoc)) {
                File cadastralDocument = new File(cadastralDoc.getPath());
                if (StringUtils.isNotBlank(cadastralDoc.getPath()) && cadastralDocument.exists()) {
                    FileHelper.delete(cadastralDocument);
                }
            }
            /*if (!ValidationHelper.isNullOrEmpty(mapDocumentContents)) {
                Document mapDocument = handleDocumentCertification("map", certificationRequest, certificationDataEntity,
                        cadastralFileName, cadastralDocumentContents, mapFileName,
                        mapDocumentContents, signedCertificationFileName, signedCertificationDocumentContents);
                certificationDataEntity.setMapDocument(mapDocument);
            } else if (!ValidationHelper.isNullOrEmpty(mapDoc)) {
                File mapDocument = new File(mapDoc.getPath());
                if (StringUtils.isNotBlank(mapDoc.getPath()) && mapDocument.exists()) {
                    FileHelper.delete(mapDocument);
                }
            }*/
            if (!ValidationHelper.isNullOrEmpty(signedCertificationDocumentContents)) {
                Document certificateDocument = handleDocumentCertification("certification", certificationRequest, certificationDataEntity,
                        cadastralFileName, cadastralDocumentContents, mapFileName,
                        mapDocumentContents, signedCertificationFileName, signedCertificationDocumentContents);
                certificationDataEntity.setSignedCertificationDocument(certificateDocument);
            } else if (!ValidationHelper.isNullOrEmpty(signedCertificationDocument)) {
                File certificateDocument = new File(signedCertificationDocument.getPath());
                if (StringUtils.isNotBlank(signedCertificationDocument.getPath()) && certificateDocument.exists()) {
                    FileHelper.delete(certificateDocument);
                }
            }
            DaoManager.save(certificationDataEntity);
            /*if (isNew) {
            	if(ValidationHelper.isNullOrEmpty(getTranscriptionDataEntityId()))
            		RedirectHelper.goToTranscriptionCertification(PageTypes.TRANSCRIPTION_CERTIFICATION_MANAGEMENT, getCertificationRequest().getId(), null, getCertificationDataEntity().getId());
            	else
            		RedirectHelper.goToTranscriptionCertification(PageTypes.TRANSCRIPTION_CERTIFICATION_MANAGEMENT, getCertificationRequest().getId(), getTranscriptionDataEntityId(), getCertificationDataEntity().getId());
            }*/
        } catch (Exception e) {
            if (tr != null) {
                tr.rollback();
            }
            LogHelper.log(log, e);
            MessageHelper.addGlobalMessage(FacesMessage.SEVERITY_ERROR, "",
                    ResourcesHelper.getValidation("objectEditedException"));
        } finally {
            if (tr != null && !tr.wasRolledBack() && tr.isActive()) {
                try {
                    tr.commit();
                } catch (StaleObjectStateException e) {
                    MessageHelper
                            .addGlobalMessage(
                                    FacesMessage.SEVERITY_ERROR,
                                    "",
                                    ResourcesHelper
                                            .getValidation("exceptionOccuredWhileSaving"));
                    LogHelper.log(log, e);
                } catch (Exception e) {
                    LogHelper.log(log, e);
                    e.printStackTrace();
                }
            }
        }
        try {
            getCostManipulationHelper().setCostNote(null);
            getCostManipulationHelper().performCostCalculation(certificationRequest, true);
        } catch (Exception e) {
            LogHelper.log(log, e);
            e.printStackTrace();
        }
        return certificationDataEntity;
    }

    private Document handleDocumentCertification(String directory, Request certificationRequest, CertificationData certificationDataEntity,
                                                 String cadastralFileName, byte[] cadastralDocumentContents, String mapFileName, byte[] mapDocumentContents,
                                                 String signedCertificationFileName, byte[] signedCertificationDocumentContents) {
        Document document = null;
        try {
            String path = FileHelper.getTranscriptionDocumentSavePath(certificationDataEntity.getId(), directory, "certification");
            File filePath = new File(path);
            if (directory.equalsIgnoreCase("cadastral")) {
                FileHelper.writeFileToFolder(cadastralFileName, filePath, cadastralDocumentContents);
                if (ValidationHelper.isNullOrEmpty(certificationDataEntity.getCadastralDocument()))
                    document = new Document();
                else
                    document = certificationDataEntity.getCadastralDocument();
                document.setTitle(FileHelper.getFileNameWOExtension(cadastralFileName));
                document.setPath(path + cadastralFileName);
                document.setTypeId(DocumentType.CADASTRAL.getId());
            } /*else if (directory.equalsIgnoreCase("map")) {
                FileHelper.writeFileToFolder(mapFileName, filePath, mapDocumentContents);
                if (ValidationHelper.isNullOrEmpty(certificationDataEntity.getMapDocument()))
                    document = new Document();
                else
                    document = certificationDataEntity.getMapDocument();
                document.setTitle(FileHelper.getFileNameWOExtension(mapFileName));
                document.setPath(path + mapFileName);
                document.setTypeId(DocumentType.ESTRATTO_MAPPA.getId());

            }*/
            else if (directory.equalsIgnoreCase("certification")) {
                FileHelper.writeFileToFolder(signedCertificationFileName, filePath, signedCertificationDocumentContents);
                if (ValidationHelper.isNullOrEmpty(certificationDataEntity.getSignedCertificationDocument()))
                    document = new Document();
                else
                    document = certificationDataEntity.getSignedCertificationDocument();
                document.setTitle(FileHelper.getFileNameWOExtension(signedCertificationFileName));
                document.setPath(path + signedCertificationFileName);
                document.setTypeId(DocumentType.SIGNED_CERTIFICATE.getId());
            }
            document.setRequest(certificationRequest);

            document.setDate(new Date());
            DaoManager.save(document);

        } catch (Exception e) {
            LogHelper.log(log, e);
        }
        return document;
    }

    public byte[] getXlsBytesReportSpesePdf(Request transcriptionRequest) {
        byte[] excelFile = null;
        try {
        	ExcelDataWrapper excelDataWrapper = populateExcelDataWrapper(transcriptionRequest);
            List<Request> filteredRequests = new ArrayList<>();
            List<Request> transcriptionRequests = new ArrayList<>();
            transcriptionRequests.add(transcriptionRequest);
            excelDataWrapper.setShowReport(Boolean.FALSE);
            excelFile = new CreateExcelRequestsReportHelper(true).convertMailUserDataToExcel(
                    filteredRequests, transcriptionRequests, document, excelDataWrapper);
        } catch (Exception e) {
            LogHelper.log(log, e);
        }
        return excelFile;
    }

    private void fillSelectedClientManagers() {
        setSelectedClientManagers(new ArrayList<>());
        if (!ValidationHelper.isNullOrEmpty(getMail().getManagers())) {
            for (Client item : getMail().getManagers()) {
                SelectItemWrapper<Client> selectItem = new SelectItemWrapper<>(item);
                getSelectedClientManagers().add(selectItem);
            }
        }
    }

    public void openTranscriptionManagement(Long requestId) throws PersistenceBeanException,
            InstantiationException, IllegalAccessException {
        Request request = DaoManager.get(Request.class,
                new Criterion[]{Restrictions.eq("id", requestId)
                });

        if (!ValidationHelper.isNullOrEmpty(request) && !ValidationHelper.isNullOrEmpty(request.getService())) {
            if(!ValidationHelper.isNullOrEmpty(request.getService().getManageRenewal())
                    && request.getService().getManageRenewal()){
                TranscriptionData transcriptionData = DaoManager.get(TranscriptionData.class,
                        new CriteriaAlias[]{
                                new CriteriaAlias("request", "r", JoinType.INNER_JOIN)
                        },
                        new Criterion[]{Restrictions.eq("request.id", requestId)
                        });

                if (!ValidationHelper.isNullOrEmpty(transcriptionData)) {
                    RedirectHelper.goToTranscriptionCertification(PageTypes.TRANSCRIPTION_CERTIFICATION_MANAGEMENT, TranscriptionPage.RENEWAL, requestId, transcriptionData.getId(), null);
                } else {
                    RedirectHelper.goToTranscriptionCertification(PageTypes.TRANSCRIPTION_CERTIFICATION_MANAGEMENT, TranscriptionPage.RENEWAL, requestId, null, null);
                }
            }else  if (!ValidationHelper.isNullOrEmpty(request.getService().getManageTranscription())
                    && request.getService().getManageTranscription()
                    && !ValidationHelper.isNullOrEmpty(request.getService().getManageCertification())
                    && request.getService().getManageCertification()) {
                TranscriptionData transcriptionData = DaoManager.get(TranscriptionData.class,
                        new CriteriaAlias[]{
                                new CriteriaAlias("request", "r", JoinType.INNER_JOIN)
                        },
                        new Criterion[]{Restrictions.eq("request.id", requestId)
                        });

                CertificationData certificationData = DaoManager.get(CertificationData.class,
                        new CriteriaAlias[]{
                                new CriteriaAlias("request", "r", JoinType.INNER_JOIN)
                        },
                        new Criterion[]{Restrictions.eq("request.id", requestId)
                        });

                if (!ValidationHelper.isNullOrEmpty(transcriptionData) && !ValidationHelper.isNullOrEmpty(certificationData)) {
                    RedirectHelper.goToTranscriptionCertification(PageTypes.TRANSCRIPTION_CERTIFICATION_MANAGEMENT, TranscriptionPage.TRANSCRIPTIONCERTIFICATION,
                            requestId, transcriptionData.getId(), certificationData.getId());
                } else if (!ValidationHelper.isNullOrEmpty(transcriptionData) && ValidationHelper.isNullOrEmpty(certificationData)) {
                    RedirectHelper.goToTranscriptionCertification(PageTypes.TRANSCRIPTION_CERTIFICATION_MANAGEMENT, TranscriptionPage.TRANSCRIPTIONCERTIFICATION,
                            requestId, transcriptionData.getId(), null);
                } else if (ValidationHelper.isNullOrEmpty(transcriptionData) && !ValidationHelper.isNullOrEmpty(certificationData)) {
                    RedirectHelper.goToTranscriptionCertification(PageTypes.TRANSCRIPTION_CERTIFICATION_MANAGEMENT, TranscriptionPage.TRANSCRIPTIONCERTIFICATION,
                            requestId, null, certificationData.getId());
                } else if (ValidationHelper.isNullOrEmpty(transcriptionData) && ValidationHelper.isNullOrEmpty(certificationData)) {
                    RedirectHelper.goToTranscriptionCertification(PageTypes.TRANSCRIPTION_CERTIFICATION_MANAGEMENT, TranscriptionPage.TRANSCRIPTIONCERTIFICATION,
                            requestId, null, null);
                }
            } else if (!ValidationHelper.isNullOrEmpty(request.getService().getManageTranscription())
                    && request.getService().getManageTranscription()) {
                TranscriptionData transcriptionData = DaoManager.get(TranscriptionData.class,
                        new CriteriaAlias[]{
                                new CriteriaAlias("request", "r", JoinType.INNER_JOIN)
                        },
                        new Criterion[]{Restrictions.eq("request.id", requestId)
                        });

                if (!ValidationHelper.isNullOrEmpty(transcriptionData)) {
                    RedirectHelper.goToTranscriptionCertification(PageTypes.TRANSCRIPTION_CERTIFICATION_MANAGEMENT, TranscriptionPage.TRANSCRIPTION, requestId, transcriptionData.getId(), null);
                } else {
                    RedirectHelper.goToTranscriptionCertification(PageTypes.TRANSCRIPTION_CERTIFICATION_MANAGEMENT, TranscriptionPage.TRANSCRIPTION, requestId, null, null);
                }
            } else if (!ValidationHelper.isNullOrEmpty(request.getService().getManageCertification())
                    && request.getService().getManageCertification()) {
                CertificationData certificationData = DaoManager.get(CertificationData.class,
                        new CriteriaAlias[]{
                                new CriteriaAlias("request", "r", JoinType.INNER_JOIN)
                        },
                        new Criterion[]{Restrictions.eq("request.id", requestId)
                        });
                if (!ValidationHelper.isNullOrEmpty(certificationData)) {
                    RedirectHelper.goToTranscriptionCertification(PageTypes.TRANSCRIPTION_CERTIFICATION_MANAGEMENT, TranscriptionPage.CERTIFICATION, requestId, null, certificationData.getId());
                } else {
                    RedirectHelper.goToTranscriptionCertification(PageTypes.TRANSCRIPTION_CERTIFICATION_MANAGEMENT, TranscriptionPage.CERTIFICATION, requestId, null, null);
                }
            }
        }
    }

    public Boolean checkTranscriptionCertificationExists(Request request) throws HibernateException, InstantiationException, IllegalAccessException, PersistenceBeanException {
    	/*Request request = DaoManager.get(Request.class,
                new Criterion[]{Restrictions.eq("id", requestId)
                });*/

        if (!ValidationHelper.isNullOrEmpty(request) && !ValidationHelper.isNullOrEmpty(request.getService())) {
            if (!ValidationHelper.isNullOrEmpty(request.getService().getManageTranscription())
                    && request.getService().getManageTranscription()
                    && !ValidationHelper.isNullOrEmpty(request.getService().getManageCertification())
                    && request.getService().getManageCertification()) {
                return Boolean.TRUE;
            } else if (!ValidationHelper.isNullOrEmpty(request.getService().getManageTranscription())
                    && request.getService().getManageTranscription()) {
                return Boolean.TRUE;
            } else if (!ValidationHelper.isNullOrEmpty(request.getService().getManageCertification())
                    && request.getService().getManageCertification()) {
                return Boolean.TRUE;
            }
        }
        return false;
    }
    
    public ExcelDataWrapper populateExcelDataWrapper(Request request) throws HibernateException, InstantiationException, IllegalAccessException, PersistenceBeanException {
    	ExcelDataWrapper excelDataWrapper = new ExcelDataWrapper();
        if (!ValidationHelper.isNullOrEmpty(request.getMail()) && !ValidationHelper.isNullOrEmpty(request.getMail().getId())) {
            setMail(request.getMail());
            Document document = DaoManager.get(Document.class, new Criterion[]{
                    Restrictions.eq("mail.id", getMail().getId())});
            if (!ValidationHelper.isNullOrEmpty(document)) {
                excelDataWrapper.setReportn(document.getReportNumber());
                excelDataWrapper.setFatturan(document.getInvoiceNumber());
                excelDataWrapper.setData((document == null || document.getInvoiceDate() == null ? DateTimeHelper.getNow() : document.getInvoiceDate()));
                if (!ValidationHelper.isNullOrEmpty(document) && !ValidationHelper.isNullOrEmpty(document.getNote()))
                    excelDataWrapper.setDocumentNote(document.getNote());
                setDocument(document);
            }
            excelDataWrapper.setNdg(getMail().getNdg());
            excelDataWrapper.setReferenceRequest(getMail().getReferenceRequest());
            if (!ValidationHelper.isNullOrEmpty(getMail().getClientInvoice())) {
                excelDataWrapper.setClientInvoice(DaoManager.get(Client.class, getMail().getClientInvoice().getId()));
            }

            fillSelectedClientManagers();

            if (!ValidationHelper.isNullOrEmpty(getSelectedClientManagers())) {
                excelDataWrapper.setManagers(new ArrayList<>());
                List<Client> clients = DaoManager.load(Client.class, new Criterion[]{
                        Restrictions.in("id", getSelectedClientManagers().stream()
                                .map(SelectItemWrapper::getId).collect(Collectors.toList()))});
                if (!ValidationHelper.isNullOrEmpty(clients)) {
                    excelDataWrapper.setManagers(clients);
                }
            }
            if (!ValidationHelper.isNullOrEmpty(getMail().getClientFiduciary())) {
                excelDataWrapper.setClientFiduciary(DaoManager.get(Client.class, getMail().getClientFiduciary().getId()));
            }
            excelDataWrapper.setFiduciary(getMail().getFiduciary());
            if (!ValidationHelper.isNullOrEmpty(getMail().getOffice())) {
                excelDataWrapper.setOffice(getMail().getOffice().getDescription());
            } else {
                if (!ValidationHelper.isNullOrEmpty(getMail()))
                    getMail().setOffice(null);
                excelDataWrapper.setOffice(null);
            }
        }
        return excelDataWrapper;
    }

}
