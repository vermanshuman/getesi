package it.nexera.ris.web.beans.pages.dictionary;

import it.nexera.ris.common.enums.RequestOutputTypes;
import it.nexera.ris.common.enums.ServiceReferenceTypes;
import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.helpers.*;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.CostConfiguration;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.DataGroup;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.RequestType;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.Service;
import it.nexera.ris.web.beans.EntityEditPageBean;
import it.nexera.ris.web.beans.wrappers.Icon;
import org.hibernate.HibernateException;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.primefaces.model.DualListModel;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.model.SelectItem;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

@ManagedBean(name = "serviceEditBean")
@ViewScoped
public class ServiceEditBean extends EntityEditPageBean<Service>
        implements Serializable {

    private static final long serialVersionUID = 7436062700462718393L;

    private boolean onlyView;

    private List<SelectItem> requestTypes;

    private List<SelectItem> dataGroupTypes;

    private DualListModel<CostConfiguration> costConfigurations;

    private DualListModel<CostConfiguration> serviceCostUnauthorizedQuoteList;

    private Long selectedTypeId;

    private Long selectedGroupId;

    private List<SelectItem> updateServices;

    private Long selectedUpdateServiceId;

    private List<CostConfiguration> costConfigurationsForUpdate;

    private List<CostConfiguration> selectedCostConfigurationsForUpdate;

    private List<SelectItem> requestOutputItems;

    private Long selectedRequestOutputId;

    private Icon icon;

    private List<Icon> icons;
    
    private List<SelectItem> referenceItems;

    private Long selectedReferenceId;

    private Boolean salesDevelopment;

    private Boolean landOmi;

    @Override
    public void onLoad() throws NumberFormatException, HibernateException,
            PersistenceBeanException, InstantiationException,
            IllegalAccessException {
        if (SessionHelper.get("only_view_services") != null
                && (Boolean) SessionHelper.get("only_view_services")) {
            SessionHelper.removeObject("only_view_services");
            setOnlyView(true);
        }
        fillIconList();
        this.setRequestTypes(ComboboxHelper.fillList(RequestType.class, false));

        if (this.getEntity().getRequestType() != null) {
            this.setSelectedTypeId(this.getEntity().getRequestType().getId());
            this.changeUpdateService();
        } else {
            this.setUpdateServices(new ArrayList<>());
            this.getUpdateServices().add(SelectItemHelper.getNotSelected());
        }

        if (this.getEntity().getServiceForUpdate() != null) {
            this.setSelectedUpdateServiceId(
                    this.getEntity().getServiceForUpdate().getId());
        }

        if (this.getEntity().getCostConfigurationsUpdate() != null) {
            this.setSelectedCostConfigurationsForUpdate(
                    this.getEntity().getCostConfigurationsUpdate());
            this.changeUpdateCosts();
        } else {
            this.setSelectedCostConfigurationsForUpdate(new ArrayList<>());
        }

        List<CostConfiguration> sourceCost = DaoManager
                .load(CostConfiguration.class);
        List<CostConfiguration> targetCost = new ArrayList<>();

        List<CostConfiguration> sourceCostUnauthorizedQuote = new ArrayList<>(sourceCost.size());
        sourceCostUnauthorizedQuote.addAll(sourceCost);
        List<CostConfiguration> targetCostUnauthorizedQuote = new ArrayList<>();

        if (this.getEntity().getCostConfigurations() != null) {
            targetCost = this.getEntity().getCostConfigurations();

            for (CostConfiguration costEntity : targetCost) {
                if (sourceCost.contains(costEntity)) {
                    sourceCost.remove(costEntity);
                }
            }
        }

        if (this.getEntity().getServiceCostUnauthorizedQuoteList() != null) {
            targetCostUnauthorizedQuote = this.getEntity().getServiceCostUnauthorizedQuoteList();

            for (CostConfiguration costEntity : targetCostUnauthorizedQuote) {
                if (sourceCostUnauthorizedQuote.contains(costEntity)) {
                    sourceCostUnauthorizedQuote.remove(costEntity);
                }
            }
        }

        setDataGroupTypes(ComboboxHelper.fillList(DataGroup.class,new Criterion[]{
				Restrictions.or(
						Restrictions.eq("isDeleted", Boolean.FALSE),
						Restrictions.isNull("isDeleted"))
		}));
        

        if (getEntity().getGroup() != null) {
            this.setSelectedGroupId(getEntity().getGroup().getId());
        }

        if (getEntity().getRequestOutputType() != null) {
            setSelectedRequestOutputId(getEntity().getRequestOutputType().getId());
        }
        
        if (getEntity().getServiceReferenceType() != null) {
            setSelectedReferenceId(getEntity().getServiceReferenceType().getId());
        }

        this.setCostConfigurations(new DualListModel<>(sourceCost, targetCost));

        this.setServiceCostUnauthorizedQuoteList(new DualListModel<>(sourceCostUnauthorizedQuote, targetCostUnauthorizedQuote));

        this.setRequestOutputItems(ComboboxHelper.fillList(RequestOutputTypes.class, false));
        
        this.setReferenceItems(ComboboxHelper.fillList(ServiceReferenceTypes.class, true));

        executeJS("setIcon();");
        setSalesDevelopment(this.getEntity().getSalesDevelopment());
        setLandOmi(this.getEntity().getLandOmi());
    }

    public void editService() {
        this.setOnlyView(false);
    }

    @Override
    public void onValidate() throws PersistenceBeanException,
            HibernateException, IllegalAccessException {
        if (ValidationHelper.isNullOrEmpty(this.getEntity().getName())) {
            addRequiredFieldException("form:name");
        } else if (!ValidationHelper.isUnique(Service.class, "name",
                getEntity().getName(), this.getEntity().getId())) {
            addFieldException("form:name", "nameAlreadyInUse");
        }

        if (ValidationHelper.isNullOrEmpty(this.getSelectedTypeId())) {
            addRequiredFieldException("form:requestType");
        } else {
            if (!ValidationHelper.isNullOrEmpty(this.getEntity().getIsUpdate())
                    && this.getEntity().getEstimate()) {
                List<Service> services = DaoManager.load(Service.class,
                        new Criterion[]
                                {Restrictions.eq("estimate", Boolean.TRUE),
                                        Restrictions.eq("requestType.id",
                                                this.getSelectedTypeId()),
                                        Restrictions.ne("id",
                                                this.getEntity().getId() == null ? 0l
                                                        : this.getEntity().getId())});

                if (!ValidationHelper.isNullOrEmpty(services)) {
                    addFieldException("form:estimate",
                            "onlyOneUpdateOnEachRequest");
                }
            }
        }

        if (ValidationHelper.isNullOrEmpty(getSelectedGroupId())) {
            addRequiredFieldException("form:dataGroup");
        }

        if (ValidationHelper
                .isNullOrEmpty(this.getCostConfigurations().getTarget())) {
            addRequiredFieldException("form:costs");
        }

        if (!ValidationHelper.isNullOrEmpty(this.getEntity().getIsUpdate())
                && this.getEntity().getIsUpdate()) {
            if (ValidationHelper
                    .isNullOrEmpty(this.getSelectedUpdateServiceId())) {
                addRequiredFieldException("form:servicesForUpdate");
            }

            if (ValidationHelper.isNullOrEmpty(
                    this.getSelectedCostConfigurationsForUpdate())) {
                addRequiredFieldException("form:costsForUpdate");
            }
        }

        if (ValidationHelper.isNullOrEmpty(getSelectedRequestOutputId())) {
            addRequiredFieldException("form:requestOutput");
        }
    }

    @Override
    public void onSave() throws HibernateException, PersistenceBeanException,
            NumberFormatException, IOException, InstantiationException,
            IllegalAccessException {
        if (!ValidationHelper.isNullOrEmpty(this.getSelectedUpdateServiceId())) {
            this.getEntity().setServiceForUpdate(DaoManager.get(Service.class,
                    this.getSelectedUpdateServiceId()));
        }

        if (!ValidationHelper
                .isNullOrEmpty(this.getSelectedCostConfigurationsForUpdate())) {
            this.getEntity().setCostConfigurationsUpdate(
                    this.getSelectedCostConfigurationsForUpdate());
        }

        if (!ValidationHelper.isNullOrEmpty(getSelectedGroupId())) {
            getEntity().setGroup(DaoManager.get(DataGroup.class, getSelectedGroupId()));
        }

        this.getEntity().setCostConfigurations(
                this.getCostConfigurations().getTarget());
        this.getEntity().setServiceCostUnauthorizedQuoteList(
                this.getServiceCostUnauthorizedQuoteList().getTarget());
        this.getEntity().setRequestType(
                DaoManager.get(RequestType.class, this.getSelectedTypeId()));
        this.getEntity().setRequestOutputType(RequestOutputTypes.getById(getSelectedRequestOutputId()));

        this.getEntity().setServiceReferenceType(ServiceReferenceTypes.getById(getSelectedReferenceId()));

        if (!ValidationHelper.isNullOrEmpty(this.getSalesDevelopment()) && this.getSalesDevelopment()) {
            this.getEntity().setSalesDevelopment(this.getSalesDevelopment());
        }else {
            this.getEntity().setSalesDevelopment(null);
        }

        if (!ValidationHelper.isNullOrEmpty(this.getLandOmi()) && this.getLandOmi()) {
            this.getEntity().setLandOmi(this.getLandOmi());
        }else {
            this.getEntity().setLandOmi(null);
        }
        DaoManager.save(this.getEntity());
    }

    private void fillIconList() {
        setIcons(new ArrayList<>());
        InputStream in = getClass().getResourceAsStream("/faList");
        try (BufferedReader br = new BufferedReader(new InputStreamReader(in))) {
            String line;
            while ((line = br.readLine()) != null) {
                getIcons().add(new Icon(line));
            }
            setIcon(getIcons().get(0));
        } catch (Exception e) {
            LogHelper.log(log, e);
        }
    }

    public void changeUpdateService() {
        this.setCostConfigurationsForUpdate(new ArrayList<>());
        this.setSelectedCostConfigurationsForUpdate(new ArrayList<>());

        if (!ValidationHelper.isNullOrEmpty(this.getSelectedTypeId())) {
            try {
                this.setUpdateServices(ComboboxHelper.fillList(Service.class,
                        Order.asc("name"), new Criterion[]
                                {Restrictions.eq("requestType.id", this.getSelectedTypeId()),
                                        Restrictions.ne("id",
                                                this.getEntity().getId() == null ? 0l
                                                        : this.getEntity().getId())}));
            } catch (Exception e) {
                LogHelper.log(log, e);
            }
        } else {
            this.setUpdateServices(new ArrayList<>());
            this.getUpdateServices().add(SelectItemHelper.getNotSelected());
        }
    }

    public void changeUpdateCosts() {
        if (!ValidationHelper.isNullOrEmpty(this.getSelectedUpdateServiceId())) {
            try {
                Service service = DaoManager.get(Service.class,
                        this.getSelectedUpdateServiceId());

                if (service.getCostConfigurations() != null) {
                    this.setCostConfigurationsForUpdate(
                            service.getCostConfigurations());
                }
            } catch (Exception e) {
                LogHelper.log(log, e);
            }
        } else {
            this.setCostConfigurationsForUpdate(new ArrayList<>());
        }
    }

    public boolean isOnlyView() {
        return onlyView;
    }

    public void setOnlyView(boolean onlyView) {
        this.onlyView = onlyView;
    }

    public List<SelectItem> getRequestTypes() {
        return requestTypes;
    }

    public void setRequestTypes(List<SelectItem> requestTypes) {
        this.requestTypes = requestTypes;
    }

    public DualListModel<CostConfiguration> getCostConfigurations() {
        return costConfigurations;
    }

    public void setCostConfigurations(
            DualListModel<CostConfiguration> costConfigurations) {
        this.costConfigurations = costConfigurations;
    }

    public Long getSelectedTypeId() {
        return selectedTypeId;
    }

    public void setSelectedTypeId(Long selectedTypeId) {
        this.selectedTypeId = selectedTypeId;
    }

    public List<SelectItem> getUpdateServices() {
        return updateServices;
    }

    public void setUpdateServices(List<SelectItem> updateServices) {
        this.updateServices = updateServices;
    }

    public Long getSelectedUpdateServiceId() {
        return selectedUpdateServiceId;
    }

    public void setSelectedUpdateServiceId(Long selectedUpdateServiceId) {
        this.selectedUpdateServiceId = selectedUpdateServiceId;
    }

    public List<CostConfiguration> getCostConfigurationsForUpdate() {
        return costConfigurationsForUpdate;
    }

    public void setCostConfigurationsForUpdate(
            List<CostConfiguration> costConfigurationsForUpdate) {
        this.costConfigurationsForUpdate = costConfigurationsForUpdate;
    }

    public List<CostConfiguration> getSelectedCostConfigurationsForUpdate() {
        return selectedCostConfigurationsForUpdate;
    }

    public void setSelectedCostConfigurationsForUpdate(
            List<CostConfiguration> selectedCostConfigurationsForUpdate) {
        this.selectedCostConfigurationsForUpdate = selectedCostConfigurationsForUpdate;
    }

    public Long getSelectedGroupId() {
        return selectedGroupId;
    }

    public void setSelectedGroupId(Long selectedGroupId) {
        this.selectedGroupId = selectedGroupId;
    }

    public List<SelectItem> getDataGroupTypes() {
        return dataGroupTypes;
    }

    public void setDataGroupTypes(List<SelectItem> dataGroupTypes) {
        this.dataGroupTypes = dataGroupTypes;
    }

    public List<SelectItem> getRequestOutputItems() {
        return requestOutputItems;
    }

    public void setRequestOutputItems(List<SelectItem> requestOutputItems) {
        this.requestOutputItems = requestOutputItems;
    }

    public Long getSelectedRequestOutputId() {
        return selectedRequestOutputId;
    }

    public void setSelectedRequestOutputId(Long selectedRequestOutputId) {
        this.selectedRequestOutputId = selectedRequestOutputId;
    }

    public Icon getIcon() {
        return icon;
    }

    public void setIcon(Icon icon) {
        this.icon = icon;
    }

    public List<Icon> getIcons() {
        return icons;
    }

    public void setIcons(List<Icon> icons) {
        this.icons = icons;
    }

    public DualListModel<CostConfiguration> getServiceCostUnauthorizedQuoteList() {
        return serviceCostUnauthorizedQuoteList;
    }

    public void setServiceCostUnauthorizedQuoteList(DualListModel<CostConfiguration> serviceCostUnauthorizedQuoteList) {
        this.serviceCostUnauthorizedQuoteList = serviceCostUnauthorizedQuoteList;
    }

    public List<SelectItem> getReferenceItems() {
        return referenceItems;
    }

    public Long getSelectedReferenceId() {
        return selectedReferenceId;
    }

    public void setReferenceItems(List<SelectItem> referenceItems) {
        this.referenceItems = referenceItems;
    }

    public void setSelectedReferenceId(Long selectedReferenceId) {
        this.selectedReferenceId = selectedReferenceId;
    }

    public Boolean getSalesDevelopment() {
        return salesDevelopment;
    }

    public void setSalesDevelopment(Boolean salesDevelopment) {
        this.salesDevelopment = salesDevelopment;
    }

    public Boolean getLandOmi() {
        return landOmi;
    }

    public void setLandOmi(Boolean landOmi) {
        this.landOmi = landOmi;
    }
}
