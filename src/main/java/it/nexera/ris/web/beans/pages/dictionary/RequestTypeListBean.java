package it.nexera.ris.web.beans.pages.dictionary;

import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.helpers.ComboboxHelper;
import it.nexera.ris.common.helpers.LogHelper;
import it.nexera.ris.common.helpers.ValidationHelper;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.domain.Request;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.AggregationLandChargesRegistry;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.RequestType;
import it.nexera.ris.web.beans.EntityLazyInListEditPageBean;
import it.nexera.ris.web.beans.wrappers.Icon;
import org.hibernate.HibernateException;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.model.SelectItem;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

@ManagedBean(name = "requestTypeListBean")
@ViewScoped
public class RequestTypeListBean extends
        EntityLazyInListEditPageBean<RequestType> implements Serializable {

    private static final long serialVersionUID = -6260035111902337296L;

    private Icon icon;

    private List<Icon> icons;
    
    private List<SelectItem> landAggregations;
    
    private Long aggregationFilterId;

    @Override
    protected void setEditedValues() {
        this.getEditedEntity().setName(this.getEntity().getName());
        this.getEditedEntity()
                .setDescription(this.getEntity().getDescription());
        this.getEditedEntity().setIcon(this.getEntity().getIcon());
        this.getEditedEntity().setDefault_registry(this.getEntity().getDefault_registry());
    }

    @Override
    protected void validate() throws PersistenceBeanException {
        if (ValidationHelper.isNullOrEmpty(this.getEntity().getName())) {
            addRequiredFieldException("form:name");
        } else if (!ValidationHelper.isUnique(RequestType.class, "name",
                getEntity().getName(), this.getEntity().getId())) {
            addFieldException("form:name", "nameAlreadyInUse");
        }
    }

    @Override
    public void save() throws HibernateException, PersistenceBeanException,
            NumberFormatException, IOException, InstantiationException,
            IllegalAccessException {
        if(!ValidationHelper.isNullOrEmpty(getAggregationFilterId()))
            this.getEntity().setDefault_registry(DaoManager.get(AggregationLandChargesRegistry.class, getAggregationFilterId()));
        DaoManager.save(this.getEntity());
    }

    @Override
    public void onLoad() throws NumberFormatException, HibernateException,
            PersistenceBeanException, InstantiationException,
            IllegalAccessException, IOException {
        fillIconList();
        setAggregationFilterId(aggregationFilterId);
        setLandAggregations(ComboboxHelper.fillList(AggregationLandChargesRegistry.class, Order.asc("name"), Boolean.TRUE));
        this.loadList(RequestType.class, new Criterion[]
                {Restrictions.or(Restrictions.eq("isDeleted", Boolean.FALSE),
                        Restrictions.isNull("isDeleted"))}, new Order[]
                {Order.asc("name")});
    }

    @Override
    protected void deleteEntityInternal(Long id)
            throws HibernateException, PersistenceBeanException,
            InstantiationException, IllegalAccessException {
        try {
            super.deleteEntityInternal(id);
        } catch (Exception e) {
            try {
                this.getEntity().setIsDeleted(Boolean.TRUE);

                DaoManager.save(getEntity());
            } catch (Exception e1) {
                LogHelper.log(log, e1);
            }
        }
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

    @Override
    public void editEntity() {
        this.cleanValidation();
        setAggregationFilterId(null);
        executeJS("setIcon();");
        if (!ValidationHelper.isNullOrEmpty(this.getEntityEditId())) {
            try {
                this.setEntity(DaoManager.get(getType(), this.getEntityEditId()));
                DaoManager.getSession().evict(this.getEntity());
                setAggregationFilterId(this.getEntity().getDefault_registry().getId());
            } catch (Exception e) {
                LogHelper.log(log, e);
            }
        }
    }
    
    @Override
    public void resetFields() {
        setAggregationFilterId(null);
        super.resetFields();
    }

    public Icon getIcon() {
        return icon;
    }

    public void setIcon(Icon icon) {
        this.icon = icon;
    }

    public List<Icon> getIcons() {
        return icons;
    }

    public void setIcons(List<Icon> icons) {
        this.icons = icons;
    }

    public List<SelectItem> getLandAggregations() {
        return landAggregations;
    }

    public void setLandAggregations(List<SelectItem> landAggregations) {
        this.landAggregations = landAggregations;
    }

    public Long getAggregationFilterId() {
        return aggregationFilterId;
    }

    public void setAggregationFilterId(Long aggregationFilterId) {
        this.aggregationFilterId = aggregationFilterId;
    }

}
