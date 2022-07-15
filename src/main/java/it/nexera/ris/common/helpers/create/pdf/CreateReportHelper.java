package it.nexera.ris.common.helpers.create.pdf;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import it.nexera.ris.persistence.HibernateUtil;
import org.hibernate.Hibernate;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;
import org.hibernate.sql.JoinType;

import it.nexera.ris.common.enums.CostType;
import it.nexera.ris.common.enums.DocumentType;
import it.nexera.ris.common.enums.ExtraCostType;
import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.helpers.BaseHelper;
import it.nexera.ris.common.helpers.ValidationHelper;
import it.nexera.ris.persistence.beans.dao.CriteriaAlias;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.IndexedEntity;
import it.nexera.ris.persistence.beans.entities.domain.Document;
import it.nexera.ris.persistence.beans.entities.domain.DocumentSubject;
import it.nexera.ris.persistence.beans.entities.domain.ExtraCost;
import it.nexera.ris.persistence.beans.entities.domain.PriceList;
import it.nexera.ris.persistence.beans.entities.domain.Request;
import it.nexera.ris.persistence.beans.entities.domain.RequestFormality;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.RequestType;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.Service;

public abstract class CreateReportHelper extends BaseHelper {

    final String zeroValue = "0,00";
    final String euroSymbol = "\u20ac ";


    protected void sortRequestsByType(List<Request> requests, Map<RequestType, List<Request>> sortedRequests) {
        for (Request elem : requests) {
            if (!sortedRequests.containsKey(elem.getRequestType())) {
                sortedRequests.put(elem.getRequestType(), new ArrayList<>());
                sortedRequests.get(elem.getRequestType()).add(elem);
            } else if (sortedRequests.containsKey(elem.getRequestType())) {
                sortedRequests.get(elem.getRequestType()).add(elem);
            }
        }
    }

    protected Double getCostEstateFormalityAndExtraCostRelated(Request request) throws PersistenceBeanException, IllegalAccessException {
        Double result = 0d;

        if (!ValidationHelper.isNullOrEmpty(request.getCostEstateFormality())) {
            result += request.getCostEstateFormality();
        }

        List<ExtraCost> extraCosts = DaoManager.load(ExtraCost.class, new Criterion[]{
                Restrictions.eq("requestId", request.getId()),
                Restrictions.eq("type", ExtraCostType.IPOTECARIO)});

        if (!ValidationHelper.isNullOrEmpty(extraCosts)) {
            for (ExtraCost cost : extraCosts) {
                result += cost.getPrice();
            }
        }

        return result.equals(0d) ? result : Math.round(result * 100000d) / 100000d;
    }
    
    public Double getCostEstateFormalityAndExtraCostRelated(Request request
            , Service service, Boolean billingClient, boolean restictionForPriceList) throws PersistenceBeanException, IllegalAccessException, InstantiationException {
        Double result = 0d;
        result += getCostEstate(request, service, billingClient, restictionForPriceList);
        result += getCostEstateFormality(request, service, billingClient, restictionForPriceList);
        result += getCostPlus(request, service, billingClient, restictionForPriceList);
        List<ExtraCost> extraCosts = DaoManager.load(ExtraCost.class, new Criterion[]{
                Restrictions.eq("requestId", request.getId()),
                Restrictions.eq("type", ExtraCostType.IPOTECARIO)});

        if (!ValidationHelper.isNullOrEmpty(extraCosts)) {
            for (ExtraCost cost : extraCosts) {
                result += cost.getPrice();
            }
        }
        return result.equals(0d) ? result : Math.round(result * 100000d) / 100000d;
    }


    protected Double getCostCadastralAndExtraCostRelated(Request request) throws PersistenceBeanException, IllegalAccessException {
        Double result = 0d;

        if (!ValidationHelper.isNullOrEmpty(request.getCostCadastral())) {
            result += request.getCostCadastral();
        }

        List<ExtraCost> extraCosts = DaoManager.load(ExtraCost.class, new Criterion[]{
                Restrictions.eq("requestId", request.getId()),
                Restrictions.eq("type", ExtraCostType.CATASTO)});

        if (!ValidationHelper.isNullOrEmpty(extraCosts)) {
            for (ExtraCost cost : extraCosts) {
                result += cost.getPrice();
            }
        }
        return result.equals(0d) ? result : Math.round(result * 100000d) / 100000d;
    }


