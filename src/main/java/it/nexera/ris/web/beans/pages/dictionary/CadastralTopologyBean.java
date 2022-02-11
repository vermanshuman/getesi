package it.nexera.ris.web.beans.pages.dictionary;

import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.helpers.LogHelper;
import it.nexera.ris.common.helpers.MessageHelper;
import it.nexera.ris.common.helpers.ResourcesHelper;
import it.nexera.ris.common.helpers.ValidationHelper;
import it.nexera.ris.persistence.PersistenceSessionManager;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.CadastralTopology;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.NoCamelCaseWord;
import it.nexera.ris.web.beans.EntityLazyInListEditPageBean;
import it.nexera.ris.web.common.EntityLazyListModel;
import lombok.Data;
import org.hibernate.HibernateException;
import org.hibernate.Transaction;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.hibernate.exception.ConstraintViolationException;
import org.primefaces.model.LazyDataModel;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import java.io.IOException;
import java.io.Serializable;
import java.util.Iterator;
import java.util.List;

@ManagedBean(name = "cadastralTopologyBean")
@ViewScoped
@Data
public class CadastralTopologyBean extends EntityLazyInListEditPageBean<CadastralTopology> implements Serializable {

    private static final long serialVersionUID = -3244425165957089127L;

    private LazyDataModel<NoCamelCaseWord> lazyCamelModel;

    private String camelCaseDescription;

    private Long camelEntityEditId;

    private NoCamelCaseWord noCamelCaseWordEntity;

    private Long camelEntityDeleteId;

    private List<NoCamelCaseWord> filteredNoCamelList;

    @Override
    public void onLoad() throws NumberFormatException, HibernateException, PersistenceBeanException, InstantiationException, IllegalAccessException, IOException {
        this.loadList(CadastralTopology.class, new Order[]{});
        setNoCamelCaseWordEntity(new NoCamelCaseWord());
        loadNoCamelCaseData();
    }


    private void loadNoCamelCaseData(){
        setLazyCamelModel(new EntityLazyListModel<>(NoCamelCaseWord.class, new Order[]{}));
    }
    @Override
    protected void validate() throws PersistenceBeanException {
        if(ValidationHelper.isNullOrEmpty(getEntity().getArticle())) {
            addRequiredFieldException("article");
        }
        if(ValidationHelper.isNullOrEmpty(getEntity().getDescription())) {
            addRequiredFieldException("description");
        }
    }

    @Override
    public void save() throws HibernateException, PersistenceBeanException, NumberFormatException, IOException, InstantiationException, IllegalAccessException {
        DaoManager.save(getEntity());
    }

    @Override
    protected void setEditedValues() {

    }

    protected void validateNoCamelCase() throws PersistenceBeanException {
        if(ValidationHelper.isNullOrEmpty(getCamelCaseDescription())){
            addRequiredFieldException("right_form:camelCaseDescription");
        }

    }
    public void saveCamelCase() {
        this.cleanValidation();
        this.setValidationFailed(false);
        try {
            this.validateNoCamelCase();
        } catch (PersistenceBeanException e) {
            LogHelper.log(log, e);
        }

        if (this.getValidationFailed()) {
            return;
        }
        Transaction tr = null;
        try {
             tr =  PersistenceSessionManager.getBean().getSession().beginTransaction();
             getNoCamelCaseWordEntity().setDescription(getCamelCaseDescription());
            DaoManager.save(getNoCamelCaseWordEntity());
        } catch (Exception e) {
            e.printStackTrace();
            if (tr != null) {
                tr.rollback();
                LogHelper.log(log, e);
            }
        }finally {
            if (tr != null && !tr.wasRolledBack()
                    && tr.isActive()) {
                tr.commit();
            }
        }
        this.resetCamelCaseFields();
        loadNoCamelCaseData();
    }

    public void resetCamelCaseFields() {
        this.cleanValidation();
        setCamelCaseDescription(null);
        setNoCamelCaseWordEntity(new NoCamelCaseWord());
    }

    public void editNoCamelCaseEntity() {
        this.cleanValidation();
        if (!ValidationHelper.isNullOrEmpty(this.getCamelEntityEditId())) {
            try {
                NoCamelCaseWord noCamelCaseWord = DaoManager.get(NoCamelCaseWord.class, this.getCamelEntityEditId());
                if(!ValidationHelper.isNullOrEmpty(noCamelCaseWord)){
                    setCamelCaseDescription(noCamelCaseWord.getDescription());
                    setNoCamelCaseWordEntity(noCamelCaseWord);
                    DaoManager.getSession().evict(this.getNoCamelCaseWordEntity());
                }

            } catch (Exception e) {
                LogHelper.log(log, e);
            }
        }
    }

    public void deleteCamelEntity() throws HibernateException, NumberFormatException {
        if (this.getCamelEntityDeleteId() != null) {
            Transaction tr = null;
            try {
                tr = PersistenceSessionManager.getBean().getSession()
                        .beginTransaction();

                this.deleteNoCamelCaseEntityInternal(this.getCamelEntityDeleteId());
            } catch (Exception e) {
                if (tr != null) {
                    tr.rollback();
                }
                if (e instanceof ConstraintViolationException) {
                    MessageHelper.addGlobalMessage(FacesMessage.SEVERITY_ERROR,
                            ResourcesHelper.getValidation("deleteFail"), "");
                } else {
                    MessageHelper.addGlobalMessage(FacesMessage.SEVERITY_ERROR,
                            e.getMessage(), e.getCause().getMessage());
                }
                LogHelper.log(log, e);
            } finally {
                if (tr != null && !tr.wasRolledBack() && tr.isActive()) {
                    try {
                        tr.commit();
                    } catch (Exception e) {
                        if (e instanceof ConstraintViolationException) {
                            MessageHelper.addGlobalMessage(FacesMessage.SEVERITY_ERROR,
                                    ResourcesHelper.getValidation("deleteFail"), "");
                        } else {
                            MessageHelper.addGlobalMessage(FacesMessage.SEVERITY_ERROR,
                                    e.getMessage(), e.getCause().getMessage());
                        }

                        tr.rollback();

                        LogHelper.log(log, e);
                    }
                    if (tr != null && !tr.wasRolledBack()) {
                        afterEntityRemoved();
                    }
                }
            }
            this.loadNoCamelCaseData();
        }
    }
    protected void deleteNoCamelCaseEntityInternal(Long id) throws HibernateException,
            PersistenceBeanException, InstantiationException,
            IllegalAccessException {
        if (id == null || id == 0L) {
            return;
        }
        DaoManager.remove(NoCamelCaseWord.class, id);
    }

    public void afterEntityRemoved() {
        if (this.getCamelEntityDeleteId() != null && this.getFilteredNoCamelList() != null) {
            Iterator<NoCamelCaseWord> iterator = this.getFilteredNoCamelList().iterator();
            while (iterator.hasNext()) {
                if (this.getCamelEntityDeleteId().equals(iterator.next().getId())) {
                    iterator.remove();
                    break;
                }
            }
        }
    }
}
