package it.nexera.ris.web.beans.pages.dictionary;

import it.nexera.ris.common.enums.LandChargesRegistryType;
import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.helpers.ComboboxHelper;
import it.nexera.ris.common.helpers.LogHelper;
import it.nexera.ris.common.helpers.ValidationHelper;
import it.nexera.ris.persistence.PersistenceSessionManager;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.domain.TaxRate;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.City;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.LandChargesRegistry;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.Province;
import it.nexera.ris.web.beans.EntityLazyListPageBean;
import lombok.Getter;
import lombok.Setter;
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
import javax.faces.model.SelectItem;
import java.io.IOException;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@ManagedBean(name = "landChargesRegistryListBean")
@ViewScoped
@Getter
@Setter
public class LandChargesRegistryListBean extends
        EntityLazyListPageBean<LandChargesRegistry> implements Serializable {

    private static final long serialVersionUID = 4380347375101763116L;

    private LandChargesRegistry entity;

    private List<SelectItem> types;

    private DualListModel<Province> provinces;

    private DualListModel<City> cities;

    private String landChargesRegistryType;

    private String name;

    private String description;

    private String symbol;

    @Override
    public void onLoad() throws NumberFormatException, HibernateException,
            PersistenceBeanException, InstantiationException,
            IllegalAccessException, IOException {
        setEntity(new LandChargesRegistry());
        filterTableFromPanel();

        setTypes(ComboboxHelper.fillList(LandChargesRegistryType.class));

        List<Province> sourceProvince = DaoManager.load(Province.class, new Criterion[]{
                Restrictions.isNotNull("description")
        }, Order.asc("description"));
        List<Province> targetProvince = new ArrayList<>();

        if (!ValidationHelper.isNullOrEmpty(getEntity().getProvinces())) {
            targetProvince = getEntity().getProvinces();

            targetProvince.stream()
                    .filter(sourceProvince::contains)
                    .forEach(sourceProvince::remove);
        }

        setProvinces(new DualListModel<>(sourceProvince, targetProvince));
        onProvinceTransfer();

        if (!ValidationHelper.isNullOrEmpty(getEntity().getCities())) {
            this.getCities().setTarget(getEntity().getCities());

            List<City> sourceCity = this.getCities().getSource();

            this.getCities().getTarget().stream()
                    .filter(sourceCity::contains)
                    .forEach(sourceCity::remove);

            this.getCities().setSource(sourceCity);

        }
        if (ValidationHelper.isNullOrEmpty(getEntity().getId())) {
            this.getEntity().setVisualize(true);
        }
    }

    public void onProvinceTransfer() {
        if (getCities() == null) {
            setCities(new DualListModel<>(new ArrayList<>(), new ArrayList<>()));
        }

        if (getProvinces() != null && !ValidationHelper.isNullOrEmpty(getProvinces().getTarget())) {
            try {
                List<Long> provinceIds = getProvinces().getTarget().stream()
                        .map(Province::getId).collect(Collectors.toList());
                List<City> sourceCity = DaoManager.load(City.class, new Criterion[]{
                        Restrictions.in("province.id", provinceIds)
                });
                List<City> targetCity = new ArrayList<>();

                getCities().setSource(sourceCity);
                getCities().setTarget(targetCity);
            } catch (Exception e) {
                LogHelper.log(log, e);
            }
        }
    }

    public void filterTableFromPanel() throws PersistenceBeanException, IllegalAccessException, InstantiationException {
        List<Criterion> restrictions = new ArrayList<>();

        if (!ValidationHelper.isNullOrEmpty(getName())) {
            restrictions.add(Restrictions.eq("name", getName()));
        }
        if (!ValidationHelper.isNullOrEmpty(getDescription())) {
            restrictions.add(Restrictions.eq("description", getDescription()));
        }
        if (!ValidationHelper.isNullOrEmpty(getSymbol())) {
            restrictions.add(Restrictions.eq("symbol", getSymbol()));
        }
        if (!ValidationHelper.isNullOrEmpty(getLandChargesRegistryType())) {

            restrictions.add(Restrictions.eq("type",
                    LandChargesRegistryType.valueOf(getLandChargesRegistryType())));
        }
        restrictions.add(Restrictions.or(Restrictions.eq("isDeleted", Boolean.FALSE),
                Restrictions.isNull("isDeleted")));
        this.loadList(LandChargesRegistry.class, restrictions.toArray(new Criterion[0]), new Order[]
                {Order.asc("name")});
    }

    public void clearFilterPanel() throws PersistenceBeanException, IllegalAccessException, InstantiationException {
        setName(null);
        setDescription(null);
        setSymbol(null);
        setLandChargesRegistryType(null);
        filterTableFromPanel();
    }

    @Override
    public void editEntity() throws HibernateException, InstantiationException, IllegalAccessException, PersistenceBeanException {
        if (this.getCanEdit()) {
            this.cleanValidation();
            if (!ValidationHelper.isNullOrEmpty(this.getEntityEditId())) {
                try {
                    this.setEntity(DaoManager.get(getType(), this.getEntityEditId()));
                    DaoManager.getSession().evict(this.getEntity());
                } catch (Exception e) {
                    LogHelper.log(log, e);
                }
            }
            RequestContext.getCurrentInstance().update("addLandChargesRegistryDialog");
            executeJS("PF('addLandChargesRegistryDialogWV').show();");
        }
    }

    @Override
    public void addEntity() throws HibernateException, InstantiationException, IllegalAccessException, PersistenceBeanException {
        if (this.getCanCreate()) {
            this.cleanValidation();
            setEntity(new LandChargesRegistry());
            RequestContext.getCurrentInstance().update("addLandChargesRegistryDialog");
            executeJS("PF('addLandChargesRegistryDialogWV').show();");
        }
    }

    protected void validate() throws PersistenceBeanException, IllegalAccessException {
        if (ValidationHelper.isNullOrEmpty(this.getEntity().getName())) {
            addRequiredFieldException("form:name");
        }
        if (ValidationHelper.isNullOrEmpty(this.getEntity().getType())) {
            addRequiredFieldException("form:type");
        }
        Long count = DaoManager.getCount(
                LandChargesRegistry.class,
                "type",
                null,
                new Criterion[]
                        {Restrictions.eq("type", this.getEntity().getType()),
                                Restrictions.eq("name", this.getEntity().getName()),
                                Restrictions.ne("id", this.getEntity().getId() == null ? 0 : this.getEntity().getId())});

        if(count > 0){
            addException("nameTypeAlreadyInUse");
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
        executeJS("PF('addLandChargesRegistryDialogWV').hide()");
        executeJS("refreshTable()");
    }

    private void saveEntity() {
        Transaction tr = null;
        try {
            tr = PersistenceSessionManager.getBean().getSession()
                    .beginTransaction();
            this.getEntity().setProvinces(getProvinces().getTarget());
            this.getEntity().setCities(getCities().getTarget());
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

    public void resetFields() throws PersistenceBeanException, IllegalAccessException, InstantiationException {
        setEntity(new LandChargesRegistry());
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
}
