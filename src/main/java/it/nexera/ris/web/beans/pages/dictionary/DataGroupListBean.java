package it.nexera.ris.web.beans.pages.dictionary;

import java.io.IOException;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import org.hibernate.HibernateException;
import org.hibernate.Transaction;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.hibernate.exception.ConstraintViolationException;

import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.helpers.LogHelper;
import it.nexera.ris.common.helpers.MessageHelper;
import it.nexera.ris.common.helpers.ResourcesHelper;
import it.nexera.ris.persistence.PersistenceSessionManager;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.domain.DataGroupInputCard;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.DataGroup;
import it.nexera.ris.web.beans.EntityLazyListPageBean;

@ManagedBean
@ViewScoped
public class DataGroupListBean extends EntityLazyListPageBean<DataGroup> {

	private static final long serialVersionUID = 3596252090879926457L;

	@Override
    public void onLoad() throws NumberFormatException, HibernateException, PersistenceBeanException,
            InstantiationException, IllegalAccessException, IOException {
        this.loadList(DataGroup.class, new Criterion[]{
				Restrictions.or(
						Restrictions.eq("isDeleted", Boolean.FALSE),
						Restrictions.isNull("isDeleted"))
		}, new Order[]{Order.desc("createDate")});
    }
	
	@Override
	public void deleteEntity() throws HibernateException, PersistenceBeanException, InstantiationException,
	IllegalAccessException, NumberFormatException, IOException {
		  try {
              DaoManager.getSession().clear();
              DataGroup dataGroup = DaoManager.get(DataGroup.class, getEntityDeleteId());
              dataGroup.setIsDeleted(Boolean.TRUE);
              DaoManager.save(dataGroup, true);
          } catch (Exception e1) {
              LogHelper.log(log, e1);
          }
	}
}
