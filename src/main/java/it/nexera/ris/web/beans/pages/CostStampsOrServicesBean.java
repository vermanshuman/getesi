package it.nexera.ris.web.beans.pages;

import it.nexera.ris.common.enums.PageTypes;
import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.helpers.RedirectHelper;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.CostStampsOrServices;
import it.nexera.ris.web.beans.EntityEditPageBean;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.HibernateException;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import java.io.IOException;
import java.io.Serializable;
import java.util.List;

@ManagedBean(name = "costStampsOrServicesBean")
@ViewScoped
@Getter
@Setter
public class CostStampsOrServicesBean extends EntityEditPageBean<CostStampsOrServices>
        implements Serializable  {

    private static final long serialVersionUID = 9025554059225176866L;

    @Override
    public void onLoad() throws NumberFormatException, HibernateException,
            PersistenceBeanException, InstantiationException,
            IllegalAccessException {
        List<CostStampsOrServices> costStampsOrServicesList = DaoManager.load(CostStampsOrServices.class);
        if(costStampsOrServicesList != null && costStampsOrServicesList.size()>0) {
            setEntity(costStampsOrServicesList.get(0));
        } else {
            setEntity(new CostStampsOrServices());
        }
    }

    @Override
    public void onValidate() throws PersistenceBeanException, HibernateException, IllegalAccessException {
    }

    @Override
    public void onSave() throws HibernateException, PersistenceBeanException, NumberFormatException, IOException, InstantiationException, IllegalAccessException {
        DaoManager.save(this.getEntity());
        onLoad();
    }

    @Override
    public void goBack() {
        executeJS("history.go(-1);");
    }
}
