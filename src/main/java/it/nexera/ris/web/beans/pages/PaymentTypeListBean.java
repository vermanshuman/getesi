package it.nexera.ris.web.beans.pages;

import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.helpers.LogHelper;
import it.nexera.ris.common.helpers.ValidationHelper;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.domain.PaymentType;
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
}
