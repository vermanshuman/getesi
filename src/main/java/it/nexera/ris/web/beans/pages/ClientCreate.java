package it.nexera.ris.web.beans.pages;

import static org.apache.commons.collections4.ListUtils.emptyIfNull;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.model.SelectItem;

import it.nexera.ris.common.helpers.*;
import it.nexera.ris.common.xml.wrappers.SelectItemWrapper;
import it.nexera.ris.persistence.beans.dao.CriteriaAlias;
import it.nexera.ris.persistence.beans.entities.Dictionary;
import it.nexera.ris.persistence.beans.entities.ITreeNode;
import org.hibernate.HibernateException;
import org.hibernate.criterion.*;
import org.hibernate.sql.JoinType;
import org.primefaces.context.RequestContext;

import it.nexera.ris.common.enums.ClientType;
import it.nexera.ris.common.enums.EmailType;
import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.domain.Client;
import it.nexera.ris.persistence.beans.entities.domain.ClientEmail;
import it.nexera.ris.persistence.beans.entities.domain.WLGInbox;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.Area;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.Office;
import it.nexera.ris.persistence.view.ClientView;
import it.nexera.ris.web.beans.EntityEditPageBean;
import it.nexera.ris.web.beans.wrappers.logic.ClientEmailWrapper;

