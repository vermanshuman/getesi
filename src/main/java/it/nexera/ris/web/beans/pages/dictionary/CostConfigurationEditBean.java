package it.nexera.ris.web.beans.pages.dictionary;

import com.sun.faces.application.resource.ResourceHelper;
import it.nexera.ris.common.enums.CostType;
import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.helpers.ComboboxHelper;
import it.nexera.ris.common.helpers.SessionHelper;
import it.nexera.ris.common.helpers.ValidationHelper;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.CostConfiguration;
import it.nexera.ris.web.beans.EntityEditPageBean;
import org.hibernate.HibernateException;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.model.SelectItem;
import java.io.IOException;
import java.io.Serializable;
import java.util.List;

@ManagedBean(name = "costConfigurationEditBean")
@ViewScoped
public class CostConfigurationEditBean
        extends EntityEditPageBean<CostConfiguration> implements Serializable {

    private static final long serialVersionUID = -3393148662394593827L;

    private List<SelectItem> costTypes;

    private boolean onlyView;

    @Override
    public void onLoad() throws NumberFormatException, HibernateException,
            PersistenceBeanException, InstantiationException,
            IllegalAccessException {
        if (SessionHelper.get("only_view_cost_configuration") != null
                && (Boolean) SessionHelper.get("only_view_cost_configuration")) {
            SessionHelper.removeObject("only_view_cost_configuration");
            setOnlyView(true);
        }

        setCostTypes(ComboboxHelper.fillList(CostType.class, false));
    }

    @Override
    public void onValidate() throws PersistenceBeanException,
            HibernateException, IllegalAccessException {
        if (ValidationHelper.isNullOrEmpty(this.getEntity().getName())) {
            addRequiredFieldException("form:name");
        } else if (!ValidationHelper.isUnique(CostConfiguration.class, "name",
                getEntity().getName(), this.getEntity().getId())) {
            addFieldException("form:name", "nameAlreadyInUse");
        }

        if (ValidationHelper.isNullOrEmpty(this.getEntity().getTypeId())) {
            addRequiredFieldException("form:type");
        }
    }

    @Override
    public void onSave() throws HibernateException, PersistenceBeanException,
            NumberFormatException, IOException, InstantiationException,
            IllegalAccessException {
        DaoManager.save(getEntity());
    }

    public void editCostConfiguration() {
        this.setOnlyView(false);
    }

    public List<SelectItem> getCostTypes() {
        return costTypes;
    }

    public void setCostTypes(List<SelectItem> costTypes) {
        this.costTypes = costTypes;
    }

    public boolean isOnlyView() {
        return onlyView;
    }

    public void setOnlyView(boolean onlyView) {
        this.onlyView = onlyView;
    }

}
