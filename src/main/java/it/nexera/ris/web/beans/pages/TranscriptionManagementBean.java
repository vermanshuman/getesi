package it.nexera.ris.web.beans.pages;

import it.nexera.ris.common.enums.DocumentType;
import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.helpers.*;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.domain.Document;
import it.nexera.ris.persistence.beans.entities.domain.Request;
import it.nexera.ris.persistence.beans.entities.domain.TranscriptionData;
import it.nexera.ris.web.beans.EntityViewPageBean;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.HibernateException;
import org.hibernate.StaleObjectStateException;
import org.hibernate.Transaction;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.UploadedFile;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

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
    
    @Override
    protected void preLoad() throws PersistenceBeanException, IllegalAccessException, InstantiationException {
        String referrentId = getRequestParameter(RedirectHelper.PARENT_ID_PARAMETER);
        if(StringUtils.isNotBlank(referrentId)){
            setTranscriptionRequest(DaoManager.get(Request.class, Long.parseLong(referrentId)));
        }else
            setTranscriptionRequest(null);
    }

    @Override
    public void onLoad() throws NumberFormatException, HibernateException, PersistenceBeanException, InstantiationException, IllegalAccessException, IOException {
        if(ValidationHelper.isNullOrEmpty(getEntity().getCourierEnvelopeDate()))
            getEntity().setCourierEnvelopeDate(new Date());
        if(getEntity().isNew())
            getEntity().setRequest(getTranscriptionRequest());
        if(!ValidationHelper.isNullOrEmpty(getEntity().getCourierDocument())){
        	setCourierFileName(getEntity().getCourierDocument().getTitle());
        	setCourierDocumentContents(FileHelper.loadContentByPath(getEntity().getCourierDocument().getPath()));
        }
    }

    public void saveTranscription() throws PersistenceBeanException, InstantiationException, IllegalAccessException {
        Transaction tr = null;
        try {
            tr = DaoManager.getSession().beginTransaction();
            DaoManager.save(getEntity());
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
        if(!ValidationHelper.isNullOrEmpty(getCourierDocumentContents()) && !ValidationHelper.isNullOrEmpty(getEntity())){
        	Calendar calendar = Calendar.getInstance(Locale.ITALY);
    		int year = calendar.get(Calendar.YEAR);
    		int month = calendar.get(Calendar.MONTH);
    		int day = calendar.get(Calendar.DAY_OF_MONTH);
        	StringBuffer sb = new StringBuffer();
            sb.append(FileHelper.getDocumentSavePath());
            sb.append("\\");
            sb.append("transcription");
            sb.append("\\");
            sb.append(year);
            sb.append("\\");
            sb.append(month);
            sb.append("\\");
            sb.append(day);
            sb.append("\\");
            sb.append(getEntity().getId());
            sb.append("\\");
            File filePath = new File(sb.toString());

            try {
                String path = FileHelper.writeFileToFolder(getCourierFileName(), filePath, getCourierDocumentContents());
            } catch (IOException e) {
                LogHelper.log(log, e);
            }
            Document document = null;
            if(ValidationHelper.isNullOrEmpty(getEntity().getCourierDocument()))
            	document = new Document();
            else
            	document = getEntity().getCourierDocument();
            document.setRequest(getTranscriptionRequest());
            document.setTitle(getCourierFileName());
            document.setTypeId(DocumentType.OTHER.getId());
            document.setDate(new Date());
            document.setPath(sb + getCourierFileName());
            DaoManager.save(document, true);
            getEntity().setCourierDocument(document);
            DaoManager.save(getEntity(), true);
        }
    }
    public void handleCourierFileUpload(FileUploadEvent event) throws PersistenceBeanException {
        setCourierFileName(event.getFile().getFileName());
        setCourierDocumentContents(event.getFile().getContents());
    }
    
    public void downloadCourierDocument() {
    	if(!ValidationHelper.isNullOrEmpty(getCourierFileName()) && !ValidationHelper.isNullOrEmpty(getCourierDocumentContents())) {
    		FileHelper.sendFile(getCourierFileName(), getCourierDocumentContents());
    	}
    }
    
    public void deleteCourierDocument() {
    	if(!ValidationHelper.isNullOrEmpty(getCourierFileName())) {
    		if(!ValidationHelper.isNullOrEmpty(getCourierDocumentContents())) {
    			setCourierFileName(null);
    			setCourierDocumentContents(null);
    		}
    	}
    }
    
}
