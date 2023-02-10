package it.nexera.ris.web.beans.pages.dictionary;

import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.helpers.LogHelper;
import it.nexera.ris.common.helpers.ValidationHelper;
import it.nexera.ris.persistence.PersistenceSessionManager;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.AggregationLandChargesRegistry;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.CadastralCategory;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.LandChargesRegistry;
import it.nexera.ris.web.beans.EntityLazyListPageBean;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.Transaction;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.primefaces.context.RequestContext;
import org.primefaces.model.DualListModel;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@ManagedBean(name = "aggregationLandChargesRegistryListBean")
@ViewScoped
@Getter
@Setter
public class AggregationLandChargesRegistryListBean
        extends EntityLazyListPageBean<AggregationLandChargesRegistry>
        implements Serializable {

    private static final long serialVersionUID = 3495255198503928428L;

    private AggregationLandChargesRegistry entity;

    private DualListModel<LandChargesRegistry> registries;

    private String filterName;

    @Override
    public void onLoad() throws NumberFormatException, HibernateException,
            PersistenceBeanException, InstantiationException,
            IllegalAccessException, IOException {
        filterTableFromPanel();
        setEntity(new AggregationLandChargesRegistry());
        this.setRegistries(new DualListModel<>(new ArrayList<>(), new ArrayList<>()));
    }

    public void filterTableFromPanel() throws PersistenceBeanException, IllegalAccessException, InstantiationException {
        List<Criterion> restrictions = new ArrayList<>();
        if (!ValidationHelper.isNullOrEmpty(getFilterName())) {
            restrictions.add(Restrictions.ilike("name", getFilterName(), MatchMode.ANYWHERE));
        }
        this.loadList(AggregationLandChargesRegistry.class,  restrictions.toArray(new Criterion[0]), new Order[]
                {Order.asc("name")});
    }

    public void clearFilterPanel() throws PersistenceBeanException, IllegalAccessException, InstantiationException {
        setFilterName(null);
        filterTableFromPanel();
    }

    @Override
    public void editEntity() throws HibernateException, InstantiationException, IllegalAccessException, PersistenceBeanException {
        if (this.getCanEdit()) {
            this.cleanValidation();
            if (!ValidationHelper.isNullOrEmpty(this.getEntityEditId())) {
                try {
                    this.setEntity(DaoManager.get(getType(), this.getEntityEditId()));
                    if(!Hibernate.isInitialized(this.getEntity().getLandChargesRegistries()))
                        Hibernate.initialize(this.getEntity().getLandChargesRegistries());

                    List<LandChargesRegistry> sourceRegistry = DaoManager
                            .load(LandChargesRegistry.class);
                    List<LandChargesRegistry> targetRegistry = new ArrayList<>();

                    if (!ValidationHelper
                            .isNullOrEmpty(this.getEntity().getLandChargesRegistries())) {
                        targetRegistry = this.getEntity().getLandChargesRegistries();

                        for (LandChargesRegistry registry : targetRegistry) {
                            if (sourceRegistry.contains(registry)) {
                                sourceRegistry.remove(registry);
                            }
                        }
                    }
                    this.setRegistries(new DualListModel<>(sourceRegistry, targetRegistry));
                    DaoManager.getSession().evict(this.getEntity());
                } catch (Exception e) {
                    LogHelper.log(log, e);
                }
            }
            RequestContext.getCurrentInstance().update("addAggregationLandChargesRegistryDialog");
            executeJS("PF('addAggregationLandChargesRegistryDialogWV').show();");
        }
    }

    @Override
    public void addEntity() throws HibernateException, InstantiationException, IllegalAccessException, PersistenceBeanException {
        if (this.getCanCreate()) {
            List<LandChargesRegistry> sourceRegistry = DaoManager
                    .load(LandChargesRegistry.class);
            this.setRegistries(new DualListModel<>(sourceRegistry, new ArrayList<>()));
            setEntity(new AggregationLandChargesRegistry());
            this.cleanValidation();
            RequestContext.getCurrentInstance().update("addAggregationLandChargesRegistryDialog");
            executeJS("PF('addAggregationLandChargesRegistryDialogWV').show();");
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
        executeJS("PF('addAggregationLandChargesRegistryDialogWV').hide()");
        executeJS("refreshTable()");
    }

    protected void validate() throws PersistenceBeanException, IllegalAccessException {
        if (ValidationHelper.isNullOrEmpty(this.getEntity().getName())) {
            addRequiredFieldException("form:name");
        } else if (!ValidationHelper.isUnique(
                AggregationLandChargesRegistry.class, "name",
                getEntity().getName(), this.getEntity().getId())) {
            addFieldException("form:name", "nameAlreadyInUse");
        }

        if (ValidationHelper.isNullOrEmpty(this.getRegistries().getTarget())
                || this.getRegistries().getTarget().size() < 1) {
            addRequiredFieldException("form:registries");
        }
    }

    public void resetFields() throws PersistenceBeanException, IllegalAccessException, InstantiationException {
        setEntity(new AggregationLandChargesRegistry());
        this.cleanValidation();
        this.filterTableFromPanel();
    }

    @Override
    public void deleteEntity() throws HibernateException, PersistenceBeanException, InstantiationException, IllegalAccessException, NumberFormatException, IOException {
        this.setEntity(DaoManager.get(getType(), this.getEntityDeleteId()));
        getEntity().setIsDeleted(Boolean.TRUE);
        saveEntity();
        filterTableFromPanel();
    }

    private void saveEntity() {
        Transaction tr = null;
        try {
            tr = PersistenceSessionManager.getBean().getSession()
                    .beginTransaction();
            this.getEntity()
                    .setLandChargesRegistries(this.getRegistries().getTarget());
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
