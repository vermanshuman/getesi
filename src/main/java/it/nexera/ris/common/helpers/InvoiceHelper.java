package it.nexera.ris.common.helpers;

import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.domain.InvoiceItem;
import it.nexera.ris.persistence.beans.entities.domain.PriceList;
import it.nexera.ris.persistence.beans.entities.domain.Request;
import it.nexera.ris.persistence.beans.entities.domain.TaxRate;
import it.nexera.ris.web.common.RequestPriceListModel;
import org.hibernate.HibernateException;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class InvoiceHelper {
    public static List<InvoiceItem> groupingItemsByTaxRate(List<Request> selectedRequestList) throws HibernateException, IllegalAccessException, PersistenceBeanException, InstantiationException {
        Map<Long, List<PriceList>> priceListMap = new HashMap<Long, List<PriceList>>();
        for(Request request: selectedRequestList) {
            List<PriceList> priceList = new ArrayList<PriceList>();
            if(!ValidationHelper.isNullOrEmpty(request.getService())) {
                priceList = DaoManager.load(PriceList.class,
                        new Criterion[]{
                                Restrictions.eq("client", request.getClient() != null ? request.getClient() : request.getBillingClient()),
                                Restrictions.eq("isNegative", true),
                                Restrictions.eq("service", request.getService())});

            }else if(!ValidationHelper.isNullOrEmpty(request.getMultipleServices())) {
                priceList = DaoManager.load(PriceList.class,
                        new Criterion[]{
                                Restrictions.eq("client", request.getClient() != null ? request.getClient() : request.getBillingClient()),
                                Restrictions.eq("isNegative", true),
                                Restrictions.in("service", request.getMultipleServices())});
            }
            priceListMap.put(request.getId(), priceList);
        }

        List<RequestPriceListModel> requestPriceListModels = new ArrayList<>();
        for(Map.Entry<Long, List<PriceList>> entry : priceListMap.entrySet()) {
            Long requestId = entry.getKey();
            Request request = DaoManager.get(Request.class, requestId);
            String totalCostStr = request.getTotalCost() != null ? request.getTotalCost() : "0.0";
            if(totalCostStr.contains(",")) {
                totalCostStr = totalCostStr.replace(",", ".");
            }
            Double totalCost = Double.parseDouble(totalCostStr);
            List<PriceList> prices = entry.getValue();

            for(PriceList priceList : prices) {
                RequestPriceListModel requestPriceListModel = new RequestPriceListModel();
                requestPriceListModel.setRequestId(requestId);
                requestPriceListModel.setRequest(request);
                requestPriceListModel.setTotalCost(totalCost);
                requestPriceListModel.setClient(priceList.getClient());
                requestPriceListModel.setService(priceList.getService());
                requestPriceListModel.setTaxRate(priceList.getTaxRate());
                requestPriceListModels.add(requestPriceListModel);
            }
        }
        Map<Request, Map<TaxRate, Double>> map = requestPriceListModels
                .stream()
                .filter(rp -> !ValidationHelper.isNullOrEmpty(rp.getTaxRate())
                                && !ValidationHelper.isNullOrEmpty(rp.getRequest())
                                && !ValidationHelper.isNullOrEmpty(rp.getTotalCost()))
                .collect(
                Collectors.groupingBy(RequestPriceListModel::getRequest,
                        Collectors.groupingBy(RequestPriceListModel::getTaxRate,
                                Collectors.summingDouble(RequestPriceListModel::getTotalCost))));
        List<InvoiceItem> invoiceItems = new ArrayList<>();
        for(Map.Entry<Request, Map<TaxRate, Double>> entry : map.entrySet()) {
            Request request = entry.getKey();
            Map<TaxRate, Double> taxRateMap = entry.getValue();
            for(Map.Entry<TaxRate, Double> taxRateEntry : taxRateMap.entrySet()) {
                TaxRate taxRate = taxRateEntry.getKey();
                InvoiceItem invoiceItem = new InvoiceItem();
                invoiceItem.setSubject(request.getSubject().toString());
                invoiceItem.setTaxRate(taxRate);
                invoiceItem.setInvoiceTotalCost(taxRateEntry.getValue().doubleValue());
                invoiceItems.add(invoiceItem);
            }
        }
        return invoiceItems;
    }
}