    public Double getSumOfCostEstateFormality(List<Request> requests) throws PersistenceBeanException, IllegalAccessException {
        Double result = 0d;
        for (Request request : requests) {
            if (!ValidationHelper.isNullOrEmpty(request.getCostEstateFormality())) {
                result += request.getCostEstateFormality();
            }
            List<ExtraCost> extraCosts = DaoManager.load(ExtraCost.class, new Criterion[]{
                    Restrictions.eq("requestId", request.getId()),
                    Restrictions.eq("type", ExtraCostType.IPOTECARIO)});

            if (!ValidationHelper.isNullOrEmpty(extraCosts)) {
                for (ExtraCost cost : extraCosts) {
                    result += cost.getPrice();
                }
            }
        }
        return result;
    }

    public Double getSumOfCostEstateFormalityService(List<Request> requests) throws PersistenceBeanException, IllegalAccessException, InstantiationException {
        Double result = 0d;
        for (Request request : requests) {
            
            if(!ValidationHelper.isNullOrEmpty(request.getService())) {
                if (!ValidationHelper.isNullOrEmpty(request.getCostEstateFormality())) {
                    result += request.getCostEstateFormality();
                }
                List<ExtraCost> extraCosts = DaoManager.load(ExtraCost.class, new Criterion[]{
                        Restrictions.eq("requestId", request.getId()),
                        Restrictions.eq("type", ExtraCostType.IPOTECARIO)});

                if (!ValidationHelper.isNullOrEmpty(extraCosts)) {
                    for (ExtraCost cost : extraCosts) {
                        result += cost.getPrice();
                    }
                }
            }else if(!ValidationHelper.isNullOrEmpty(request.getMultipleServices())) {
                Boolean billingClient = isBillingClient(request);
                boolean restrictionForPriceList = restrictionForPriceList(request);
                for(Service service : request.getMultipleServices()) {
                    result += getCostEstateFormalityAndExtraCostRelated(
                            request,service,billingClient, restrictionForPriceList);
                }
            }
        }
        return result;
    }
    protected Double getSumOfCostCadastral(List<Request> requests) throws PersistenceBeanException, IllegalAccessException {
        Double result = 0d;
        for (Request request : requests) {
            if (!ValidationHelper.isNullOrEmpty(request.getCostCadastral())) {
                result += request.getCostCadastral();
            }
            List<ExtraCost> extraCosts = DaoManager.load(ExtraCost.class, new Criterion[]{
                    Restrictions.eq("requestId", request.getId()),
                    Restrictions.eq("type", ExtraCostType.CATASTO)});

            if (!ValidationHelper.isNullOrEmpty(extraCosts)) {
                for (ExtraCost cost : extraCosts) {
                    result += cost.getPrice();
                }
            }
        }
        return result;
    }

    protected Double getSumOfCostPay(List<Request> requests) {
        Double result = 0d;
        for (Request request : requests) {
            if (!ValidationHelper.isNullOrEmpty(request.getCostPay())) {
                result += request.getCostPay();
            }
        }
        return result;
    }
    
    public Double getSumOfCostPayServices(List<Request> requests) throws IllegalAccessException, PersistenceBeanException, InstantiationException {
        Double result = 0d;
        for (Request request : requests) {
            if(!ValidationHelper.isNullOrEmpty(request.getService())) {
                if (!ValidationHelper.isNullOrEmpty(request.getCostPay())) {
                    result += request.getCostPay();
                }
            }else if(!ValidationHelper.isNullOrEmpty(request.getMultipleServices())) {
                Boolean billingClient = isBillingClient(request);
                boolean restrictionForPriceList = restrictionForPriceList(request);
                for(Service service : request.getMultipleServices()) {
                    result += getCostPay(request,service,billingClient, restrictionForPriceList);
                }
            
            }
        }
        return result;
    }

