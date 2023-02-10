package it.nexera.ris.web.beans.pages;

import it.nexera.ris.common.enums.DocumentType;
import it.nexera.ris.common.enums.ExtraCostType;
import it.nexera.ris.common.enums.PageTypes;
import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.helpers.*;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.domain.*;
import it.nexera.ris.web.beans.EntityViewPageBean;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.HibernateException;
import org.hibernate.StaleObjectStateException;
import org.hibernate.Transaction;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;
import org.primefaces.event.FileUploadEvent;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import java.io.*;
import java.util.Date;
import java.util.List;

@Setter
@Getter
@ManagedBean
@ViewScoped
public class CertificationManagementBean extends
        EntityViewPageBean<CertificationData> implements Serializable {

    /*private static final long serialVersionUID = 89628747518866396L;
    private int activeTabIndex;
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
    private TranscriptionAndCertificationHelper transcriptionAndCertificationHelper;*/

    /*@Override
    protected void preLoad() throws PersistenceBeanException, IllegalAccessException, InstantiationException {
        String referrentId = getRequestParameter(RedirectHelper.PARENT_ID_PARAMETER);
        if(StringUtils.isNotBlank(referrentId)){
            setCertificationRequest(DaoManager.get(Request.class, Long.parseLong(referrentId)));
        }else
            setCertificationRequest(null);
    }*/

    @Override
    public void onLoad() throws NumberFormatException, HibernateException, PersistenceBeanException, InstantiationException, IllegalAccessException, IOException {
        /*if(getEntity().isNew())
            getEntity().setRequest(getCertificationRequest());

        if(!ValidationHelper.isNullOrEmpty(getEntity().getCadastralDocument())){
            setCadastralFileName(FileHelper.getFileName(getEntity().getCadastralDocument().getPath()));
            setCadastralDocumentContents(FileHelper.loadContentByPath(getEntity().getCadastralDocument().getPath()));
            setCadastralDocument(getEntity().getCadastralDocument());
        }else {
            setCadastralFileName(null);
            setCadastralDocumentContents(null);
            setCadastralDocument(null);
        }

        if(!ValidationHelper.isNullOrEmpty(getEntity().getMapDocument())){
            setMapFileName(FileHelper.getFileName(getEntity().getMapDocument().getPath()));
            setMapDocumentContents(FileHelper.loadContentByPath(getEntity().getMapDocument().getPath()));
            setMapDocument(getEntity().getMapDocument());
        }else {
            setMapFileName(null);
            setMapDocumentContents(null);
            setMapDocument(null);
        }

        if(!ValidationHelper.isNullOrEmpty(getEntity().getSignedCertificationDocument())){
            setSignedCertificationFileName(FileHelper.getFileName(getEntity().getSignedCertificationDocument().getPath()));
            setSignedCertificationDocumentContents(FileHelper.loadContentByPath(getEntity().getSignedCertificationDocument().getPath()));
            setSignedCertificationDocument(getEntity().getSignedCertificationDocument());
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
        setTranscriptionAndCertificationHelper(new TranscriptionAndCertificationHelper());*/
    }


   /* private void saveExtraCost(ExtraCost newCost, Double price, ExtraCostType extraCostType)
            throws PersistenceBeanException {
        if(newCost == null){
            newCost = new ExtraCost();
          if(extraCostType.equals(ExtraCostType.IPOTECARIO))
                newCost.setNote("Sintetico");
            else if(extraCostType.equals(ExtraCostType.POSTALE))
                newCost.setNote("Spese postali");
        }
        newCost.setPrice(price);
        newCost.setType(extraCostType);
        newCost.setRequestId(getCertificationRequest().getId());
        DaoManager.save(newCost);
    }*/

    /*public void saveCertification() {
        Transaction tr = null;
        try {
            tr = DaoManager.getSession().beginTransaction();
            boolean isNew = this.getEntity().isNew();
            if(!ValidationHelper.isNullOrEmpty(getCertificationRequest())){
                List<ExtraCost> extraCosts = DaoManager.load(ExtraCost.class, new Criterion[]{
                        Restrictions.eq("requestId", getCertificationRequest().getId()),
                        Restrictions.eq("type", ExtraCostType.CATASTO)});

                if(!ValidationHelper.isNullOrEmpty(getCadastralCost())){
                    if(ValidationHelper.isNullOrEmpty(extraCosts)){
                        saveExtraCost(null, getCadastralCost(), ExtraCostType.CATASTO);
                    }else {
                        saveExtraCost(extraCosts.get(0), getCadastralCost(), ExtraCostType.CATASTO);
                    }
                }else if(!ValidationHelper.isNullOrEmpty(extraCosts)){
                    saveExtraCost(null, getCadastralCost(), ExtraCostType.CATASTO);
                }

                extraCosts = DaoManager.load(ExtraCost.class, new Criterion[]{
                        Restrictions.eq("requestId", getCertificationRequest().getId()),
                        Restrictions.eq("type", ExtraCostType.IPOTECARIO)});

                if(!ValidationHelper.isNullOrEmpty(getMortgageCost())){
                    if(ValidationHelper.isNullOrEmpty(extraCosts)){
                        saveExtraCost(null, getMortgageCost(), ExtraCostType.IPOTECARIO);
                    }else {
                        saveExtraCost(extraCosts.get(0), getMortgageCost(), ExtraCostType.IPOTECARIO);
                    }
                }else if(!ValidationHelper.isNullOrEmpty(extraCosts)){
                    saveExtraCost(null, getMortgageCost(), ExtraCostType.IPOTECARIO);
                }

                extraCosts = DaoManager.load(ExtraCost.class, new Criterion[]{
                        Restrictions.eq("requestId", getCertificationRequest().getId()),
                        Restrictions.eq("type", ExtraCostType.ANAGRAFICO)});

                if(!ValidationHelper.isNullOrEmpty(getPersonalCost())){
                    if(ValidationHelper.isNullOrEmpty(extraCosts)){
                        saveExtraCost(null, getPersonalCost(), ExtraCostType.ANAGRAFICO);
                    }else {
                        saveExtraCost(extraCosts.get(0), getPersonalCost(), ExtraCostType.ANAGRAFICO);
                    }
                }else if(!ValidationHelper.isNullOrEmpty(extraCosts)){
                    saveExtraCost(null, getPersonalCost(), ExtraCostType.ANAGRAFICO);
                }

                extraCosts = DaoManager.load(ExtraCost.class, new Criterion[]{
                        Restrictions.eq("requestId", getCertificationRequest().getId()),
                        Restrictions.eq("type", ExtraCostType.POSTALE)});

                if(!ValidationHelper.isNullOrEmpty(getPostalCost())){
                    if(ValidationHelper.isNullOrEmpty(extraCosts)){
                        saveExtraCost(null, getPostalCost(), ExtraCostType.POSTALE);
                    }else {
                        saveExtraCost(extraCosts.get(0), getPostalCost(), ExtraCostType.POSTALE);
                    }
                }else if(!ValidationHelper.isNullOrEmpty(extraCosts)){
                    saveExtraCost(null, getPostalCost(), ExtraCostType.POSTALE);
                }
            }
            DaoManager.save(getEntity());
            if(!ValidationHelper.isNullOrEmpty(getCadastralDocumentContents())){
                Document cadastralDocument = handleDocument("cadastral");
                getEntity().setCadastralDocument(cadastralDocument);
            }else if(!ValidationHelper.isNullOrEmpty(getCadastralDocument())){
                File cadastralDocument =  new File(getCadastralDocument().getPath());
                if(StringUtils.isNotBlank(getCadastralDocument().getPath()) && cadastralDocument.exists()){
                    FileHelper.delete(cadastralDocument);
                }
            }
            if(!ValidationHelper.isNullOrEmpty(getMapDocumentContents())){
                Document mapDocument = handleDocument("map");
                getEntity().setMapDocument(mapDocument);
            }else if(!ValidationHelper.isNullOrEmpty(getMapDocument())){
                File mapDocument =  new File(getMapDocument().getPath());
                if(StringUtils.isNotBlank(getMapDocument().getPath()) && mapDocument.exists()){
                    FileHelper.delete(mapDocument);
                }
            }
            if(!ValidationHelper.isNullOrEmpty(getSignedCertificationDocumentContents())){
                Document certificateDocument = handleDocument("certification");
                getEntity().setSignedCertificationDocument(certificateDocument);
            }else if(!ValidationHelper.isNullOrEmpty(getSignedCertificationDocument())){
                File certificateDocument =  new File(getSignedCertificationDocument().getPath());
                if(StringUtils.isNotBlank(getSignedCertificationDocument().getPath()) && certificateDocument.exists()){
                    FileHelper.delete(certificateDocument);
                }
            }
            DaoManager.save(getEntity());
            if (isNew) {
                RedirectHelper.goTo(PageTypes.CERTIFICATION_MANAGEMENT, getCertificationRequest().getId(), getEntity().getId());
            }
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
    }*/
    
    /*public void saveCertification() {
    	boolean isNew = this.getEntity().isNew();
        setEntity(getTranscriptionAndCertificationHelper().saveCertification(getCertificationRequest(), 
        		getEntity(), getCadastralCost(), getMortgageCost(), getPersonalCost(), getPostalCost(), 
        		getCadastralFileName(), getCadastralDocument(), getCadastralDocumentContents(), getMapFileName(), 
        		getMapDocument(), getMapDocumentContents(), getSignedCertificationFileName(), 
        		getSignedCertificationDocument(), getSignedCertificationDocumentContents()));
        if (isNew) {
        	RedirectHelper.goTo(PageTypes.CERTIFICATION_MANAGEMENT, getCertificationRequest().getId(), getEntity().getId());
        }
    }

    public void handleCadastralFileUpload(FileUploadEvent event) throws PersistenceBeanException {
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

    /*private Document handleDocument(String directory){
        Document document = null;
        try {
            String path = FileHelper.getTranscriptionDocumentSavePath(getEntity().getId(), directory, "certification");
            File filePath = new File(path);
            if(directory.equalsIgnoreCase("cadastral")){
                FileHelper.writeFileToFolder(getCadastralFileName(), filePath, getCadastralDocumentContents());
                if(ValidationHelper.isNullOrEmpty(getEntity().getCadastralDocument()))
                    document = new Document();
                else
                    document = getEntity().getCadastralDocument();
                document.setTitle(FileHelper.getFileNameWOExtension(getCadastralFileName()));
                document.setPath(path + getCadastralFileName());
                document.setTypeId(DocumentType.CADASTRAL.getId());
            }else  if(directory.equalsIgnoreCase("map")){
                FileHelper.writeFileToFolder(getMapFileName(), filePath, getMapDocumentContents());
                if(ValidationHelper.isNullOrEmpty(getEntity().getMapDocument()))
                    document = new Document();
                else
                    document = getEntity().getMapDocument();
                document.setTitle(FileHelper.getFileNameWOExtension(getMapFileName()));
                document.setPath(path + getMapFileName());
                document.setTypeId(DocumentType.ESTRATTO_MAPPA.getId());

            }else if(directory.equalsIgnoreCase("certification")){
                FileHelper.writeFileToFolder(getSignedCertificationFileName(), filePath, getSignedCertificationDocumentContents());
                if(ValidationHelper.isNullOrEmpty(getEntity().getSignedCertificationDocument()))
                    document = new Document();
                else
                    document = getEntity().getSignedCertificationDocument();
                document.setTitle(FileHelper.getFileNameWOExtension(getSignedCertificationFileName()));
                document.setPath(path + getSignedCertificationFileName());
                document.setTypeId(DocumentType.SIGNED_CERTIFICATE.getId());
            }
            document.setRequest(getCertificationRequest());

            document.setDate(new Date());
            DaoManager.save(document);

        } catch (Exception e) {
            LogHelper.log(log, e);
        }
        return document;
    }*/
}
