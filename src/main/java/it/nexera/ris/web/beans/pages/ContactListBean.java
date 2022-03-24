package it.nexera.ris.web.beans.pages;

import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.helpers.ResourcesHelper;
import it.nexera.ris.common.helpers.ValidationHelper;
import it.nexera.ris.persistence.beans.dao.CriteriaAlias;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.domain.readonly.ClientShort;
import it.nexera.ris.web.beans.EntityLazyListPageBean;
import it.nexera.ris.web.common.EntityLazyListModel;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.HibernateException;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.hibernate.sql.JoinType;
import org.primefaces.model.LazyDataModel;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.model.SelectItem;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

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
    
    @Override
    public void onLoad() throws NumberFormatException, HibernateException, PersistenceBeanException, InstantiationException, IllegalAccessException, IOException {
        setShowSearch(Boolean.FALSE);

    }

    public void search() throws PersistenceBeanException, InstantiationException, IllegalAccessException {
        setShowSearch(Boolean.TRUE);
        loadClientTabOnSearch();
        loadManagersTabOnSearch();
        loadTrusteesTabOnSearch();
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
        setManagerList(DaoManager.load(ClientShort.class, new CriteriaAlias[] {
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
        setTrusteesList(DaoManager.load(ClientShort.class, new CriteriaAlias[] {
                new CriteriaAlias("referenceClients", "rc", JoinType.INNER_JOIN)
        		}, criterions.toArray(new Criterion[0]), new Order[]{
                        Order.asc("clientName")
                }));
    }
}