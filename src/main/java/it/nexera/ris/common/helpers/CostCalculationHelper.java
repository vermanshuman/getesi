package it.nexera.ris.common.helpers;

import it.nexera.ris.common.enums.CostType;
import it.nexera.ris.common.enums.DocumentType;
import it.nexera.ris.common.enums.ExtraCostType;
import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.persistence.beans.dao.CriteriaAlias;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.IndexedEntity;
import it.nexera.ris.persistence.beans.entities.domain.*;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.Service;
import it.nexera.ris.web.beans.pages.RequestTextEditBean;
import org.hibernate.Hibernate;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;
import org.hibernate.sql.JoinType;

import javax.faces.context.FacesContext;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CostCalculationHelper {

    private Request request;

    public Request getRequest() {
        return request;
    }

    public void setRequest(Request request) {
        this.request = request;
    }

    public CostCalculationHelper(Request request) {
        this.request = request;
    }

    public void calculateAllCosts(Boolean isCostOutputImportant) throws IllegalAccessException, PersistenceBeanException, InstantiationException {
        calculateAllCosts(isCostOutputImportant, Boolean.FALSE);
    }

    public void calculateAllCosts(Boolean isCostOutputImportant, boolean recalculate) throws IllegalAccessException, PersistenceBeanException, InstantiationException {
        calculateAllCosts(isCostOutputImportant, isBillingClient(), restrictionForPriceList(), recalculate);
    }

    public double calculateTotalCost(Boolean isCostOutputImportant) throws IllegalAccessException, PersistenceBeanException, InstantiationException {

        RequestCosts requestCosts = new RequestCosts();

        if (!ValidationHelper.isNullOrEmpty(isBillingClient()) || isCostOutputImportant) {
            List<ExtraCost> extraCost = DaoManager.load(ExtraCost.class, new Criterion[]{
                    Restrictions.eq("requestId", getRequest().getId())});

            if (!ValidationHelper.isNullOrEmpty(getRequest().getAggregationLandChargesRegistry())
                    && !ValidationHelper.isNullOrEmpty(getRequest().getAggregationLandChargesRegistry().getNational())
                    && getRequest().getAggregationLandChargesRegistry().getNational()) {

                for (ExtraCost cost : extraCost) {
                    requestCosts.finalCostWithExtraCost += cost.getPrice();
                }

                double initCost = 0.0d;
                if (!ValidationHelper.isNullOrEmpty(getRequest().getService())
                        && !ValidationHelper.isNullOrEmpty(getRequest().getService().getNationalPrice())) {
                    initCost = getRequest().getService().getNationalPrice();
                } else if (!ValidationHelper.isNullOrEmpty(getRequest().getMultipleServices())) {
                    initCost = getRequest().getMultipleServices()
                            .stream()
                            .filter(s -> !ValidationHelper.isNullOrEmpty(s.getNationalPrice()))
                            .mapToDouble(Service::getNationalPrice).sum();
                }

                getRequest().setCostEstateFormality(initCost);

                List<PriceList> priceList = new ArrayList<PriceList>();
                if (!ValidationHelper.isNullOrEmpty(getRequest().getService())) {
                    priceList = DaoManager.load(PriceList.class, new CriteriaAlias[]{
                            new CriteriaAlias("costConfiguration", "cc", JoinType.INNER_JOIN)}, new Criterion[]{
                            Restrictions.eq("client", getRequest().getClient()),
                            Restrictions.eq("service", getRequest().getService()),
                            Restrictions.isNotNull("cc.id"),
                            Restrictions.eq("cc.typeId", CostType.EXTRA_COST.getId())});

                } else if (!ValidationHelper.isNullOrEmpty(getRequest().getMultipleServices())) {
                    priceList = DaoManager.load(PriceList.class, new CriteriaAlias[]{
                            new CriteriaAlias("costConfiguration", "cc", JoinType.INNER_JOIN)}, new Criterion[]{
                            Restrictions.eq("client", getRequest().getClient()),
                            Restrictions.in("service", getRequest().getMultipleServices()),
                            Restrictions.isNotNull("cc.id"),
                            Restrictions.eq("cc.typeId", CostType.EXTRA_COST.getId())});
                }

                if (!ValidationHelper.isNullOrEmpty(priceList)) {
                    for (PriceList price : priceList) {
                        if (!ValidationHelper.isNullOrEmpty(price.getPrice())) {
                            initCost += Double.parseDouble(price.getPrice());
                        }
                    }
                }

                if (isModelIdOfTemplateEqualsTo(2L)) {
                    double result = 0d;
                    priceList.clear();

                    if (!ValidationHelper.isNullOrEmpty(getRequest().getService())) {
                        priceList = DaoManager.load(PriceList.class,
                                new Criterion[]{
                                        Restrictions.eq("client", getRequest().getClient()),
                                        Restrictions.eq("isNegative", true),
                                        Restrictions.eq("service", getRequest().getService())});

                    } else if (!ValidationHelper.isNullOrEmpty(getRequest().getMultipleServices())) {
                        priceList = DaoManager.load(PriceList.class,
                                new Criterion[]{
                                        Restrictions.eq("client", getRequest().getClient()),
                                        Restrictions.eq("isNegative", true),
                                        Restrictions.in("service", getRequest().getMultipleServices())});
                    }

                    if (!ValidationHelper.isNullOrEmpty(priceList)) {
                        PriceList first = priceList.get(0);
                        Double conservationCosts = Double.parseDouble(first.getPrice().replaceAll(",", "."));
                        if (!ValidationHelper.isNullOrEmpty(getRequest().getAggregationLandChargesRegistry())
                                && !ValidationHelper.isNullOrEmpty(getRequest().getAggregationLandChargesRegistry().getLandChargesRegistries())) {
                            result = conservationCosts * getRequest().getAggregationLandChargesRegistry()
                                    .getNumberOfVisualizedLandChargesRegistries();
                        }
                    }
                    initCost += result;
                    getRequest().setCostPay(result);
                }
                requestCosts.finalCostWithExtraCost += initCost;

            } else if ((getRequest().getCalculateCost() == null || !getRequest().getCalculateCost()) && getRequest().getCostButtonConfirmClicked() != null
                    && getRequest().getCostButtonConfirmClicked()) {
                for (ExtraCost cost : extraCost) {
                    if (ValidationHelper.isNullOrEmpty(cost.getType()) ||
                            !ExtraCostType.NAZIONALEPOSITIVA.equals(cost.getType())) {
                        requestCosts.finalCostWithExtraCost += cost.getPrice();
                    }
                }
                double initCost = 0.0d;
                if (getRequest().getInitCost() != null) {
                    initCost = Double.parseDouble(getRequest().getInitCost().replaceAll(",", "."));
                }

                requestCosts.finalCostWithExtraCost += initCost;
            } else if (isModelIdOfTemplateEqualsTo(5L)) {
                for (ExtraCost cost : extraCost) {
                    requestCosts.finalCostWithExtraCost += cost.getPrice();
                }

                if (!ValidationHelper.isNullOrEmpty(getRequest().getService())
                        && !ValidationHelper.isNullOrEmpty(getRequest().getService().getNationalPrice())) {
                    requestCosts.finalCostWithExtraCost += getRequest().getService().getNationalPrice();
                }
                double initCost = 0.0d;
                if (getRequest().getInitCost() != null) {
                    initCost = Double.parseDouble(getRequest().getInitCost().replaceAll(",", "."));
                }
                requestCosts.finalCostWithExtraCost += initCost;
            } else {
                requestCosts = calculateAll(isBillingClient(), restrictionForPriceList(), extraCost);
            }
        }
        return requestCosts.finalCostWithExtraCost;
    }

    private RequestCosts calculateAllCosts(boolean isCostOutputImportant, Boolean isBillingClient, boolean restrictionForPriceList,
                                           boolean recalculate)
            throws IllegalAccessException, PersistenceBeanException, InstantiationException {
        RequestCosts requestCosts = new RequestCosts();

        if (!ValidationHelper.isNullOrEmpty(isBillingClient) || isCostOutputImportant) {
            List<ExtraCost> extraCost = DaoManager.load(ExtraCost.class, new Criterion[]{
                    Restrictions.eq("requestId", getRequest().getId())});
            if (!ValidationHelper.isNullOrEmpty(getRequest().getAggregationLandChargesRegistry())
                    && !ValidationHelper.isNullOrEmpty(getRequest().getAggregationLandChargesRegistry().getNational())
                    && getRequest().getAggregationLandChargesRegistry().getNational()) {

                for (ExtraCost cost : extraCost) {
                    if (ValidationHelper.isNullOrEmpty(cost.getType()) ||
                            !ExtraCostType.NAZIONALEPOSITIVA.equals(cost.getType())) {
                        requestCosts.finalCostWithExtraCost += cost.getPrice();
                    }

                }
                double initCost = 0.0d;
                if (!ValidationHelper.isNullOrEmpty(getRequest().getService())
                        && !ValidationHelper.isNullOrEmpty(getRequest().getService().getNationalPrice())) {
                    initCost = getRequest().getService().getNationalPrice();
                } else if (!ValidationHelper.isNullOrEmpty(getRequest().getMultipleServices())) {
                    initCost = getRequest().getMultipleServices()
                            .stream()
                            .filter(s -> !ValidationHelper.isNullOrEmpty(s.getNationalPrice()))
                            .mapToDouble(Service::getNationalPrice).sum();
                }

                getRequest().setCostEstateFormality(initCost);

                List<PriceList> priceList = new ArrayList<PriceList>();
                if (!ValidationHelper.isNullOrEmpty(getRequest().getService())) {
                    priceList = DaoManager.load(PriceList.class, new CriteriaAlias[]{
                            new CriteriaAlias("costConfiguration", "cc", JoinType.INNER_JOIN)}, new Criterion[]{
                            Restrictions.eq("client", getRequest().getClient()),
                            Restrictions.eq("service", getRequest().getService()),
                            Restrictions.isNotNull("cc.id"),
                            Restrictions.eq("cc.typeId", CostType.EXTRA_COST.getId())});

                } else if (!ValidationHelper.isNullOrEmpty(getRequest().getMultipleServices())) {
                    priceList = DaoManager.load(PriceList.class, new CriteriaAlias[]{
                            new CriteriaAlias("costConfiguration", "cc", JoinType.INNER_JOIN)}, new Criterion[]{
                            Restrictions.eq("client", getRequest().getClient()),
                            Restrictions.in("service", getRequest().getMultipleServices()),
                            Restrictions.isNotNull("cc.id"),
                            Restrictions.eq("cc.typeId", CostType.EXTRA_COST.getId())});
                }

                if (!ValidationHelper.isNullOrEmpty(priceList)) {
                    for (PriceList price : priceList) {
                        if (!ValidationHelper.isNullOrEmpty(price.getPrice())) {
                            initCost += Double.parseDouble(price.getPrice());
                        }
                    }
                }

                if (isModelIdOfTemplateEqualsTo(2L)) {
                    double result = 0d;
                    priceList.clear();

                    if (!ValidationHelper.isNullOrEmpty(getRequest().getService())) {
                        priceList = DaoManager.load(PriceList.class,
                                new Criterion[]{
                                        Restrictions.eq("client", getRequest().getClient()),
                                        Restrictions.eq("isNegative", true),
                                        Restrictions.eq("service", getRequest().getService())});

                    } else if (!ValidationHelper.isNullOrEmpty(getRequest().getMultipleServices())) {
                        priceList = DaoManager.load(PriceList.class,
                                new Criterion[]{
                                        Restrictions.eq("client", getRequest().getClient()),
                                        Restrictions.eq("isNegative", true),
                                        Restrictions.in("service", getRequest().getMultipleServices())});
                    }

                    if (!ValidationHelper.isNullOrEmpty(priceList)) {
                        PriceList first = priceList.get(0);
                        Double conservationCosts = Double.parseDouble(first.getPrice().replaceAll(",", "."));
                        if (!ValidationHelper.isNullOrEmpty(getRequest().getAggregationLandChargesRegistry())
                                && !ValidationHelper.isNullOrEmpty(getRequest().getAggregationLandChargesRegistry().getLandChargesRegistries())) {
                            result = conservationCosts * getRequest().getAggregationLandChargesRegistry()
                                    .getNumberOfVisualizedLandChargesRegistries();
                        }
                    }
                    initCost += result;
                    getRequest().setCostPay(result);
                }
                requestCosts.finalCostWithExtraCost += initCost;
                updateCostsOfRequest(initCost, requestCosts.finalCostWithExtraCost);

            } else if (!recalculate && !getRequest().isCalledFromReportList()) {
                for (ExtraCost cost : extraCost) {
                    if (ValidationHelper.isNullOrEmpty(cost.getType()) ||
                            !ExtraCostType.NAZIONALEPOSITIVA.equals(cost.getType())) {
                        requestCosts.finalCostWithExtraCost += cost.getPrice();
                    }

                }
                double initCost = 0.0d;
                if (getRequest().getInitCost() != null) {
                    initCost = Double.parseDouble(getRequest().getInitCost().replaceAll(",", "."));
                }

                requestCosts.finalCostWithExtraCost += initCost;

                updateCostsOfRequest(initCost, requestCosts.finalCostWithExtraCost);
            } else if (isModelIdOfTemplateEqualsTo(5L)) {
                for (ExtraCost cost : extraCost) {
                    if (ValidationHelper.isNullOrEmpty(cost.getType()) ||
                            !ExtraCostType.NAZIONALEPOSITIVA.equals(cost.getType())) {
                        requestCosts.finalCostWithExtraCost += cost.getPrice();
                    }

                }
                if (!ValidationHelper.isNullOrEmpty(getRequest().getService())
                        && !ValidationHelper.isNullOrEmpty(getRequest().getService().getNationalPrice())) {
                    requestCosts.finalCostWithExtraCost += getRequest().getService().getNationalPrice();
                }

                double initCost = 0.0d;
                if (getRequest().getInitCost() != null) {
                    initCost = Double.parseDouble(getRequest().getInitCost().replaceAll(",", "."));
                }
                requestCosts.finalCostWithExtraCost += initCost;
                updateCostsOfRequest(initCost, requestCosts.finalCostWithExtraCost);
            } else {
                requestCosts = calculateAll(isBillingClient, restrictionForPriceList, extraCost);

                getRequest().setCostCadastral(requestCosts.costCadastral);
                getRequest().setCostExtra(requestCosts.costExtra);
                getRequest().setCostEstateFormality(BigDecimal.valueOf(requestCosts.costEstateFormality)
                        .setScale(3, RoundingMode.HALF_UP).doubleValue());
                getRequest().setCostPay(requestCosts.costPay);
                getRequest().setTaxable(requestCosts.costTaxable);
                updateCostsOfRequest(requestCosts.finalCost, requestCosts.finalCostWithExtraCost);

            }
        }
        return requestCosts;
    }

    public Boolean isBillingClient() {
        if (!ValidationHelper.isNullOrEmpty(getRequest().getBillingClient())) {
            return true;
        } else if (!ValidationHelper.isNullOrEmpty(getRequest().getClient())
                && !ValidationHelper.isNullOrEmpty(getRequest().getClient().getCostOutput())
                && getRequest().getClient().getCostOutput()) {
            return false;
        }
        return null;
    }

    private RequestCosts calculateAll(Boolean isBillingClient, boolean restrictionForPriceList, List<ExtraCost> extraCost)
            throws PersistenceBeanException, IllegalAccessException, InstantiationException {
        RequestCosts requestCosts = new RequestCosts();
        requestCosts.costCadastral = getCostCadastral();
        requestCosts.costExtra = getCostExtra(isBillingClient, restrictionForPriceList);
        requestCosts.costEstate = getCostEstate(isBillingClient, restrictionForPriceList);
        requestCosts.costEstateFormality = getCostEstateFormality(isBillingClient, restrictionForPriceList);
        requestCosts.costEstateFormality += getCostPlus(isBillingClient, restrictionForPriceList);
        requestCosts.costEstateFormality += requestCosts.costEstate;
        requestCosts.costPay = getCostPay(isBillingClient, restrictionForPriceList);
        requestCosts.costTaxable = getCostTaxable(isBillingClient, restrictionForPriceList);
        requestCosts.finalCost = (double) Math.round((requestCosts.costCadastral + requestCosts.costExtra
                + requestCosts.costEstateFormality + requestCosts.costPay)
                * 100000d) / 100000d;
        requestCosts.finalCostWithExtraCost = requestCosts.finalCost;

        for (ExtraCost cost : extraCost) {
            if (ValidationHelper.isNullOrEmpty(cost.getType()) ||
                    !ExtraCostType.NAZIONALEPOSITIVA.equals(cost.getType())) {
                requestCosts.finalCostWithExtraCost += cost.getPrice();
            }

        }
        return requestCosts;
    }

    public boolean restrictionForPriceList() {
        boolean result = false;

        if (!ValidationHelper.isNullOrEmpty(getRequest().getDocumentsRequest())) {
            boolean isRequestHasDocumentWithSecondType = getRequest().getDocumentsRequest().stream()
                    .anyMatch(x -> DocumentType.OTHER.getId().equals(x.getTypeId()));
            if (!ValidationHelper.isNullOrEmpty(getRequest().getService())
                    && !ValidationHelper.isNullOrEmpty(getRequest().getService().getUnauthorizedQuote())) {
                result = isRequestHasDocumentWithSecondType && getRequest().getService().getUnauthorizedQuote();
            }
        }
        return result;
    }

    public Double getCostCadastral() throws PersistenceBeanException, IllegalAccessException {
        double result = 0d;
        if(!ValidationHelper.isNullOrEmpty(getRequest().getUnauthorizedQuote()) && getRequest().getUnauthorizedQuote()
                &&  ValidationHelper.isNullOrEmpty(getRequest().getPropertyList())) {
            getRequest().setCostCadastral(!ValidationHelper.isNullOrEmpty(getRequest().getClient().getUnauthorizedCostCadastral())
                    ? getRequest().getClient().getUnauthorizedCostCadastral() : 0d);
            return getRequest().getCostCadastral();
        }
        List<Document> documents = DaoManager.load(Document.class, new Criterion[]{
                Restrictions.eq("request.id", getRequest().getId())});
        if (!ValidationHelper.isNullOrEmpty(documents)) {
            for (Document document : documents) {
                if (DocumentType.CADASTRAL.getId().equals(document.getTypeId())
                        && !ValidationHelper.isNullOrEmpty(document.getCost())) {
                    String cost = document.getCost().replaceAll(",", ".");
                    result += Double.parseDouble(cost);
                }
            }
        }
        return result;
    }

    private Double getCostExtra(Boolean billingClient, boolean restrictionForPriceList)
            throws IllegalAccessException, PersistenceBeanException {
        double result = 0d;
        List<PriceList> priceList = new ArrayList<PriceList>();
        if (!ValidationHelper.isNullOrEmpty(getRequest().getService())) {
            priceList = DaoManager.load(PriceList.class, new CriteriaAlias[]{
                    new CriteriaAlias("costConfiguration", "cc", JoinType.INNER_JOIN)}, new Criterion[]{
                    Restrictions.eq("client", getRequest().getClient()),
                    Restrictions.eq("service", getRequest().getService()),

                    restrictionForPriceList ?
                            Restrictions.in("cc.id", getRequest().getService().getServiceCostUnauthorizedQuoteList()
                                    .stream().map(IndexedEntity::getId).collect(Collectors.toList())) :
                            Restrictions.isNotNull("cc.id"),

                    Restrictions.eq("cc.typeId", CostType.EXTRA_COST.getId())});
        } else if (!ValidationHelper.isNullOrEmpty(getRequest().getMultipleServices())) {
            priceList = DaoManager.load(PriceList.class, new CriteriaAlias[]{
                    new CriteriaAlias("costConfiguration", "cc", JoinType.INNER_JOIN)}, new Criterion[]{
                    Restrictions.eq("client", getRequest().getClient()),
                    Restrictions.in("service", getRequest().getMultipleServices()),
                    restrictionForPriceList ?
                            Restrictions.in("cc.id", getRequest().getService().getServiceCostUnauthorizedQuoteList()
                                    .stream().map(IndexedEntity::getId).collect(Collectors.toList())) :
                            Restrictions.isNotNull("cc.id"),
                    Restrictions.eq("cc.typeId", CostType.EXTRA_COST.getId())});
        }
        if (!ValidationHelper.isNullOrEmpty(priceList)) {
            for (PriceList price : priceList) {
                if (!ValidationHelper.isNullOrEmpty(price.getPrice())) {
                    result += Double.parseDouble(price.getPrice().replaceAll("\\,", "."));
                }
            }
        }
        return result;
    }

    public Double getCostEstate(Boolean billingClient, boolean restictionForPriceList)
            throws PersistenceBeanException, IllegalAccessException {
        double result = 0d;
        if(!ValidationHelper.isNullOrEmpty(getRequest().getUnauthorizedQuote()) && getRequest().getUnauthorizedQuote()){
            return result;
        }
        if (!ValidationHelper.isNullOrEmpty(getRequest().getService())) {
            List<PriceList> priceList = DaoManager.load(PriceList.class, new CriteriaAlias[]{
                    new CriteriaAlias("costConfiguration", "cc", JoinType.INNER_JOIN)}, new Criterion[]{
                    Restrictions.eq("client", getRequest().getClient()),

                    restictionForPriceList ?
                            Restrictions.in("cc.id", getRequest().getService().getServiceCostUnauthorizedQuoteList()
                                    .stream().map(IndexedEntity::getId).collect(Collectors.toList())) :
                            Restrictions.isNotNull("cc.id"),

                    Restrictions.eq("service", getRequest().getService()),
                    Restrictions.eq("cc.typeId", CostType.DEPENDING_ON_NUMBER_OF_FORMALITIES.getId())});

            if (!ValidationHelper.isNullOrEmpty(priceList) && !ValidationHelper.isNullOrEmpty(priceList.get(0).getPrice())) {

                if (!ValidationHelper.isNullOrEmpty(getRequest().getNumberActUpdate())) {
                    result = Double.parseDouble(priceList.get(0).getPrice().replaceAll(",", "."))
                            * getRequest().getNumberActUpdate();
                } else {
                    result = Double.parseDouble(priceList.get(0).getPrice().replaceAll(",", "."))
                            * getRequest().getSumOfEstateFormalitiesAndCommunicationsAndSuccess();
                }
            }
        } else if (!ValidationHelper.isNullOrEmpty(getRequest().getMultipleServices())) {
            for (Service service : getRequest().getMultipleServices()) {
                List<PriceList> priceList = DaoManager.load(PriceList.class, new CriteriaAlias[]{
                        new CriteriaAlias("costConfiguration", "cc", JoinType.INNER_JOIN)}, new Criterion[]{
                        Restrictions.eq("client", getRequest().getClient()),

                        restictionForPriceList ?
                                Restrictions.in("cc.id", getRequest().getService().getServiceCostUnauthorizedQuoteList()
                                        .stream().map(IndexedEntity::getId).collect(Collectors.toList())) :
                                Restrictions.isNotNull("cc.id"),

                        Restrictions.eq("service", service),
                        Restrictions.eq("cc.typeId", CostType.DEPENDING_ON_NUMBER_OF_FORMALITIES.getId())});
                if (!ValidationHelper.isNullOrEmpty(priceList) && !ValidationHelper.isNullOrEmpty(priceList.get(0).getPrice())) {

                    if (!ValidationHelper.isNullOrEmpty(getRequest().getNumberActUpdate())) {
                        result += (Double.parseDouble(priceList.get(0).getPrice().replaceAll(",", "."))
                                * getRequest().getNumberActUpdate());
                    } else {
                        result += (Double.parseDouble(priceList.get(0).getPrice().replaceAll(",", "."))
                                * getRequest().getSumOfEstateFormalitiesAndCommunicationsAndSuccess());
                    }
                }
            }
        }
        return result;
    }

    public Double getCostEstateFormality(Boolean billingClient, boolean restrictionForPriceList)
            throws PersistenceBeanException, IllegalAccessException, InstantiationException {
        double result = 0d;
        if(!ValidationHelper.isNullOrEmpty(getRequest().getUnauthorizedQuote()) && getRequest().getUnauthorizedQuote()
                &&  ValidationHelper.isNullOrEmpty(getRequest().getEstateFormalityList())) {
            getRequest().setCostEstateFormality(!ValidationHelper.isNullOrEmpty(getRequest().getClient().getUnauthorizedCostFormality())
                    ? getRequest().getClient().getUnauthorizedCostFormality() : 0d);
            return getRequest().getCostEstateFormality();
        }
        if (isModelIdOfTemplateEqualsTo(2L)) {
            if (!ValidationHelper.isNullOrEmpty(getRequest().getService())) {
                List<PriceList> priceList = DaoManager.load(PriceList.class,
                        new Criterion[]{
                                Restrictions.eq("client", getRequest().getClient()),
                                Restrictions.eq("isNegative", true),
                                Restrictions.eq("service", getRequest().getService())});
                if (!ValidationHelper.isNullOrEmpty(priceList)) {
                    PriceList first = priceList.get(0);
                    Double conservationCosts = Double.parseDouble(first.getFirstPrice().replaceAll(",", "."));
                    if (!ValidationHelper.isNullOrEmpty(getRequest().getAggregationLandChargesRegistry())
                            && !ValidationHelper.isNullOrEmpty(getRequest().getAggregationLandChargesRegistry().getLandChargesRegistries())) {
                        result = conservationCosts * getRequest().getAggregationLandChargesRegistry()
                                .getNumberOfVisualizedLandChargesRegistries();
                    }
                }
            } else if (!ValidationHelper.isNullOrEmpty(getRequest().getMultipleServices())) {
                for (Service service : getRequest().getMultipleServices()) {
                    List<PriceList> priceList = DaoManager.load(PriceList.class,
                            new Criterion[]{
                                    Restrictions.eq("client", getRequest().getClient()),
                                    Restrictions.eq("isNegative", true),
                                    Restrictions.eq("service", service)});
                    if (!ValidationHelper.isNullOrEmpty(priceList)) {
                        PriceList first = priceList.get(0);
                        Double conservationCosts = Double.parseDouble(first.getFirstPrice().replaceAll(",", "."));
                        if (!ValidationHelper.isNullOrEmpty(getRequest().getAggregationLandChargesRegistry())
                                && !ValidationHelper.isNullOrEmpty(getRequest().getAggregationLandChargesRegistry().getLandChargesRegistries())) {
                            result += (conservationCosts * getRequest().getAggregationLandChargesRegistry()
                                    .getNumberOfVisualizedLandChargesRegistries());
                        }
                    }

                }
            }
            return result;
        }

        List<Double> numberOfGroupedEstateFormality = getRequest().getSumOfGroupedEstateFormalities();
        Integer numberOfGroupsByDocumentOfEstateFormality = numberOfGroupedEstateFormality.size();

        boolean isServiceUpdate = false;
        if (!ValidationHelper.isNullOrEmpty(getRequest().getService())
                && !ValidationHelper.isNullOrEmpty(getRequest().getService().getIsUpdate())
                && getRequest().getService().getIsUpdate()) {
            isServiceUpdate = true;
        }

        if(isServiceUpdate){
            numberOfGroupedEstateFormality = Collections.singletonList(getNumActs(getRequest().getId()).doubleValue());
            numberOfGroupsByDocumentOfEstateFormality = numberOfGroupedEstateFormality.size();
        }
        
        if (!ValidationHelper.isNullOrEmpty(getRequest().getRequestFormalities())) {
        	if(!Hibernate.isInitialized(getRequest().getRequestFormalities())){
                Hibernate.initialize(getRequest().getRequestFormalities());
            }
            List<Long> documentIds = getRequest().getRequestFormalities().stream()
                    .filter(rf -> !ValidationHelper.isNullOrEmpty(rf.getDocumentId()))
                    .map(RequestFormality::getDocumentId)
                    .distinct().collect(Collectors.toList());
            if (ValidationHelper.isNullOrEmpty(documentIds) && isServiceUpdate) {
            	numberOfGroupedEstateFormality = getRequest().getSumOfGroupedEstateFormalities();
                numberOfGroupsByDocumentOfEstateFormality = numberOfGroupedEstateFormality.size();
            }
        }

        boolean isPriceList = Boolean.FALSE;
        if (!ValidationHelper.isNullOrEmpty(getRequest().getService())) {
            List<PriceList> priceList = loadPriceList(billingClient, restrictionForPriceList, getRequest().getService());
            if (!ValidationHelper.isNullOrEmpty(priceList)) {
                isPriceList = true;
                PriceList first = priceList.get(0);
                for (Integer i = 0; i < numberOfGroupsByDocumentOfEstateFormality; i++) {
                    if (!ValidationHelper.isNullOrEmpty(first.getNumberNextBlock())
                            && !ValidationHelper.isNullOrEmpty(first.getNextPrice())) {
                        if (numberOfGroupedEstateFormality.size() > i
                                && numberOfGroupedEstateFormality.get(i) > Double.parseDouble(first.getNumberFirstBlock())) {

                            double y = (numberOfGroupedEstateFormality.get(i) - Double.parseDouble(first.getNumberFirstBlock()))
                                    / Double.parseDouble(first.getNumberNextBlock());
                            y = Math.ceil(y);
                            double yCost = y * Double.parseDouble(first.getNextPrice().replaceAll(",", "."));

                            result += yCost + Double.parseDouble(first.getFirstPrice().replaceAll(",", "."));
                        } else {
                            result += Double.parseDouble(first.getFirstPrice().replaceAll(",", "."));
                        }
                    }
                }
            }
        } else if (!ValidationHelper.isNullOrEmpty(getRequest().getMultipleServices())) {
            for (Service service : getRequest().getMultipleServices()) {
                List<PriceList> priceList = loadPriceList(billingClient, restrictionForPriceList, service);
                if (!ValidationHelper.isNullOrEmpty(priceList)) {
                    isPriceList = true;
                    PriceList first = priceList.get(0);
                    for (Integer i = 0; i < numberOfGroupsByDocumentOfEstateFormality; i++) {
                        if (!ValidationHelper.isNullOrEmpty(first.getNumberNextBlock())
                                && !ValidationHelper.isNullOrEmpty(first.getNextPrice())) {
                            if (numberOfGroupedEstateFormality.size() > i
                                    && numberOfGroupedEstateFormality.get(i) > Double.parseDouble(first.getNumberFirstBlock())) {

                                double y = (numberOfGroupedEstateFormality.get(i) - Double.parseDouble(first.getNumberFirstBlock()))
                                        / Double.parseDouble(first.getNumberNextBlock());
                                y = Math.ceil(y);
                                double yCost = y * Double.parseDouble(first.getNextPrice().replaceAll(",", "."));

                                result += yCost + Double.parseDouble(first.getFirstPrice().replaceAll(",", "."));
                            } else {
                                result += Double.parseDouble(first.getFirstPrice().replaceAll(",", "."));
                            }

                        }
                    }
                }
            }
        }

        if (!isPriceList) {
            result = numberOfGroupedEstateFormality.stream().mapToDouble(Double::doubleValue).sum();
        }
        return result;
    }


    private BigInteger getNumActs(Long requestId) throws PersistenceBeanException, IllegalAccessException {
        BigInteger countFormality = DaoManager.countQuery("select count(*) " +
                "from request_formality where request_id=" + requestId);
        BigInteger countSuccessFormality = DaoManager.countQuery("select count(*)" +
                "from (estate_formality_success inner join request_formality on " +
                "estate_formality_success.estate_formality_id=request_formality.formality_id) " +
                "where estate_formality_success.note_type=\"NOTE_TYPE_A\" and request_formality.request_id=" + requestId);
        BigInteger countCommunication = DaoManager.countQuery("select count(*) " +
                "from (communication inner join request_formality on " +
                "communication.estate_formality_id=request_formality.formality_id) " +
                "where request_formality.request_id=" + requestId);
        return countFormality.add(countSuccessFormality).add(countCommunication);
    }

    public List<PriceList> loadPriceList(Boolean billingClient, boolean restrictionForPriceList)
            throws PersistenceBeanException, IllegalAccessException {

        if(!ValidationHelper.isNullOrEmpty(getRequest().getUnauthorizedQuote()) && getRequest().getUnauthorizedQuote()
                &&  ValidationHelper.isNullOrEmpty(getRequest().getEstateFormalityList())) {
            return new ArrayList<>();
        }

        return DaoManager.load(PriceList.class, new CriteriaAlias[]{
                new CriteriaAlias("costConfiguration", "cc", JoinType.INNER_JOIN)}, new Criterion[]{
                Restrictions.eq("client", getRequest().getClient()),

                restrictionForPriceList ?
                        Restrictions.in("cc.id", getRequest().getService().getServiceCostUnauthorizedQuoteList()
                                .stream().map(IndexedEntity::getId).collect(Collectors.toList())) :
                        Restrictions.isNotNull("cc.id"),

                Restrictions.eq("service", getRequest().getService()),
                Restrictions.eq("cc.typeId", CostType.FIXED_COST.getId())});
    }

    public List<PriceList> loadPriceList(Boolean billingClient, boolean restrictionForPriceList, Service service)
            throws PersistenceBeanException, IllegalAccessException {
//        if(!ValidationHelper.isNullOrEmpty(getRequest().getUnauthorizedQuote()) && getRequest().getUnauthorizedQuote()) {
//            return new ArrayList<>();
//        }
        return DaoManager.load(PriceList.class, new CriteriaAlias[]{
                new CriteriaAlias("costConfiguration", "cc", JoinType.INNER_JOIN)}, new Criterion[]{
                Restrictions.eq("client", getRequest().getClient()),

                restrictionForPriceList ?
                        Restrictions.in("cc.id", getRequest().getService().getServiceCostUnauthorizedQuoteList()
                                .stream().map(IndexedEntity::getId).collect(Collectors.toList())) :
                        Restrictions.isNotNull("cc.id"),

                Restrictions.eq("service", service),
                Restrictions.eq("cc.typeId", CostType.FIXED_COST.getId())});
    }

    public Double getCostPlus(Boolean billingClient, boolean restrictionForPriceList)
            throws PersistenceBeanException, IllegalAccessException, InstantiationException {
        double result = 0D;
        double firstPrice = 0D;
        int numRegistry = 0;
        int numRequestRegistry = 0;
        int numPlus = 0;

        if (isModelIdOfTemplateEqualsTo(2L)) {
            return result;
        }

        if (!ValidationHelper.isNullOrEmpty(getRequest().getAggregationLandChargesRegistry())
                && !ValidationHelper.isNullOrEmpty(getRequest().getAggregationLandChargesRegistry().getLandChargesRegistries())) {
            numRegistry = getRequest().getAggregationLandChargesRegistry().getNumberOfVisualizedLandChargesRegistries();
        }

        if (!ValidationHelper.isNullOrEmpty(getRequest().getRequestFormalities())) {
        	if(!Hibernate.isInitialized(getRequest().getRequestFormalities())){
                Hibernate.initialize(getRequest().getRequestFormalities());
            }
            List<Long> documentIds = getRequest().getRequestFormalities().stream()
                    .filter(rf -> !ValidationHelper.isNullOrEmpty(rf.getDocumentId()))
                    .map(RequestFormality::getDocumentId)
                    .distinct().collect(Collectors.toList());
            if (!ValidationHelper.isNullOrEmpty(documentIds)) {
                numRequestRegistry = DaoManager.getCount(DocumentSubject.class, "office.id",
                        new CriteriaAlias[]{
                                new CriteriaAlias("office", "office", JoinType.INNER_JOIN),
                                new CriteriaAlias("document", "document", JoinType.INNER_JOIN)},
                        new Criterion[]{
                                Restrictions.in("document.id", documentIds)}).intValue();
            } else {
                numRequestRegistry = 0;
            }
        }

        numPlus = numRegistry - numRequestRegistry;

        if (!ValidationHelper.isNullOrEmpty(getRequest().getService())) {
            List<PriceList> priceList = loadPriceList(billingClient, restrictionForPriceList);

            if (!ValidationHelper.isNullOrEmpty(priceList)) {
                PriceList first = priceList.get(0);
                firstPrice = Double.parseDouble(first.getFirstPrice().replaceAll(",", "."));
            }

            result = firstPrice * numPlus;
        } else if (!ValidationHelper.isNullOrEmpty(getRequest().getMultipleServices())) {
            for (Service service : request.getMultipleServices()) {
                List<PriceList> priceList = loadPriceList(billingClient, restrictionForPriceList, service);

                if (!ValidationHelper.isNullOrEmpty(priceList)) {
                    PriceList first = priceList.get(0);
                    firstPrice = Double.parseDouble(first.getFirstPrice().replaceAll(",", "."));
                }
                result += firstPrice * numPlus;
            }
        }
        return result;
    }

    public Double getCostPay(Boolean billingClient, boolean restrictionForPriceList)
            throws PersistenceBeanException, IllegalAccessException, InstantiationException {
        Double result = 0d;
        if (isModelIdOfTemplateEqualsTo(2L)) {
            if (!ValidationHelper.isNullOrEmpty(getRequest().getService())) {
                List<PriceList> priceList = DaoManager.load(PriceList.class,
                        new Criterion[]{
                                Restrictions.eq("client", getRequest().getClient()),
                                Restrictions.eq("isNegative", true),
                                Restrictions.eq("service", getRequest().getService())});

                if (!ValidationHelper.isNullOrEmpty(priceList)) {
                    PriceList first = priceList.get(0);
                    if (!ValidationHelper.isNullOrEmpty(first.getPrice())) {
                        result = Double.parseDouble(first.getPrice());
                    }
                }
            } else if (!ValidationHelper.isNullOrEmpty(getRequest().getMultipleServices())) {
                for (Service service : request.getMultipleServices()) {
                    List<PriceList> priceList = DaoManager.load(PriceList.class,
                            new Criterion[]{
                                    Restrictions.eq("client", getRequest().getClient()),
                                    Restrictions.eq("isNegative", true),
                                    Restrictions.eq("service", service)});

                    if (!ValidationHelper.isNullOrEmpty(priceList)) {
                        PriceList first = priceList.get(0);
                        if (!ValidationHelper.isNullOrEmpty(first.getPrice())) {
                            result += Double.parseDouble(first.getPrice());
                        }
                    }
                }
            }
            return result;
        }
        Double numberOfEstateFormality;
        if (ValidationHelper.isNullOrEmpty(getRequest().getNumberActUpdate())) {
            numberOfEstateFormality = Double.valueOf(getRequest().getSumOfEstateFormalitiesAndCommunicationsAndSuccess());
        } else {
            numberOfEstateFormality = getRequest().getNumberActUpdate();
        }
        if(!ValidationHelper.isNullOrEmpty(getRequest().getUnauthorizedQuote()) && getRequest().getUnauthorizedQuote()) {
            getRequest().setCostPay(!ValidationHelper.isNullOrEmpty(request.getClient().getUnauthorizedCostPay()) ? request.getClient().getUnauthorizedCostPay() : 0d);
            return getRequest().getCostPay();
        }
        List<PriceList> priceList = DaoManager.load(PriceList.class, new CriteriaAlias[]{
                new CriteriaAlias("costConfiguration", "cc", JoinType.INNER_JOIN)}, new Criterion[]{
                Restrictions.eq("client", getRequest().getClient()),

                restrictionForPriceList ?
                        Restrictions.in("cc.id", getRequest().getService().getServiceCostUnauthorizedQuoteList()
                                .stream().map(IndexedEntity::getId).collect(Collectors.toList())) :
                        Restrictions.isNotNull("cc.id"),

                Restrictions.eq("service", getRequest().getService()),
                Restrictions.eq("cc.typeId", CostType.SALARY_COST.getId())});

        if (!ValidationHelper.isNullOrEmpty(priceList)) {
            PriceList first = priceList.get(0);
            if (!ValidationHelper.isNullOrEmpty(first.getPayNextPrice())
                    && !ValidationHelper.isNullOrEmpty(first.getPayNumberBlock())) {
                if (numberOfEstateFormality > Double.parseDouble(first.getPayNumberBlock())) {
                    double x = (numberOfEstateFormality - Double.parseDouble(first.getPayNumberBlock()))
                            * Double.parseDouble(first.getPayNextPrice());

                    result = Double.parseDouble(first.getPrice()) + x;
                } else {
                    result = Double.valueOf(first.getPrice());
                }
            }
        }
        return result;
    }

    private Double getCostTaxable(Boolean billingClient, boolean restrictionForPriceList)
            throws PersistenceBeanException, IllegalAccessException {
        double result = 0d;
        Double numberOfEstateFormality;
        if (ValidationHelper.isNullOrEmpty(getRequest().getNumberActUpdate())) {
            numberOfEstateFormality = Double.valueOf(getRequest().getSumOfEstateFormalitiesAndCommunicationsAndSuccess());
        } else {
            numberOfEstateFormality = getRequest().getNumberActUpdate();
        }
        List<PriceList> priceList = null;
        if (!ValidationHelper.isNullOrEmpty(getRequest().getService())) {
            priceList = DaoManager.load(PriceList.class, new CriteriaAlias[]{
                    new CriteriaAlias("costConfiguration", "cc", JoinType.INNER_JOIN)}, new Criterion[]{
                    Restrictions.eq("client", getRequest().getClient()),

                    restrictionForPriceList ?
                            Restrictions.in("cc.id", getRequest().getService().getServiceCostUnauthorizedQuoteList()
                                    .stream().map(IndexedEntity::getId).collect(Collectors.toList())) :
                            Restrictions.isNotNull("cc.id"),
                    Restrictions.eq("service", getRequest().getService()),
            });
        } else if (!ValidationHelper.isNullOrEmpty(getRequest().getMultipleServices())) {
            priceList = DaoManager.load(PriceList.class, new CriteriaAlias[]{
                    new CriteriaAlias("costConfiguration", "cc", JoinType.INNER_JOIN)}, new Criterion[]{
                    Restrictions.eq("client", getRequest().getClient()),

                    restrictionForPriceList ?
                            Restrictions.in("cc.id", getRequest().getService().getServiceCostUnauthorizedQuoteList()
                                    .stream().map(IndexedEntity::getId).collect(Collectors.toList())) :
                            Restrictions.isNotNull("cc.id"),
                    Restrictions.in("service", getRequest().getMultipleServices()),
            });
        }

        if (!ValidationHelper.isNullOrEmpty(priceList)) {
            for (PriceList price : priceList) {

                if (!ValidationHelper.isNullOrEmpty(price.getPayNextPrice())
                        && !ValidationHelper.isNullOrEmpty(price.getPayNumberBlock())) {
                    if (numberOfEstateFormality > Double.parseDouble(price.getPayNumberBlock())) {
                        double x = (numberOfEstateFormality - Double.parseDouble(price.getPayNumberBlock()))
                                * Double.parseDouble(price.getPayNextPrice());
                        if (!ValidationHelper.isNullOrEmpty(price.getTaxable()) && price.getTaxable())
                            result += Double.parseDouble(price.getPrice()) + x;
                    } else {
                        if (!ValidationHelper.isNullOrEmpty(price.getPrice()) &&
                                (!ValidationHelper.isNullOrEmpty(price.getTaxable()) && price.getTaxable())) {
                            result += Double.parseDouble(price.getPrice());
                        }

                    }
                }
            }
        }
        return result;
    }

    private boolean isModelIdOfTemplateEqualsTo(Long value)
            throws IllegalAccessException, PersistenceBeanException, InstantiationException {

        Map<String, Object> viewMap = FacesContext.getCurrentInstance().getViewRoot().getViewMap();
        RequestTextEditBean requestTextEditBean = (RequestTextEditBean) viewMap.get("requestTextEditBean");
        Long modelIdOfTemplate = 0L;
        if (!ValidationHelper.isNullOrEmpty(requestTextEditBean)) {
            modelIdOfTemplate = DaoManager.get(DocumentTemplate.class, requestTextEditBean.getSelectedTemplateId()).getModel().getId();
        } else {
            if (!ValidationHelper.isNullOrEmpty(getRequest()) &&
                    !ValidationHelper.isNullOrEmpty(getRequest().getSelectedTemplateId())) {
                modelIdOfTemplate = DaoManager.get(DocumentTemplate.class, getRequest().getSelectedTemplateId()).getModel().getId();
            }
        }

        return modelIdOfTemplate.equals(value);
    }

    private void updateCostsOfRequest(Double init, Double total) throws PersistenceBeanException, IllegalAccessException {
        getRequest().setInitCost(String.format("%.2f", init));
        if (total != null) {
            getRequest().setTotalCost(String.format("%.2f", total));
        }
        if (!Hibernate.isInitialized(getRequest().getRequestSubjects())) {
            getRequest().reloadRequestSubjects();
        }
        if (!Hibernate.isInitialized(getRequest().getRequestFormalities())) {
            getRequest().reloadRequestFormalities();
        }
        DaoManager.save(getRequest(), true);
    }

    private static class RequestCosts {
        private double costCadastral;
        private double costExtra;
        private double costEstate;
        private double costEstateFormality;
        private double costPay;
        private double costTaxable;
        private double finalCost;
        private double finalCostWithExtraCost;
    }

}