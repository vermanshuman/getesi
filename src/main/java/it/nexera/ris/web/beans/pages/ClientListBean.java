package it.nexera.ris.web.beans.pages;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.model.SelectItem;

import org.hibernate.HibernateException;
import org.hibernate.Transaction;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.hibernate.exception.ConstraintViolationException;
import org.hibernate.sql.JoinType;
import org.primefaces.component.tabview.TabView;
import org.primefaces.event.TabChangeEvent;
import org.primefaces.model.LazyDataModel;

import it.nexera.ris.common.enums.ClientType;
import it.nexera.ris.common.enums.PageTypes;
import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.helpers.ComboboxHelper;
import it.nexera.ris.common.helpers.LogHelper;
import it.nexera.ris.common.helpers.MessageHelper;
import it.nexera.ris.common.helpers.RedirectHelper;
import it.nexera.ris.common.helpers.ResourcesHelper;
import it.nexera.ris.common.helpers.SelectItemHelper;
import it.nexera.ris.common.helpers.SessionHelper;
import it.nexera.ris.common.helpers.ValidationHelper;
import it.nexera.ris.persistence.PersistenceSessionManager;
import it.nexera.ris.persistence.beans.dao.CriteriaAlias;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.domain.Client;
import it.nexera.ris.persistence.beans.entities.domain.ClientEmail;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.City;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.Country;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.Office;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.Province;
import it.nexera.ris.persistence.beans.entities.domain.readonly.ClientShort;
import it.nexera.ris.web.beans.EntityLazyListPageBean;
import it.nexera.ris.web.common.EntityLazyListModel;
import lombok.Getter;

@ManagedBean(name = "clientListBean")
@ViewScoped
public class ClientListBean extends EntityLazyListPageBean<ClientShort> implements Serializable {

    @Getter
    private enum ClientKind {
        USUAL(ResourcesHelper.getString("client")),
        MANAGER(ResourcesHelper.getString("clientManager")),
        FIDUCIARY(ResourcesHelper.getString("clientTypeTrust"));

        private String label;

        ClientKind(String label) {
            this.label = label;
        }
    }

    private static final String ONLY_VIEW_CLIENT = "ONLY_VIEW_CLIENT";

    private static final long serialVersionUID = -7950331950944469020L;

    private String nameOfTheCompany;

    private String numberVAT;

    private Long addressProvinceId;

    private Long addressCityId;

    private List<SelectItem> provinces;

    private List<SelectItem> addressCities;

    private Boolean foreignCountry;

    private List<SelectItem> countries;

    private Long selectedCountryId;

    private int activeClientListIndex;

    private LazyDataModel<ClientShort> lazyDeletedClientModel;
    
    private LazyDataModel<ClientShort> lazyManagerClientModel;

    private LazyDataModel<ClientShort> lazyClientTrustModel;

    private List<SelectItem> kindList;

    private List<String> selectedKindList;
    
    private List<SelectItem> clients;
    
    private Long selectedClientId;

    private Long selectedClientTrustId;
    
    private Long selectedClientTypeId;
    
    private List<SelectItem> clientTypes;
    
    private Long selectedClientOfficeId;
    