@ManagedBean(name = "clientCreateBean")
@ViewScoped
public class ClientCreate extends EntityEditPageBean<Client>
implements Serializable {

	private static final long serialVersionUID = -1120608445741010143L;

	private Boolean renderImportant;

	private boolean subjectInvoice;

	private Long selectedClientId;

	private List<SelectItem> billingRecipients;

	private List<ClientView> billingRecipientTable;

	private Long deleteClientId;

	private boolean hasAgency;

	private boolean hasAgencyOffice;

	private Long selectedAreaId;

	private Long selectedOfficeId;

	 private List<SelectItemWrapper<Area>> areas;

	 private List<SelectItemWrapper<Office>> offices;

	private String clientEmail;

	private List<ClientEmailWrapper> personalEmails;

	private Long mailId;
	
	private Long[] selectedNonManagerOrFiduciaryClientIds;
	
	private boolean clientFiduciaryOrManager;
	
	private SelectItemWrapperConverter<Area> areaConverter;
	
	private List<SelectItemWrapper<Area>> selectedAreas;
	
	private SelectItemWrapper<Area> selectedArea;
	
	private List<Office> allOfficesList;
	
	 private List<SelectItemWrapper<Office>> selectedOffices;
	 
	 private SelectItemWrapperConverter<Office> officeConverter;
	 
	 private SelectItemWrapper<Office> selectedOffice;

	@Override
	public void onLoad() throws NumberFormatException, HibernateException, PersistenceBeanException,
			InstantiationException, IllegalAccessException {
		setEntity(super.getEntity());
		setAreasAndOffices();
		loadClientList();
		checkFiduciaryAndManagerField();
		checkRenderImportant();
		setClientEmailFromWLGInbox();
	}

	private void setAreasAndOffices() throws PersistenceBeanException, IllegalAccessException {
		//this.setAreas(getAreaOrOfficeSelectItemList(Area.class, null));
	    
		//this.setOffices(getAreaOrOfficeSelectItemList(Office.class, null));
	    setSelectedAreas(new ArrayList<>());
        setSelectedArea(SelectItemHelper.getNotSelectedWrapper());
        setSelectedOffices(new ArrayList<>());
        setSelectedOffice(SelectItemHelper.getNotSelectedWrapper());

		 if(!ValidationHelper.isNullOrEmpty(getSelectedNonManagerOrFiduciaryClientIds())){
	            List<Client> clients = DaoManager.load(Client.class, new Criterion[]{
	                    Restrictions.in("id", getSelectedNonManagerOrFiduciaryClientIds())
	            });
	            
	            if(!ValidationHelper.isNullOrEmpty(clients)) {
	                List<Area> areas = clients.stream()
	                        .filter(c -> !ValidationHelper.isNullOrEmpty(c.getAreas()))
	                        .map(Client:: getAreas)
	                        .flatMap(List:: stream).collect(Collectors.toList());
	                setAreas(ComboboxHelper.fillWrapperList(emptyIfNull(areas), true));
	                List<Office> offices = clients.stream()
	                        .filter(c -> !ValidationHelper.isNullOrEmpty(c.getOffices()))
	                        .map(Client:: getOffices)
	                        .flatMap(List:: stream).collect(Collectors.toList());
	                setAllOfficesList(offices);
	            }
	        }else{
	            setAreas(ComboboxHelper.fillWrapperList(Area.class, new Criterion[]{}, true));
	            setAllOfficesList(DaoManager.load(Office.class));
	        }
		 if(getAllOfficesList().size() > 0) {
	            Collections.sort(getAllOfficesList(), new Comparator<Office>() {
	                @Override
	                public int compare(final Office object1, final Office object2) {
	                    return object1.getDescription().compareTo(object2.getDescription());
	                }
	            });
	       }
		setOffices(ComboboxHelper.fillWrapperList(getAllOfficesList(), true));
		setAreaConverter(new SelectItemWrapperConverter<>(Area.class, new ArrayList<>(getAreas())));
	    setOfficeConverter(new SelectItemWrapperConverter<>(Office.class, new ArrayList<>(getOffices())));
		if (!isClientFiduciaryOrManager()) {
		    getAreas().remove(0);
		    getOffices().remove(0);
		}
	}

	private <T extends Dictionary & ITreeNode> List<SelectItem> getAreaOrOfficeSelectItemList(Class<T> clazz,
																							  List<Criterion> restrictions)
			throws PersistenceBeanException, IllegalAccessException {
		List<Long> clientIds = null;
		if (!ValidationHelper.isNullOrEmpty(getBillingRecipientTable())) {
			clientIds = getBillingRecipientTable().stream().map(ClientView::getId).collect(Collectors.toList());
		}
		if (ValidationHelper.isNullOrEmpty(restrictions)) {
			restrictions = new ArrayList<>();
		}
		if (!ValidationHelper.isNullOrEmpty(clientIds)) {
			restrictions.add(Restrictions.in("client.id", clientIds));
			return ComboboxHelper.fillList(clazz, Order.asc("id"), new CriteriaAlias[]{
					new CriteriaAlias("clients", "client", JoinType.INNER_JOIN)
			}, restrictions.toArray(new Criterion[0]), true, false);
		} else {
			return ComboboxHelper.fillList(clazz, restrictions.toArray(new Criterion[0]));
		}
	}

	private void setClientEmailFromWLGInbox() throws PersistenceBeanException, InstantiationException, IllegalAccessException {
		if (ValidationHelper.isNullOrEmpty(getMailId())) {
			if (!ValidationHelper.isNullOrEmpty(getRequestParameter(RedirectHelper.ID_PARAMETER))) {
				setMailId(Long.valueOf(getRequestParameter(RedirectHelper.ID_PARAMETER)));
			} else if (!ValidationHelper.isNullOrEmpty(getRequestParameter(RedirectHelper.MAIL_ID))) {
				setMailId(Long.valueOf(getRequestParameter(RedirectHelper.MAIL_ID)));
			}
		}
		if (!ValidationHelper.isNullOrEmpty(getMailId()) && ValidationHelper.isNullOrEmpty(getClientEmail())) {
			WLGInbox wlgInbox = DaoManager.get(WLGInbox.class, getMailId());
			if (!ValidationHelper.isNullOrEmpty(wlgInbox)
					&& ValidationHelper.isNullOrEmpty(wlgInbox.getEmailFrom())) {
				List<String> onlyEmails = MailHelper.getOnlyEmails(wlgInbox.getEmailFrom());
				if(!ValidationHelper.isNullOrEmpty(onlyEmails))
					setClientEmail(onlyEmails.get(0));
			}
		}
	}

	@Override
	public void onValidate() throws PersistenceBeanException, HibernateException, IllegalAccessException {
		cleanValidation();
		if (getRenderImportant() && ValidationHelper.isNullOrEmpty(getEntity().getNameProfessional())) {
			addRequiredFieldException("nameProfessional");
		}
	}

	@Override
	public void onSave() throws HibernateException, PersistenceBeanException, NumberFormatException, IOException,
			InstantiationException, IllegalAccessException {
		this.getEntity().setHasAgency(this.isHasAgency());
		this.getEntity().setHasAgencyOffice(isHasAgencyOffice());
		this.getEntity().setSubjectInvoice(this.isSubjectInvoice());
		if (!ValidationHelper.isNullOrEmpty(getBillingRecipientTable())) {
			getEntity().setBillingRecipientList(DaoManager.load(Client.class, new Criterion[]{
					Restrictions.in("id", getBillingRecipientTable().stream()
							.map(ClientView::getId).collect(Collectors.toList()))
			}));
		} else {
			getEntity().setBillingRecipientList(null);
		}
		
		if (!ValidationHelper.isNullOrEmpty(getSelectedArea()) && isHasAgency()
		        && isClientFiduciaryOrManager()) {
		    this.getEntity().setArea(DaoManager.get(Area.class, new Criterion[]{
		            Restrictions.eq("id", getSelectedArea().getId())}));
		    this.getEntity().setAreas(null);
		} else if (!ValidationHelper.isNullOrEmpty(getSelectedAreas()) && isHasAgency()
		        && !isClientFiduciaryOrManager()) {
		    this.getEntity().setAreas(DaoManager.load(Area.class, new Criterion[]{
		            Restrictions.in("id", getSelectedAreas()
		                    .stream().map(SelectItemWrapper::getId).collect(Collectors.toList()))
		    }));
		    this.getEntity().setArea(null);
		} else {
		    this.getEntity().setAreas(null);
		    this.getEntity().setArea(null);
		}
		
		if (!ValidationHelper.isNullOrEmpty(getSelectedNonManagerOrFiduciaryClientIds())) {
            List<Client> clients = DaoManager.load(Client.class, new Criterion[]{
                    Restrictions.in("id", getSelectedNonManagerOrFiduciaryClientIds())
            });
             this.getEntity().setReferenceClients(clients);

        }else {
            this.getEntity().setReferenceClients(null);
        }
		   
//		if (!ValidationHelper.isNullOrEmpty(getSelectedAreaId()) && isHasAgency()) {
//			this.getEntity().setArea(DaoManager.get(Area.class, new Criterion[]{
//					Restrictions.eq("id", getSelectedAreaId())}));
//		} else {
//			this.getEntity().setArea(null);
//		}
//		if (!ValidationHelper.isNullOrEmpty(getSelectedOfficeId()) && isHasAgencyOffice()) {
//			this.getEntity().setOffice(DaoManager.get(Office.class, new Criterion[]{
//					Restrictions.eq("id", getSelectedOfficeId())}));
//		} else {
//			this.getEntity().setOffice(null);
//		}
		
        if (!ValidationHelper.isNullOrEmpty(getSelectedOffice()) && isHasAgencyOffice()
                && isClientFiduciaryOrManager()) {
            this.getEntity().setOffice(DaoManager.get(Office.class, new Criterion[]{
                    Restrictions.eq("id", getSelectedOffice().getId())}));
            this.getEntity().setOffices(null);
        } else if (!ValidationHelper.isNullOrEmpty(getSelectedOffices()) && isHasAgencyOffice()
                && !isClientFiduciaryOrManager()) {
            this.getEntity().setOffices(DaoManager.load(Office.class, new Criterion[]{
                    Restrictions.in("id", getSelectedOffices()
                            .stream().map(SelectItemWrapper::getId).collect(Collectors.toList()))
            }));
            this.getEntity().setOffice(null);
        } else {
            this.getEntity().setOffice(null);
            this.getEntity().setOffices(null);
        }
        
		if (!ValidationHelper.isNullOrEmpty(getClientEmail())) {
			addNewEmail();
		}
		
		if (!ValidationHelper.isNullOrEmpty(getSelectedNonManagerOrFiduciaryClientIds())) {
		    List<Client> clients = DaoManager.load(Client.class, new Criterion[]{
                    Restrictions.in("id", getSelectedNonManagerOrFiduciaryClientIds())
            });
		    this.getEntity().setReferenceClients(clients);
		}else {
            this.getEntity().setReferenceClients(null);
        }
		this.getEntity().setTypeId(ClientType.PROFESSIONAL.getId());
		DaoManager.save(getEntity(), true);
		this.saveEmails();
		this.clearFormData();
	}

	public void updateForms() {
		checkRenderImportant();
		RequestContext context = RequestContext.getCurrentInstance();
		context.update("typegrid");
	}

	public void clearFormData() throws PersistenceBeanException, IllegalAccessException {
		getEntity().setNameOfTheCompany(null);
		getEntity().setNameProfessional(null);
		setBillingRecipientTable(null);
		setRenderImportant(true);
		setSelectedNonManagerOrFiduciaryClientIds(null);
		setAreasAndOffices();
	}

	private void checkRenderImportant() {
		if((!ValidationHelper.isNullOrEmpty(this.getEntity().getManager()) && this.getEntity().getManager())
				|| (!ValidationHelper.isNullOrEmpty(this.getEntity().getFiduciary()) && this.getEntity().getFiduciary())) {
			setRenderImportant(false);
		} else {
			setRenderImportant(true);
		}
	}

	private void loadClientList() throws PersistenceBeanException, IllegalAccessException {
		setBillingRecipients(ComboboxHelper.fillList(ClientView.class, Order.asc("name"), new Criterion[]{
				Restrictions.or(
						Restrictions.eq("isDeleted", Boolean.FALSE),
						Restrictions.isNull("isDeleted"))
		}));
	}

	public void addInvoicesRecipient() throws PersistenceBeanException, IllegalAccessException, InstantiationException {
		if (!ValidationHelper.isNullOrEmpty(getSelectedClientId())) {
			if (getBillingRecipientTable() == null) {
				setBillingRecipientTable(new ArrayList<>());
			}
			getBillingRecipientTable().add(DaoManager.get(ClientView.class, getSelectedClientId()));
			setSelectedClientId(null);
			loadClientList();
			setAreasAndOffices();
		}
	}

	public void deleteInvoicesRecipient() throws PersistenceBeanException, IllegalAccessException {
		if (!ValidationHelper.isNullOrEmpty(getDeleteClientId())
				&& !ValidationHelper.isNullOrEmpty(getBillingRecipientTable())) {
			getBillingRecipientTable().stream().filter(c -> c.getId().equals(getDeleteClientId()))
			.findAny().ifPresent(cv -> getBillingRecipientTable().remove(cv));
			setDeleteClientId(null);
			loadClientList();
			setAreasAndOffices();
		}
	}

	public void setOfficesByArea() throws PersistenceBeanException, IllegalAccessException {
	    //	    if (!isHasAgency()) {
	    //	        setSelectedAreaId(null);
	    //	    }
	    //	    if (!ValidationHelper.isNullOrEmpty(getSelectedAreaId()) && isHasAgency()) {
	    //	        setOffices(getAreaOrOfficeSelectItemList(Office.class,
	    //	                new ArrayList<>(Collections.singletonList(Restrictions.eq("area.id", getSelectedAreaId())))));
	    //	    } else {
	    //	        setOffices(getAreaOrOfficeSelectItemList(Office.class, null));
	    //	    }

	    final List<Long> areaIds;
	    if (isHasAgency()) {
	        if (!ValidationHelper.isNullOrEmpty(getSelectedArea()) && isClientFiduciaryOrManager()
	                && !getSelectedArea().getId().equals(0L)) {
	            areaIds = Collections.singletonList(getSelectedArea().getId());
	        } else if (!ValidationHelper.isNullOrEmpty(getSelectedAreas()) && !isClientFiduciaryOrManager()) {
	            areaIds = getSelectedAreas().stream().filter(x -> !x.getId().equals(0L))
	                    .map(SelectItemWrapper::getId).collect(Collectors.toList());
	        } else {
	            areaIds = null;
	        }
	    } else {
	        areaIds = null;
	    }

	    setOffices(ComboboxHelper.fillWrapperList(getAllOfficesList().stream()
	            .filter(x -> ValidationHelper.isNullOrEmpty(areaIds) || areaIds.contains(x.getArea().getId()))
	            .collect(Collectors.toList()), isClientFiduciaryOrManager()));

	    if (!ValidationHelper.isNullOrEmpty(getSelectedOffices()) && !isClientFiduciaryOrManager()) {
	        setSelectedOffices(getSelectedOffices().stream().filter(office -> getOffices().contains(office))
	                .collect(Collectors.toList()));
	    }
	}

	public void addNewEmail() {
		ClientEmail clientEmail = new ClientEmail();
		clientEmail.setEmail(getClientEmail());
		if (getPersonalEmails() == null) {
			setPersonalEmails(new ArrayList<>());
		}
		getPersonalEmails().add(new ClientEmailWrapper(clientEmail));
	}

	private void saveEmails() throws PersistenceBeanException {
		if (!ValidationHelper.isNullOrEmpty(getEmails())) {
			for (ClientEmailWrapper em : getEmails()) {
				em.getClientEmail().setClient(getEntity());
				em.getClientEmail().setTypeId(EmailType.PERSONAL.getId());
				DaoManager.save(em.getClientEmail(), true);
			}
			setPersonalEmails(null);
		}
	}

	public List<ClientEmailWrapper> getEmails() {
		if (!ValidationHelper.isNullOrEmpty(getPersonalEmails())) {
			return getPersonalEmails().stream().filter(it -> !it.getDelete()).collect(Collectors.toList());
		}

		return null;
	}
	
	private void checkFiduciaryAndManagerField() {
        setClientFiduciaryOrManager(
                (!ValidationHelper.isNullOrEmpty(this.getEntity().getManager())
                    && this.getEntity().getManager())
                || (!ValidationHelper.isNullOrEmpty(this.getEntity().getFiduciary())
                    && this.getEntity().getFiduciary()));
    }

	@Override
	public void goBack() {
		RedirectHelper.goToMailViewFromClient(getMailId());
	}

	public boolean isSubjectInvoice() {
		return subjectInvoice;
	}

	public void setSubjectInvoice(boolean subjectInvoice) {
		this.subjectInvoice = subjectInvoice;
	}

	public Long getSelectedClientId() {
		return selectedClientId;
	}

	public void setSelectedClientId(Long selectedClientId) {
		this.selectedClientId = selectedClientId;
	}

	public List<SelectItem> getBillingRecipients() {
		return billingRecipients;
	}

	public void setBillingRecipients(List<SelectItem> billingRecipients) {
		this.billingRecipients = billingRecipients;
	}


	public List<ClientView> getBillingRecipientTable() {
		return billingRecipientTable;
	}

	public void setBillingRecipientTable(List<ClientView> billingRecipientTable) {
		this.billingRecipientTable = billingRecipientTable;
	}


	public Boolean getRenderImportant() {
		return renderImportant;
	}

	public void setRenderImportant(Boolean renderImportant) {
		this.renderImportant = renderImportant;
	}

	public Long getDeleteClientId() {
		return deleteClientId;
	}

	public void setDeleteClientId(Long deleteClientId) {
		this.deleteClientId = deleteClientId;
	}

	public boolean isHasAgency() {
		return hasAgency;
	}

	public void setHasAgency(boolean hasAgency) {
		this.hasAgency = hasAgency;
	}

	public Long getSelectedAreaId() {
		return selectedAreaId;
	}

	public void setSelectedAreaId(Long selectedAreaId) {
		this.selectedAreaId = selectedAreaId;
	}

	public List<SelectItemWrapper<Office>> getOffices() {
	    return offices;
	}

	public void setOffices(List<SelectItemWrapper<Office>> offices) {
	    this.offices = offices;
	}

	public List<SelectItemWrapper<Area>> getAreas() {
	    return areas;
	}

	public void setAreas(List<SelectItemWrapper<Area>> areas) {
	    this.areas = areas;
	}

	public boolean isHasAgencyOffice() {
		return hasAgencyOffice;
	}

	public void setHasAgencyOffice(boolean hasAgencyOffice) {
		this.hasAgencyOffice = hasAgencyOffice;
	}

	public Long getSelectedOfficeId() {
		return selectedOfficeId;
	}

	public void setSelectedOfficeId(Long selectedOfficeId) {
		this.selectedOfficeId = selectedOfficeId;
	}

	public String getClientEmail() {
		return clientEmail;
	}

	public void setClientEmail(String clientEmail) {
		this.clientEmail = clientEmail;
	}

	public List<ClientEmailWrapper> getPersonalEmails() {
		return personalEmails;
	}

	public void setPersonalEmails(List<ClientEmailWrapper> personalEmails) {
		this.personalEmails = personalEmails;
	}

	public Long getMailId() {
		return mailId;
	}

	public void setMailId(Long mailId) {
		this.mailId = mailId;
	}

    public Long[] getSelectedNonManagerOrFiduciaryClientIds() {
        return selectedNonManagerOrFiduciaryClientIds;
    }

    public void setSelectedNonManagerOrFiduciaryClientIds(Long[] selectedNonManagerOrFiduciaryClientIds) {
        this.selectedNonManagerOrFiduciaryClientIds = selectedNonManagerOrFiduciaryClientIds;
    }

    public boolean isClientFiduciaryOrManager() {
        return clientFiduciaryOrManager;
    }

    public void setClientFiduciaryOrManager(boolean clientFiduciaryOrManager) {
        this.clientFiduciaryOrManager = clientFiduciaryOrManager;
    }
    
    public SelectItemWrapperConverter<Area> getAreaConverter() {
        return areaConverter;
    }

    public void setAreaConverter(SelectItemWrapperConverter<Area> areaConverter) {
        this.areaConverter = areaConverter;
    }

    public List<SelectItemWrapper<Area>> getSelectedAreas() {
        return selectedAreas;
    }

    public void setSelectedAreas(List<SelectItemWrapper<Area>> selectedAreas) {
        this.selectedAreas = selectedAreas;
    }

    public SelectItemWrapper<Area> getSelectedArea() {
        return selectedArea;
    }

    public void setSelectedArea(SelectItemWrapper<Area> selectedArea) {
        this.selectedArea = selectedArea;
    }

    public List<Office> getAllOfficesList() {
        return allOfficesList;
    }

    public void setAllOfficesList(List<Office> allOfficesList) {
        this.allOfficesList = allOfficesList;
    }

    public List<SelectItemWrapper<Office>> getSelectedOffices() {
        return selectedOffices;
    }

    public void setSelectedOffices(List<SelectItemWrapper<Office>> selectedOffices) {
        this.selectedOffices = selectedOffices;
    }

    public SelectItemWrapperConverter<Office> getOfficeConverter() {
        return officeConverter;
    }

    public void setOfficeConverter(SelectItemWrapperConverter<Office> officeConverter) {
        this.officeConverter = officeConverter;
    }

    public SelectItemWrapper<Office> getSelectedOffice() {
        return selectedOffice;
    }

    public void setSelectedOffice(SelectItemWrapper<Office> selectedOffice) {
        this.selectedOffice = selectedOffice;
    }
}
