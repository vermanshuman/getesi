package it.nexera.ris.web.beans.pages;

import it.nexera.ris.common.enums.*;
import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.helpers.*;
import it.nexera.ris.persistence.beans.dao.CriteriaAlias;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.domain.Client;
import it.nexera.ris.persistence.beans.entities.domain.Document;
import it.nexera.ris.persistence.beans.entities.domain.PaymentType;
import it.nexera.ris.persistence.beans.entities.domain.Request;
import it.nexera.ris.persistence.beans.entities.domain.User;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.Office;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.RequestType;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.Service;
import it.nexera.ris.persistence.view.RequestView;
import it.nexera.ris.web.beans.EntityLazyInListEditPageBean;
import it.nexera.ris.web.beans.EntityLazyListPageBean;
import it.nexera.ris.web.beans.wrappers.logic.RequestStateWrapper;
import it.nexera.ris.web.beans.wrappers.logic.UserFilterWrapper;
import org.hibernate.HibernateException;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.hibernate.sql.JoinType;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.model.SelectItem;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@ManagedBean(name = "paymentTypeListBean")
@ViewScoped
public class PaymentTypeListBean extends EntityLazyInListEditPageBean<PaymentType>
        implements Serializable {

	private static final long serialVersionUID = 1L;

	@Override
	public void onLoad() throws NumberFormatException, HibernateException, PersistenceBeanException,
			InstantiationException, IllegalAccessException, IOException {
		this.loadList(PaymentType.class, new Criterion[] { },
				new Order[]
		                {Order.asc("description")} );
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