    private List<SelectItem> clientOffices;
    /*
     * (non-Javadoc)
     *
     * @see it.nexera.web.beans.EntityListPageBean#pageLoad()
     */
    @Override
    public void onLoad() throws NumberFormatException, HibernateException, PersistenceBeanException {
        if (getSession().containsKey("clientSaved")) {
            MessageHelper.addGlobalMessage(FacesMessage.SEVERITY_INFO, "",
                    ResourcesHelper.getString("clientSavedCorrectly"));
            getSession().remove("clientSaved");
        }

        loadList(ClientShort.class, new Criterion[]{
                Restrictions.or(
                        Restrictions.eq("deleted", Boolean.FALSE),
                        Restrictions.isNull("deleted")),
                Restrictions.and(
                        Restrictions.or(
                                Restrictions.eq("manager", Boolean.FALSE),
                                Restrictions.isNull("manager")),
                        Restrictions.or(
                                Restrictions.eq("fiduciary", Boolean.FALSE),
                                Restrictions.isNull("fiduciary"))
                )
        }, new Order[]{
                Order.asc("clientName")
        });

        setLazyDeletedClientModel(new EntityLazyListModel<>(ClientShort.class, new Criterion[]{
                Restrictions.eq("deleted", Boolean.TRUE)
        }, new Order[]{
                Order.asc("clientName")
        }));
        
        setLazyManagerClientModel(new EntityLazyListModel<>(ClientShort.class, new Criterion[]{
               Restrictions.eq("manager", Boolean.TRUE),Restrictions.isNotNull("manager"),  
               Restrictions.or(Restrictions.eq("deleted", Boolean.FALSE),Restrictions.isNull("deleted"))
        }, new Order[]{
                Order.asc("clientName")
        }));
        
        setLazyClientTrustModel(new EntityLazyListModel<>(ClientShort.class, new Criterion[]{
                Restrictions.eq("fiduciary", Boolean.TRUE),
                Restrictions.or(Restrictions.eq("deleted", Boolean.FALSE),Restrictions.isNull("deleted"))
                },new Order[]{
                        Order.asc("clientName")
                }));
        
        try {
        	setClients(ComboboxHelper.fillList(DaoManager.load(ClientShort.class, 
        			new Criterion[]{
        			        Restrictions.and(
        			                Restrictions.or(Restrictions.eq("manager", Boolean.FALSE),
                                            Restrictions.isNull("manager")),
        			                Restrictions.or(Restrictions.eq("fiduciary", Boolean.FALSE),
                                            Restrictions.isNull("fiduciary"))
        			                )
		        	}
            ).stream().sorted(Comparator.comparing(ClientShort::toString)).collect(Collectors.toList()), true));
         
        	setProvinces(ComboboxHelper.fillList(Province.class, Order.asc("description")));
            getProvinces().add(new SelectItem(Province.FOREIGN_COUNTRY_ID, Province.FOREIGN_COUNTRY));
            setAddressCities(ComboboxHelper.fillList(Collections.emptyList(), true, false));

            setCountries(ComboboxHelper.fillList(Country.class, Order.asc("description"), new Criterion[]{
                    Restrictions.ne("description", "ITALIA")
            }));
            setClientTypes(ComboboxHelper.fillList(ClientType.class, true));
        } catch (IllegalAccessException e) {
            LogHelper.log(log, e);
        }

        
        
        if (!ValidationHelper.isNullOrEmpty(SessionHelper.get("activeClientListIndex"))) {
            setActiveClientListIndex((Integer) SessionHelper.get("activeClientListIndex"));
        }

        setKindList(Arrays.stream(ClientKind.values()).map(x -> new SelectItem(x.name(), x.getLabel()))
                .collect(Collectors.toList()));

        setSelectedKindList(new LinkedList<>());
        getSelectedKindList().add(ClientKind.USUAL.name());
    }

