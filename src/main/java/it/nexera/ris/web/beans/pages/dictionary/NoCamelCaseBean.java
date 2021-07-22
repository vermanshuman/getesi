package it.nexera.ris.web.beans.pages.dictionary;

import java.io.IOException;
import java.io.Serializable;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import it.nexera.ris.persistence.beans.dao.DaoManager;
import org.hibernate.HibernateException;
import org.hibernate.criterion.Order;

import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.NoCamelCaseWord;
import it.nexera.ris.web.beans.EntityLazyInListEditPageBean;

@ManagedBean(name = "noCamelCaseBean")
@ViewScoped
public class NoCamelCaseBean extends EntityLazyInListEditPageBean<NoCamelCaseWord> implements Serializable {
    private static final long serialVersionUID = 7538786571081990789L;

    @Override
    protected void setEditedValues() {
    }

    @Override
    protected void validate() throws PersistenceBeanException {
    }

    @Override
    public void save() throws HibernateException, PersistenceBeanException, NumberFormatException, IOException,
            InstantiationException, IllegalAccessException {
        DaoManager.save(getEntity());
    }

    @Override
    public void onLoad() throws NumberFormatException, HibernateException, PersistenceBeanException,
            InstantiationException, IllegalAccessException, IOException {
        this.loadList(NoCamelCaseWord.class, new Order[]{});
    }
}