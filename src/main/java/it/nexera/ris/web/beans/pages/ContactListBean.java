package it.nexera.ris.web.beans.pages;

import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.persistence.beans.entities.domain.readonly.ClientShort;
import it.nexera.ris.web.beans.EntityLazyListPageBean;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.HibernateException;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import java.io.IOException;
import java.io.Serializable;

@ManagedBean(name = "contactListBean")
@ViewScoped
@Getter
@Setter
public class ContactListBean extends EntityLazyListPageBean<ClientShort> implements Serializable {

    private Boolean showSearch;

    private String searchValue;


    @Override
    public void onLoad() throws NumberFormatException, HibernateException, PersistenceBeanException, InstantiationException, IllegalAccessException, IOException {
        setShowSearch(Boolean.FALSE);

    }

    public void search() throws PersistenceBeanException, InstantiationException, IllegalAccessException {
        setShowSearch(Boolean.TRUE);
    }
}