    @Override
    public void deleteEntity() throws HibernateException, PersistenceBeanException, InstantiationException,
            IllegalAccessException, NumberFormatException, IOException {
        if (getEntityDeleteId() != null) {
            Transaction tr = null;
            try {
                tr = PersistenceSessionManager.getBean().getSession().beginTransaction();
                deleteEntityInternal(getEntityDeleteId());
            } catch (Exception e) {
                if (tr != null) {
                    tr.rollback();
                }
                if (e instanceof ConstraintViolationException) {
                    MessageHelper.addGlobalMessage(FacesMessage.SEVERITY_ERROR,
                            ResourcesHelper.getValidation("deleteFail"), "");
                } else {
                    MessageHelper.addGlobalMessage(FacesMessage.SEVERITY_ERROR,
                            e.getMessage(), e.getCause().getMessage());
                }
                LogHelper.log(log, e);
            } finally {
                if (tr != null && !tr.wasRolledBack() && tr.isActive()) {
                    try {
                        tr.commit();
                    } catch (ConstraintViolationException e) {
                        tr.rollback();
                        try {
                            DaoManager.getSession().clear();
                            Client client = DaoManager.get(Client.class, getEntityDeleteId());
                            client.setDeleted(true);
                            List<ClientEmail> clientEmailList = DaoManager.load(ClientEmail.class, new Criterion[]{
                                    Restrictions.eq("client.id", getEntityDeleteId())
                            });
                            for (ClientEmail email : clientEmailList) {
                                DaoManager.remove(email, true);
                            }
                            DaoManager.save(client, true);
                        } catch (Exception e1) {
                            LogHelper.log(log, e1);
                        }
                    } catch (Exception e) {
                        tr.rollback();
                        LogHelper.log(log, e);
                    }
                    if (tr != null && !tr.wasRolledBack()) {
                        afterEntityRemoved();
                    }
                }
            }
            onLoad();
        }
    }

    @Override
    public void viewEntity() {
        SessionHelper.put(ONLY_VIEW_CLIENT, Boolean.TRUE);
        RedirectHelper.goTo(PageTypes.getEditPageByClass(getType().getSimpleName()), getEntityEditId());
    }

    public void searchClients() {
        List<Criterion> criterions = new ArrayList<>();

        if (!ValidationHelper.isNullOrEmpty(getNameOfTheCompany())) {
            Criterion criteria1 = Restrictions.like("nameOfTheCompany",
                    getNameOfTheCompany(), MatchMode.ANYWHERE);

            Criterion criteria2 = Restrictions.and(Restrictions.like("nameProfessional",
                    getNameOfTheCompany(), MatchMode.ANYWHERE), Restrictions.isNull("nameOfTheCompany"));

            criterions.add(Restrictions.or(criteria1, criteria2));
        }

        if (!ValidationHelper.isNullOrEmpty(getNumberVAT())) {
            criterions.add(Restrictions.like("numberVAT", getNumberVAT(), MatchMode.ANYWHERE));
        }

        if (!Province.FOREIGN_COUNTRY_ID.equals(getAddressProvinceId())
                && !ValidationHelper.isNullOrEmpty(getAddressProvinceId())) {
            criterions.add(Restrictions.eq("addressProvinceId.id", getAddressProvinceId()));
        } else if (Province.FOREIGN_COUNTRY_ID.equals(getAddressProvinceId())) {
            criterions.add(Restrictions.isNull("addressProvinceId"));
        }

        if (!Province.FOREIGN_COUNTRY_ID.equals(getAddressProvinceId())
                && !ValidationHelper.isNullOrEmpty(getAddressCityId())) {
            criterions.add(Restrictions.eq("addressCityId.id", getAddressCityId()));
            criterions.add(Restrictions.or(
                    Restrictions.eq("foreignCountry", false),
                    Restrictions.isNull("foreignCountry")));
        } else if (Province.FOREIGN_COUNTRY_ID.equals(getAddressProvinceId())
                && !ValidationHelper.isNullOrEmpty(getSelectedCountryId())) {
            criterions.add(Restrictions.eq("country.id", getSelectedCountryId()));
            criterions.add(Restrictions.eq("foreignCountry", true));
        }

        if (!ValidationHelper.isNullOrEmpty(getSelectedKindList())) {
            Criterion[] kindRestrictions = new Criterion[getSelectedKindList().size()];
            for (int i = 0; i < getSelectedKindList().size(); i++) {
                switch (ClientKind.valueOf(getSelectedKindList().get(i))) {
                    case USUAL:
                        kindRestrictions[i] = Restrictions.and(
                                Restrictions.and(
                                        Restrictions.or(
                                                Restrictions.eq("manager", Boolean.FALSE),
                                                Restrictions.isNull("manager")),
                                        Restrictions.or(
                                                Restrictions.eq("fiduciary", Boolean.FALSE),
                                                Restrictions.isNull("fiduciary"))
                                )
                        );
                        break;
                    case MANAGER:
                        kindRestrictions[i] = Restrictions.eq("manager", Boolean.TRUE);
                        break;
                    case FIDUCIARY:
                        kindRestrictions[i] = Restrictions.eq("fiduciary", Boolean.TRUE);
                        break;
                }
            }
            if (kindRestrictions.length == 1) {
                criterions.add(kindRestrictions[0]);
            } else {
                criterions.add(Restrictions.or(kindRestrictions));
            }
        }
        
        if (!ValidationHelper.isNullOrEmpty(getSelectedClientTypeId())) {
            criterions.add(Restrictions.eq("typeId", getSelectedClientTypeId()));
        }

        if (getActiveClientListIndex() == 0) {
            criterions.add(Restrictions.or(
                    Restrictions.eq("deleted", Boolean.FALSE),
                    Restrictions.isNull("deleted")));
            loadList(ClientShort.class, criterions.toArray(new Criterion[0]), new Order[]{
                    Order.asc("nameOfTheCompany")
            });
        } else {
            criterions.add(Restrictions.eq("deleted", Boolean.TRUE));
            setLazyDeletedClientModel(new EntityLazyListModel<>(ClientShort.class, criterions.toArray(new Criterion[0]),
                    new Order[]{
                            Order.asc("nameOfTheCompany")
                    }));
        }
    }

