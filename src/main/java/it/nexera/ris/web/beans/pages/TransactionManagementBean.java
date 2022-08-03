package it.nexera.ris.web.beans.pages;

import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.persistence.beans.entities.domain.Request;
import it.nexera.ris.web.beans.EntityEditPageBean;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.HibernateException;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import java.io.IOException;
import java.io.Serializable;
import java.util.Date;

@Setter
@Getter
@ManagedBean
@RequestScoped
public class TransactionManagementBean extends EntityEditPageBean<Request> implements Serializable {

    private static final long serialVersionUID = 5504028298870066325L;
    private int activeTabIndex;
    private Date birthDate;

    @Override
    public void onLoad() throws NumberFormatException, HibernateException,
            PersistenceBeanException, InstantiationException, IllegalAccessException {
        setBirthDate(new Date());
    }

    @Override
    public void onValidate() throws PersistenceBeanException, HibernateException, IllegalAccessException {

    }

    @Override
    public void onSave() throws HibernateException, PersistenceBeanException,
            NumberFormatException, IOException, InstantiationException, IllegalAccessException {
    }
}
