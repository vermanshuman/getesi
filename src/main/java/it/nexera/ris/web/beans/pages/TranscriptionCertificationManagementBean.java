package it.nexera.ris.web.beans.pages;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.model.SelectItem;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.HibernateException;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.StreamedContent;

import it.nexera.ris.common.enums.ApplicationSettingsKeys;
import it.nexera.ris.common.enums.DocumentType;
import it.nexera.ris.common.enums.ExtraCostType;
import it.nexera.ris.common.enums.MortgageType;
import it.nexera.ris.common.enums.PageTypes;
import it.nexera.ris.common.enums.SupportType;
import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.helpers.ComboboxHelper;
import it.nexera.ris.common.helpers.CostManipulationHelper;
import it.nexera.ris.common.helpers.FileHelper;
import it.nexera.ris.common.helpers.LogHelper;
import it.nexera.ris.common.helpers.PrintPDFHelper;
import it.nexera.ris.common.helpers.RedirectHelper;
import it.nexera.ris.common.helpers.TranscriptionAndCertificationHelper;
import it.nexera.ris.common.helpers.ValidationHelper;
import it.nexera.ris.common.helpers.VisureManageHelper;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.domain.CertificationData;
import it.nexera.ris.persistence.beans.entities.domain.Document;
import it.nexera.ris.persistence.beans.entities.domain.ExtraCost;
import it.nexera.ris.persistence.beans.entities.domain.Request;
import it.nexera.ris.persistence.beans.entities.domain.TranscriptionData;
import it.nexera.ris.settings.ApplicationSettingsHolder;
import it.nexera.ris.web.beans.BaseEntityPageBean;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@ManagedBean
@ViewScoped
public class TranscriptionCertificationManagementBean implements Serializable {
	
	/*private static final long serialVersionUID = -2486361254576344821L;
	
	private TranscriptionAndCertificationHelper transcriptionAndCertificationHelper;
	private TranscriptionData transcriptionDataEntity;
	private Long transcriptionDataEntityId;
	private Long certificationDataEntityId;
	
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
    
    private CertificationData certificationDataEntity;
    private Request certificationRequest;
    private String cadastralFileName;
    private byte[] cadastralDocumentContents;
    private Document cadastralDocument;
    private String mapFileName;
    private byte[] mapDocumentContents;
    private Document mapDocument;
    private String signedCertificationFileName;
    private byte[] signedCertificationDocumentContents;
    private Document signedCertificationDocument;
    private Boolean sendCourier;
    private Boolean sendCertification;
    private Double cadastralCost;
    private Double mortgageCost;
    private Double personalCost;
    private Double postalCost;
    private List<SelectItem> supportTypes;*/
    
    /*public Long getTranscriptionDataEntityId() {
        if (this.getTranscriptionDataEntity() != null && this.getTranscriptionDataEntity().getId() != null) {
            return this.getTranscriptionDataEntity().getId();
        } else {
            if (this.transcriptionDataEntityId != null) {
                return transcriptionDataEntityId;
            }
            return null;
        }
    }

    public void setTranscriptionDataEntityId(Long transcriptionDataEntityId) {
        this.transcriptionDataEntityId = transcriptionDataEntityId;
    }
    
    public Long getCertificationDataEntityId() {
        if (this.getCertificationDataEntity() != null && this.getCertificationDataEntity().getId() != null) {
            return this.getCertificationDataEntity().getId();
        } else {
            if (this.certificationDataEntityId != null) {
                return certificationDataEntityId;
            }
            return null;
        }
    }

    public void setCertificationDataEntityId(Long certificationDataEntityId) {
        this.certificationDataEntityId = certificationDataEntityId;
    }*/
    