    public void handleAddressProvinceChange() throws HibernateException, PersistenceBeanException, IllegalAccessException {
        if (!Province.FOREIGN_COUNTRY_ID.equals(getAddressProvinceId())) {
            if (!ValidationHelper.isNullOrEmpty(getAddressProvinceId())) {
                setAddressCities(ComboboxHelper.fillList(City.class, Order.asc("description"),
                        Restrictions.eq("province.id", getAddressProvinceId())));
            } else {
                setAddressCities(ComboboxHelper.fillList(City.class, Order.asc("description")));
                setAddressCities(ComboboxHelper.fillList(City.class, Order.asc("description"),
                        Restrictions.eq("external", Boolean.TRUE)));
            }
            setForeignCountry(Boolean.FALSE);
        } else {
            setForeignCountry(Boolean.TRUE);
        }
    }
    
    public void onClientSelect() 
            throws HibernateException, InstantiationException, IllegalAccessException, PersistenceBeanException {
    	if(getSelectedClientId() != null && getSelectedClientId() > 0) {
	    	setLazyManagerClientModel(new EntityLazyListModel<>(ClientShort.class, new Criterion[]{
	    	        Restrictions.and(Restrictions.eq("manager", Boolean.TRUE),
	    	                Restrictions.eq("rc.id", getSelectedClientId()))
             }, new Order[]{
                     Order.asc("nameProfessional")
             },new CriteriaAlias[]{
                     new CriteriaAlias("referenceClients", "rc", JoinType.INNER_JOIN)
	        }));
	    	Client selectedClient = DaoManager.get(Client.class, getSelectedClientId());
	    	setClientOffices(new ArrayList<>());
	    	getClientOffices().add(SelectItemHelper.getVirtualEntity());
	    	if(!ValidationHelper.isNullOrEmpty(selectedClient.getOffices())) {
	    	    List<Office> clientOffices = selectedClient.getOffices().stream()
	    	            .distinct()
	    	            .collect(Collectors.toList());
	    	    Collections.sort(clientOffices, new Comparator<Office>() {
	                @Override
	                public int compare(final Office object1, final Office object2) {
	                    return object1.getDescription().compareTo(object2.getDescription());
	                }
	            });
	    	    clientOffices.stream().forEach(o ->{
	    	        getClientOffices().add(new SelectItem(o.getId(), o.getDescription()));
	    	    });
	    	}
    	}else {
	    	setLazyManagerClientModel(new EntityLazyListModel<>(ClientShort.class, new Criterion[]{
	    	        Restrictions.and(Restrictions.eq("manager", Boolean.TRUE),
	    	                Restrictions.isNotNull("referenceClients"))
             }, new Order[]{
                     Order.asc("nameProfessional")
             }));
    	}
    }
    
