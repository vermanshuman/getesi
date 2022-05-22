package it.nexera.ris.web.beans.pages;

import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.helpers.ValidationHelper;
import it.nexera.ris.persistence.beans.entities.domain.Iban;
import it.nexera.ris.web.beans.EntityLazyListPageBean;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.HibernateException;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@ManagedBean(name = "ibanListBean")
@ViewScoped
@Getter
@Setter
public class IbanListBean extends EntityLazyListPageBean<Iban>
        implements Serializable {

    private static final long serialVersionUID = -1497288251321029451L;

    private String description;

    private String bankName;

    private String bankAccountAddress;

    @Override
    public void onLoad() throws NumberFormatException, HibernateException, PersistenceBeanException,
            InstantiationException, IllegalAccessException, IOException {
        filterTableFromPanel();
    }

    public void filterTableFromPanel() throws PersistenceBeanException, IllegalAccessException, InstantiationException {
        List<Criterion> restrictions = new ArrayList<>();
        if (!ValidationHelper.isNullOrEmpty(getDescription())) {
            restrictions.add(Restrictions.ilike("description", getDescription(), MatchMode.ANYWHERE));
        }
        if (!ValidationHelper.isNullOrEmpty(getBankName())) {
            restrictions.add(Restrictions.ilike("bankName", getBankName(), MatchMode.ANYWHERE));
        }
        if (!ValidationHelper.isNullOrEmpty(getBankAccountAddress())) {
            restrictions.add(Restrictions.ilike("address", getBankAccountAddress(), MatchMode.ANYWHERE));
        }

        this.loadList(Iban.class, restrictions.toArray(new Criterion[0]),
                new Order[]
                        {Order.asc("description")});
    }
}
