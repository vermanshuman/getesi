package it.nexera.ris.web.beans.pages;

import java.io.IOException;
import java.io.Serializable;

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
import it.nexera.ris.persistence.beans.entities.domain.InputCard;
import it.nexera.ris.web.beans.EntityLazyListPageBean;

@ManagedBean(name = "inputCardListBean")
@ViewScoped
public class InputCardListBean extends EntityLazyListPageBean<InputCard> implements Serializable {

	private static final long serialVersionUID = -2489023023433319088L;

	@Override
	public void onLoad() throws NumberFormatException, HibernateException, PersistenceBeanException,
	InstantiationException, IllegalAccessException, IOException {
		loadList(InputCard.class, new Criterion[]{
				Restrictions.or(
						Restrictions.eq("isDeleted", Boolean.FALSE),
						Restrictions.isNull("isDeleted"))
		}, new Order[]{Order.desc("name")});
	}

	@Override
	public void deleteEntity() throws HibernateException, PersistenceBeanException, InstantiationException,
	IllegalAccessException, NumberFormatException, IOException {
		  try {
              DaoManager.getSession().clear();
              InputCard inputCard = DaoManager.get(InputCard.class, getEntityDeleteId());
              inputCard.setIsDeleted(Boolean.TRUE);
              DaoManager.save(inputCard, true);
          } catch (Exception e1) {
              LogHelper.log(log, e1);
          }
	}
}