    public void onClientOfficeSelect() 
            throws HibernateException, InstantiationException, IllegalAccessException, PersistenceBeanException {
        if(getSelectedClientOfficeId() != null && getSelectedClientOfficeId() > 0) {
            setLazyManagerClientModel(new EntityLazyListModel<>(ClientShort.class, new Criterion[]{
                    Restrictions.and(Restrictions.eq("manager", Boolean.TRUE),
                            Restrictions.eq("rc.id", getSelectedClientId()),
                            Restrictions.eq("office.id", getSelectedClientOfficeId()))
             }, new Order[]{
                     Order.asc("nameProfessional")
             },new CriteriaAlias[]{
                     new CriteriaAlias("referenceClients", "rc", JoinType.INNER_JOIN)
            }));
        }else {

            setLazyManagerClientModel(new EntityLazyListModel<>(ClientShort.class, new Criterion[]{
                    Restrictions.and(Restrictions.eq("manager", Boolean.TRUE),
                            Restrictions.eq("rc.id", getSelectedClientId()))
             }, new Order[]{
                     Order.asc("nameProfessional")
             },new CriteriaAlias[]{
                     new CriteriaAlias("referenceClients", "rc", JoinType.INNER_JOIN)
            }));
        }
    }

    public void onClientTrustSelect() {
        if(getSelectedClientTrustId() != null) {
            setLazyClientTrustModel(new EntityLazyListModel<>(ClientShort.class, new Criterion[]{
                    Restrictions.and(Restrictions.eq("fiduciary", Boolean.TRUE),
                            Restrictions.eq("rc.id", getSelectedClientTrustId()))
             }, new Order[]{
                     Order.asc("nameProfessional")
             },new CriteriaAlias[]{
                     new CriteriaAlias("referenceClients", "rc", JoinType.INNER_JOIN)
            }));
            
        }else {
            setLazyClientTrustModel(new EntityLazyListModel<>(ClientShort.class, new Criterion[]{
                    Restrictions.isNotNull("referenceClients")
             }, new Order[]{
                     Order.asc("nameProfessional")
             }));
        }
    }

    @Override
    public void addEntity() throws HibernateException, InstantiationException,
            IllegalAccessException, PersistenceBeanException {
        if (getCanCreate()) {
            RedirectHelper.goTo(PageTypes.getEditPageByClass(getType().getSimpleName()
                    .substring(0, getType().getSimpleName().indexOf("Short"))), null);
        }
    }

    public final void onTabChange(final TabChangeEvent event) {
        TabView tv = (TabView) event.getComponent();
        this.activeClientListIndex = tv.getActiveIndex();
        setSelectedClientId(null);
        setSelectedClientTrustId(null);
        SessionHelper.put("activeClientListIndex", activeClientListIndex);
    }

    public String getNameOfTheCompany() {
        return nameOfTheCompany;
    }

    public void setNameOfTheCompany(String nameOfTheCompany) {
        this.nameOfTheCompany = nameOfTheCompany;
    }

    public String getNumberVAT() {
        return numberVAT;
    }

    public void setNumberVAT(String numberVAT) {
        this.numberVAT = numberVAT;
    }

    public Long getAddressProvinceId() {
        return addressProvinceId;
    }

    public void setAddressProvinceId(Long addressProvinceId) {
        this.addressProvinceId = addressProvinceId;
    }

    public Long getAddressCityId() {
        return addressCityId;
    }