    /*@Override
    protected void onConstruct() throws NumberFormatException, HibernateException {
    	String referrentId = getRequestParameter(RedirectHelper.PARENT_ID_PARAMETER);
    	String transcriptionId = getRequestParameter(RedirectHelper.TRANSCRIPTION_ID);
    	String certificationId = getRequestParameter(RedirectHelper.CERTIFICATION_ID);
        if(StringUtils.isNotBlank(referrentId)){
        	Request request;
			try {
				request = DaoManager.get(Request.class, Long.parseLong(referrentId));
				setTranscriptionRequest(request);
				setCertificationRequest(request);
				setTranscriptionDataEntity(new TranscriptionData());
				setCertificationDataEntity(new CertificationData());
				if(!ValidationHelper.isNullOrEmpty(transcriptionId)) {
					TranscriptionData transcriptionData = DaoManager.get(TranscriptionData.class, Long.parseLong(transcriptionId));
					if(!ValidationHelper.isNullOrEmpty(transcriptionData)) {
						setTranscriptionDataEntity(transcriptionData);
					}
				}
				if(!ValidationHelper.isNullOrEmpty(certificationId)) {
					CertificationData certificationData = DaoManager.get(CertificationData.class, Long.parseLong(certificationId));
					if(!ValidationHelper.isNullOrEmpty(certificationData)) {
						setCertificationDataEntity(certificationData);
					}
				}
			} catch (InstantiationException | IllegalAccessException | PersistenceBeanException e) {
				e.printStackTrace();
			}
        }else {
            setTranscriptionRequest(null);
            setCertificationRequest(null);
        }
        try {
        	loadTranscriptionData();
			loadCertificationData();
		} catch (IllegalAccessException | PersistenceBeanException | IOException | InstantiationException e) {
			e.printStackTrace();
		}
        setTranscriptionAndCertificationHelper(new TranscriptionAndCertificationHelper());
    }*/
    
    /*public void loadTranscriptionData() throws NumberFormatException, HibernateException, PersistenceBeanException, IllegalAccessException, IOException {
        if(ValidationHelper.isNullOrEmpty(getTranscriptionDataEntity().getCourierEnvelopeDate()))
            getTranscriptionDataEntity().setCourierEnvelopeDate(new Date());
        if(getTranscriptionDataEntity().isNew())
            getTranscriptionDataEntity().setRequest(getTranscriptionRequest());
        setDisableStampFields(Boolean.FALSE);
        if(!ValidationHelper.isNullOrEmpty(getTranscriptionRequest())){
            if(ValidationHelper.isNullOrEmpty(getTranscriptionRequest().getAggregationLandChargesRegistry()) ||
                    ValidationHelper.isNullOrEmpty(getTranscriptionRequest().getAggregationLandChargesRegistry().getStamp()) ||
                !getTranscriptionRequest().getAggregationLandChargesRegistry().getStamp()){
                setDisableStampFields(Boolean.TRUE);
            }
        }
        if(!ValidationHelper.isNullOrEmpty(getTranscriptionDataEntity().getCourierDocument())){
            setCourierFileName(FileHelper.getFileName(getTranscriptionDataEntity().getCourierDocument().getPath()));
            setCourierDocumentContents(FileHelper.loadContentByPath(getTranscriptionDataEntity().getCourierDocument().getPath()));
            setCourierDocument(getTranscriptionDataEntity().getCourierDocument());
        }else {
            setCourierFileName(null);
            setCourierDocumentContents(null);
            setCourierDocument(null);
        }

        if(!ValidationHelper.isNullOrEmpty(getTranscriptionDataEntity().getEntryDocument())){
            setEntryFileName(FileHelper.getFileName(getTranscriptionDataEntity().getEntryDocument().getPath()));
            setEntryDocumentContents(FileHelper.loadContentByPath(getTranscriptionDataEntity().getEntryDocument().getPath()));
            setEntryDocument(getTranscriptionDataEntity().getEntryDocument());
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
        setSupportTypes(ComboboxHelper.fillList(SupportType.class, false));
    }*/
	