    protected Double getSumOfCostTotal(List<Request> requests) {
        double result = 0d;
        for (Request request : requests) {
            if (!ValidationHelper.isNullOrEmpty(request.getTotalCost())) {
                result += Double.parseDouble(request.getTotalCostDouble());
            }
        }
        return result;
    }
    
    public Double getSumOfCostTotalServices(List<Request> requests) throws IllegalAccessException, PersistenceBeanException, InstantiationException {
        double result = 0d;
        for (Request request : requests) {
            if(!ValidationHelper.isNullOrEmpty(request.getService())) {
                if (!ValidationHelper.isNullOrEmpty(request.getTotalCost())) {
                    result += Double.parseDouble(request.getTotalCostDouble());
                }
            }else if(!ValidationHelper.isNullOrEmpty(request.getMultipleServices())) {
                Boolean billingClient = isBillingClient(request);
                boolean restrictionForPriceList = restrictionForPriceList(request);
                if (!ValidationHelper.isNullOrEmpty(request.getCostCadastral())) {
                    result += request.getCostCadastral();
                }
                for(Service service : request.getMultipleServices()) {
                    result += getCostExtra(request, service, billingClient, restrictionForPriceList);
                    result += getCostEstateFormality(request, service, billingClient, restrictionForPriceList);
                    result += getCostPay(request, service, billingClient, restrictionForPriceList);
                }
                List<ExtraCost> extraCost = DaoManager.load(ExtraCost.class, new Criterion[]{
                        Restrictions.eq("requestId", request.getId())});
                for (ExtraCost cost : extraCost) {
                    result += cost.getPrice();
                }
            }
            
            List<ExtraCost> extraCosts = DaoManager.load(ExtraCost.class, new Criterion[]{
                    Restrictions.eq("requestId", request.getId()),
                    Restrictions.eq("type", ExtraCostType.NAZIONALEPOSITIVA)});
            if (!ValidationHelper.isNullOrEmpty(extraCosts)) {
                for (ExtraCost cost : extraCosts) {
                    result += cost.getPrice();
                }
            }

        }
        return result;
    }
    
    
    public Boolean isBillingClient(Request request) {
        if (!ValidationHelper.isNullOrEmpty(request.getBillingClient())) {
            return true;
        } else if (!ValidationHelper.isNullOrEmpty(request.getClient())
                && !ValidationHelper.isNullOrEmpty(request.getClient().getCostOutput())
                && request.getClient().getCostOutput()) {
            return false;
        }
        return null;
    }
    
    public boolean restrictionForPriceList(Request request) {
        boolean result = false;

        if (!ValidationHelper.isNullOrEmpty(request.getDocumentsRequest())) {
            boolean isRequestHasDocumentWithSecondType = request.getDocumentsRequest().stream()
                    .anyMatch(x -> DocumentType.OTHER.getId().equals(x.getTypeId()));
            if (!ValidationHelper.isNullOrEmpty(request.getService())
                    && !ValidationHelper.isNullOrEmpty(request.getService().getUnauthorizedQuote())) {
                result = isRequestHasDocumentWithSecondType && request.getService().getUnauthorizedQuote();
            }
        }
        return result;
    }
    
