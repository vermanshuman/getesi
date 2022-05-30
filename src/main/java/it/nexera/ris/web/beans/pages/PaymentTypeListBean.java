package it.nexera.ris.web.beans.pages;

import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.helpers.LogHelper;
import it.nexera.ris.common.helpers.ValidationHelper;
import it.nexera.ris.persistence.PersistenceSessionManager;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.domain.PaymentType;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.TypeFormality;
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

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@ManagedBean(name = "paymentTypeListBean")
@ViewScoped
@Getter
@Setter
public class PaymentTypeListBean extends EntityLazyListPageBean<PaymentType>
        implements Serializable {

    private static final long serialVersionUID = -2631748089590161876L;

    private String description;

    private String code;

    private String beneficiary;

    private String istitutionName;

    private String iban;
    
    private PaymentType entity;

    @Override
    public void onLoad() throws NumberFormatException, HibernateException, PersistenceBeanException,
            InstantiationException, IllegalAccessException, IOException {
        filterTableFromPanel();
        setEntity(new PaymentType());
    }

    public void filterTableFromPanel() throws PersistenceBeanException, IllegalAccessException, InstantiationException {
        List<Criterion> restrictions = new ArrayList<>();
        if (!ValidationHelper.isNullOrEmpty(getDescription())) {
            restrictions.add(Restrictions.ilike("description", getDescription(), MatchMode.ANYWHERE));
        }
        if (!ValidationHelper.isNullOrEmpty(getCode())) {
            restrictions.add(Restrictions.ilike("code", getCode(), MatchMode.ANYWHERE));
        }
        if (!ValidationHelper.isNullOrEmpty(getBeneficiary())) {
            restrictions.add(Restrictions.ilike("beneficiary", getBeneficiary(), MatchMode.ANYWHERE));
        }
        if (!ValidationHelper.isNullOrEmpty(getIstitutionName())) {
            restrictions.add(Restrictions.ilike("istitutionName", getIstitutionName(), MatchMode.ANYWHERE));
        }
        if (!ValidationHelper.isNullOrEmpty(getIban())) {
            restrictions.add(Restrictions.ilike("iban", getIban(), MatchMode.ANYWHERE));
        }
        this.loadList(PaymentType.class, restrictions.toArray(new Criterion[0]),
                new Order[]
                        {Order.asc("description")});
    }

    public void clearFilterPanel() throws PersistenceBeanException, IllegalAccessException, InstantiationException {
        setDescription(null);
        setCode(null);
        setBeneficiary(null);
        setIban(null);
        setIstitutionName(null);
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
            RequestContext.getCurrentInstance().update("addPaymentTypeDialog");
            executeJS("PF('addPaymentTypeDialogWV').show();");
        }
    }
    
    @Override
    public void addEntity() throws HibernateException, InstantiationException, IllegalAccessException, PersistenceBeanException {
        if (this.getCanCreate()) {
            setEntity(new PaymentType());
            this.cleanValidation();
            RequestContext.getCurrentInstance().update("addPaymentTypeDialog");
            executeJS("PF('addPaymentTypeDialogWV').show();");
        }
    }
    
	public void save() throws HibernateException, PersistenceBeanException, NumberFormatException, IOException,
			InstantiationException, IllegalAccessException {
		this.cleanValidation();
		this.setValidationFailed(false);

		this.validate();
		
		if (this.getValidationFailed()) {
			return;
		}
		saveEntity();
		this.resetFields();
		executeJS("PF('addPaymentTypeDialogWV').hide()");
		executeJS("refreshTable()");
	}
	
	protected void validate() {
		if (ValidationHelper.isNullOrEmpty(this.getEntity().getDescription())) {
            addRequiredFieldException("form:description");
        } else if (!ValidationHelper.isUnique(PaymentType.class, "description",
                getEntity().getDescription(), this.getEntity().getId())) {
            addFieldException("form:description", "nameAlreadyInUse");
        }
	}
	
	public void resetFields() throws PersistenceBeanException, IllegalAccessException, InstantiationException {
        setEntity(new PaymentType());
        this.cleanValidation();
        this.filterTableFromPanel();
    }
	
	private void saveEntity() {
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
    }
}
