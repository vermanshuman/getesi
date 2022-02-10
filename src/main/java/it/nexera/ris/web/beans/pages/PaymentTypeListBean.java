package it.nexera.ris.web.beans.pages;

import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.helpers.LogHelper;
import it.nexera.ris.common.helpers.ValidationHelper;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.domain.PaymentType;
import it.nexera.ris.web.beans.EntityLazyInListEditPageBean;
import org.hibernate.HibernateException;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import java.io.IOException;
import java.io.Serializable;

@ManagedBean(name = "paymentTypeListBean")
@ViewScoped
public class PaymentTypeListBean extends EntityLazyInListEditPageBean<PaymentType>
        implements Serializable {

    private static final long serialVersionUID = -435289789434007013L;

    @Override
    public void onLoad() throws NumberFormatException, HibernateException, PersistenceBeanException,
            InstantiationException, IllegalAccessException, IOException {
        this.loadList(PaymentType.class, new Criterion[]{},
                new Order[]
                        {Order.asc("description")});
    }

    @Override
    protected void deleteEntityInternal(Long id)
            throws HibernateException, PersistenceBeanException,
            InstantiationException, IllegalAccessException {
        try {
            super.deleteEntityInternal(id);
        } catch (Exception e) {
            try {
                DaoManager.remove(getEntity());
            } catch (Exception e1) {
                LogHelper.log(log, e1);
            }
        }
    }

    @Override
    protected void setEditedValues() {
        this.getEditedEntity().setDescription(
                this.getEntity().getDescription());
        this.getEditedEntity().setCode(
                this.getEntity().getCode());
        this.getEditedEntity().setBeneficiary(
                this.getEntity().getBeneficiary());
        this.getEditedEntity().setIban(
                this.getEntity().getIban());
        this.getEditedEntity().setIstitutionName(
                this.getEntity().getIstitutionName());
    }

    @Override
    protected void validate() throws PersistenceBeanException {
        if (ValidationHelper.isNullOrEmpty(this.getEntity().getDescription())) {
            addRequiredFieldException("form:description");
        } else if (!ValidationHelper.isUnique(PaymentType.class, "description",
                getEntity().getDescription(), this.getEntity().getId())) {
            addFieldException("form:description", "nameAlreadyInUse");
        }
    }

    @Override
    public void save() throws HibernateException, PersistenceBeanException, NumberFormatException, IOException,
            InstantiationException, IllegalAccessException {
        DaoManager.save(this.getEntity());
    }

}
