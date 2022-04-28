package it.nexera.ris.common.helpers;

import it.nexera.ris.common.enums.CostType;
import it.nexera.ris.common.enums.ExtraCostType;
import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.persistence.beans.dao.CriteriaAlias;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.IndexedEntity;
import it.nexera.ris.persistence.beans.entities.domain.*;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.Service;
import it.nexera.ris.web.beans.pages.BillingListBean;
import it.nexera.ris.web.common.RequestPriceListModel;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.HibernateException;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;
import org.hibernate.sql.JoinType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class InvoiceHelper {
    private static transient final Log log = LogFactory.getLog(InvoiceHelper.class);

    public static List<InvoiceItem> groupingItemsByTaxRate(List<Request> selectedRequestList) throws HibernateException, IllegalAccessException, PersistenceBeanException, InstantiationException {
        Map<Long, List<PriceList>> priceListMap = new HashMap<>();
        for(Request request: selectedRequestList) {
            CostCalculationHelper costCalculationHelper = new CostCalculationHelper(request);
            List<PriceList> requestPriceList = new ArrayList<>();
            if(!ValidationHelper.isNullOrEmpty(request.getService())) {
                List<PriceList>  priceList = costCalculationHelper.loadPriceList(costCalculationHelper.isBillingClient(), costCalculationHelper.restrictionForPriceList(),request.getService());
                if (!ValidationHelper.isNullOrEmpty(priceList)) {
                    requestPriceList.addAll(priceList);
                }
                priceList = DaoManager.load(PriceList.class, new CriteriaAlias[]{
                        new CriteriaAlias("costConfiguration", "cc", JoinType.INNER_JOIN)}, new Criterion[]{
                        Restrictions.eq("client", (costCalculationHelper.isBillingClient() == null || !costCalculationHelper.isBillingClient())
                                ? request.getClient() : request.getBillingClient()),
                        Restrictions.eq("service", request.getService()),
                        Restrictions.isNotNull("cc.id"),
                        Restrictions.eq("cc.typeId", CostType.EXTRA_COST.getId())});
                if (!ValidationHelper.isNullOrEmpty(priceList)) {
                    requestPriceList.addAll(priceList);
                }

                priceList = DaoManager.load(PriceList.class, new CriteriaAlias[]{
                        new CriteriaAlias("costConfiguration", "cc", JoinType.INNER_JOIN)}, new Criterion[]{
                        Restrictions.eq("client", (costCalculationHelper.isBillingClient() == null || !costCalculationHelper.isBillingClient())
                                ? request.getClient() : request.getBillingClient()),

                        costCalculationHelper.restrictionForPriceList() ?
                                Restrictions.in("cc.id", request.getService().getServiceCostUnauthorizedQuoteList()
                                        .stream().map(IndexedEntity::getId).collect(Collectors.toList())) :
                                Restrictions.isNotNull("cc.id"),

                        Restrictions.eq("service", request.getService()),
                        Restrictions.eq("cc.typeId", CostType.DEPENDING_ON_NUMBER_OF_FORMALITIES.getId())});

                if (!ValidationHelper.isNullOrEmpty(priceList)) {
                    requestPriceList.addAll(priceList);
                }

                priceList = DaoManager.load(PriceList.class, new CriteriaAlias[]{
                        new CriteriaAlias("costConfiguration", "cc", JoinType.INNER_JOIN)}, new Criterion[]{
                        Restrictions.eq("client", (costCalculationHelper.isBillingClient() == null || !costCalculationHelper.isBillingClient())
                                ? request.getClient() : request.getBillingClient()),

                        costCalculationHelper.restrictionForPriceList() ?
                                Restrictions.in("cc.id", request.getService().getServiceCostUnauthorizedQuoteList()
                                        .stream().map(IndexedEntity::getId).collect(Collectors.toList())) :
                                Restrictions.isNotNull("cc.id"),

                        Restrictions.eq("service", request.getService()),
                        Restrictions.eq("cc.typeId", CostType.SALARY_COST.getId())});
                if (!ValidationHelper.isNullOrEmpty(priceList)) {
                    requestPriceList.addAll(priceList);
                }
            }else if(!ValidationHelper.isNullOrEmpty(request.getMultipleServices())) {
                for(Service service : request.getMultipleServices()) {
                    List<PriceList>  priceList =  costCalculationHelper.loadPriceList(costCalculationHelper.isBillingClient(), costCalculationHelper.restrictionForPriceList(),service);
                    if (!ValidationHelper.isNullOrEmpty(priceList)) {
                        requestPriceList.addAll(priceList);
                    }
                    priceList = DaoManager.load(PriceList.class, new CriteriaAlias[]{
                            new CriteriaAlias("costConfiguration", "cc", JoinType.INNER_JOIN)}, new Criterion[]{
                            Restrictions.eq("client", (costCalculationHelper.isBillingClient() == null || !costCalculationHelper.isBillingClient())
                                    ? request.getClient() : request.getBillingClient()),

                            costCalculationHelper.restrictionForPriceList() ?
                                    Restrictions.in("cc.id", request.getService().getServiceCostUnauthorizedQuoteList()
                                            .stream().map(IndexedEntity::getId).collect(Collectors.toList())) :
                                    Restrictions.isNotNull("cc.id"),

                            Restrictions.eq("service", service),
                            Restrictions.eq("cc.typeId", CostType.DEPENDING_ON_NUMBER_OF_FORMALITIES.getId())});
                    if (!ValidationHelper.isNullOrEmpty(priceList)) {
                        requestPriceList.addAll(priceList);
                    }
                }
                List<PriceList>  priceList = DaoManager.load(PriceList.class, new CriteriaAlias[]{
                        new CriteriaAlias("costConfiguration", "cc", JoinType.INNER_JOIN)}, new Criterion[]{
                        Restrictions.eq("client", (costCalculationHelper.isBillingClient() == null || !costCalculationHelper.isBillingClient())
                                ? request.getClient() : request.getBillingClient()),
                        Restrictions.in("service", request.getMultipleServices()),
                        Restrictions.isNotNull("cc.id"),
                        Restrictions.eq("cc.typeId", CostType.EXTRA_COST.getId())});
                if (!ValidationHelper.isNullOrEmpty(priceList)) {
                    requestPriceList.addAll(priceList);
                }
            }
//
//            if(!ValidationHelper.isNullOrEmpty(request.getService())) {
//                priceList = DaoManager.load(PriceList.class,
//                        new Criterion[]{
//                                Restrictions.eq("client", request.getClient() != null ? request.getClient() : request.getBillingClient()),
//                                Restrictions.eq("isNegative", true),
//                                Restrictions.eq("service", request.getService())});
//
//            }else if(!ValidationHelper.isNullOrEmpty(request.getMultipleServices())) {
//                priceList = DaoManager.load(PriceList.class,
//                        new Criterion[]{
//                                Restrictions.eq("client", request.getClient() != null ? request.getClient() : request.getBillingClient()),
//                                Restrictions.eq("isNegative", true),
//                                Restrictions.in("service", request.getMultipleServices())});
//            }
            priceListMap.put(request.getId(), requestPriceList);
        }

        List<RequestPriceListModel> requestPriceListModels = new ArrayList<>();
        for(Map.Entry<Long, List<PriceList>> entry : priceListMap.entrySet()) {
            Long requestId = entry.getKey();
            Request request = DaoManager.get(Request.class, requestId);
//            String totalCostStr = request.getTotalCost() != null ? request.getTotalCost() : "0.0";
//            if(totalCostStr.contains(",")) {
//                totalCostStr = totalCostStr.replace(",", ".");
//            }
          //  Double totalCost = Double.parseDouble(totalCostStr);
            List<PriceList> prices = entry.getValue();
            for(PriceList priceList : prices) {
                Double totalCost = 0.0;
                RequestPriceListModel requestPriceListModel = new RequestPriceListModel();
                if(!ValidationHelper.isNullOrEmpty(priceList.getCostConfiguration())
                        && !ValidationHelper.isNullOrEmpty(priceList.getCostConfiguration().getTypeId())){
                    if(priceList.getCostConfiguration().getTypeId().equals(CostType.FIXED_COST.getId())){

                        int numRegistry = 0;
                        int numRequestRegistry = 0;
                        int numPlus = 0;

                        if (!ValidationHelper.isNullOrEmpty(request.getAggregationLandChargesRegistry())
                                && !ValidationHelper.isNullOrEmpty(request.getAggregationLandChargesRegistry().getLandChargesRegistries())) {
                            numRegistry = request.getAggregationLandChargesRegistry().getNumberOfVisualizedLandChargesRegistries();
                        }

                        if (!ValidationHelper.isNullOrEmpty(request.getRequestFormalities())) {
                            List<Long> documentIds = request.getRequestFormalities().stream()
                                    .filter(rf -> !ValidationHelper.isNullOrEmpty(rf.getDocumentId()))
                                    .map(RequestFormality::getDocumentId)
                                    .distinct().collect(Collectors.toList());
                            if(!ValidationHelper.isNullOrEmpty(documentIds)) {
                                numRequestRegistry = DaoManager.getCount(DocumentSubject.class, "office.id",
                                        new CriteriaAlias[] {
                                                new CriteriaAlias("office", "office", JoinType.INNER_JOIN),
                                                new CriteriaAlias("document", "document", JoinType.INNER_JOIN)},
                                        new Criterion[]{
                                                Restrictions.in("document.id", documentIds)}).intValue();
                            }else {
                                numRequestRegistry = 0;
                            }
                        }
                        numPlus = numRegistry - numRequestRegistry;
                        if (!ValidationHelper.isNullOrEmpty(priceList)) {
                            Double firstPrice = Double.parseDouble(priceList.getFirstPrice().replaceAll(",", "."));
                            totalCost += firstPrice * numPlus;
                        }

                        List<Double> numberOfGroupedEstateFormality = request.getSumOfGroupedEstateFormalities();
                        Integer numberOfGroupsByDocumentOfEstateFormality = numberOfGroupedEstateFormality.size();
                        for (Integer i = 0; i < numberOfGroupsByDocumentOfEstateFormality; i++) {
                            if (!ValidationHelper.isNullOrEmpty(priceList.getNumberNextBlock())
                                    && !ValidationHelper.isNullOrEmpty(priceList.getNextPrice())) {
                                if (numberOfGroupedEstateFormality.size() > i
                                        && numberOfGroupedEstateFormality.get(i) > Double.parseDouble(priceList.getNumberFirstBlock())) {

                                    double y = (numberOfGroupedEstateFormality.get(i) - Double.parseDouble(priceList.getNumberFirstBlock()))
                                            / Double.parseDouble(priceList.getNumberNextBlock());
                                    y = Math.ceil(y);
                                    double yCost = y * Double.parseDouble(priceList.getNextPrice().replaceAll(",", "."));

                                    totalCost += yCost + Double.parseDouble(priceList.getFirstPrice().replaceAll(",", "."));
                                } else {
                                    totalCost += Double.parseDouble(priceList.getFirstPrice().replaceAll(",", "."));
                                }
                            }
                        }
                        requestPriceListModel.setTaxRate(priceList.getTaxRate());
                    }else  if(priceList.getCostConfiguration().getTypeId().equals(CostType.EXTRA_COST.getId())){
                        totalCost += Double.parseDouble(priceList.getPrice().replaceAll("\\,", "."));
                    } else  if(priceList.getCostConfiguration().getTypeId().equals(
                            CostType.DEPENDING_ON_NUMBER_OF_FORMALITIES.getId())){
                        requestPriceListModel.setTaxRate(priceList.getTaxRate());
                        if (!ValidationHelper.isNullOrEmpty(request.getNumberActUpdate())) {
                            totalCost += Double.parseDouble(priceList.getPrice().replaceAll(",", "."))
                                    * request.getNumberActUpdate();
                        } else {
                            totalCost += Double.parseDouble(priceList.getPrice().replaceAll(",", "."))
                                    * request.getSumOfEstateFormalitiesAndCommunicationsAndSuccess();
                        }
                    }else  if(priceList.getCostConfiguration().getTypeId().equals(
                            CostType.SALARY_COST.getId())){
                        requestPriceListModel.setTaxRate(priceList.getTaxRate());
                        Double numberOfEstateFormality;
                        if (ValidationHelper.isNullOrEmpty(request.getNumberActUpdate())) {
                            numberOfEstateFormality = Double.valueOf(request.getSumOfEstateFormalitiesAndCommunicationsAndSuccess());
                        } else {
                            numberOfEstateFormality = request.getNumberActUpdate();
                        }
                        if (!ValidationHelper.isNullOrEmpty(priceList.getPayNextPrice())
                                && !ValidationHelper.isNullOrEmpty(priceList.getPayNumberBlock())) {
                            if (numberOfEstateFormality > Double.parseDouble(priceList.getPayNumberBlock())) {
                                double x = (numberOfEstateFormality - Double.parseDouble(priceList.getPayNumberBlock()))
                                        * Double.parseDouble(priceList.getPayNextPrice());

                                totalCost += Double.parseDouble(priceList.getPrice()) + x;
                            } else {
                                totalCost += Double.valueOf(priceList.getPrice());
                            }
                        }
                    }
                }else if(!ValidationHelper.isNullOrEmpty(priceList.getIsNegative()) && priceList.getIsNegative()){
                    requestPriceListModel.setTaxRate(priceList.getTaxRate());
                    Double conservationCosts = Double.parseDouble(priceList.getPrice().replaceAll(",", "."));
                    if (!ValidationHelper.isNullOrEmpty(request.getAggregationLandChargesRegistry())
                            && !ValidationHelper.isNullOrEmpty(request.getAggregationLandChargesRegistry().getLandChargesRegistries())) {
                        totalCost += conservationCosts * request.getAggregationLandChargesRegistry()
                                .getNumberOfVisualizedLandChargesRegistries();
                    }
                }
                requestPriceListModel.setRequestId(requestId);
                requestPriceListModel.setRequest(request);
                requestPriceListModel.setTotalCost(totalCost);
                requestPriceListModel.setClient(priceList.getClient());
                requestPriceListModel.setService(priceList.getService());

                requestPriceListModels.add(requestPriceListModel);
            }

            List<ExtraCost> extraCost = DaoManager.load(ExtraCost.class, new Criterion[]{
                    Restrictions.eq("requestId", requestId)});

            for (ExtraCost cost : extraCost) {
                if(!ExtraCostType.NAZIONALEPOSITIVA.equals(extraCost)) {
                    RequestPriceListModel requestPriceListModel = new RequestPriceListModel();
                    requestPriceListModel.setRequestId(requestId);
                    requestPriceListModel.setRequest(request);
                    requestPriceListModel.setTotalCost(cost.getPrice());
                    requestPriceListModel.setClient(request.getClient());
                    requestPriceListModel.setService(request.getService());
                    List<TaxRateExtraCost> taxRateExtraCosts =  DaoManager.load(TaxRateExtraCost.class, new Criterion[]{
                            Restrictions.isNotNull("extraCostType"),
                            Restrictions.isNotNull("clientId"),
                            Restrictions.isNotNull("service"),
                            Restrictions.eq("clientId", request.getClient().getId()),
                            Restrictions.eq("service", request.getService()),
                            Restrictions.eq("extraCostType", cost.getType())});
                    if(!ValidationHelper.isNullOrEmpty(taxRateExtraCosts)){
                        requestPriceListModel.setTaxRate(taxRateExtraCosts.get(0).getTaxRate());
                        requestPriceListModels.add(requestPriceListModel);
                    }
                }
            }
        }
        requestPriceListModels.stream()
                .forEach(rp -> {
                    log.info(rp.getTaxRate() + "   " + rp.getTotalCost());
                });
        Map<TaxRate, Double> taxRateMap = requestPriceListModels
                .stream()
                .filter(rp -> !ValidationHelper.isNullOrEmpty(rp.getTaxRate())
                        && !ValidationHelper.isNullOrEmpty(rp.getRequest())
                        && !ValidationHelper.isNullOrEmpty(rp.getTotalCost()))
                .collect(
                        Collectors.groupingBy(RequestPriceListModel::getTaxRate,
                                Collectors.summingDouble(RequestPriceListModel::getTotalCost)));
        List<InvoiceItem> invoiceItems = new ArrayList<>();
        for(Map.Entry<TaxRate, Double> taxRateEntry : taxRateMap.entrySet()) {
            TaxRate taxRate = taxRateEntry.getKey();
            InvoiceItem invoiceItem = new InvoiceItem();
            invoiceItem.setTaxRate(taxRate);
            invoiceItem.setInvoiceTotalCost(taxRateEntry.getValue().doubleValue());
            invoiceItems.add(invoiceItem);
        }
            return invoiceItems;
    }
}