    /*public void saveTranscription() {
        boolean isNew = this.getTranscriptionDataEntity().isNew();
        setTranscriptionDataEntity(getTranscriptionAndCertificationHelper().saveTranscription(getTranscriptionRequest(), getTranscriptionDataEntity(), 
        		getIpotecarioCost(), getIpotecarioPostale(), getCourierFileName(), getCourierDocument(), getCourierDocumentContents(), 
        		getEntryFileName(), getEntryDocument(), getEntryDocumentContents()));
        if (isNew) {
        	if(ValidationHelper.isNullOrEmpty(getCertificationDataEntityId()))
        		RedirectHelper.goToTranscriptionCertification(PageTypes.TRANSCRIPTION_CERTIFICATION_MANAGEMENT, 
        				getTranscriptionRequest().getId(), getTranscriptionDataEntity().getId(), null);
        	else
        		RedirectHelper.goToTranscriptionCertification(PageTypes.TRANSCRIPTION_CERTIFICATION_MANAGEMENT, 
        				getTranscriptionRequest().getId(), getTranscriptionDataEntity().getId(), getCertificationDataEntityId());
        }
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
        getTranscriptionDataEntity().setCourierDocument(null);
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
        getTranscriptionDataEntity().setEntryDocument(null);
    }

    public void preparePdfOfLetter() throws IOException, PersistenceBeanException, IllegalAccessException, InstantiationException {
        getTranscriptionAndCertificationHelper().preparePdfOfLetter(getTranscriptionRequest(), getTranscriptionDataEntity());
    }

    public void createNewMail(String documentType) throws IllegalAccessException, PersistenceBeanException, InstantiationException {
        RedirectHelper.goToMailEditTranscription(getTranscriptionRequest().getId(), getTranscriptionDataEntityId(), documentType);
    }

    public void downloadPdfFile() throws PersistenceBeanException, IllegalAccessException, InstantiationException {
        try {
            String body = getTranscriptionAndCertificationHelper().generatePdfPage("I° COPIA PER LA BANCA/POSTE/AGENTE DELLA RISCOSSIONE", true, getTranscriptionRequest());
            body += "<pd4ml-page-break />";
            body += getTranscriptionAndCertificationHelper().generatePdfPage("2° COPIA PER LA BANCA/POSTE/AGENTE DELLA RISCOSSIONE", false, getTranscriptionRequest());
            body += "<pd4ml-page-break />";
            body += getTranscriptionAndCertificationHelper().generatePdfPage("COPIA PER IL SOGGETTO CHE EFFETTUA IL VERSAMENTO", false, getTranscriptionRequest());
            FileHelper.sendFile("transcription-" + this.getTranscriptionDataEntityId() + ".pdf",
                    PrintPDFHelper.convertToPDF(null, body, null,
                            DocumentType.OTHER));
        } catch (Exception e) {
            LogHelper.log(log, e);
        }
    }*/
    
   /* public void generateReportSpesePdf() {
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

    /************************************************* CERTIFICATION CODE **************************************************************************/

    /*public void loadCertificationData() throws NumberFormatException, HibernateException, PersistenceBeanException, InstantiationException, IllegalAccessException, IOException {
        if(getCertificationDataEntity().isNew())
            getCertificationDataEntity().setRequest(getCertificationRequest());

        if(!ValidationHelper.isNullOrEmpty(getCertificationDataEntity().getCadastralDocument())){
            setCadastralFileName(FileHelper.getFileName(getCertificationDataEntity().getCadastralDocument().getPath()));
            setCadastralDocumentContents(FileHelper.loadContentByPath(getCertificationDataEntity().getCadastralDocument().getPath()));
            setCadastralDocument(getCertificationDataEntity().getCadastralDocument());
        }else {
            setCadastralFileName(null);
            setCadastralDocumentContents(null);
            setCadastralDocument(null);
        }

        if(!ValidationHelper.isNullOrEmpty(getCertificationDataEntity().getMapDocument())){
            setMapFileName(FileHelper.getFileName(getCertificationDataEntity().getMapDocument().getPath()));
            setMapDocumentContents(FileHelper.loadContentByPath(getCertificationDataEntity().getMapDocument().getPath()));
            setMapDocument(getCertificationDataEntity().getMapDocument());
        }else {
            setMapFileName(null);
            setMapDocumentContents(null);
            setMapDocument(null);
        }

        if(!ValidationHelper.isNullOrEmpty(getCertificationDataEntity().getSignedCertificationDocument())){
            setSignedCertificationFileName(FileHelper.getFileName(getCertificationDataEntity().getSignedCertificationDocument().getPath()));
            setSignedCertificationDocumentContents(FileHelper.loadContentByPath(getCertificationDataEntity().getSignedCertificationDocument().getPath()));
            setSignedCertificationDocument(getCertificationDataEntity().getSignedCertificationDocument());
        }else {
            setSignedCertificationFileName(null);
            setSignedCertificationDocumentContents(null);
            setSignedCertificationDocument(null);
        }

        List<ExtraCost> extraCosts = DaoManager.load(ExtraCost.class, new Criterion[]{
                Restrictions.eq("requestId", getCertificationRequest().getId()),
                Restrictions.isNotNull("type"),
                Restrictions.or(
                        Restrictions.eq("type", ExtraCostType.CATASTO),
                        Restrictions.eq("type", ExtraCostType.IPOTECARIO),
                        Restrictions.eq("type", ExtraCostType.ANAGRAFICO),
                        Restrictions.eq("type", ExtraCostType.POSTALE)
                )
        });

        for(ExtraCost extraCost : extraCosts){
            if(extraCost.getType().equals(ExtraCostType.CATASTO))
                setCadastralCost(extraCost.getPrice());
            else if(extraCost.getType().equals(ExtraCostType.IPOTECARIO))
                setMortgageCost(extraCost.getPrice());
            else if(extraCost.getType().equals(ExtraCostType.CATASTO))
                setPersonalCost(extraCost.getPrice());
            else if(extraCost.getType().equals(ExtraCostType.ANAGRAFICO))
                setPersonalCost(extraCost.getPrice());
            else if(extraCost.getType().equals(ExtraCostType.POSTALE))
                setPostalCost(extraCost.getPrice());
        }
    }*/
    
