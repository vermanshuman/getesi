package it.nexera.ris.web.beans.pages.dictionary;

import it.nexera.ris.common.enums.LandChargesRegistryType;
import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.helpers.ComboboxHelper;
import it.nexera.ris.common.helpers.LogHelper;
import it.nexera.ris.common.helpers.ValidationHelper;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.City;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.LandChargesRegistry;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.Province;
import it.nexera.ris.web.beans.EntityEditPageBean;
import org.hibernate.HibernateException;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.primefaces.model.DualListModel;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.model.SelectItem;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@ManagedBean(name = "landChargesRegistryEditBean")
@ViewScoped
public class LandChargesRegistryEditBean extends EntityEditPageBean<LandChargesRegistry> implements Serializable {

    private static final long serialVersionUID = -4597370884558523590L;

    private List<SelectItem> types;

    private DualListModel<Province> provinces;

    private DualListModel<City> cities;

    @Override
    public void onLoad() throws NumberFormatException, HibernateException,
            PersistenceBeanException, InstantiationException,
            IllegalAccessException {
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
        if (ValidationHelper.isNullOrEmpty(getEntityId())) {
            this.getEntity().setVisualize(true);
        }

    }

    @Override
    public void onValidate() throws PersistenceBeanException,
            HibernateException, IllegalAccessException {
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

    @Override
    public void onSave() throws HibernateException, PersistenceBeanException,
            NumberFormatException, IOException, InstantiationException,
            IllegalAccessException {
        this.getEntity().setProvinces(getProvinces().getTarget());
        this.getEntity().setCities(getCities().getTarget());

        DaoManager.save(getEntity());
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

    public List<SelectItem> getTypes() {
        return types;
    }

    public void setTypes(List<SelectItem> types) {
        this.types = types;
    }

    public DualListModel<Province> getProvinces() {
        return provinces;
    }

    public void setProvinces(DualListModel<Province> provinces) {
        this.provinces = provinces;
    }

    public DualListModel<City> getCities() {
        return cities;
    }

    public void setCities(DualListModel<City> cities) {
        this.cities = cities;
    }

}
