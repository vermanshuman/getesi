package it.nexera.ris.web.beans.pages.dictionary;

import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.helpers.LogHelper;
import it.nexera.ris.common.helpers.ValidationHelper;
import it.nexera.ris.persistence.PersistenceSessionManager;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.AggregationLandChargesRegistry;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.RequestType;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.TypeFormality;
import it.nexera.ris.web.beans.EntityLazyListPageBean;
import it.nexera.ris.web.beans.wrappers.Icon;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.HibernateException;
import org.hibernate.Transaction;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.primefaces.context.RequestContext;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.model.SelectItem;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

@ManagedBean(name = "requestTypeListBean")
@ViewScoped
@Getter
@Setter
public class RequestTypeListBean extends
        EntityLazyListPageBean<RequestType> implements Serializable {

    private static final long serialVersionUID = -6260035111902337296L;

    private String name;

    private String description;

    private RequestType entity;

    private Icon icon;

    private List<Icon> icons;

    private List<SelectItem> landAggregations;

    private Long aggregationId;

    @Override
    public void onLoad() throws NumberFormatException, HibernateException,
            PersistenceBeanException, InstantiationException,
            IllegalAccessException, IOException {
        filterTableFromPanel();
        fillIconList();
        setEntity(new RequestType());
    }

    public void filterTableFromPanel() throws PersistenceBeanException, IllegalAccessException, InstantiationException {
        List<Criterion> restrictions = new ArrayList<>();
        if (!ValidationHelper.isNullOrEmpty(getName())) {
            restrictions.add(Restrictions.ilike("name", getName(), MatchMode.ANYWHERE));
        }
        if (!ValidationHelper.isNullOrEmpty(getDescription())) {
            restrictions.add(Restrictions.ilike("description", getDescription(), MatchMode.ANYWHERE));
        }
        restrictions.add(Restrictions.or(Restrictions.eq("isDeleted", Boolean.FALSE),
                Restrictions.isNull("isDeleted")));
        this.loadList(RequestType.class, restrictions.toArray(new Criterion[0]), new Order[]
                {Order.asc("name")});
    }

    public void clearFilterPanel() throws PersistenceBeanException, IllegalAccessException, InstantiationException {
        setName(null);
        setDescription(null);
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
                    if(!ValidationHelper.isNullOrEmpty(this.getEntity().getDefault_registry()))
                        setAggregationId(this.getEntity().getDefault_registry().getId());
                } catch (Exception e) {
                    LogHelper.log(log, e);
                }
            }
            RequestContext.getCurrentInstance().update("addRequestTypeDialog");
            executeJS("PF('addRequestTypeDialogWV').show();");
        }
    }

    @Override
    public void addEntity() throws HibernateException, InstantiationException, IllegalAccessException, PersistenceBeanException {
        if (this.getCanCreate()) {
            setEntity(new RequestType());
            this.cleanValidation();
            RequestContext.getCurrentInstance().update("addRequestTypeDialog");
            executeJS("PF('addRequestTypeDialogWV').show();");
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
        executeJS("PF('addRequestTypeDialogWV').hide()");
        executeJS("refreshTable()");
    }

    protected void validate() throws PersistenceBeanException {
        if (ValidationHelper.isNullOrEmpty(this.getEntity().getName())) {
            addRequiredFieldException("form:name");
        } else if (!ValidationHelper.isUnique(RequestType.class, "name",
                getEntity().getName(), this.getEntity().getId())) {
            addFieldException("form:name", "nameAlreadyInUse");
        }
    }

    public void resetFields() throws PersistenceBeanException, IllegalAccessException, InstantiationException {
        setEntity(new RequestType());
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
            if (!ValidationHelper.isNullOrEmpty(getAggregationId()))
                this.getEntity().setDefault_registry(
                        DaoManager.get(AggregationLandChargesRegistry.class,
                                getAggregationId()));
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
