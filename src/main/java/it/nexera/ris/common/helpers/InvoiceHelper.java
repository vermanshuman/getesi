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
import it.nexera.ris.web.beans.wrappers.GoodsServicesFieldWrapper;
import it.nexera.ris.web.common.RequestPriceListModel;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.hibernate.sql.JoinType;

import javax.faces.model.SelectItem;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class InvoiceHelper {
    private static transient final Log log = LogFactory.getLog(InvoiceHelper.class);
    public static String specialCharacters = "[!@#$%*()=|<>?{}\\[\\]~]";
    public static Pattern specialCharactersPattern = Pattern.compile (specialCharacters);
    private static final String MAIL_FOOTER = ResourcesHelper.getString("emailFooter");

    public static List<RequestPriceListModel> groupingItemsByTaxRate(List<Request> selectedRequestList)
            throws HibernateException, IllegalAccessException, PersistenceBeanException, InstantiationException {
        Map<Long, List<PriceList>> priceListMap = new HashMap<>();

        for(Request request: selectedRequestList) {
            request.setSelectedTemplateId(null);
            RequestPrint requestPrint = DaoManager.get(RequestPrint.class,
                    new CriteriaAlias[]{new CriteriaAlias("request", "rq", JoinType.INNER_JOIN)},
                    new Criterion[]{Restrictions.eq("rq.id", request.getId())});
            if (requestPrint != null) {
                if (requestPrint.getTemplate() != null) {
                    request.setSelectedTemplateId(requestPrint.getTemplate().getId());
                }
            }
            CostCalculationHelper costCalculationHelper = new CostCalculationHelper(request);
            List<PriceList> requestPriceList = new ArrayList<>();
            if(!ValidationHelper.isNullOrEmpty(request.getService())) {
                List<PriceList>  priceList = costCalculationHelper.loadPriceList(costCalculationHelper.isBillingClient(), costCalculationHelper.restrictionForPriceList(),request.getService());
                if (!ValidationHelper.isNullOrEmpty(priceList)) {
                    requestPriceList.addAll(priceList);
                }
                priceList = DaoManager.load(PriceList.class, new CriteriaAlias[]{
                        new CriteriaAlias("costConfiguration", "cc", JoinType.INNER_JOIN)}, new Criterion[]{
                        Restrictions.eq("client", request.getClient()),
                        Restrictions.eq("service", request.getService()),
                        Restrictions.isNotNull("cc.id"),
                        Restrictions.eq("cc.typeId", CostType.EXTRA_COST.getId())});
                if (!ValidationHelper.isNullOrEmpty(priceList)) {
                    requestPriceList.addAll(priceList);
                }

                if(ValidationHelper.isNullOrEmpty(request.getUnauthorizedQuote()) || !request.getUnauthorizedQuote()){
                    priceList = DaoManager.load(PriceList.class, new CriteriaAlias[]{
                            new CriteriaAlias("costConfiguration", "cc", JoinType.INNER_JOIN)}, new Criterion[]{
                            Restrictions.eq("client", request.getClient()),
                            costCalculationHelper.restrictionForPriceList() ?
                                    Restrictions.in("cc.id", request.getService().getServiceCostUnauthorizedQuoteList()
                                            .stream().map(IndexedEntity::getId).collect(Collectors.toList())) :
                                    Restrictions.isNotNull("cc.id"),

                            Restrictions.eq("service", request.getService()),
                            Restrictions.eq("cc.typeId", CostType.DEPENDING_ON_NUMBER_OF_FORMALITIES.getId())});

                    if (!ValidationHelper.isNullOrEmpty(priceList)) {
                        requestPriceList.addAll(priceList);
                    }
                }

                priceList = DaoManager.load(PriceList.class, new CriteriaAlias[]{
                        new CriteriaAlias("costConfiguration", "cc", JoinType.INNER_JOIN)}, new Criterion[]{
                        Restrictions.eq("client", request.getClient()),
                        costCalculationHelper.restrictionForPriceList() ?
                                Restrictions.in("cc.id", request.getService().getServiceCostUnauthorizedQuoteList()
                                        .stream().map(IndexedEntity::getId).collect(Collectors.toList())) :
                                Restrictions.isNotNull("cc.id"),

                        Restrictions.eq("service", request.getService()),
                        Restrictions.eq("cc.typeId", CostType.SALARY_COST.getId())});
                if (!ValidationHelper.isNullOrEmpty(priceList)) {
                    requestPriceList.addAll(priceList);
                }

                //if negativo template is applied
                if(!ValidationHelper.isNullOrEmpty(request.getSelectedTemplateId()) && request.getSelectedTemplateId().equals(8L)) {
                    priceList = DaoManager.load(PriceList.class,
                            new Criterion[]{
                                    Restrictions.eq("client", request.getClient()),
                                    Restrictions.eq("isNegative", true),
                                    Restrictions.eq("service", request.getService())});
                    requestPriceList = new ArrayList<>();
                    if (!ValidationHelper.isNullOrEmpty(priceList)) {
                        requestPriceList.addAll(priceList);
                    }
                }
                
                //if unautorized quote
                if(!ValidationHelper.isNullOrEmpty(request.getUnauthorizedQuote()) && request.getUnauthorizedQuote()) {
                	requestPriceList.clear();
                	Double costCadastral = costCalculationHelper.getCostCadastral();
                    Double costEstate = costCalculationHelper.getCostEstate(costCalculationHelper.isBillingClient(), costCalculationHelper.restrictionForPriceList());
                    Double costEstateFormality = costCalculationHelper.getCostEstateFormality(costCalculationHelper.isBillingClient(), costCalculationHelper.restrictionForPriceList());
                    Double costPlus = costCalculationHelper.getCostPlus(costCalculationHelper.isBillingClient(), costCalculationHelper.restrictionForPriceList());
                    costEstateFormality += costPlus;
                    costEstateFormality += costEstate;
                    Double costPay = costCalculationHelper.getCostPay(costCalculationHelper.isBillingClient(), costCalculationHelper.restrictionForPriceList());
            		
                    if(!ValidationHelper.isNullOrEmpty(request.getClient().getTaxRateUnauthorizedCostCadastral())) {
	                    PriceList pricesCostCadastral = new PriceList();
	            		pricesCostCadastral.setPrice(String.valueOf(costCadastral.doubleValue()));
	            		pricesCostCadastral.setTaxRate(request.getClient().getTaxRateUnauthorizedCostCadastral());
	            		pricesCostCadastral.setClient(request.getClient());
	            		pricesCostCadastral.setService(request.getService());
	            		requestPriceList.add(pricesCostCadastral);
                    }
            		
                    if(!ValidationHelper.isNullOrEmpty(request.getClient().getTaxRateUnauthorizedCostFormality()) && costPlus.doubleValue() > 0.0) {
                        priceList = costCalculationHelper.loadPriceList(costCalculationHelper.isBillingClient(), costCalculationHelper.restrictionForPriceList());
                        if (!ValidationHelper.isNullOrEmpty(priceList)) {
                            PriceList first = priceList.get(0);
                            PriceList pricesCostFormality = new PriceList();
    	            		pricesCostFormality.setPrice(String.valueOf(costEstateFormality.doubleValue()));
    	            		pricesCostFormality.setTaxRate(first.getTaxRate());
    	            		pricesCostFormality.setClient(request.getClient());
    	            		pricesCostFormality.setService(request.getService());
    	            		requestPriceList.add(pricesCostFormality);
                        }
                    } else {
                    	PriceList pricesCostFormality = new PriceList();
	            		pricesCostFormality.setPrice(String.valueOf(costEstateFormality.doubleValue()));
	            		pricesCostFormality.setTaxRate(request.getClient().getTaxRateUnauthorizedCostFormality());
	            		pricesCostFormality.setClient(request.getClient());
	            		pricesCostFormality.setService(request.getService());
	            		requestPriceList.add(pricesCostFormality);
                    }
            		
            		if(!ValidationHelper.isNullOrEmpty(request.getClient().getTaxRateUnauthorizedCostPay())) {
	            		PriceList pricesCostPay = new PriceList();
	            		pricesCostPay.setPrice(String.valueOf(costPay.doubleValue()));
	            		pricesCostPay.setTaxRate(request.getClient().getTaxRateUnauthorizedCostPay());
	            		pricesCostPay.setClient(request.getClient());
	            		pricesCostPay.setService(request.getService());
	            		requestPriceList.add(pricesCostPay);
            		}
                }
            }else if(!ValidationHelper.isNullOrEmpty(request.getMultipleServices())) {
                if(ValidationHelper.isNullOrEmpty(request.getUnauthorizedQuote()) || !request.getUnauthorizedQuote()){
                    for(Service service : request.getMultipleServices()) {
                        List<PriceList>  priceList =  costCalculationHelper.loadPriceList(costCalculationHelper.isBillingClient(), costCalculationHelper.restrictionForPriceList(),service);
                        if (!ValidationHelper.isNullOrEmpty(priceList)) {
                            requestPriceList.addAll(priceList);
                        }
                        priceList = DaoManager.load(PriceList.class, new CriteriaAlias[]{
                                new CriteriaAlias("costConfiguration", "cc", JoinType.INNER_JOIN)}, new Criterion[]{
                                Restrictions.eq("client", request.getClient()),

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
                }
                List<PriceList>  priceList = DaoManager.load(PriceList.class, new CriteriaAlias[]{
                        new CriteriaAlias("costConfiguration", "cc", JoinType.INNER_JOIN)}, new Criterion[]{
                        Restrictions.eq("client", request.getClient()),
                        Restrictions.in("service", request.getMultipleServices()),
                        Restrictions.isNotNull("cc.id"),
                        Restrictions.eq("cc.typeId", CostType.EXTRA_COST.getId())});
                if (!ValidationHelper.isNullOrEmpty(priceList)) {
                    requestPriceList.addAll(priceList);
                }

                //if negativo template is applied
                if(!ValidationHelper.isNullOrEmpty(request.getSelectedTemplateId()) && request.getSelectedTemplateId().equals(8L)) {
                    requestPriceList = new ArrayList<>();
                    for (Service service : request.getMultipleServices()) {
                        priceList = DaoManager.load(PriceList.class,
                                new Criterion[]{
                                        Restrictions.eq("client", request.getClient()),
                                        Restrictions.eq("isNegative", true),
                                        Restrictions.eq("service", service)});
                        if (!ValidationHelper.isNullOrEmpty(priceList)) {
                            requestPriceList.addAll(priceList);
                        }
                    }
                }
            }
            priceListMap.put(request.getId(), requestPriceList);
        }

        List<RequestPriceListModel> requestPriceListModels = new ArrayList<>();
        for(Map.Entry<Long, List<PriceList>> entry : priceListMap.entrySet()) {
            Long requestId = entry.getKey();
            Request request = DaoManager.get(Request.class, requestId);
            request.setSelectedTemplateId(null);
            RequestPrint requestPrint = DaoManager.get(RequestPrint.class,
                    new CriteriaAlias[]{new CriteriaAlias("request", "rq", JoinType.INNER_JOIN)},
                    new Criterion[]{Restrictions.eq("rq.id", request.getId())});
            if (requestPrint != null) {
                if (requestPrint.getTemplate() != null) {
                    request.setSelectedTemplateId(requestPrint.getTemplate().getId());
                }
            }
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

                        if (!Hibernate.isInitialized(request.getRequestFormalities())) {
                            request.reloadRequestFormalities();
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

                        boolean isServiceUpdate = false;
                        if (!ValidationHelper.isNullOrEmpty(request.getService())
                                && !ValidationHelper.isNullOrEmpty(request.getService().getIsUpdate())
                                && request.getService().getIsUpdate()) {
                            isServiceUpdate = true;
                        }

                        if(isServiceUpdate){
                            numberOfGroupedEstateFormality = Collections.singletonList(getNumActs(request.getId()).doubleValue());
                            numberOfGroupsByDocumentOfEstateFormality = numberOfGroupedEstateFormality.size();
                        }else if (!ValidationHelper.isNullOrEmpty(request.getNumberActUpdate()) && request.getNumberActUpdate() > 0) {
                            numberOfGroupedEstateFormality = Collections.singletonList(request.getNumberActUpdate());
                            numberOfGroupsByDocumentOfEstateFormality = numberOfGroupedEstateFormality.size();
                        }


                        if (!ValidationHelper.isNullOrEmpty(request.getRequestFormalities())) {
                        	if(!Hibernate.isInitialized(request.getRequestFormalities())){
                                Hibernate.initialize(request.getRequestFormalities());
                            }
                            List<Long> documentIds = request.getRequestFormalities().stream()
                                    .filter(rf -> !ValidationHelper.isNullOrEmpty(rf.getDocumentId()))
                                    .map(RequestFormality::getDocumentId)
                                    .distinct().collect(Collectors.toList());
                            if (ValidationHelper.isNullOrEmpty(documentIds) && isServiceUpdate) {
                            	numberOfGroupedEstateFormality = request.getSumOfGroupedEstateFormalities();
                                numberOfGroupsByDocumentOfEstateFormality = numberOfGroupedEstateFormality.size();
                            }
                        }
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
                        requestPriceListModel.setTaxRate(priceList.getTaxRate());
                    } else if(priceList.getCostConfiguration().getTypeId().equals(
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
                            CostType.BASED_OF_NUMBER_OF_FORMALITIES_CONSULTED.getId())){
                        totalCost += Double.parseDouble(priceList.getFirstPrice().replaceAll(",", "."));
                        List<TaxRateExtraCost> taxRateExtraCosts =  DaoManager.load(TaxRateExtraCost.class, new Criterion[]{
                                Restrictions.isNotNull("extraCostType"),
                                Restrictions.isNotNull("clientId"),
                                Restrictions.isNotNull("service"),
                                Restrictions.eq("clientId", request.getClient().getId()),
                                Restrictions.eq("service", request.getService()),
                                Restrictions.eq("extraCostType", ExtraCostType.CATASTO)});
                        if(!ValidationHelper.isNullOrEmpty(taxRateExtraCosts)){
                            requestPriceListModel.setTaxRate(taxRateExtraCosts.get(0).getTaxRate());
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
                }
                //if negativo template is applied
                else if(!ValidationHelper.isNullOrEmpty(priceList.getIsNegative()) && priceList.getIsNegative()
                        && !ValidationHelper.isNullOrEmpty(request.getSelectedTemplateId()) && request.getSelectedTemplateId().equals(8L)){
                    requestPriceListModel.setTaxRate(priceList.getTaxRate());
                    totalCost = 0d;
                    double firstPrice = 0d;
                    double price = 0d;
                    double result = 0d;
                    double nationalPrice = 0d;
                    Double conservationCosts = Double.parseDouble(priceList.getFirstPrice().replaceAll(",", "."));
                    if (!ValidationHelper.isNullOrEmpty(request.getAggregationLandChargesRegistry())
                            && !ValidationHelper.isNullOrEmpty(request.getAggregationLandChargesRegistry().getLandChargesRegistries())) {
                        firstPrice = conservationCosts * request.getAggregationLandChargesRegistry()
                                .getNumberOfVisualizedLandChargesRegistries();
                    }
                    if (!ValidationHelper.isNullOrEmpty(priceList.getPrice())) {
                        price = Double.parseDouble(priceList.getPrice().replaceAll(",", "."));
                    }
                    totalCost = firstPrice + price;
                    if (!ValidationHelper.isNullOrEmpty(request.getAggregationLandChargesRegistry())
                            && !ValidationHelper.isNullOrEmpty(request.getAggregationLandChargesRegistry().getNational())
                            && request.getAggregationLandChargesRegistry().getNational()) {
                        conservationCosts = Double.parseDouble(priceList.getPrice().replaceAll(",", "."));
                        if (!ValidationHelper.isNullOrEmpty(request.getAggregationLandChargesRegistry())
                                && !ValidationHelper.isNullOrEmpty(request.getAggregationLandChargesRegistry().getLandChargesRegistries())) {
                            result = conservationCosts * request.getAggregationLandChargesRegistry()
                                    .getNumberOfVisualizedLandChargesRegistries();
                        }

                        if (!ValidationHelper.isNullOrEmpty(request.getService())
                                && !ValidationHelper.isNullOrEmpty(request.getService().getNationalPrice())) {
                            if(!ValidationHelper.isNullOrEmpty(request.getService().getNationalTaxRate())) {
                                if(request.getService().getNationalTaxRate().equals(priceList.getTaxRate())) {
                                    nationalPrice = request.getService().getNationalPrice();
                                } else {
                                    nationalPrice = 0d;
                                    RequestPriceListModel requestPriceListModelNational = new RequestPriceListModel();
                                    requestPriceListModelNational.setRequestId(requestId);
                                    requestPriceListModelNational.setRequest(request);
                                    requestPriceListModelNational.setTotalCost(request.getService().getNationalPrice());
                                    requestPriceListModelNational.setClient(priceList.getClient());
                                    requestPriceListModelNational.setService(priceList.getService());
                                    requestPriceListModelNational.setTaxRate(request.getService().getNationalTaxRate());
                                    requestPriceListModels.add(requestPriceListModelNational);
                                }
                            } else {
                                nationalPrice = request.getService().getNationalPrice();
                            }
                        }
                        totalCost = result + nationalPrice;
                    }
                }
                else if(!ValidationHelper.isNullOrEmpty(priceList.getIsNegative()) && priceList.getIsNegative()){
                    requestPriceListModel.setTaxRate(priceList.getTaxRate());
                    Double conservationCosts = Double.parseDouble(priceList.getPrice().replaceAll(",", "."));
                    if (!ValidationHelper.isNullOrEmpty(request.getAggregationLandChargesRegistry())
                            && !ValidationHelper.isNullOrEmpty(request.getAggregationLandChargesRegistry().getLandChargesRegistries())) {
                        totalCost += conservationCosts * request.getAggregationLandChargesRegistry()
                                .getNumberOfVisualizedLandChargesRegistries();
                    }
                }
                
                //if unautorized quote
                else if(!ValidationHelper.isNullOrEmpty(request.getUnauthorizedQuote()) && request.getUnauthorizedQuote()) {
                	requestPriceListModel.setTaxRate(priceList.getTaxRate());
                	totalCost = Double.parseDouble(priceList.getPrice().replaceAll(",", "."));
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

            Boolean manageTranscription = null;
            Boolean manageCertification = null;
            if(request.getService() != null && request.getService().getManageTranscription() != null
                    && request.getService().getManageTranscription())
                manageTranscription = Boolean.TRUE;

            if(request.getService() != null && request.getService().getManageCertification() != null
                    && request.getService().getManageCertification())
                manageCertification = Boolean.TRUE;
            for (ExtraCost cost : extraCost) {
                if((manageTranscription != null && manageTranscription && (
                        cost.getTranscription() == null || !cost.getTranscription())) ||
                        (manageCertification != null && manageCertification && (
                                cost.getCertification() == null || !cost.getCertification()))){
                    break;
                }
                if(!ExtraCostType.NAZIONALEPOSITIVA.equals(cost.getType())) {
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
                } else {
                    RequestPriceListModel requestPriceListModel = new RequestPriceListModel();
                    requestPriceListModel.setRequestId(requestId);
                    requestPriceListModel.setRequest(request);
                    requestPriceListModel.setTotalCost(cost.getPrice());
                    requestPriceListModel.setClient(request.getClient());
                    requestPriceListModel.setService(request.getService());
                    requestPriceListModel.setTaxRate(request.getService().getNationalTaxRate());
                    requestPriceListModels.add(requestPriceListModel);
                }
            }

            if(!(!ValidationHelper.isNullOrEmpty(request.getUnauthorizedQuote())
                    && request.getUnauthorizedQuote())) {
	            List<Document> documents = DaoManager.load(Document.class, new Criterion[]{
	                    Restrictions.eq("request.id", requestId)});
	            if (!ValidationHelper.isNullOrEmpty(documents)) {
	                for (Document document : documents) {
	                    if (DocumentType.CADASTRAL.getId().equals(document.getTypeId())
	                            && !ValidationHelper.isNullOrEmpty(document.getCost())) {
	                        String cost = document.getCost().replaceAll(",", ".");
	                        RequestPriceListModel requestPriceListModel = new RequestPriceListModel();
	                        requestPriceListModel.setRequestId(requestId);
	                        requestPriceListModel.setRequest(request);
	                        requestPriceListModel.setTotalCost(Double.parseDouble(cost));
	                        requestPriceListModel.setClient(request.getClient());
	                        requestPriceListModel.setService(request.getService());
	                        List<TaxRateExtraCost> taxRateExtraCosts = DaoManager.load(TaxRateExtraCost.class, new Criterion[]{
	                                Restrictions.isNotNull("extraCostType"),
	                                Restrictions.isNotNull("clientId"),
	                                Restrictions.isNotNull("service"),
	                                Restrictions.eq("clientId", request.getClient().getId()),
	                                Restrictions.eq("service", request.getService()),
	                                Restrictions.eq("extraCostType", ExtraCostType.CATASTO)});
	                        if (!ValidationHelper.isNullOrEmpty(taxRateExtraCosts)) {
	                            requestPriceListModel.setTaxRate(taxRateExtraCosts.get(0).getTaxRate());
	                            requestPriceListModels.add(requestPriceListModel);
	                        }
	                    }
	                }
	            }
            }
            TranscriptionData transcriptionData =  DaoManager.get(TranscriptionData.class,
                    new CriteriaAlias[]{
                            new CriteriaAlias("request", "r", JoinType.INNER_JOIN)
                    },
                    new Criterion[]{Restrictions.eq("request.id", request.getId())
                    });
            if(!ValidationHelper.isNullOrEmpty(transcriptionData)){
                RequestPriceListModel requestPriceListModel = new RequestPriceListModel();
                requestPriceListModel.setRequestId(requestId);
                requestPriceListModel.setRequest(request);
                if(!ValidationHelper.isNullOrEmpty(transcriptionData.getTranscriptionAmount()))
                    requestPriceListModel.setTotalCost(transcriptionData.getTranscriptionAmount());
                requestPriceListModel.setClient(request.getClient());
                requestPriceListModel.setService(request.getService());
                if(!ValidationHelper.isNullOrEmpty(request.getClient()) &&
                        !ValidationHelper.isNullOrEmpty(request.getClient().getF24TaxRat()))
                    requestPriceListModel.setTaxRate(request.getClient().getF24TaxRat());
                requestPriceListModels.add(requestPriceListModel);
            }
        }
        return requestPriceListModels;
    }

    private static BigInteger getNumActs(Long requestId) throws PersistenceBeanException, IllegalAccessException {
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

    public static List<InvoiceItem> getInvoiceItems(List<RequestPriceListModel> requestPriceListModels, String causal,
                                                    List<Request> selectedRequestList) {

        Map<TaxRate, List<RequestPriceListModel>> taxRateMap = requestPriceListModels
                .stream()
                .filter(rp -> !ValidationHelper.isNullOrEmpty(rp.getTaxRate())
                        && !ValidationHelper.isNullOrEmpty(rp.getRequest())
                        && !ValidationHelper.isNullOrEmpty(rp.getTotalCost()))
                .collect(
                        Collectors.groupingBy(RequestPriceListModel::getTaxRate, Collectors.toList()));
        List<InvoiceItem> invoiceItems = new ArrayList<>();

        String requestName = selectedRequestList
                .stream()
                .map(r-> r.getRequestTypeName())
                .distinct()
                .collect(Collectors.joining(" + "));

        for(Map.Entry<TaxRate, List<RequestPriceListModel>> taxRateEntry : taxRateMap.entrySet()) {
            TaxRate taxRate = taxRateEntry.getKey();
            List<RequestPriceListModel> list = taxRateEntry.getValue();
            double sumTotalCost = 0d;
            for(RequestPriceListModel requestPriceListModel : list) {
                sumTotalCost = sumTotalCost + requestPriceListModel.getTotalCost();
            }
            InvoiceItem invoiceItem = new InvoiceItem();
            invoiceItem.setTaxRate(taxRate);
            BigDecimal bd = BigDecimal.valueOf(sumTotalCost);
            bd = bd.setScale(2,RoundingMode.HALF_UP);
            invoiceItem.setInvoiceTotalCost(bd.doubleValue());
            if(!ValidationHelper.isNullOrEmpty(invoiceItem.getTaxRate()) &&
                    (invoiceItem.getTaxRate().getPercentage() == null
                            || invoiceItem.getTaxRate().getPercentage().doubleValue() == 0.0) &&
                    StringUtils.isNotBlank(invoiceItem.getTaxRate().getDescription())){
                invoiceItem.setDescription("SPESE " + invoiceItem.getTaxRate().getDescription().toUpperCase());
            }else
                invoiceItem.setDescription(causal + " + " +requestName);
            invoiceItems.add(invoiceItem);
        }
        return invoiceItems;
    }

    public GoodsServicesFieldWrapper createGoodsServicesFieldWrapper() throws IllegalAccessException, PersistenceBeanException {
        GoodsServicesFieldWrapper wrapper = new GoodsServicesFieldWrapper();
        List<SelectItem> ums = new ArrayList<>();
        ums.add(new SelectItem("pz", "PZ"));
        wrapper.setUms(ums);
        wrapper.setInvoiceItemAmount(1.0d);
        wrapper.setVatAmounts(ComboboxHelper.fillList(TaxRate.class, Order.asc("description"), new CriteriaAlias[]{}, new Criterion[]{
                Restrictions.eq("use", Boolean.TRUE)
        }, true, false));
        wrapper.setTotalLine(0D);
        return wrapper;
    }

    public Double getTotalVat(List<GoodsServicesFieldWrapper> goodsServicesFields) throws PersistenceBeanException, InstantiationException, IllegalAccessException {
        Double total = 0D;
        if(!ValidationHelper.isNullOrEmpty(goodsServicesFields)) {
            for(GoodsServicesFieldWrapper wrapper: goodsServicesFields) {
                if(!ValidationHelper.isNullOrEmpty(wrapper.getTotalLine())) {
                    if(!ValidationHelper.isNullOrEmpty(wrapper.getSelectedTaxRateId())) {
                        TaxRate taxrate = DaoManager.get(TaxRate.class, wrapper.getSelectedTaxRateId());
                        if(!ValidationHelper.isNullOrEmpty(taxrate.getPercentage())){
                            total += wrapper.getTotalLine().doubleValue() * (taxrate.getPercentage().doubleValue()/100);
                        }
                    }
                }
            }
            BigDecimal tot = BigDecimal.valueOf(total);
            tot = tot.setScale(2, RoundingMode.HALF_UP);
            total = tot.doubleValue();
        }
        return total;
    }

    public Double getAllTotalLine(List<GoodsServicesFieldWrapper> goodsServicesFields) {
        Double total = 0D;
        if(!ValidationHelper.isNullOrEmpty(goodsServicesFields)) {
            total = goodsServicesFields.stream().collect(
                    Collectors.summingDouble(GoodsServicesFieldWrapper::getTotalLine));
            BigDecimal tot = BigDecimal.valueOf(total);
            tot = tot.setScale(2, RoundingMode.HALF_UP);
            total = tot.doubleValue();
        }
        return total;
    }

    public Double getNonZeroTotalLine(List<GoodsServicesFieldWrapper> goodsServicesFields) {
        Double total = 0D;
        if(!ValidationHelper.isNullOrEmpty(goodsServicesFields)) {
            total = goodsServicesFields.stream()
                    .filter(gs -> (gs.getPercentage() != null && gs.getPercentage().compareTo(BigDecimal.ZERO) != 0))
                    .collect(Collectors.summingDouble(GoodsServicesFieldWrapper::getTotalLine));
            BigDecimal tot = BigDecimal.valueOf(total);
            tot = tot.setScale(2, RoundingMode.HALF_UP);
            total = tot.doubleValue();
        }
        return total;
    }

    public Double getZeroTotalLine(List<GoodsServicesFieldWrapper> goodsServicesFields) {
        Double total = 0D;
        if(!ValidationHelper.isNullOrEmpty(goodsServicesFields)) {
            total = goodsServicesFields.stream()
                    .filter(gs -> (gs.getPercentage() == null || gs.getPercentage().compareTo(BigDecimal.ZERO) == 0))
                    .collect(
                            Collectors.summingDouble(GoodsServicesFieldWrapper::getTotalLine));
            BigDecimal tot = BigDecimal.valueOf(total);
            tot = tot.setScale(2, RoundingMode.HALF_UP);
            total = tot.doubleValue();
        }
        return total;
    }

    public Double getNonZeroTotalVat(List<GoodsServicesFieldWrapper> goodsServicesFields) throws PersistenceBeanException, InstantiationException, IllegalAccessException {
        Double total = 0D;
        if(!ValidationHelper.isNullOrEmpty(goodsServicesFields)) {
            for(GoodsServicesFieldWrapper wrapper: goodsServicesFields) {
                if(!ValidationHelper.isNullOrEmpty(wrapper.getTotalLine())) {
                    if(!ValidationHelper.isNullOrEmpty(wrapper.getSelectedTaxRateId())) {
                        TaxRate taxrate = DaoManager.get(TaxRate.class, wrapper.getSelectedTaxRateId());
                        if(!ValidationHelper.isNullOrEmpty(taxrate.getPercentage()) && (taxrate.getPercentage().compareTo(BigDecimal.ZERO) != 0)){
                            total += wrapper.getTotalLine().doubleValue() * (taxrate.getPercentage().doubleValue()/100);
                        }
                    }
                }
            }
            BigDecimal tot = BigDecimal.valueOf(total);
            tot = tot.setScale(2, RoundingMode.HALF_UP);
            total = tot.doubleValue();
        }
        return total;
    }

    public Double getTotalGrossAmount(List<GoodsServicesFieldWrapper> goodsServicesFields) throws PersistenceBeanException, InstantiationException, IllegalAccessException {
        Double totalGrossAmount = 0D;
        if(!ValidationHelper.isNullOrEmpty(goodsServicesFields)) {
            for(GoodsServicesFieldWrapper wrapper: goodsServicesFields) {
                if(!ValidationHelper.isNullOrEmpty(wrapper.getTotalLine())){
                    totalGrossAmount += wrapper.getTotalLine();
                    if(!ValidationHelper.isNullOrEmpty(wrapper.getSelectedTaxRateId())){
                        TaxRate taxrate = DaoManager.get(TaxRate.class, wrapper.getSelectedTaxRateId());
                        if(!ValidationHelper.isNullOrEmpty(taxrate.getPercentage())){
                            totalGrossAmount += (wrapper.getTotalLine() * (taxrate.getPercentage().doubleValue()/100));
                        }
                    }
                }
            }
            BigDecimal tot = BigDecimal.valueOf(totalGrossAmount);
            tot = tot.setScale(2, RoundingMode.HALF_UP);
            totalGrossAmount = tot.doubleValue();
        }
        return totalGrossAmount;
    }

    public Double getTotalPayment(Invoice invoice) throws PersistenceBeanException, IllegalAccessException, InstantiationException {
        List<PaymentInvoice> paymentInvoices = DaoManager.load(PaymentInvoice.class,
                Restrictions.eq("invoice.id", invoice.getId()));
        double paymentImportTotal = 0.0;
        for(PaymentInvoice paymentInvoice : paymentInvoices) {
            Double total = paymentInvoice.getPaymentImport().doubleValue();
            paymentImportTotal = paymentImportTotal + total;
        }
        return paymentImportTotal;
    }

    public static String format(Double amount) {
        if(amount != null){
            StringBuilder sb = new StringBuilder();
            Formatter formatter = new Formatter(sb, Locale.GERMAN);
            formatter.format("%(,.2f", amount);
            return sb.toString();
            //return String.format("%.2f",value).replaceAll(",", ".");
        }
        return "";
    }

    public List<GoodsServicesFieldWrapper> goodsServicesFields(Invoice invoice) throws PersistenceBeanException, IllegalAccessException {
        List<GoodsServicesFieldWrapper> wrapperList = new ArrayList<>();
        int counter = 1;
        List<InvoiceItem> invoiceItems = DaoManager.load(InvoiceItem.class,
                new Criterion[]{Restrictions.eq("invoice", invoice)});
        for (InvoiceItem invoiceItem : invoiceItems) {
            GoodsServicesFieldWrapper wrapper = createGoodsServicesFieldWrapper();
            wrapper.setCounter(counter);
            wrapper.setInvoiceTotalCost(invoiceItem.getInvoiceTotalCost());
            wrapper.setSelectedTaxRateId(invoiceItem.getTaxRate().getId());
            wrapper.setPercentage(invoiceItem.getTaxRate().getPercentage());
            wrapper.setInvoiceItemAmount(ValidationHelper.isNullOrEmpty(invoiceItem.getAmount()) ? 0.0 : invoiceItem.getAmount());
            double totalcost = !(ValidationHelper.isNullOrEmpty(invoiceItem.getInvoiceTotalCost())) ? invoiceItem.getInvoiceTotalCost().doubleValue() : 0.0;
            double amount = !(ValidationHelper.isNullOrEmpty(invoiceItem.getAmount())) ? invoiceItem.getAmount().doubleValue() : 0.0;
            double totalLine;
            if(amount != 0.0) {
                totalLine = totalcost * amount;
            } else {
                totalLine = totalcost;
            }
            wrapper.setTotalLine(totalLine);
            if(!ValidationHelper.isNullOrEmpty(invoiceItem.getDescription()))
                wrapper.setDescription(invoiceItem.getDescription());
            wrapperList.add(wrapper);
            counter = counter + 1;
        }
        return wrapperList;
    }

    public String createInvoiceEmailDefaultBody(Invoice invoice) throws HibernateException, InstantiationException, IllegalAccessException, PersistenceBeanException {
        if (!ValidationHelper.isNullOrEmpty(invoice)) {
            if (ValidationHelper.isNullOrEmpty(invoice.getEmail()) && !ValidationHelper.isNullOrEmpty(invoice.getEmailFrom())) {
                String dearCustomer = ResourcesHelper.getString("dearCustomer");
                String attachedCopyMessage = ResourcesHelper.getString("attachedCopyMessage");
                String thanksMessage = ResourcesHelper.getString("thanksMessage");
                String reference = "";
                String ndg = "";
                if (!ValidationHelper.isNullOrEmpty(invoice.getEmailFrom().getNdg()))
                    ndg = invoice.getEmailFrom().getNdg();
                if (!ValidationHelper.isNullOrEmpty(invoice.getEmailFrom().getReferenceRequest()))
                    reference = invoice.getEmailFrom().getReferenceRequest();
                String requestType = "";
                List<Request> requests = DaoManager.load(Request.class, new Criterion[]{
                        Restrictions.eq("invoice", invoice),
                        Restrictions.or(Restrictions.eq("isDeleted", Boolean.FALSE),
                                Restrictions.isNull("isDeleted"))
                });
                Set<String> requestTypeSet = new HashSet<>();
                requests.stream().forEach(request -> {
                    requestTypeSet.add(request.getRequestType().getName());
                });
                for (String request : requestTypeSet) {
                    if (!requestType.isEmpty())
                        requestType = requestType + " + ";
                    requestType = requestType + request;
                }
                String fiduciario = "";
                if (!ValidationHelper.isNullOrEmpty(invoice.getEmailFrom().getClientFiduciary()))
                    fiduciario = invoice.getEmailFrom().getClientFiduciary().toString();
                else if(!ValidationHelper.isNullOrEmpty(invoice.getEmailFrom().getFiduciary()))
                    fiduciario = invoice.getEmailFrom().getFiduciary();

                String emailsFrom = MailHelper.getEmailFrom();
                String mail_footer = MAIL_FOOTER.replace("info@brexa.it", emailsFrom);

                String emailBody = dearCustomer + ",</br>"
                        + attachedCopyMessage + "</br></br>"
                        + "<table style='border:none;'>"
                        + (!ndg.isEmpty() ? "<tr><td style='border:none;'>NDG: " + "</td><td style='padding-left: 50px; border:none;'>" + ndg + "</td></tr>" : "")
                        + (!reference.isEmpty() ? "<tr><td style='border:none;'>RIF: " + "</td><td style='padding-left: 50px; border:none;'>" + reference + "</td></tr>" : "")
                        + (!requestType.isEmpty() ? "<tr><td style='border:none;'>REQUEST: " + "</td><td style='padding-left: 50px; border:none;'>" + requestType + "</td></tr>" : "")
                        + (!fiduciario.isEmpty() ? "<tr><td style='border:none;'>FIDUCIARIO: " + "</td><td style='padding-left: 50px; border:none;'>" + fiduciario + "</td></tr>" : "")
                        + "</table></br>"
                        + thanksMessage
                        + mail_footer;
                return emailBody;
            } else if (ValidationHelper.isNullOrEmpty(invoice.getEmail())) {
                String dearCustomer = ResourcesHelper.getString("dearCustomer");
                String attachedMessage = ResourcesHelper.getString("attachedMessage");
                String thanksMessage = ResourcesHelper.getString("thanksMessage");

                String emailBody = dearCustomer + ",</br></br>"
                        + attachedMessage + "</br></br>"
                        + thanksMessage;

                return emailBody;
            }
        }
        return "";
    }
}
