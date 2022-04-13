package it.nexera.ris.web.beans.wrappers.logic;

import it.nexera.ris.common.enums.CostType;
import it.nexera.ris.common.enums.ExtraCostType;
import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.helpers.LogHelper;
import it.nexera.ris.common.helpers.ValidationHelper;
import it.nexera.ris.persistence.beans.dao.CriteriaAlias;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.domain.Client;
import it.nexera.ris.persistence.beans.entities.domain.ClientServiceInfo;
import it.nexera.ris.persistence.beans.entities.domain.PriceList;
import it.nexera.ris.persistence.beans.entities.domain.TaxRateExtraCost;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.CostConfiguration;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.Service;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.HibernateException;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;
import org.hibernate.sql.JoinType;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class ServiceWrapper implements Serializable {

    private static final long serialVersionUID = 2548659873242007382L;

    private transient final Log log = LogFactory.getLog(ServiceWrapper.class);

    private Service service;

    private List<PriceList> priceLists;

    private Date configureDate;

    private Client client;

    private ClientServiceInfo info;

    private List<TaxRateExtraCost> taxRateExtraCosts;

    public ServiceWrapper(Service service, Client client) {
        this.service = service;
        this.priceLists = new ArrayList<>();
        this.client = client;
        this.taxRateExtraCosts = new ArrayList<>();

        if (!client.isNew()) {
            try {
                List<Date> configureDates = DaoManager.loadField(PriceList.class, "configureDate", Date.class,
                        new Criterion[]{
                                Restrictions.eq("service.id", getService().getId()),
                                Restrictions.eq("client.id", getClient().getId())
                        });

                if (!ValidationHelper.isNullOrEmpty(configureDates)) {
                    configureDate = configureDates.get(0);
                }

                info = DaoManager.get(ClientServiceInfo.class, new Criterion[]{
                        Restrictions.eq("service.id", getService().getId()),
                        Restrictions.eq("client.id", getClient().getId())
                });

            } catch (Exception e) {
                LogHelper.log(log, e);
            }
        }
        if (info == null) {
            info = new ClientServiceInfo(getClient(), getService(), null);
        }
    }

    public void fillPriceLists() {
        if (ValidationHelper.isNullOrEmpty(getPriceLists())) {
            try {
                List<PriceList> priceLists = DaoManager.load(PriceList.class, new Criterion[]{
                        Restrictions.eq("service.id", getService().getId()),
                        Restrictions.eq("client.id", getClient().getId())
                });

                List<CostConfiguration> costConfigurations = DaoManager.load(CostConfiguration.class, new CriteriaAlias[]{
                        new CriteriaAlias("services", "services", JoinType.INNER_JOIN)
                }, new Criterion[]{
                        Restrictions.eq("services.id", getService().getId())
                });

                if (!ValidationHelper.isNullOrEmpty(priceLists)) {
                    List<PriceList> wastePriceList = priceLists.stream()
                            .filter(pl -> ValidationHelper.isNullOrEmpty(pl.getCostConfiguration())
                                    || costConfigurations.stream().noneMatch(cc -> cc.getId()
                                    .equals(pl.getCostConfiguration().getId())))
                            .collect(Collectors.toList());
                    if (!ValidationHelper.isNullOrEmpty(wastePriceList)) {
                        priceLists.removeAll(wastePriceList);
                        for (PriceList priceList : wastePriceList) {
                            DaoManager.remove(priceList);
                        }
                    }
                    priceLists.addAll(costConfigurations.stream().filter(cc->priceLists.stream()
                            .noneMatch(pl->pl.getCostConfiguration().getId().equals(cc.getId())))
                            .map(cc -> new PriceList(getService(), getClient(), cc)).collect(Collectors.toList()));
                    setPriceLists(priceLists);
                } else {
                    setPriceLists(costConfigurations.stream().map(cc -> new PriceList(getService(), getClient(), cc))
                            .collect(Collectors.toList()));
                }
            } catch (Exception e) {
                LogHelper.log(log, e);
            }
        }
    }

    public void fillTaxRateExtraCostLists() {
        if (ValidationHelper.isNullOrEmpty(getTaxRateExtraCosts())) {
            TaxRateExtraCost mortgageCosts = new TaxRateExtraCost();
            TaxRateExtraCost landRegistryCosts = new TaxRateExtraCost();
            TaxRateExtraCost stamps = new TaxRateExtraCost();
            TaxRateExtraCost postalCosts = new TaxRateExtraCost();
            TaxRateExtraCost other = new TaxRateExtraCost();
            taxRateExtraCosts.add(mortgageCosts);
            taxRateExtraCosts.add(landRegistryCosts);
            taxRateExtraCosts.add(stamps);
            taxRateExtraCosts.add(postalCosts);
            taxRateExtraCosts.add(other);

            try {
                List<TaxRateExtraCost> taxRateExtraCosts = DaoManager.load(TaxRateExtraCost.class, new Criterion[]{
                        Restrictions.eq("service.id", getService().getId()),
                        Restrictions.eq("clientId", getClient().getId())
                });
                for (TaxRateExtraCost taxRateExtraCost :taxRateExtraCosts) {
                    if(taxRateExtraCost.getExtraCostType().equals(ExtraCostType.IPOTECARIO)) {
                        taxRateExtraCosts.get(0).setTaxRateExtraCost(taxRateExtraCost);
                    } else if(taxRateExtraCost.getExtraCostType().equals(ExtraCostType.CATASTO)) {
                        taxRateExtraCosts.get(1).setTaxRateExtraCost(taxRateExtraCost);
                    } else if(taxRateExtraCost.getExtraCostType().equals(ExtraCostType.MARCA)) {
                        taxRateExtraCosts.get(2).setTaxRateExtraCost(taxRateExtraCost);
                    } else if(taxRateExtraCost.getExtraCostType().equals(ExtraCostType.POSTALE)) {
                        taxRateExtraCosts.get(3).setTaxRateExtraCost(taxRateExtraCost);
                    } else if(taxRateExtraCost.getExtraCostType().equals(ExtraCostType.ALTRO)) {
                        taxRateExtraCosts.get(4).setTaxRateExtraCost(taxRateExtraCost);
                    }
                }
            } catch (Exception e) {
                LogHelper.log(log, e);
            }
        }
    }
    
    public List<PriceList> getCompensationList() throws HibernateException, IllegalAccessException, PersistenceBeanException {
        
        List<PriceList> priceLists = DaoManager.load(PriceList.class, new Criterion[]{
                Restrictions.eq("service.id", getService().getId()),
                Restrictions.eq("client.id", getClient().getId())
        });
        
        if (!ValidationHelper.isNullOrEmpty(priceLists)) {
            return priceLists.stream()
                    .filter(i ->
                            (!ValidationHelper.isNullOrEmpty(i.getPrice()) &&
                                    !ValidationHelper.isNullOrEmpty(i.getCostConfiguration()) &&
                                    !ValidationHelper.isNullOrEmpty(i.getCostConfiguration().getTypeId()) && 
                                    i.getCostConfiguration().getTypeId().equals(CostType.SALARY_COST.getId())))
                    .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }

    public List<PriceList> getUrgencyPriceList() throws HibernateException, IllegalAccessException, PersistenceBeanException {
        List<PriceList> priceLists = DaoManager.load(PriceList.class, new Criterion[]{
                Restrictions.eq("service.id", getService().getId()),
                Restrictions.eq("client.id", getClient().getId())
        });

        if (!ValidationHelper.isNullOrEmpty(priceLists)) {
            return priceLists.stream()
                    .filter(i ->
                            (!ValidationHelper.isNullOrEmpty(i.getPrice()) &&
                            i.getCostConfiguration() != null && !ValidationHelper.isNullOrEmpty(i.getCostConfiguration().getUrgency()) && 
                            i.getCostConfiguration().getUrgency()))
                    .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }

    public Service getService() {
        return service;
    }

    public void setService(Service service) {
        this.service = service;
    }

    public List<PriceList> getPriceLists() {
        return priceLists;
    }

    public void setPriceLists(List<PriceList> priceLists) {
        this.priceLists = priceLists;
    }

    public Date getConfigureDate() {
        return configureDate;
    }

    public void setConfigureDate(Date configureDate) {
        this.configureDate = configureDate;
    }

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    public ClientServiceInfo getInfo() {
        return info;
    }

    public void setInfo(ClientServiceInfo info) {
        this.info = info;
    }

    public List<TaxRateExtraCost> getTaxRateExtraCosts() {
        return taxRateExtraCosts;
    }

    public void setTaxRateExtraCosts(List<TaxRateExtraCost> taxRateExtraCosts) {
        this.taxRateExtraCosts = taxRateExtraCosts;
    }
}
