package it.nexera.ris.web.beans.pages;

import it.nexera.ris.common.enums.DocumentGenerationPlaces;
import it.nexera.ris.common.enums.DocumentType;
import it.nexera.ris.common.enums.RequestEnumTypes;
import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.helpers.ComboboxHelper;
import it.nexera.ris.common.helpers.LogHelper;
import it.nexera.ris.common.helpers.ValidationHelper;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.InstancePhases;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.TemplateDocumentModel;
import it.nexera.ris.web.beans.EntityEditPageBean;
import org.hibernate.HibernateException;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.model.SelectItem;
import java.io.IOException;
import java.io.Serializable;
import java.util.List;

@ManagedBean(name = "instancePhasesEditBean")
@ViewScoped
public class InstancePhasesEditBean extends EntityEditPageBean<InstancePhases>
        implements Serializable {

    private static final long serialVersionUID = 9152346992792318061L;

    private List<SelectItem> places;

    private List<SelectItem> models;

    private List<SelectItem> documentTypes;

    private List<SelectItem> requestTypes;

    private Long modelId;

    private String placeId;

    private Boolean showRequestTypes;

    private Boolean showDocumentTypes;

    private Long selectedRequestTypeId;

    private Long selectedDocumentTypeId;

    /*
     * (non-Javadoc)
     *
     * @see it.nexera.web.beans.EntityEditPageBean#pageLoad()
     */
    @Override
    public void onLoad() throws NumberFormatException, HibernateException,
            PersistenceBeanException, InstantiationException,
            IllegalAccessException {
        fillDropDown();
        fillValues();
    }

    private void fillValues() {
        if (this.getEntity().getModel() != null) {
            this.setModelId(this.getEntity().getModel().getId());
        }
        if (this.getEntity().getPlace() != null) {
            this.setPlaceId(this.getEntity().getPlace().name());
        }
        if (this.getEntity().getRequestTypeId() != null) {
            this.setShowRequestTypes(Boolean.TRUE);
            this.setSelectedRequestTypeId(this.getEntity().getRequestTypeId());
        }
        if (this.getEntity().getDocumentTypeId() != null) {
            this.setShowDocumentTypes(Boolean.TRUE);
            this.setSelectedDocumentTypeId(
                    this.getEntity().getDocumentTypeId());
        }
    }

    private void fillDropDown() {
        try {
            this.setPlaces(
                    ComboboxHelper.fillList(DocumentGenerationPlaces.class));

            this.setModels(
                    ComboboxHelper.fillList(TemplateDocumentModel.class));

            this.setRequestTypes(ComboboxHelper.fillList(RequestEnumTypes.class));

            this.setDocumentTypes(ComboboxHelper.fillList(DocumentType.class));
        } catch (Exception e) {
            LogHelper.log(log, e);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see it.nexera.web.beans.EntityEditPageBean#validate()
     */
    @Override
    public void onValidate() throws PersistenceBeanException,
            HibernateException, IllegalAccessException {
        if (ValidationHelper.isNullOrEmpty(getModelId())) {
            addRequiredFieldException("model");
        }
        if (ValidationHelper.isNullOrEmpty(getPlaceId())) {
            addRequiredFieldException("place");
        }
        if (getShowRequestTypes().booleanValue()
                && ValidationHelper.isNullOrEmpty(getSelectedRequestTypeId())) {
            addRequiredFieldException("requests");
        }
        if (getShowDocumentTypes().booleanValue()
                && ValidationHelper.isNullOrEmpty(getSelectedDocumentTypeId())) {
            addRequiredFieldException("documents");
        }
        if (!ValidationHelper.isNullOrEmpty(this.getModelId())
                && !ValidationHelper.isNullOrEmpty(this.getPlaceId())
                && ((!ValidationHelper
                .isNullOrEmpty(this.getSelectedRequestTypeId())
                || !getShowRequestTypes().booleanValue())
                || (!ValidationHelper
                .isNullOrEmpty(this.getSelectedDocumentTypeId())
                || !getShowDocumentTypes().booleanValue()))) {
            DocumentGenerationPlaces place = DocumentGenerationPlaces
                    .valueOf(this.getPlaceId());
            if (checkInstancePhasesExistanse(getModelId(),
                    getSelectedRequestTypeId(), getSelectedDocumentTypeId(),
                    place)) {
                addException("instancePhaseAlreadyExist");
            }
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see it.nexera.web.beans.EntityEditPageBean#save()
     */
    @Override
    public void onSave() throws HibernateException, PersistenceBeanException,
            NumberFormatException, IOException, InstantiationException,
            IllegalAccessException {
        DocumentGenerationPlaces place = DocumentGenerationPlaces
                .valueOf(this.getPlaceId());
        TemplateDocumentModel model = DaoManager
                .get(TemplateDocumentModel.class, this.getModelId());

        if (!ValidationHelper.isNullOrEmpty(place)
                && !ValidationHelper.isNullOrEmpty(model)) {
            this.getEntity().setModel(model);
            this.getEntity().setPlace(place);
            this.getEntity().setRequestTypeId(getSelectedRequestTypeId());
            this.getEntity().setDocumentTypeId(getSelectedDocumentTypeId());

            DaoManager.save(this.getEntity());
        }
    }

    private boolean checkInstancePhasesExistanse(Long modelId, Long requestId,
                                                 Long documentId, DocumentGenerationPlaces place) {
        try {
            InstancePhases instancePhases = null;

            if (getShowRequestTypes().booleanValue()) {
                instancePhases = DaoManager.get(InstancePhases.class,
                        new Criterion[]
                                {
                                        Restrictions.eq("place", place),
                                        Restrictions.eq("model.id", modelId),
                                        Restrictions.eq("requestTypeId", requestId)
                                });
            } else if (getShowDocumentTypes().booleanValue()) {
                instancePhases = DaoManager.get(InstancePhases.class,
                        new Criterion[]
                                {
                                        Restrictions.eq("place", place),
                                        Restrictions.eq("model.id", modelId),
                                        Restrictions.eq("documentTypeId", documentId)
                                });
            } else {
                instancePhases = DaoManager.get(InstancePhases.class,
                        new Criterion[]
                                {
                                        Restrictions.eq("place", place),
                                        Restrictions.eq("model.id", modelId)
                                });
            }

            if (instancePhases != null
                    && !instancePhases.getId().equals(this.getEntity().getId())) {
                return true;
            }
        } catch (Exception e) {
            LogHelper.log(log, e);
        }

        return false;
    }

    public void plaseChange() {
        if (!ValidationHelper.isNullOrEmpty(this.getPlaceId())) {
            DocumentGenerationPlaces place = DocumentGenerationPlaces
                    .valueOf(this.getPlaceId());
            if (DocumentGenerationPlaces.DATABASE_DOCUMENT.equals(place)) {
                this.setShowDocumentTypes(Boolean.TRUE);
                this.setShowRequestTypes(Boolean.FALSE);
            } else if (DocumentGenerationPlaces.REQUEST_MANAGEMENT.equals(place)) {
                this.setShowRequestTypes(Boolean.TRUE);
                this.setShowDocumentTypes(Boolean.FALSE);
            } else {
                this.setShowDocumentTypes(Boolean.FALSE);
                this.setShowRequestTypes(Boolean.FALSE);
            }
        } else {
            this.setShowDocumentTypes(Boolean.FALSE);
            this.setShowRequestTypes(Boolean.FALSE);
        }
    }

    public List<SelectItem> getPlaces() {
        return places;
    }

    public void setPlaces(List<SelectItem> places) {
        this.places = places;
    }

    public List<SelectItem> getModels() {
        return models;
    }

    public void setModels(List<SelectItem> models) {
        this.models = models;
    }

    public Long getModelId() {
        return modelId;
    }

    public void setModelId(Long modelId) {
        this.modelId = modelId;
    }

    public String getPlaceId() {
        return placeId;
    }

    public void setPlaceId(String placeId) {
        this.placeId = placeId;
    }

    public Boolean getShowRequestTypes() {
        return showRequestTypes == null ? Boolean.FALSE : showRequestTypes;
    }

    public void setShowRequestTypes(Boolean showRequestTypes) {
        this.showRequestTypes = showRequestTypes;
    }

    public Boolean getShowDocumentTypes() {
        return showDocumentTypes == null ? Boolean.FALSE : showDocumentTypes;
    }

    public void setShowDocumentTypes(Boolean showDocumentTypes) {
        this.showDocumentTypes = showDocumentTypes;
    }

    public Long getSelectedRequestTypeId() {
        return selectedRequestTypeId;
    }

    public void setSelectedRequestTypeId(Long selectedRequestTypeId) {
        this.selectedRequestTypeId = selectedRequestTypeId;
    }

    public Long getSelectedDocumentTypeId() {
        return selectedDocumentTypeId;
    }

    public void setSelectedDocumentTypeId(Long selectedDocumentTypeId) {
        this.selectedDocumentTypeId = selectedDocumentTypeId;
    }

    public List<SelectItem> getDocumentTypes() {
        return documentTypes;
    }

    public void setDocumentTypes(List<SelectItem> documentTypes) {
        this.documentTypes = documentTypes;
    }

    public List<SelectItem> getRequestTypes() {
        return requestTypes;
    }

    public void setRequestTypes(List<SelectItem> requestTypes) {
        this.requestTypes = requestTypes;
    }
}
