package it.nexera.ris.web.beans.pages;

import it.nexera.ris.common.enums.PageTypes;
import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.helpers.RedirectHelper;
import it.nexera.ris.common.helpers.SessionHelper;
import it.nexera.ris.common.helpers.ValidationHelper;
import it.nexera.ris.persistence.beans.dao.CriteriaAlias;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.domain.readonly.ClientShort;
import it.nexera.ris.web.beans.EntityLazyListPageBean;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.HibernateException;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.hibernate.sql.JoinType;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@ManagedBean(name = "contactListBean")
@ViewScoped
@Getter
@Setter
public class ContactListBean extends EntityLazyListPageBean<ClientShort> implements Serializable {

    private static final long serialVersionUID = 8500587365296660346L;

    private Boolean showSearch;

    private String nameOfTheCompany;

    private List<ClientShort> clientList;

    private List<ClientShort> managerList;

    private List<ClientShort> trusteesList;

    private Long selectedClientId;

    private static final String ONLY_VIEW_CLIENT = "ONLY_VIEW_CLIENT";

    private String searchHeader;

    @Override
    public void onLoad() throws NumberFormatException, HibernateException, PersistenceBeanException, InstantiationException, IllegalAccessException, IOException {
        setShowSearch(Boolean.FALSE);

    }

    public void search() throws PersistenceBeanException, InstantiationException, IllegalAccessException {
        setShowSearch(Boolean.TRUE);
        loadClientTabOnSearch();
        loadManagersTabOnSearch();
        loadTrusteesTabOnSearch();
        setSearchHeader("\"" + getNameOfTheCompany() + "\"");

    }

    public void loadClientTabOnSearch() throws IllegalAccessException, PersistenceBeanException {
        List<Criterion> criterions = new ArrayList<>();

        if (!ValidationHelper.isNullOrEmpty(getNameOfTheCompany())) {
            Criterion criteria1 = Restrictions.like("nameOfTheCompany",
                    getNameOfTheCompany(), MatchMode.ANYWHERE);

            Criterion criteria2 = Restrictions.and(Restrictions.like("nameProfessional",
                    getNameOfTheCompany(), MatchMode.ANYWHERE), Restrictions.isNull("nameOfTheCompany"));

            criterions.add(Restrictions.or(criteria1, criteria2));
        }
        criterions.add(Restrictions.or(
                Restrictions.eq("deleted", Boolean.FALSE),
                Restrictions.isNull("deleted")));
        setClientList(DaoManager.load(ClientShort.class, criterions.toArray(new Criterion[0]), new Order[]{
                Order.asc("nameOfTheCompany")
        }));
    }

    public void loadManagersTabOnSearch() throws IllegalAccessException, PersistenceBeanException {
        List<Criterion> criterions = new ArrayList<>();

        if (!ValidationHelper.isNullOrEmpty(getNameOfTheCompany())) {
            Criterion criteria1 = Restrictions.like("nameOfTheCompany",
                    getNameOfTheCompany(), MatchMode.ANYWHERE);

            Criterion criteria2 = Restrictions.and(Restrictions.like("nameProfessional",
                    getNameOfTheCompany(), MatchMode.ANYWHERE), Restrictions.isNull("nameOfTheCompany"));

            criterions.add(Restrictions.or(criteria1, criteria2));
        }
        criterions.add(Restrictions.or(
                Restrictions.eq("deleted", Boolean.FALSE),
                Restrictions.isNull("deleted")));
        criterions.add(Restrictions.and(Restrictions.isNotNull("manager"),
                Restrictions.eq("manager", Boolean.TRUE)));
        setManagerList(DaoManager.load(ClientShort.class, new CriteriaAlias[]{
                new CriteriaAlias("referenceClients", "rc", JoinType.INNER_JOIN)
        }, criterions.toArray(new Criterion[0]), new Order[]{
                Order.asc("clientName")
        }));
    }

    public void loadTrusteesTabOnSearch() throws IllegalAccessException, PersistenceBeanException {
        List<Criterion> criterions = new ArrayList<>();

        if (!ValidationHelper.isNullOrEmpty(getNameOfTheCompany())) {
            Criterion criteria1 = Restrictions.like("nameOfTheCompany",
                    getNameOfTheCompany(), MatchMode.ANYWHERE);

            Criterion criteria2 = Restrictions.and(Restrictions.like("nameProfessional",
                    getNameOfTheCompany(), MatchMode.ANYWHERE), Restrictions.isNull("nameOfTheCompany"));

            criterions.add(Restrictions.or(criteria1, criteria2));
        }
        criterions.add(Restrictions.or(
                Restrictions.eq("deleted", Boolean.FALSE),
                Restrictions.isNull("deleted")));
        criterions.add(Restrictions.and(Restrictions.isNotNull("fiduciary"),
                Restrictions.eq("fiduciary", Boolean.TRUE)));

        setTrusteesList(DaoManager.load(ClientShort.class, new CriteriaAlias[]{
                new CriteriaAlias("referenceClients", "rc", JoinType.INNER_JOIN)
        }, criterions.toArray(new Criterion[0]), new Order[]{
                Order.asc("clientName")
        }));
    }

    public void viewEntity() {
        SessionHelper.put(ONLY_VIEW_CLIENT, Boolean.TRUE);
        SessionHelper.put("REDIRECT_FROM_CONTACT_LIST", Boolean.TRUE);
        RedirectHelper.goTo(PageTypes.CLIENT_EDIT, getSelectedClientId());
    }
}