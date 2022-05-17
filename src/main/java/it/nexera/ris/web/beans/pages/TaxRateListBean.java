package it.nexera.ris.web.beans.pages;

import java.io.IOException;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import it.nexera.ris.common.helpers.ComboboxHelper;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.City;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.Court;
import it.nexera.ris.web.beans.EntityLazyListPageBean;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.HibernateException;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;

import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.helpers.LogHelper;
import it.nexera.ris.common.helpers.ValidationHelper;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.domain.TaxRate;
import it.nexera.ris.web.beans.EntityLazyInListEditPageBean;
import org.hibernate.criterion.Restrictions;

@ManagedBean(name = "taxRateListBean")
@ViewScoped
@Getter
@Setter
public class TaxRateListBean extends EntityLazyListPageBean<TaxRate>
        implements Serializable  {

    private BigDecimal percentage;

    private String description;

    private String codeSDI;

    private Integer selectedUse;

    private static final long serialVersionUID = -2398683768894736150L;

//    @Override
//    protected void setEditedValues() {
//        this.getEditedEntity().setDescription(
//                this.getEntity().getDescription());
//        this.getEditedEntity().setCodeSDI(
//                this.getEntity().getCodeSDI());
//        this.getEditedEntity().setPercentage(
//                this.getEntity().getPercentage());
//        this.getEditedEntity().setUse(
//                this.getEntity().getUse());
//    }
//
//    @Override
//    protected void validate() throws PersistenceBeanException {
//        if(ValidationHelper.isNullOrEmpty(getEntity().getPercentage())) {
//            addRequiredFieldException("form:percentage");
//        }
//        if (ValidationHelper.isNullOrEmpty(this.getEntity().getDescription())) {
//            addRequiredFieldException("form:description");
//        }
//        if(ValidationHelper.isNullOrEmpty(getEntity().getCodeSDI())) {
//            addRequiredFieldException("form:codeSDI");
//        }
//        if (ValidationHelper.isNullOrEmpty(this.getEntity().getUse())) {
//            addRequiredFieldException("form:use");
//        }
//    }
//
//    @Override
//    public void save() throws HibernateException, PersistenceBeanException, NumberFormatException, IOException,
//            InstantiationException, IllegalAccessException {
//        DaoManager.save(this.getEntity());
//    }

    @Override
    public void onLoad() throws NumberFormatException, HibernateException,
            PersistenceBeanException, InstantiationException,
            IllegalAccessException, IOException {
        filterTableFromPanel();
    }

    public void filterTableFromPanel() throws PersistenceBeanException, IllegalAccessException, InstantiationException {
        List<Criterion> restrictions = new ArrayList<>();
        if (!ValidationHelper.isNullOrEmpty(getPercentage()) && !getPercentage().equals(BigDecimal.ZERO)) {
            restrictions.add(Restrictions.eq("percentage", getPercentage()));
        }
        if (!ValidationHelper.isNullOrEmpty(getDescription())) {
            restrictions.add(Restrictions.ilike("description", getDescription(), MatchMode.ANYWHERE));
        }
        if (!ValidationHelper.isNullOrEmpty(getCodeSDI())) {
            restrictions.add(Restrictions.ilike("codeSDI", getCodeSDI(), MatchMode.ANYWHERE));
        }
        if (!ValidationHelper.isNullOrEmpty(getSelectedUse()) && getSelectedUse() > -1) {
            restrictions.add(Restrictions.eq("use", getSelectedUse().equals(1) ? true : false));
        }

        this.loadList(TaxRate.class, restrictions.toArray(new Criterion[0]), new Order[]
                {Order.asc("description")});
    }

    public void clearFilterPanel() throws PersistenceBeanException, IllegalAccessException, InstantiationException {
        setPercentage(null);
        setDescription(null);
        setCodeSDI(null);
        setSelectedUse(null);
        filterTableFromPanel();
    }
}