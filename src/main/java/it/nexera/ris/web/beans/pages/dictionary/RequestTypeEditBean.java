package it.nexera.ris.web.beans.pages.dictionary;

import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.helpers.ComboboxHelper;
import it.nexera.ris.common.helpers.LogHelper;
import it.nexera.ris.common.helpers.ValidationHelper;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.AggregationLandChargesRegistry;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.Court;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.RequestType;
import it.nexera.ris.web.beans.EntityEditPageBean;
import it.nexera.ris.web.beans.wrappers.Icon;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.HibernateException;
import org.hibernate.criterion.Order;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.model.SelectItem;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

@ManagedBean(name = "requestTypeEditBean")
@ViewScoped
@Getter
@Setter
public class RequestTypeEditBean extends EntityEditPageBean<RequestType>
        implements Serializable {

    private static final long serialVersionUID = 8308787058600926365L;

    private Icon icon;

    private List<Icon> icons;

    private List<SelectItem> landAggregations;

    private Long aggregationFilterId;

    @Override
    public void onLoad() throws NumberFormatException, HibernateException,
            PersistenceBeanException, InstantiationException,
            IllegalAccessException {
        fillIconList();
        setAggregationFilterId(aggregationFilterId);
        setLandAggregations(ComboboxHelper.fillList(AggregationLandChargesRegistry.class, Order.asc("name"), Boolean.TRUE));
    }

    @Override
    public void onValidate() throws PersistenceBeanException,
            HibernateException, IllegalAccessException {
        if (ValidationHelper.isNullOrEmpty(this.getEntity().getName())) {
            addRequiredFieldException("form:name");
        } else if (!ValidationHelper.isUnique(RequestType.class, "name",
                getEntity().getName(), this.getEntity().getId())) {
            addFieldException("form:name", "nameAlreadyInUse");
        }
    }

    @Override
    public void onSave() throws HibernateException, PersistenceBeanException,
            NumberFormatException, IOException, InstantiationException,
            IllegalAccessException {
        if (!ValidationHelper.isNullOrEmpty(getAggregationFilterId()))
            this.getEntity().setDefault_registry(DaoManager.get(AggregationLandChargesRegistry.class, getAggregationFilterId()));
        DaoManager.save(this.getEntity());
    }

    private void fillIconList() {
        setIcons(new ArrayList<>());
        InputStream in = getClass().getResourceAsStream("/faList");
        try (BufferedReader br = new BufferedReader(new InputStreamReader(in))) {
            String line = null;
            while ((line = br.readLine()) != null) {
                getIcons().add(new Icon(line));
            }
            setIcon(getIcons().get(0));
        } catch (Exception e) {
            LogHelper.log(log, e);
        }
    }
}