    public void setAddressCityId(Long addressCityId) {
        this.addressCityId = addressCityId;
    }

    public List<SelectItem> getProvinces() {
        return provinces;
    }

    public void setProvinces(List<SelectItem> provinces) {
        this.provinces = provinces;
    }

    public List<SelectItem> getAddressCities() {
        return addressCities;
    }

    public void setAddressCities(List<SelectItem> addressCities) {
        this.addressCities = addressCities;
    }

    public Boolean getForeignCountry() {
        return foreignCountry;
    }

    public void setForeignCountry(Boolean foreignCountry) {
        this.foreignCountry = foreignCountry;
    }

    public List<SelectItem> getCountries() {
        return countries;
    }

    public void setCountries(List<SelectItem> countries) {
        this.countries = countries;
    }

    public Long getSelectedCountryId() {
        return selectedCountryId;
    }

    public void setSelectedCountryId(Long selectedCountryId) {
        this.selectedCountryId = selectedCountryId;
    }

    public int getActiveClientListIndex() {
        return activeClientListIndex;
    }

    public void setActiveClientListIndex(int activeClientListIndex) {
        this.activeClientListIndex = activeClientListIndex;
    }

    public LazyDataModel<ClientShort> getLazyDeletedClientModel() {
        return lazyDeletedClientModel;
    }

    public void setLazyDeletedClientModel(LazyDataModel<ClientShort> lazyDeletedClientModel) {
        this.lazyDeletedClientModel = lazyDeletedClientModel;
    }

    public List<SelectItem> getKindList() {
        return kindList;
    }

    public void setKindList(List<SelectItem> kindList) {
        this.kindList = kindList;
    }

    public List<String> getSelectedKindList() {
        return selectedKindList;
    }

    public void setSelectedKindList(List<String> selectedKindList) {
        this.selectedKindList = selectedKindList;
    }

	public LazyDataModel<ClientShort> getLazyManagerClientModel() {
		return lazyManagerClientModel;
	}

	public void setLazyManagerClientModel(LazyDataModel<ClientShort> lazyManagerClientModel) {
		this.lazyManagerClientModel = lazyManagerClientModel;
	}

	public LazyDataModel<ClientShort> getLazyClientTrustModel() {
		return lazyClientTrustModel;
	}

	public void setLazyClientTrustModel(LazyDataModel<ClientShort> lazyClientTrustModel) {
		this.lazyClientTrustModel = lazyClientTrustModel;
	}

	public List<SelectItem> getClients() {
		return clients;
	}

	public void setClients(List<SelectItem> clients) {
		this.clients = clients;
	}

	public Long getSelectedClientId() {
		return selectedClientId;
	}

	public void setSelectedClientId(Long selectedClientId) {
		this.selectedClientId = selectedClientId;
	}

	public Long getSelectedClientTrustId() {
		return selectedClientTrustId;
	}

	public void setSelectedClientTrustId(Long selectedClientTrustId) {
		this.selectedClientTrustId = selectedClientTrustId;
	}

    public Long getSelectedClientTypeId() {
        return selectedClientTypeId;
    }

    public void setSelectedClientTypeId(Long selectedClientTypeId) {
        this.selectedClientTypeId = selectedClientTypeId;
    }

    public List<SelectItem> getClientTypes() {
        return clientTypes;
    }

    public void setClientTypes(List<SelectItem> clientTypes) {
        this.clientTypes = clientTypes;
    }

    public Long getSelectedClientOfficeId() {
        return selectedClientOfficeId;
    }

    public List<SelectItem> getClientOffices() {
        return clientOffices;
    }

    public void setSelectedClientOfficeId(Long selectedClientOfficeId) {
        this.selectedClientOfficeId = selectedClientOfficeId;
    }

    public void setClientOffices(List<SelectItem> clientOffices) {
        this.clientOffices = clientOffices;
    }
}
