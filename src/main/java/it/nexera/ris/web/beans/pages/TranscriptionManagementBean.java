package it.nexera.ris.web.beans.pages;

import it.nexera.ris.common.enums.*;
import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.helpers.*;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.domain.*;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.ImportF24;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.ImportF24Pdf;
import it.nexera.ris.settings.ApplicationSettingsHolder;
import it.nexera.ris.web.beans.EntityViewPageBean;
import lombok.Getter;
import lombok.Setter;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.HibernateException;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.StreamedContent;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.model.SelectItem;

import java.io.*;
import java.util.*;

@Setter
@Getter
@ManagedBean
@ViewScoped
public class TranscriptionManagementBean extends
        EntityViewPageBean<TranscriptionData> implements Serializable {

    private static final long serialVersionUID = 5504028298870066325L;
    private int activeTabIndex;
    private Request transcriptionRequest;
    private String courierFileName;
    private byte[] courierDocumentContents;
    private String entryFileName;
    private byte[] entryDocumentContents;
    private Document courierDocument;
    private Document entryDocument;
    private StreamedContent pdfOfLetterFile;
    private Double ipotecarioCost;
    private Double ipotecarioPostale;
    private CostManipulationHelper costManipulationHelper;
    private Boolean disableStampFields;
    private TranscriptionAndCertificationHelper transcriptionAndCertificationHelper;
    private List<SelectItem> supportTypes;
    private List<ImportF24Pdf> importF24Pdfs;
    private ImportF24Pdf importF24Pdf = new ImportF24Pdf();
    int editedRow = -1;
    private Integer stampValue;

    /*@Override
    protected void preLoad() throws PersistenceBeanException, IllegalAccessException, InstantiationException {
        String referrentId = getRequestParameter(RedirectHelper.PARENT_ID_PARAMETER);
        if(StringUtils.isNotBlank(referrentId)){
            setTranscriptionRequest(DaoManager.get(Request.class, Long.parseLong(referrentId)));
        }else
            setTranscriptionRequest(null);
    }*/

    @Override
    public void onLoad() throws NumberFormatException, HibernateException, PersistenceBeanException, InstantiationException, IllegalAccessException, IOException {
        /*if(ValidationHelper.isNullOrEmpty(getEntity().getCourierEnvelopeDate()))
            getEntity().setCourierEnvelopeDate(new Date());
        if(getEntity().isNew())
            getEntity().setRequest(getTranscriptionRequest());
        setDisableStampFields(Boolean.FALSE);
        if(!ValidationHelper.isNullOrEmpty(getTranscriptionRequest())){
            if(ValidationHelper.isNullOrEmpty(getTranscriptionRequest().getAggregationLandChargesRegistry()) ||
                    ValidationHelper.isNullOrEmpty(getTranscriptionRequest().getAggregationLandChargesRegistry().getStamp()) ||
                !getTranscriptionRequest().getAggregationLandChargesRegistry().getStamp()){
                setDisableStampFields(Boolean.TRUE);
            }
        }
        if(!ValidationHelper.isNullOrEmpty(getEntity().getCourierDocument())){
            setCourierFileName(FileHelper.getFileName(getEntity().getCourierDocument().getPath()));
            setCourierDocumentContents(FileHelper.loadContentByPath(getEntity().getCourierDocument().getPath()));
            setCourierDocument(getEntity().getCourierDocument());
        }else {
            setCourierFileName(null);
            setCourierDocumentContents(null);
            setCourierDocument(null);
        }

        if(!ValidationHelper.isNullOrEmpty(getEntity().getEntryDocument())){
            setEntryFileName(FileHelper.getFileName(getEntity().getEntryDocument().getPath()));
            setEntryDocumentContents(FileHelper.loadContentByPath(getEntity().getEntryDocument().getPath()));
            setEntryDocument(getEntity().getEntryDocument());
        }else {
            setEntryFileName(null);
            setEntryDocumentContents(null);
            setEntryDocument(null);
        }
        List<ExtraCost> extraCosts = DaoManager.load(ExtraCost.class, new Criterion[]{
                Restrictions.eq("requestId", getTranscriptionRequest().getId()),
                Restrictions.eq("type", ExtraCostType.IPOTECARIO)});

        if(!ValidationHelper.isNullOrEmpty(extraCosts)){
            setIpotecarioCost(extraCosts.get(0).getPrice());
        }
        extraCosts = DaoManager.load(ExtraCost.class, new Criterion[]{
                Restrictions.eq("requestId", getTranscriptionRequest().getId()),
                Restrictions.eq("type", ExtraCostType.POSTALE)});

        if(!ValidationHelper.isNullOrEmpty(extraCosts)){
            setIpotecarioPostale(extraCosts.get(0).getPrice());
        }

        setCostManipulationHelper(new CostManipulationHelper());
        getCostManipulationHelper().setMortgageTypeList(ComboboxHelper.fillList(MortgageType.class, false, false));
        if (!ValidationHelper.isNullOrEmpty(getTranscriptionRequest().getClient()) &&
                !ValidationHelper.isNullOrEmpty(getTranscriptionRequest().getClient().getCostOutput())) {
            this.getCostManipulationHelper().setCostOutput(getTranscriptionRequest().getClient().getCostOutput());
        } else {
            this.getCostManipulationHelper().setCostOutput(false);
        }
        setTranscriptionAndCertificationHelper(new TranscriptionAndCertificationHelper());
        setSupportTypes(ComboboxHelper.fillList(SupportType.class, false));

        loadImportF24PdfData();
        int stampValue = 0;
        if(!ValidationHelper.isNullOrEmpty(getEntity().getNumberRevenueStamp()) && !ValidationHelper.isNullOrEmpty(getEntity().getNumberRevenueStamp())) {
        	stampValue = (getEntity().getNumberRevenueStamp().intValue() * 8) + (getEntity().getNumberServiceStamp().intValue() * 10);
        } else if(ValidationHelper.isNullOrEmpty(getEntity().getNumberRevenueStamp()) && !ValidationHelper.isNullOrEmpty(getEntity().getNumberRevenueStamp())) {
        	stampValue = 0 + (getEntity().getNumberServiceStamp().intValue() * 10);
        } else if(!ValidationHelper.isNullOrEmpty(getEntity().getNumberRevenueStamp()) && ValidationHelper.isNullOrEmpty(getEntity().getNumberRevenueStamp())) {
        	stampValue = (getEntity().getNumberRevenueStamp().intValue() * 8) + 0;
        }
        setStampValue(stampValue);*/
    }

    /*private void saveExtraCost(ExtraCost newCost, Double price, ExtraCostType extraCostType, String note)
            throws PersistenceBeanException {
        if(newCost == null){
            newCost = new ExtraCost();
            newCost.setNote(note);
        }
        newCost.setPrice(price);
        newCost.setType(extraCostType);
        newCost.setRequestId(getTranscriptionRequest().getId());
        DaoManager.save(newCost);
    }*/
    
    /*public void saveTranscription() {

        Transaction tr = null;
        boolean isNew = this.getEntity().isNew();
        try {
            tr = DaoManager.getSession().beginTransaction();
            if(!ValidationHelper.isNullOrEmpty(getTranscriptionRequest())){
                List<ExtraCost> extraCosts = DaoManager.load(ExtraCost.class, new Criterion[]{
                        Restrictions.eq("requestId", getTranscriptionRequest().getId()),
                        Restrictions.eq("type", ExtraCostType.IPOTECARIO)});

                if(!ValidationHelper.isNullOrEmpty(getIpotecarioCost())){
                    if(ValidationHelper.isNullOrEmpty(extraCosts)){
                        saveExtraCost(null, getIpotecarioCost(), ExtraCostType.IPOTECARIO, "Sintetico");
                    }else {
                        saveExtraCost(extraCosts.get(0), getIpotecarioCost(), ExtraCostType.IPOTECARIO, "Sintetico");
                    }
                }else if(!ValidationHelper.isNullOrEmpty(extraCosts)){
                    saveExtraCost(null, getIpotecarioCost(), ExtraCostType.IPOTECARIO, "Sintetico");
                }

                extraCosts = DaoManager.load(ExtraCost.class, new Criterion[]{
                        Restrictions.eq("requestId", getTranscriptionRequest().getId()),
                        Restrictions.eq("type", ExtraCostType.POSTALE)});

                if(!ValidationHelper.isNullOrEmpty(getIpotecarioPostale())){
                    if(ValidationHelper.isNullOrEmpty(extraCosts)){
                        saveExtraCost(null, getIpotecarioPostale(), ExtraCostType.POSTALE, "Spese postali");
                    }else {
                        saveExtraCost(extraCosts.get(0), getIpotecarioPostale(), ExtraCostType.POSTALE, "Spese postali");
                    }
                }else if(!ValidationHelper.isNullOrEmpty(extraCosts)){
                    saveExtraCost(null, getIpotecarioPostale(), ExtraCostType.POSTALE, "Spese postali");
                }
                extraCosts = DaoManager.load(ExtraCost.class, new Criterion[]{
                        Restrictions.eq("requestId", getTranscriptionRequest().getId()),
                        Restrictions.eq("type", ExtraCostType.MARCA),
                        Restrictions.eq("note", "Marca da bollo").ignoreCase()});

                if(!ValidationHelper.isNullOrEmpty(getEntity().getNumberRevenueStamp())){
                    if(ValidationHelper.isNullOrEmpty(extraCosts)){
                        saveExtraCost(null, getEntity().getNumberRevenueStamp() * 8.0, ExtraCostType.MARCA, "Marca da bollo");
                    }else {
                        saveExtraCost(extraCosts.get(0), getEntity().getNumberRevenueStamp() * 8.0, ExtraCostType.MARCA, "Marca da bollo");
                    }
                }else if(!ValidationHelper.isNullOrEmpty(extraCosts)){
                    saveExtraCost(null, getEntity().getNumberRevenueStamp() * 8.0, ExtraCostType.MARCA, "Marca da bollo");
                }

                extraCosts = DaoManager.load(ExtraCost.class, new Criterion[]{
                        Restrictions.eq("requestId", getTranscriptionRequest().getId()),
                        Restrictions.eq("type", ExtraCostType.MARCA),
                        Restrictions.eq("note", "Marca servizio").ignoreCase()});

                if(!ValidationHelper.isNullOrEmpty(getEntity().getNumberServiceStamp())){
                    if(ValidationHelper.isNullOrEmpty(extraCosts)){
                        saveExtraCost(null, getEntity().getNumberServiceStamp() * 10.0, ExtraCostType.MARCA, "Marca servizio");
                    }else {
                        saveExtraCost(extraCosts.get(0), getEntity().getNumberServiceStamp() * 10.0, ExtraCostType.MARCA, "Marca servizio");
                    }
                }else if(!ValidationHelper.isNullOrEmpty(extraCosts)){
                    saveExtraCost(null, getEntity().getNumberServiceStamp() * 10.0, ExtraCostType.MARCA, "Marca servizio");
                }
            }
            DaoManager.save(getEntity());
            if(!ValidationHelper.isNullOrEmpty(getCourierDocumentContents())){
                Document courierDocument = handleDocument("courier");
                getEntity().setCourierDocument(courierDocument);
            }else if(!ValidationHelper.isNullOrEmpty(getCourierDocument())){
                File courierDocument =  new File(getCourierDocument().getPath());
                if(StringUtils.isNotBlank(getCourierDocument().getPath()) && courierDocument.exists()){
                    FileHelper.delete(courierDocument);
                }
            }
            if(!ValidationHelper.isNullOrEmpty(getEntryDocumentContents())){
                Document entryDocument = handleDocument("entry");
                getEntity().setEntryDocument(entryDocument);
            }else if(!ValidationHelper.isNullOrEmpty(getEntryDocument())){
                File entryDocument =  new File(getEntryDocument().getPath());
                if(StringUtils.isNotBlank(getEntryDocument().getPath()) && entryDocument.exists()){
                    FileHelper.delete(entryDocument);
                }
            }
            DaoManager.save(getEntity());


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
            getCostManipulationHelper().performCostCalculation(getTranscriptionRequest(), true);

        } catch (Exception e) {
            LogHelper.log(log, e);
            e.printStackTrace();
        }
        if (isNew) {
            RedirectHelper.goTo(PageTypes.TRANSCRIPTION_MANAGEMENT, getTranscriptionRequest().getId(), getEntity().getId());
        }
    }*/
    
   /* public void saveTranscription() {
        boolean isNew = this.getEntity().isNew();
        setEntity(getTranscriptionAndCertificationHelper().saveTranscription(getTranscriptionRequest(), getEntity(), 
        		getIpotecarioCost(), getIpotecarioPostale(), getCourierFileName(), getCourierDocument(), getCourierDocumentContents(), 
        		getEntryFileName(), getEntryDocument(), getEntryDocumentContents()));
        if (isNew) {
        	RedirectHelper.goTo(PageTypes.TRANSCRIPTION_MANAGEMENT, getTranscriptionRequest().getId(), getEntity().getId());
        }
    }*/

    /*private Document handleDocument(String directory){
        Document document = null;
        try {
            String path = FileHelper.getTranscriptionDocumentSavePath(getEntity().getId(), directory, "transcription");
            File filePath = new File(path);
            if(directory.equalsIgnoreCase("courier")){
                FileHelper.writeFileToFolder(getCourierFileName(), filePath, getCourierDocumentContents());
                if(ValidationHelper.isNullOrEmpty(getEntity().getCourierDocument()))
                    document = new Document();
                else
                    document = getEntity().getCourierDocument();
                document.setTitle(FileHelper.getFileNameWOExtension(getCourierFileName()));
                document.setPath(path + getCourierFileName());
                document.setTypeId(DocumentType.COURIER.getId());
            }else{
                FileHelper.writeFileToFolder(getEntryFileName(), filePath, getEntryDocumentContents());
                if(ValidationHelper.isNullOrEmpty(getEntity().getEntryDocument()))
                    document = new Document();
                else
                    document = getEntity().getEntryDocument();
                document.setTitle(FileHelper.getFileNameWOExtension(getEntryFileName()));
                document.setPath(path + getEntryFileName());
                document.setTypeId(DocumentType.ENTRY.getId());
            }
            document.setRequest(getTranscriptionRequest());

            document.setDate(new Date());
            DaoManager.save(document);

        } catch (Exception e) {
            LogHelper.log(log, e);
        }
        return document;
    }*/

    /*public void handleCourierFileUpload(FileUploadEvent event) throws PersistenceBeanException {
        setCourierFileName(event.getFile().getFileName());
        setCourierDocumentContents(event.getFile().getContents());
    }

    public void downloadCourierDocument() {
        if(!ValidationHelper.isNullOrEmpty(getCourierFileName()) && !ValidationHelper.isNullOrEmpty(getCourierDocumentContents())) {
            FileHelper.sendFile(getCourierFileName(), getCourierDocumentContents());
        }
    }

    public void deleteCourierDocument() throws HibernateException, PersistenceBeanException, InstantiationException, IllegalAccessException, NumberFormatException, IOException {
        setCourierFileName(null);
        setCourierDocumentContents(null);
        getEntity().setCourierDocument(null);
    }

    public void handleEntryFileUpload(FileUploadEvent event) throws PersistenceBeanException {
        setEntryFileName(event.getFile().getFileName());
        setEntryDocumentContents(event.getFile().getContents());
    }

    public void downloadEntryDocument() {
        if(!ValidationHelper.isNullOrEmpty(getEntryFileName()) && !ValidationHelper.isNullOrEmpty(getEntryDocumentContents())) {
            FileHelper.sendFile(getEntryFileName(), getEntryDocumentContents());
        }
    }

    public void deleteEntryDocument(){
        setEntryFileName(null);
        setEntryDocumentContents(null);
        getEntity().setEntryDocument(null);
    }*/

    /*public String preparePdfOfLetter() throws IOException, PersistenceBeanException, IllegalAccessException {
        String body = getPdfRequestBody();
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
    }*/
    
    /*public void preparePdfOfLetter() throws IOException, PersistenceBeanException, IllegalAccessException, InstantiationException {
        getTranscriptionAndCertificationHelper().preparePdfOfLetter(getTranscriptionRequest(), getEntity());
    }*/

    /*public String getPdfRequestBody() throws HibernateException, IllegalAccessException, PersistenceBeanException {
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
        if(!ValidationHelper.isNullOrEmpty(getTranscriptionRequest())) {
            if(!ValidationHelper.isNullOrEmpty(getTranscriptionRequest().getTranscriptionActId())) {
                if(!ValidationHelper.isNullOrEmpty(getTranscriptionRequest().getTranscriptionActId().getProvincialOfficeName())) {
                        realEstateName = getTranscriptionRequest().getTranscriptionActId().getProvincialOfficeName();
                } else if(!ValidationHelper.isNullOrEmpty(getTranscriptionRequest().getTranscriptionActId().getReclamePropertyService()) &&
                        !ValidationHelper.isNullOrEmpty(getTranscriptionRequest().getTranscriptionActId().getReclamePropertyService().getName())){
                    reclameServiceName = getTranscriptionRequest().getTranscriptionActId().getReclamePropertyService().getName();
                }
                if(!ValidationHelper.isNullOrEmpty(getTranscriptionRequest().getTranscriptionActId().getType())) {
                    transcriptionActType = getTranscriptionRequest().getTranscriptionActId().getType();
                }
                textInVisura = getTranscriptionRequest().getTranscriptionActId().getDicTypeFormalityAttachmentCText();
            }
           if(StringUtils.isBlank(realEstateName) && StringUtils.isBlank(reclameServiceName) &&
                    !ValidationHelper.isNullOrEmpty(getTranscriptionRequest().getAggregationLandChargesRegistryName())){
                realEstateName = getTranscriptionRequest().getAggregationLandChargesRegistryName();
           }
           if(StringUtils.isBlank(transcriptionActType) && !ValidationHelper.isNullOrEmpty(getTranscriptionRequest().getActType())){
               switch (getTranscriptionRequest().getActType()) {
                   case "I":
                       transcriptionActType = "iscrizione";
                       break;
                   case "T":
                       transcriptionActType = "trascrizione";
                       break;
                   case "A":
                       transcriptionActType = "annotazione";
                       break;
                   default:
                       transcriptionActType = "";
                       break;
               }
           }
           subjects = RequestHelper.getRequestSubjects(getTranscriptionRequest(), true);
        }

        if(!ValidationHelper.isNullOrEmpty(getEntity().getTranscriptionAmount())){
            importf24 = getEntity().getTranscriptionAmount();
        }
        if(!ValidationHelper.isNullOrEmpty(getEntity().getPacketNumber())) {
            packetNumber = getEntity().getPacketNumber();
        }
        if(!ValidationHelper.isNullOrEmpty(getEntity().getNumberRevenueStamp())) {
            isNumberRevenueStamp = true;
            numberRevenueStamp = getEntity().getNumberRevenueStamp();
        }
        if(!ValidationHelper.isNullOrEmpty(getEntity().getNumberServiceStamp())) {
            isNumberServiceStamp = true;
            numberServiceStamp = getEntity().getNumberServiceStamp();
        }
        String operationalHeadquarters = ApplicationSettingsHolder.getInstance().getByKey(ApplicationSettingsKeys.ADDRESS).getValue()
                + " - " + ApplicationSettingsHolder.getInstance().getByKey(ApplicationSettingsKeys.COMPANY_POSTAL_CODE).getValue()
                + " - " + ApplicationSettingsHolder.getInstance().getByKey(ApplicationSettingsKeys.COMPANY_CITY).getValue();

        String emailsFrom = DaoManager.loadField(WLGServer.class, "login",
                String.class, new Criterion[]{Restrictions.eq("id", Long.parseLong(
                        ApplicationSettingsHolder.getInstance().getByKey(ApplicationSettingsKeys.SENT_SERVER_ID)
                                .getValue()))}).get(0);

        String relativePath="/resources/images/";
        String absolutePath =   FacesContext.getCurrentInstance().getExternalContext().getRealPath(relativePath);
        String filePath = "file:" + absolutePath + "pdf_logo.png";

        StringBuilder result = new StringBuilder();
        result.append("<head>");
        result.append("<style>div{font-family: Verdana, sans-serif; font-size: 15px;}</style>");
        result.append("</head>");
        result.append("<div style=\"margin-top: 50px; margin-left: 40px; margin-right: 40px;\">");
        result.append("<div style=\"float: left;\">");
        result.append("<img src='"+filePath + "'>");
        result.append("</div>");
        result.append("<br><br><br>");
        result.append("<div style=\"float: right; margin-top:60px;\">");
        result.append("<i>Spett.le</i><br><br>");
        result.append("Agenzia delle Entrate - Servizi di Pubblicit&#224;<br><br>");
        result.append("Immobiliare di ");
        if(StringUtils.isNotBlank(realEstateName))
            result.append(realEstateName);
        else if(StringUtils.isNotBlank(reclameServiceName)){
            result.append("<b>");
            result.append(reclameServiceName);
            result.append("</b>");
        }
        result.append(",<br><br>");
        result.append("<u>Ufficio Trascrizione Atti</u>,<br><br>");
        result.append("</div>");
        result.append("<br><br>");
        result.append("<div style=\"margin-top: 50px; width: 100%;\">");
        result.append("<span style=\"line-height: 2;\">con la presente siamo a richiedervi l&#39;iscrizione dell&#39;ipoteca giudiziale contro "+ subjects
                + " per la quale si allegano i seguenti documenti:</span><br><br><br>");
        result.append("<ul>");
        result.append("<li style=\"line-height: 2.5;\">Modello F24 pagato (&#8364;." + InvoiceHelper.format(importf24) +");</li>");
        result.append("<li style=\"line-height: 2.5;\">Nota di " + transcriptionActType + " in formato cartaceo;</li>");
        result.append("<li style=\"line-height: 2.5;\">CD con nota in formato sogei (plico "+ packetNumber + ");</li>");
        result.append("<li style=\"line-height: 2.5;\">Copia uso " + transcriptionActType + " atto di "+ textInVisura + ";</li>");
        if(isNumberRevenueStamp)
            result.append("<li style=\"line-height: 2.5;\">"+ numberRevenueStamp +" marche da bollo da &#8364; 16.00 ;</li>");
        if(isNumberServiceStamp)
            result.append("<li style=\"line-height: 2.5;\">"+ numberServiceStamp +" marche servizi da &#8364; 10.00 ;</li>");
        if(!ValidationHelper.isNullOrEmpty(getEntity().getEnvelopStamp()) && getEntity().getEnvelopStamp())
            result.append("<li style=\"line-height: 2.5;\">Busta preaffrancata.</li>");
        result.append("</ul>");
        result.append("<br><br><br>");
        result.append("Per ogni eventuale chiarimento, lascio di seguito il contatto di riferimento della Societ&#224;	ed il mio personale:<br><br>");
        result.append("Tel. Getesi Srl: 081." + ApplicationSettingsHolder.getInstance().getByKey(ApplicationSettingsKeys.TELEPHONE).getValue()
                +"<br style=\"content: ''; display: block;\">");
        if(!ValidationHelper.isNullOrEmpty(getEntity().getUserPhone()) && getEntity().getUserPhone())
            result.append("Cel. "+ getTranscriptionRequest().getUser().getLastName() + " " + getTranscriptionRequest().getUser().getFirstName()
                    + " : " + (!ValidationHelper.isNullOrEmpty(getTranscriptionRequest().getUser().getPhone())  ?
                    getTranscriptionRequest().getUser().getPhone() : "" ) + "<br><br><br>");
        Date date = new Date();
        result.append("Napoli, "+ DateTimeHelper.toFormatedString(date, DateTimeHelper.getDatePattern()) + "<br><br><br><br><br>");
        result.append("Getesi Srl<br style=\"content: ''; display: block;\">");
        result.append("Sede Operativa: "+ operationalHeadquarters + "<br style=\"content: ''; display: block;\">");
        result.append("Tel. 081 "+ ApplicationSettingsHolder.getInstance().getByKey(ApplicationSettingsKeys.TELEPHONE).getValue()
                + " - Fax 081 "+ApplicationSettingsHolder.getInstance().getByKey(ApplicationSettingsKeys.FAX).getValue()
                + "<br style=\"content: ''; display: block;\">");
        result.append(emailsFrom+ "<br style=\"content: ''; display: block;\">");
        result.append("P.iva: "+ ApplicationSettingsHolder.getInstance().getByKey(ApplicationSettingsKeys.FISCAL_CODE).getValue());
        result.append("</div>");
        result.append("</div>");
        return result.toString();
    }*/

    /*public void createNewMail(String documentType) throws IllegalAccessException, PersistenceBeanException, InstantiationException {
        RedirectHelper.goToMailEditTranscription(getTranscriptionRequest().getId(), getEntityId(), documentType);
    }

    public void downloadPdfFile() throws PersistenceBeanException, IllegalAccessException, InstantiationException {
        try {
            getTranscriptionRequest().setImportF24PdfList(importF24Pdfs);

            String body = getTranscriptionAndCertificationHelper().generatePdfPage("I° COPIA PER LA BANCA/POSTE/AGENTE DELLA RISCOSSIONE", true, getTranscriptionRequest());
            body += "<pd4ml-page-break />";
            body += getTranscriptionAndCertificationHelper().generatePdfPage("2° COPIA PER LA BANCA/POSTE/AGENTE DELLA RISCOSSIONE", false, getTranscriptionRequest());
            body += "<pd4ml-page-break />";
            body += getTranscriptionAndCertificationHelper().generatePdfPage("COPIA PER IL SOGGETTO CHE EFFETTUA IL VERSAMENTO", false, getTranscriptionRequest());
            FileHelper.sendFile("transcription-" + this.getEntityId() + ".pdf",
                    PrintPDFHelper.convertToPDF(null, body, null,
                            DocumentType.OTHER));
        } catch (Exception e) {
            LogHelper.log(log, e);
        }
    }

    private void loadImportF24PdfData() throws PersistenceBeanException, IllegalAccessException {
        List<ImportF24> importF24sData = transcriptionRequest != null && transcriptionRequest.getSpecialFormality() != null ?
                transcriptionRequest.getSpecialFormality().getImportF24List() : null;
        importF24Pdfs = new ArrayList<>();
        if (importF24sData != null) {
            for (ImportF24 importF24 : importF24sData) {
                ImportF24Pdf importF24Pdf = new ImportF24Pdf();
                importF24Pdf.setImportF24Id(importF24.getId());
                if (importF24 != null && importF24.getIncludeNumber() != null && importF24.getIncludeNumber()
                        && !ValidationHelper.isNullOrEmpty(transcriptionRequest)) {
                    importF24Pdf.setF24IdentificationNumber(transcriptionRequest.getF24IdentificationNumber());
                }
                importF24Pdf.setCode(importF24.getCode());
                importF24Pdf.setReferenceYear(transcriptionRequest.getReferenceYear() != null ? transcriptionRequest.getReferenceYear() : null);
                importF24Pdf.setF24Import(importF24.getF24Import() != null && importF24.getF24Import() > 0 ? importF24.getF24Import() : null);
                importF24Pdf.setType(importF24.getType());
                if(importF24.getImportF24Pdf() != null) {
                    importF24Pdf.setId(importF24.getImportF24Pdf().getId());
                    importF24Pdf.setF24IdentificationNumber(importF24.getImportF24Pdf().getF24IdentificationNumber());
                    importF24Pdf.setCode(importF24.getCode());
                    importF24Pdf.setReferenceYear(importF24.getImportF24Pdf().getReferenceYear() != null ? importF24.getImportF24Pdf().getReferenceYear() : null);
                    importF24Pdf.setF24Import(importF24.getImportF24Pdf().getF24Import() != null && importF24.getImportF24Pdf().getF24Import() > 0 ? importF24.getImportF24Pdf().getF24Import() : null);
                    importF24Pdf.setType(importF24.getImportF24Pdf().getType());
                }
                importF24Pdfs.add(importF24Pdf);
            }
        }
        if(getEntity() != null && getEntity().getId() != null) {
            List<ImportF24Pdf> importF24PdfsData = DaoManager.load(ImportF24Pdf.class, new Criterion[]{Restrictions.eq("transcriptionData", getEntity())});
            if(importF24PdfsData != null) {
                for (ImportF24Pdf importF24Pdf : importF24PdfsData) {
                    ImportF24Pdf importF24PdfExists = importF24Pdfs.stream()
                            .filter(importF24PdfFilter -> importF24Pdf.getId().equals(importF24PdfFilter.getId()))
                            .findAny()
                            .orElse(null);
                    if(importF24PdfExists == null) {
                        ImportF24Pdf importF24Pdf1 = new ImportF24Pdf();
                        importF24Pdf1.setId(importF24Pdf.getId());
                        importF24Pdf1.setF24IdentificationNumber(importF24Pdf.getF24IdentificationNumber());
                        importF24Pdf1.setCode(importF24Pdf.getCode());
                        importF24Pdf1.setReferenceYear(importF24Pdf.getReferenceYear() != null ? importF24Pdf.getReferenceYear() : null);
                        importF24Pdf1.setF24Import(importF24Pdf.getF24Import() != null && importF24Pdf.getF24Import() > 0 ? importF24Pdf.getF24Import() : null);
                        importF24Pdf1.setType(importF24Pdf.getType());
                        importF24Pdfs.add(importF24Pdf1);
                    }
                }
            }
        }
    }

    public void add() {
        importF24Pdf = new ImportF24Pdf();
    }

    public void edit(ImportF24Pdf importF24, int row) {
        importF24Pdf.setId(importF24.getId());
        importF24Pdf.setF24IdentificationNumber(importF24.getF24IdentificationNumber());
        importF24Pdf.setCode(importF24.getCode());
        importF24Pdf.setReferenceYear(importF24.getReferenceYear());
        importF24Pdf.setF24Import(importF24.getF24Import());
        importF24Pdf.setImportF24Id(importF24.getImportF24Id());
        importF24Pdf.setType(importF24.getType());
        editedRow = row;
    }

    public void saveImportF24() throws PersistenceBeanException, IllegalAccessException, InstantiationException {
        boolean redirect = false;
        if(getEntity().isNew()){
            DaoManager.save(getEntity());
            redirect = true;
        }
        if(editedRow != -1) {
            if(importF24Pdf.getId() != null) {
                ImportF24Pdf importF24PdfDb = DaoManager.get(ImportF24Pdf.class, new Criterion[]{Restrictions.eq("id", importF24Pdf.getId())});
                importF24PdfDb.setF24IdentificationNumber(importF24Pdf.getF24IdentificationNumber());
                importF24PdfDb.setCode(importF24Pdf.getCode());
                importF24PdfDb.setReferenceYear(importF24Pdf.getReferenceYear());
                importF24PdfDb.setF24Import(importF24Pdf.getF24Import());
                importF24PdfDb.setType(importF24Pdf.getType());
                DaoManager.save(importF24PdfDb, true);
            } else {
                importF24Pdf.setTranscriptionData(getEntity());
                DaoManager.save(importF24Pdf, true);
                ImportF24 importF24 = DaoManager.get(ImportF24.class, new Criterion[]{Restrictions.eq("id", importF24Pdf.getImportF24Id())});
                if (importF24 != null) {
                    importF24.setImportF24Pdf(importF24Pdf);
                    DaoManager.save(importF24, true);
                }
            }
        } else {
            ImportF24Pdf importF24PdfNew = new ImportF24Pdf();
            importF24PdfNew.setTranscriptionData(getEntity());
            importF24PdfNew.setF24IdentificationNumber(importF24Pdf.getF24IdentificationNumber());
            importF24PdfNew.setCode(importF24Pdf.getCode());
            importF24PdfNew.setReferenceYear(importF24Pdf.getReferenceYear());
            importF24PdfNew.setF24Import(importF24Pdf.getF24Import());
            importF24PdfNew.setType(importF24Pdf.getType());
            DaoManager.save(importF24PdfNew, true);
        }
        loadImportF24PdfData();
        if(redirect)
            RedirectHelper.goTo(PageTypes.TRANSCRIPTION_MANAGEMENT, getTranscriptionRequest().getId(), getEntity().getId());
    }

    public void beforeDelete(ImportF24Pdf importF24Pdf, int row) {
        this.importF24Pdf = importF24Pdf;
    }

    public void delete() throws PersistenceBeanException, IllegalAccessException, InstantiationException {
        log.error(importF24Pdf.getImportF24Id());
        log.error(importF24Pdf.getId());
        if(importF24Pdf.getImportF24Id() != null) {
            ImportF24 importF24 = DaoManager.get(ImportF24.class, new Criterion[]{Restrictions.eq("id", importF24Pdf.getImportF24Id())});
            if (importF24 != null) {
                importF24.setImportF24Pdf(null);
                DaoManager.save(importF24, true);
            }
        }
        if(importF24Pdf.getId() != null) {
            ImportF24Pdf importF24PdfDb = DaoManager.get(ImportF24Pdf.class, new Criterion[]{Restrictions.eq("id", importF24Pdf.getId())});
            DaoManager.remove(importF24PdfDb, true);
        }
        editedRow = -1;
        importF24Pdf = new ImportF24Pdf();
        loadImportF24PdfData();
    }*/
    
    /*public void generateReportSpesePdf() {
    	try {
            log.info("Inside generate Pdf REPORT SPESE");
            byte[] excelFile = getTranscriptionAndCertificationHelper().getXlsBytesReportSpesePdf(transcriptionRequest);
            if(!ValidationHelper.isNullOrEmpty(excelFile)) {
                String tmpFileNameSuffix = "REPORT SPESE";
                String sofficeCommand =
                        ApplicationSettingsHolder.getInstance().getByKey(
                                ApplicationSettingsKeys.SOFFICE_COMMAND).getValue().trim();
                String tempDir = FileHelper.getLocalTempDir();
                tempDir  += File.separator + UUID.randomUUID();
                FileUtils.forceMkdir(new File(tempDir));

                FileHelper.writeFileToFolder(tmpFileNameSuffix + ".xls", new File(tempDir), excelFile);

                String path = tempDir + File.separator + tmpFileNameSuffix + ".xls";

                File file = new File(path);

                VisureManageHelper.sendPDFfromXLSFile(file, sofficeCommand,tempDir,path);
            }
            log.info("Leaving generate Pdf REPORT SPESE");
        } catch (Exception e) {
            LogHelper.log(log, e);
        }
    }*/
}