    /*public void saveCertification() {
    	boolean isNew = this.getCertificationDataEntity().isNew();
        setCertificationDataEntity(getTranscriptionAndCertificationHelper().saveCertification(getCertificationRequest(), 
        		getCertificationDataEntity(), getCadastralCost(), getMortgageCost(), getPersonalCost(), getPostalCost(), 
        		getCadastralFileName(), getCadastralDocument(), getCadastralDocumentContents(), getMapFileName(), 
        		getMapDocument(), getMapDocumentContents(), getSignedCertificationFileName(), 
        		getSignedCertificationDocument(), getSignedCertificationDocumentContents()));
        if (isNew) {
        	if(ValidationHelper.isNullOrEmpty(getTranscriptionDataEntityId()))
        		RedirectHelper.goToTranscriptionCertification(PageTypes.TRANSCRIPTION_CERTIFICATION_MANAGEMENT,  
        				getCertificationRequest().getId(), null, getCertificationDataEntity().getId());
        	else
        		RedirectHelper.goToTranscriptionCertification(PageTypes.TRANSCRIPTION_CERTIFICATION_MANAGEMENT, 
        				getCertificationRequest().getId(), getTranscriptionDataEntityId(), getCertificationDataEntity().getId());
        }
    }*/

    /*public void handleCadastralFileUpload(FileUploadEvent event) throws PersistenceBeanException {
        setCadastralFileName(event.getFile().getFileName());
        setCadastralDocumentContents(event.getFile().getContents());
    }

    public void downloadCadastralDocument() {
        if(!ValidationHelper.isNullOrEmpty(getCadastralFileName()) && !ValidationHelper.isNullOrEmpty(getCadastralDocumentContents())) {
            FileHelper.sendFile(getCadastralFileName(), getCadastralDocumentContents());
        }
    }

    public void deleteCadastralDocument(){
        setCadastralFileName(null);
        setCadastralDocumentContents(null);
    }

    public void handleMapFileUpload(FileUploadEvent event) throws PersistenceBeanException {
        setMapFileName(event.getFile().getFileName());
        setMapDocumentContents(event.getFile().getContents());
    }

    public void downloadMapDocument() {
        if(!ValidationHelper.isNullOrEmpty(getMapFileName()) && !ValidationHelper.isNullOrEmpty(getMapDocumentContents())) {
            FileHelper.sendFile(getMapFileName(), getMapDocumentContents());
        }
    }

    public void deleteMapDocument() throws HibernateException, PersistenceBeanException, InstantiationException, IllegalAccessException, NumberFormatException, IOException {
        setMapFileName(null);
        setMapDocumentContents(null);
    }

    public void handleSignedCertificationFileUpload(FileUploadEvent event) throws PersistenceBeanException {
        setSignedCertificationFileName(event.getFile().getFileName());
        setSignedCertificationDocumentContents(event.getFile().getContents());
    }

    public void downloadSignedCertificationDocument() {
        if(!ValidationHelper.isNullOrEmpty(getSignedCertificationFileName()) && !ValidationHelper.isNullOrEmpty(getSignedCertificationDocumentContents())) {
            FileHelper.sendFile(getSignedCertificationFileName(), getSignedCertificationDocumentContents());
        }
    }

    public void deleteSignedCertificationDocument() throws HibernateException, PersistenceBeanException, InstantiationException, IllegalAccessException, NumberFormatException, IOException {
        setSignedCertificationFileName(null);
        setSignedCertificationDocumentContents(null);
    }*/

}
