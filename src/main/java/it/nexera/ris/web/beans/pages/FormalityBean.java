package it.nexera.ris.web.beans.pages;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import org.hibernate.HibernateException;

import it.nexera.ris.common.enums.PageTypes;
import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.helpers.FormalityHelper;
import it.nexera.ris.common.helpers.RedirectHelper;
import it.nexera.ris.common.helpers.ResourcesHelper;
import it.nexera.ris.common.helpers.SessionHelper;
import it.nexera.ris.common.helpers.ValidationHelper;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.domain.Formality;
import it.nexera.ris.persistence.beans.entities.domain.SectionC;
import it.nexera.ris.persistence.beans.entities.domain.Subject;
import it.nexera.ris.web.beans.EntityEditPageBean;

@ManagedBean(name = "formalityBean")
@ViewScoped
public class FormalityBean extends EntityEditPageBean<Formality> implements Serializable {

    private static final long serialVersionUID = 427964461816819242L;

    private Map<String, List<Subject>> sectionC;
    private Boolean viewFromRequest;
    private Long requestId;
    private Boolean showButtons;
    
    @Override
	protected void pageLoadStatic() throws PersistenceBeanException {
        setShowButtons(Boolean.FALSE);
    	if (SessionHelper.get("requestFormalityView") != null
				&& ((Boolean) SessionHelper.get("requestFormalityView"))) {
    		setViewFromRequest(true);
    		setShowButtons(Boolean.TRUE);
    	}else {
    	    setViewFromRequest(false);
    	}
    		
    	
    	if (SessionHelper.get("editRequestId") != null) {
            setRequestId((Long) SessionHelper.get("editRequestId"));
            setShowButtons(Boolean.TRUE);
        }else {
            setRequestId(null);
        }
            
    	
    	SessionHelper.removeObject("requestFormalityView");
    	SessionHelper.removeObject("editRequestId");
    }
    @Override
    public void onLoad() throws NumberFormatException, HibernateException, PersistenceBeanException,
            InstantiationException, IllegalAccessException {
        String requestId = getRequestParameter(RedirectHelper.ID_PARAMETER);
        if (!ValidationHelper.isNullOrEmpty(requestId)) {
            setEntity(DaoManager.get(Formality.class, Long.parseLong(requestId)));
        }
        if(ValidationHelper.isNullOrEmpty(getEntity())) {
            return;
        }
        setSectionC(new HashMap<>());
        List<Subject> aFavoreSubjects = getEntity().getSectionC().stream()
                .filter(c -> "A favore".equals(c.getSectionCType())).map(SectionC::getSubject)
                .flatMap(List::stream).peek(s -> s.setTempFormality(getEntity())).distinct().collect(Collectors.toList());
        IntStream.range(0, aFavoreSubjects.size()).forEach(i -> aFavoreSubjects.get(i).setNumberInFormalityGroup(i + 1));
        List<Subject> controSubjects = getEntity().getSectionC().stream()
                .filter(c -> "Contro".equals(c.getSectionCType())).map(SectionC::getSubject)
                .flatMap(List::stream).peek(s -> s.setTempFormality(getEntity())).distinct().collect(Collectors.toList());
        IntStream.range(0, controSubjects.size()).forEach(i -> controSubjects.get(i).setNumberInFormalityGroup(i + 1));
        List<Subject> debitoriSubjects = getEntity().getSectionC().stream()
                .filter(c -> "Debitori non datori di ipoteca".equals(c.getSectionCType())).map(SectionC::getSubject)
                .flatMap(List::stream).peek(s -> s.setTempFormality(getEntity())).distinct().collect(Collectors.toList());
        IntStream.range(0, debitoriSubjects.size()).forEach(i -> debitoriSubjects.get(i).setNumberInFormalityGroup(i + 1));
        getSectionC().put(ResourcesHelper.getString("formalityInFavor"), aFavoreSubjects);
        getSectionC().put(ResourcesHelper.getString("formalityVersus"), controSubjects);
        getSectionC().put(ResourcesHelper.getString("formalityDebitori"), debitoriSubjects);
    }

    @Override
    public void onValidate() throws PersistenceBeanException, HibernateException, IllegalAccessException {

    }

    @Override
    public void onSave() throws HibernateException, PersistenceBeanException, NumberFormatException,
            IOException, InstantiationException, IllegalAccessException {

    }

    public Set<Map.Entry<String, List<Subject>>> getSectionCEntries() {
        return sectionC.entrySet();
    }

    public Map<String, List<Subject>> getSectionC() {
        return sectionC;
    }

    public void setSectionC(Map<String, List<Subject>> sectionC) {
        this.sectionC = sectionC;
    }
    
    public void onEdit() {
    	if(getViewFromRequest() != null && getViewFromRequest()) {
    		SessionHelper.put("requestViewFormality", Boolean.TRUE);
			SessionHelper.put("transcriptionActId", getEntityId());
    	}
    	SessionHelper.put("editedRequestId", getRequestId());
    	SessionHelper.put("listProperties", Boolean.TRUE);
    	RedirectHelper.goTo(PageTypes.REQUEST_FORMALITY_CREATE, null, getEntityId());
    }
    
    public void goCancel() throws PersistenceBeanException, IllegalAccessException {
        RedirectHelper.goTo(PageTypes.REQUEST_EDIT, getRequestId());
    }
    
    public void goTextEdit() {
        RedirectHelper.goTo(PageTypes.REQUEST_TEXT_EDIT, getRequestId());
    }
    
    public void downloadFormalityPdf() throws PersistenceBeanException, IllegalAccessException, InstantiationException {
        if (!ValidationHelper.isNullOrEmpty(getEntity())) {
            FormalityHelper.downloadFormalityPdf(getEntity().getId());
        }
    }
    
	public Boolean getViewFromRequest() {
		return viewFromRequest;
	}
	public void setViewFromRequest(Boolean viewFromRequest) {
		this.viewFromRequest = viewFromRequest;
	}
    public Long getRequestId() {
        return requestId;
    }
    public void setRequestId(Long requestId) {
        this.requestId = requestId;
    }
    public Boolean getShowButtons() {
        return showButtons;
    }
    public void setShowButtons(Boolean showButtons) {
        this.showButtons = showButtons;
    }

}