    public Double getCostEstate(Request request, Service service, Boolean billingClient, boolean restictionForPriceList)
            throws PersistenceBeanException, IllegalAccessException {
        double result = 0d;
        if(!ValidationHelper.isNullOrEmpty(request.getUnauthorizedQuote()) && request.getUnauthorizedQuote()){
            return result;
        }
        List<PriceList> priceList = DaoManager.load(PriceList.class, new CriteriaAlias[]{
                new CriteriaAlias("costConfiguration", "cc", JoinType.INNER_JOIN)}, new Criterion[]{
                        Restrictions.eq("client", (billingClient == null || !billingClient)
                                ? request.getClient() : request.getBillingClient()),

                        restictionForPriceList ?
                                Restrictions.in("cc.id", service.getServiceCostUnauthorizedQuoteList()
                                        .stream().map(IndexedEntity::getId).collect(Collectors.toList())) :
                                            Restrictions.isNotNull("cc.id"),

                                            Restrictions.eq("service", service),
                                            Restrictions.eq("cc.typeId", CostType.DEPENDING_ON_NUMBER_OF_FORMALITIES.getId())});

        if (!ValidationHelper.isNullOrEmpty(priceList) && !ValidationHelper.isNullOrEmpty(priceList.get(0).getPrice())) {

            if (!ValidationHelper.isNullOrEmpty(request.getNumberActUpdate())) {
                result = Double.parseDouble(priceList.get(0).getPrice().replaceAll(",", "."))
                        * request.getNumberActUpdate();
            } else {
                result = Double.parseDouble(priceList.get(0).getPrice().replaceAll(",", "."))
                        * request.getSumOfEstateFormalitiesAndCommunicationsAndSuccess();
            }
        }

        return result;
    }
  
    public Double getCostEstateFormality(Request request, Service service,Boolean billingClient, boolean restrictionForPriceList)
            throws PersistenceBeanException, IllegalAccessException, InstantiationException {
        double result = 0d;
        if (!ValidationHelper.isNullOrEmpty(service)) {
            List<PriceList> priceList = DaoManager.load(PriceList.class, 
                    new Criterion[]{
                            Restrictions.eq("client", (billingClient == null || !billingClient)
                                    ? request.getClient() : request.getBillingClient()),
                            Restrictions.eq("isNegative", true),
                            Restrictions.eq("service", service)});
            if (!ValidationHelper.isNullOrEmpty(priceList)) {
                PriceList first = priceList.get(0);
                String firstPrice = first.getFirstPrice().replaceAll(",", ".");
                if(!ValidationHelper.isNullOrEmpty(firstPrice)){
                    Double conservationCosts = Double.parseDouble(firstPrice);
                    if (!ValidationHelper.isNullOrEmpty(request.getAggregationLandChargesRegistry())
                            && !ValidationHelper.isNullOrEmpty(request.getAggregationLandChargesRegistry().getLandChargesRegistries())) {
                        result = conservationCosts * request.getAggregationLandChargesRegistry()
                                .getNumberOfVisualizedLandChargesRegistries();
                    }
                }
            }
        }
        return result;
    }
    
    public Double getCostPlus(Request request, Service service,Boolean billingClient, boolean restrictionForPriceList)
            throws PersistenceBeanException, IllegalAccessException, InstantiationException {
        double result = 0D;
        double firstPrice = 0D;
        int numRegistry = 0;
        int numRequestRegistry = 0;
        int numPlus = 0;

        if (!ValidationHelper.isNullOrEmpty(request.getAggregationLandChargesRegistry())) {
            if(!Hibernate.isInitialized(request.getAggregationLandChargesRegistry().getLandChargesRegistries())){
                Hibernate.initialize(request.getAggregationLandChargesRegistry().getLandChargesRegistries());
            }
            if(ValidationHelper.isNullOrEmpty(request.getAggregationLandChargesRegistry().getLandChargesRegistries())) {
                numRegistry = request.getAggregationLandChargesRegistry().getNumberOfVisualizedLandChargesRegistries();
            }
        }
//
//        if (!ValidationHelper.isNullOrEmpty(request.getAggregationLandChargesRegistry())
//                && !ValidationHelper.isNullOrEmpty(request.getAggregationLandChargesRegistry().getLandChargesRegistries())) {
//            numRegistry = request.getAggregationLandChargesRegistry().getNumberOfVisualizedLandChargesRegistries();
//        }

        if (!ValidationHelper.isNullOrEmpty(request.getRequestFormalities())) {
            List<Long> documentIds = request.getRequestFormalities().stream().map(RequestFormality::getDocumentId)
                    .distinct().collect(Collectors.toList());
            numRequestRegistry = DaoManager.getCount(DocumentSubject.class, "id", new Criterion[]{
                    Restrictions.in("document.id", documentIds)}).intValue();
        }

        numPlus = numRegistry - numRequestRegistry;

        if(!ValidationHelper.isNullOrEmpty(service)) {
            List<PriceList> priceList = loadPriceList(request, service,billingClient, restrictionForPriceList);
            if (!ValidationHelper.isNullOrEmpty(priceList)) {
                PriceList first = priceList.get(0);
                firstPrice = Double.parseDouble(first.getFirstPrice().replaceAll(",", "."));
            }

            result = firstPrice * numPlus;
        }
        return result;
    }

