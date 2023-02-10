package it.nexera.ris.web.beans.pages.dictionary;
import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.helpers.LogHelper;
import it.nexera.ris.common.helpers.ValidationHelper;
import it.nexera.ris.persistence.PersistenceSessionManager;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.domain.LandOmi;
import it.nexera.ris.persistence.beans.entities.domain.Subject;
import it.nexera.ris.persistence.beans.entities.domain.VisureRTF;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.Country;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.Nationality;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.TypeFormality;
import it.nexera.ris.web.beans.EntityLazyInListEditPageBean;
import it.nexera.ris.web.beans.EntityLazyListPageBean;
import it.nexera.ris.web.common.EntityLazyListModel;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.HibernateException;
import org.hibernate.Transaction;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.primefaces.context.RequestContext;
import org.primefaces.model.LazyDataModel;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@ManagedBean(name = "foreignStateListBean")
@ViewScoped
public class ForeignStatesListBean extends
        EntityLazyListPageBean<Country> implements Serializable {

    private static final long serialVersionUID = -6260035111902337296L;

    private String code;
    
    private String description;
    
    private String codeFilter;

    private String descriptionFilter;

    private String fiscalCodeFilter;

    private Country entity;

    private String fiscalCode;

    @Override
    public void onLoad() throws NumberFormatException, HibernateException,
            PersistenceBeanException, InstantiationException,
            IllegalAccessException, IOException {
        filterTableFromPanel();
        setEntity(new Country());
    }

    public void filterTableFromPanel() throws PersistenceBeanException, IllegalAccessException, InstantiationException {
        List<Criterion> restrictions = new ArrayList<>();
        if (!ValidationHelper.isNullOrEmpty(getCodeFilter())) {
            restrictions.add(Restrictions.ilike("code", getCodeFilter(), MatchMode.ANYWHERE));
        }
        if (!ValidationHelper.isNullOrEmpty(getDescriptionFilter())) {
            restrictions.add(Restrictions.ilike("description", getDescriptionFilter(), MatchMode.ANYWHERE));
        }
        restrictions.add(Restrictions.or(Restrictions.eq("isDeleted", Boolean.FALSE),
                Restrictions.isNull("isDeleted")));

        if (StringUtils.isNotBlank(getFiscalCodeFilter())) {
            List<Criterion> fiscalCodeCriteria = new ArrayList<>();
            fiscalCodeCriteria.add(Restrictions.ilike("cfis", getFiscalCodeFilter(), MatchMode.ANYWHERE));
            List<Nationality> filteredNationality = DaoManager.load(Nationality.class,
                    fiscalCodeCriteria.toArray(new Criterion[0]));
            if(!ValidationHelper.isNullOrEmpty(filteredNationality)){
                List<String> descriptions = filteredNationality
                        .stream()
                        .map(Nationality::getDescription)
                        .collect(Collectors.toList());
                List<Criterion> criteriaDescription = new ArrayList<>();
                for(String description : descriptions){
                    criteriaDescription.add( Restrictions.like("description", description, MatchMode.ANYWHERE)) ;
                }
                restrictions.add(Restrictions.or( criteriaDescription.toArray(new Criterion[criteriaDescription.size()]) ));
                this.loadList(Country.class,restrictions.toArray(new Criterion[0]), new Order[] {Order.asc("description")});
            }else
                this.setLazyModel(null);
        }else
            this.loadList(Country.class,restrictions.toArray(new Criterion[0]), new Order[] {Order.asc("description")});
    }

    public void clearFilterPanel() throws PersistenceBeanException, IllegalAccessException, InstantiationException {
        setCodeFilter(null);
        setDescriptionFilter(null);
        setFiscalCodeFilter(null);
        filterTableFromPanel();
    }

    @Override
    public void editEntity() throws HibernateException, InstantiationException, IllegalAccessException, PersistenceBeanException {
        if (this.getCanEdit()) {
            this.cleanValidation();
            if (!ValidationHelper.isNullOrEmpty(this.getEntityEditId())) {
                try {
                    this.setEntity(DaoManager.get(getType(), this.getEntityEditId()));
                    setFiscalCode(this.getEntity().getFiscalCode());
                    setCode(this.getEntity().getCode());
                    setDescription(this.getEntity().getDescription());
                    DaoManager.getSession().evict(this.getEntity());
                } catch (Exception e) {
                    LogHelper.log(log, e);
                }
            }
            RequestContext.getCurrentInstance().update("addCountryDialog");
            executeJS("PF('addCountryDialogWV').show();");
        }
    }

    @Override
    public void addEntity() throws HibernateException, InstantiationException, IllegalAccessException, PersistenceBeanException {
        if (this.getCanCreate()) {
            setEntity(new Country());
            this.cleanValidation();
            RequestContext.getCurrentInstance().update("addCountryDialog");
            executeJS("PF('addCountryDialogWV').show();");
        }
    }

    public void save() throws HibernateException, PersistenceBeanException, NumberFormatException, IOException,
            InstantiationException, IllegalAccessException {
        this.cleanValidation();
        this.setValidationFailed(false);

        try {
            this.validate();
        } catch (PersistenceBeanException e) {
            LogHelper.log(log, e);
        }
        if (this.getValidationFailed()) {
            return;
        }
        saveEntity();
        this.resetFields();
        executeJS("PF('addCountryDialogWV').hide()");
        executeJS("refreshTable()");
    }

    protected void validate() throws PersistenceBeanException {
        if (ValidationHelper.isNullOrEmpty(getFiscalCode())) {
            addRequiredFieldException("form:fiscalCode");
        } else if (ValidationHelper.isNullOrEmpty(getCode())) {
            addRequiredFieldException("form:code");
        } else if (!ValidationHelper.isUnique(Country.class, "code",
                getCode(), this.getEntity().getId())) {
            addFieldException("form:code", "codeAlreadyInUse");
        }
    }

    public void resetFields() throws PersistenceBeanException, IllegalAccessException, InstantiationException {
        setEntity(new Country());
        this.cleanValidation();
        this.filterTableFromPanel();
    }

    @Override
    public void deleteEntity() throws HibernateException, PersistenceBeanException, InstantiationException, IllegalAccessException, NumberFormatException, IOException {
        this.setEntity(DaoManager.get(getType(), this.getEntityDeleteId()));
        getEntity().setIsDeleted(Boolean.TRUE);
        DaoManager.save(getEntity(), true);
        filterTableFromPanel();
    }

    private void saveEntity() {
        Transaction tr = null;
        try {
            tr = PersistenceSessionManager.getBean().getSession()
                    .beginTransaction();

            this.getEntity().setCode(getCode());
            this.getEntity().setDescription(getDescription());
            if(this.getEntity().isNew()) {
            	Nationality nationality = new Nationality();
            	nationality.setCode(getCode());
                nationality.setDescription(getDescription());
                nationality.setCfis(getFiscalCode());
                DaoManager.save(nationality);
            } else {
            	if(!ValidationHelper.isNullOrEmpty(this.getEntity().getFiscalCode())){
                    List<Nationality> nationalityList = DaoManager.load(Nationality.class, new Criterion[]{
                            Restrictions.eq("cfis", this.getEntity().getFiscalCode()).ignoreCase()
                    });
                    if (!ValidationHelper.isNullOrEmpty(nationalityList)) {
                        Nationality nationality = nationalityList.get(0);
                        nationality.setCode(getCode());
                        nationality.setDescription(getDescription());
                        nationality.setCfis(getFiscalCode());
                        DaoManager.save(nationality);
                    } 
                }
            }
            DaoManager.save(this.getEntity());
        } catch (Exception e) {
            if (tr != null) {
                tr.rollback();
            }
            LogHelper.log(log, e);
        } finally {
            if (tr != null && !tr.wasRolledBack()
                    && tr.isActive()) {
                tr.commit();
            }
        }
    }
    
    
}