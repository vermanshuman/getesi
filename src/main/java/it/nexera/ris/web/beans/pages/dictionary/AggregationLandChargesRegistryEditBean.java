package it.nexera.ris.web.beans.pages.dictionary;

import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.helpers.ValidationHelper;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.AggregationLandChargesRegistry;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.LandChargesRegistry;
import it.nexera.ris.web.beans.EntityEditPageBean;
import org.hibernate.HibernateException;
import org.primefaces.model.DualListModel;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@ManagedBean(name = "aggregationLandChargesRegistryEditBean")
@ViewScoped
public class AggregationLandChargesRegistryEditBean
        extends EntityEditPageBean<AggregationLandChargesRegistry>
        implements Serializable {

    private static final long serialVersionUID = 7483808500484135913L;

    private DualListModel<LandChargesRegistry> registries;

    @Override
    public void onLoad() throws NumberFormatException, HibernateException,
            PersistenceBeanException, InstantiationException,
            IllegalAccessException {
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
    }

    @Override
    public void onValidate() throws PersistenceBeanException,
            HibernateException, IllegalAccessException {
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

    @Override
    public void onSave() throws HibernateException, PersistenceBeanException,
            NumberFormatException, IOException, InstantiationException,
            IllegalAccessException {
        this.getEntity()
                .setLandChargesRegistries(this.getRegistries().getTarget());

        DaoManager.save(getEntity());
    }

    public DualListModel<LandChargesRegistry> getRegistries() {
        return registries;
    }

    public void setRegistries(DualListModel<LandChargesRegistry> registries) {
        this.registries = registries;
    }

}