    private List<PriceList> loadPriceList(Request request, Service service,Boolean billingClient, boolean restrictionForPriceList)
            throws PersistenceBeanException, IllegalAccessException {
        
        
        return DaoManager.load(PriceList.class, new CriteriaAlias[]{
                new CriteriaAlias("costConfiguration", "cc", JoinType.INNER_JOIN)}, new Criterion[]{
                Restrictions.eq("client", (billingClient == null || !billingClient)
                        ? request.getClient() : request.getBillingClient()),

                restrictionForPriceList ?
                        Restrictions.in("cc.id", service.getServiceCostUnauthorizedQuoteList()
                                .stream().map(IndexedEntity::getId).collect(Collectors.toList())) :
                        Restrictions.isNotNull("cc.id"),

                Restrictions.eq("service", service),
                Restrictions.eq("cc.typeId", CostType.FIXED_COST.getId())});
    }
    
    public Double getCostPay(Request request, Service service,Boolean billingClient, boolean restrictionForPriceList)
            throws PersistenceBeanException, IllegalAccessException, InstantiationException {
        Double result = 0d;
            if(!ValidationHelper.isNullOrEmpty(service)) {
                List<PriceList> priceList = DaoManager.load(PriceList.class, 
                        new Criterion[]{
                                Restrictions.eq("client", (billingClient == null || !billingClient)
                                        ? request.getClient() : request.getBillingClient()),
                                Restrictions.eq("isNegative", true),
                                Restrictions.eq("service", service)});
                
                if (!ValidationHelper.isNullOrEmpty(priceList)) {
                    PriceList first = priceList.get(0);
                    if (!ValidationHelper.isNullOrEmpty(first.getPrice())) {
                        result = Double.parseDouble(first.getPrice());
                    }
                }
            }
            return result;
    }
    
    public Double getCostCadastral(Request request) throws PersistenceBeanException, IllegalAccessException {
        double result = 0d;

        List<Document> documents = DaoManager.load(Document.class, new Criterion[]{
                Restrictions.eq("request.id", request.getId())});
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
    
    public Double getCostExtra(Request request, Service service,Boolean billingClient, boolean restrictionForPriceList)
            throws IllegalAccessException, PersistenceBeanException {
        double result = 0d;
        List<PriceList> priceList = new ArrayList<PriceList>();
        if(!ValidationHelper.isNullOrEmpty(service)) {
            priceList = DaoManager.load(PriceList.class, new CriteriaAlias[]{
                    new CriteriaAlias("costConfiguration", "cc", JoinType.INNER_JOIN)}, new Criterion[]{
                    Restrictions.eq("client", (billingClient == null || !billingClient)
                            ? request.getClient() : request.getBillingClient()),
                    Restrictions.eq("service", service),

                    restrictionForPriceList ?
                            Restrictions.in("cc.id", service.getServiceCostUnauthorizedQuoteList()
                                    .stream().map(IndexedEntity::getId).collect(Collectors.toList())) :
                            Restrictions.isNotNull("cc.id"),

                    Restrictions.eq("cc.typeId", CostType.EXTRA_COST.getId())});
        }
        if (!ValidationHelper.isNullOrEmpty(priceList)) {
            for (PriceList price : priceList) {
                if (!ValidationHelper.isNullOrEmpty(price.getPrice())) {
                    result += Double.parseDouble(price.getPrice());
                }
            }
        }
        return result;
    }
}