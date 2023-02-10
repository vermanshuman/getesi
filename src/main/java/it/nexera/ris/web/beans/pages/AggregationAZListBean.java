package it.nexera.ris.web.beans.pages;

import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.helpers.ComboboxHelper;
import it.nexera.ris.common.helpers.LogHelper;
import it.nexera.ris.common.helpers.ValidationHelper;
import it.nexera.ris.common.xml.wrappers.CitySelectItem;
import it.nexera.ris.common.xml.wrappers.ServiceSelectItem;
import it.nexera.ris.persistence.PersistenceSessionManager;
import it.nexera.ris.persistence.beans.dao.CriteriaAlias;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.domain.AggregationService;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.AggregationAZ;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.City;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.Service;
import it.nexera.ris.web.beans.EntityLazyListPageBean;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.HibernateException;
import org.hibernate.Transaction;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.hibernate.sql.JoinType;
import org.primefaces.context.RequestContext;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.model.SelectItem;
import java.io.IOException;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

@ManagedBean(name = "aggregationAZListBean")
@ViewScoped
@Getter
@Setter
public class AggregationAZListBean extends EntityLazyListPageBean<AggregationAZ>
        implements Serializable  {

    private String code;

    private String requestType;

    private AggregationAZ entity;

    private List<ServiceSelectItem> services;

    private List<ServiceSelectItem> selectedService;

    private AggregationService aggregationService;

    private static final long serialVersionUID = 9025554059225176866L;

    @Override
    public void onLoad() throws NumberFormatException, HibernateException,
            PersistenceBeanException, InstantiationException,
            IllegalAccessException, IOException {
        setEntity(new AggregationAZ());
        List<Service> services = DaoManager.load(Service.class, new Criterion[]{},Order.asc("name"));
        setServices(services.stream().map(ServiceSelectItem::new).collect(Collectors.toList()));
        filterTableFromPanel();
    }

    public void filterTableFromPanel() throws PersistenceBeanException, IllegalAccessException, InstantiationException {
        List<Criterion> restrictions = new ArrayList<>();
        restrictions.add(
                Restrictions.or(Restrictions.eq("isDeleted", Boolean.FALSE),
                        Restrictions.isNull("isDeleted")));
        if (!ValidationHelper.isNullOrEmpty(getCode()) && !getCode().equals(BigDecimal.ZERO)) {
            restrictions.add(Restrictions.eq("code", getCode()));
        }
        if (!ValidationHelper.isNullOrEmpty(getRequestType())) {
            restrictions.add(Restrictions.ilike("description", getRequestType(), MatchMode.ANYWHERE));
        }
        this.loadList(AggregationAZ.class, restrictions.toArray(new Criterion[0]), new Order[]
                {Order.asc("code")});
    }

    public void clearFilterPanel() throws PersistenceBeanException, IllegalAccessException, InstantiationException {
        setCode(null);
        setRequestType(null);
        filterTableFromPanel();
    }

    @Override
    public void editEntity() throws HibernateException, InstantiationException, IllegalAccessException, PersistenceBeanException {
        if (this.getCanEdit()) {
            this.cleanValidation();
            setSelectedService(null);
            if (!ValidationHelper.isNullOrEmpty(this.getEntityEditId())) {
                try {
                    this.setEntity(DaoManager.get(getType(), this.getEntityEditId()));
                    DaoManager.getSession().evict(this.getEntity());
                    setAggregationService(DaoManager.get(AggregationService.class,
                            new CriteriaAlias[]{
                                    new CriteriaAlias("aggregationAZ", "a", JoinType.INNER_JOIN)
                            },
                            new Criterion[]{
                                    Restrictions.eq("a.id", getEntity().getId())}));
                    if(!ValidationHelper.isNullOrEmpty(getAggregationService()) &&
                            !ValidationHelper.isNullOrEmpty(getAggregationService().getServices())){
                        List<ServiceSelectItem> selectedServices = new LinkedList<>();
                        for (Service service : aggregationService.getServices()) {
                            ServiceSelectItem item = new ServiceSelectItem(service);
                            if(!selectedServices.contains(item)) {
                                selectedServices.add(item);
                            }
                        }
                        setSelectedService(selectedServices);
                    }
                } catch (Exception e) {
                    LogHelper.log(log, e);
                }
            }
            RequestContext.getCurrentInstance().update("addAggregationAzDialog");
            executeJS("PF('addAggregationAzDialogWV').show();");
        }
    }

    @Override
    public void addEntity() throws HibernateException, InstantiationException, IllegalAccessException, PersistenceBeanException {
        if (this.getCanCreate()) {
            this.cleanValidation();
            setEntity(new AggregationAZ());
            setSelectedService(new LinkedList<>());
            setAggregationService(new AggregationService());
            RequestContext.getCurrentInstance().update("addAggregationAzDialog");
            executeJS("PF('addAggregationAzDialogWV').show();");
        }
    }

    protected void validate() throws PersistenceBeanException {
        if(ValidationHelper.isNullOrEmpty(getEntity().getCode())) {
            addRequiredFieldException("form:code");
        }
        if(ValidationHelper.isNullOrEmpty(this.getSelectedService())) {
            addRequiredFieldException("form:services");
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
        executeJS("PF('addAggregationAzDialogWV').hide()");
        executeJS("refreshTable()");
    }

    private void saveEntity() throws PersistenceBeanException, IllegalAccessException {
        Transaction tr = null;
        try {
            tr = PersistenceSessionManager.getBean().getSession()
                    .beginTransaction();
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

        if(ValidationHelper.isNullOrEmpty(getEntity().getIsDeleted())){
            if(ValidationHelper.isNullOrEmpty(getAggregationService())){
                setAggregationService(new AggregationService());
            }
            getAggregationService().setAggregationAZ(getEntity());
            if(!ValidationHelper.isNullOrEmpty(getSelectedService())){
                List<Service> services = DaoManager.load(Service.class,
                        new Criterion[] {
                                Restrictions.in("id", getSelectedService().stream()
                                        .map(ServiceSelectItem::getId).collect(Collectors.toList()))
                        });
                if(!ValidationHelper.isNullOrEmpty(getAggregationService().getServices())){
                    List<Service> removedServices = getAggregationService().getServices().stream()
                            .filter(s -> !services.contains(s))
                            .collect(Collectors.toList());
                    removedServices
                            .stream()
                            .forEach(s ->
                                    getAggregationService().removeService(s));

                    List<Service> newServices = services.stream()
                            .filter(s -> !getAggregationService().getServices().contains(s))
                            .collect(Collectors.toList());
                    newServices
                            .stream()
                            .forEach(s ->
                                    getAggregationService().addService(s));

                }else {
                    if(ValidationHelper.isNullOrEmpty(getAggregationService().getServices()))
                        getAggregationService().setServices(new ArrayList<>());
                    for (Service service :  services) {
                        getAggregationService().addService(service);
                    }
                }
            }
            DaoManager.save(getAggregationService(), true);
        }

    }

    public void resetFields() throws PersistenceBeanException, IllegalAccessException, InstantiationException {
       setEntity(new AggregationAZ());
       this.cleanValidation();
       this.filterTableFromPanel();
    }

    @Override
    public void deleteEntity() throws HibernateException, PersistenceBeanException, InstantiationException, IllegalAccessException, NumberFormatException, IOException {
        this.setEntity(DaoManager.get(getType(), this.getEntityDeleteId()));
        AggregationService aggregationService = DaoManager.get(AggregationService.class,
                new CriteriaAlias[]{
                        new CriteriaAlias("aggregationAZ", "a", JoinType.INNER_JOIN)
                },
                new Criterion[]{
                        Restrictions.eq("a.id", getEntity().getId())});
        aggregationService.setIsDeleted(Boolean.TRUE);
        if(!ValidationHelper.isNullOrEmpty(aggregationService.getServices())){
            List<Service> deletedServices = new ArrayList<>(aggregationService.getServices());
            for(Service s : deletedServices){
                aggregationService.removeService(s);
            }
        }
        aggregationService.setServices(null);
        getEntity().setIsDeleted(Boolean.TRUE);
        saveEntity();
        filterTableFromPanel();
    }
}
