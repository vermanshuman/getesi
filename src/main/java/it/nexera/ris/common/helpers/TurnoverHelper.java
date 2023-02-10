package it.nexera.ris.common.helpers;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import it.nexera.ris.persistence.beans.dao.CriteriaAlias;
import it.nexera.ris.persistence.beans.entities.domain.*;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.HibernateException;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;

import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.web.beans.wrappers.BillingListTurnoverWrapper;
import it.nexera.ris.web.beans.wrappers.GoodsServicesFieldWrapper;
import org.hibernate.sql.JoinType;

public class TurnoverHelper {

	public List<BillingListTurnoverWrapper> getTurnoversPerMonth(int year)
			throws HibernateException, IllegalAccessException, PersistenceBeanException, InstantiationException {
		Date startDate = DateTimeHelper.getFirstDateOfMonth("01" + "/" + year);
		Date endDate = DateTimeHelper.getLastDateOfMonth("12" + "/" + year);
		List<Invoice> invoices = DaoManager.load(Invoice.class,
				new Criterion[] { Restrictions.between("date", startDate, endDate) });

		InvoiceHelper invoiceHelper = new InvoiceHelper();
		Map<Long, Double> taxMapping = new HashMap<>();
		Map<Long, Double> nonTaxMapping = new HashMap<>();
		Map<Long, Double> ivaMapping = new HashMap<>();
		for (Invoice invoice : invoices) {
			List<GoodsServicesFieldWrapper> wrapperList = goodsServicesFields(invoice, invoiceHelper);
			Double value = invoiceHelper.getNonZeroTotalLine(wrapperList);
			taxMapping.put(invoice.getId(), value);

			value = invoiceHelper.getZeroTotalLine(wrapperList);
			nonTaxMapping.put(invoice.getId(), value);

			value = invoiceHelper.getNonZeroTotalVat(wrapperList);
			ivaMapping.put(invoice.getId(), value);
		}

		List<BillingListTurnoverWrapper> billingListTurnoverWrapperList = new ArrayList<>();
		Map<Integer, List<Invoice>> groupedInvoicesByMonth = invoices.stream()
				.filter(i -> !ValidationHelper.isNullOrEmpty(i.getDate()))
				.collect(Collectors.groupingBy(i -> DateTimeHelper.getMonth(i.getDate())));
		String[] months = new String[] { "Gennaio", "Febbraio", "Marzo", "Aprile", "Maggio", "Giugno", "Luglio",
				"Agosto", "Settembre", "Ottobre", "Novembre", "Dicembre" };
		for (int m = 0; m < months.length; m++) {
			BillingListTurnoverWrapper billingListTurnoverWrapper = new BillingListTurnoverWrapper();
			if (groupedInvoicesByMonth.containsKey(m + 1)) {
				billingListTurnoverWrapper.setMonth(m + 1);
				List<Invoice> invoicesForMonth = groupedInvoicesByMonth.get(m + 1);
				double totalTax = 0d;
				totalTax = invoicesForMonth.stream().filter(i -> taxMapping.containsKey(i.getId()))
						.mapToDouble(o -> taxMapping.get(o.getId())).sum();
				billingListTurnoverWrapper.setTotalTax(totalTax);

				double nonTotalTax = 0d;
				nonTotalTax = invoicesForMonth.stream().filter(i -> nonTaxMapping.containsKey(i.getId()))
						.mapToDouble(o -> nonTaxMapping.get(o.getId())).sum();
				billingListTurnoverWrapper.setNonTotalTax(nonTotalTax);

				double totalIva = 0d;
				totalIva = invoicesForMonth.stream().filter(i -> ivaMapping.containsKey(i.getId()))
						.mapToDouble(o -> ivaMapping.get(o.getId())).sum();
				billingListTurnoverWrapper.setTotalIva(totalIva);

				double total = totalTax + nonTotalTax + totalIva;
				BigDecimal tot = BigDecimal.valueOf(total);
	            tot = tot.setScale(2, RoundingMode.HALF_UP);
	            total = tot.doubleValue();
				billingListTurnoverWrapper.setTotal(total);

				billingListTurnoverWrapperList.add(billingListTurnoverWrapper);
			} else {
				billingListTurnoverWrapper.setMonth(m + 1);
				billingListTurnoverWrapper.setTotalTax(0d);
				billingListTurnoverWrapper.setNonTotalTax(0d);
				billingListTurnoverWrapper.setTotalIva(0d);
				billingListTurnoverWrapper.setTotal(0d);
				billingListTurnoverWrapperList.add(billingListTurnoverWrapper);
			}
		}
		return billingListTurnoverWrapperList;
	}
	
	public List<BillingListTurnoverWrapper> getTurnoversPerClient(int year)
			throws HibernateException, IllegalAccessException, PersistenceBeanException, InstantiationException {
		Date startDate = DateTimeHelper.getFirstDateOfMonth("01" + "/" + year);
		Date endDate = DateTimeHelper.getLastDateOfMonth("12" + "/" + year);
		List<Invoice> invoices = DaoManager.load(Invoice.class,
				new Criterion[] { Restrictions.between("date", startDate, endDate) });

		InvoiceHelper invoiceHelper = new InvoiceHelper();
		Map<Long, Double> taxMapping = new HashMap<>();
		Map<Long, Double> nonTaxMapping = new HashMap<>();
		Map<Long, Double> ivaMapping = new HashMap<>();
		for (Invoice invoice : invoices) {
			List<GoodsServicesFieldWrapper> wrapperList = goodsServicesFields(invoice, invoiceHelper);
			Double value = invoiceHelper.getNonZeroTotalLine(wrapperList);
			taxMapping.put(invoice.getId(), value);

			value = invoiceHelper.getZeroTotalLine(wrapperList);
			nonTaxMapping.put(invoice.getId(), value);

			value = invoiceHelper.getNonZeroTotalVat(wrapperList);
			ivaMapping.put(invoice.getId(), value);
		}

		List<BillingListTurnoverWrapper> billingListTurnoverWrapperList = new ArrayList<>();
		Map<Client, List<Invoice>> groupedInvoicesByClient = invoices.stream()
				.filter(i -> !ValidationHelper.isNullOrEmpty(i.getClient()))
                .collect(Collectors.groupingBy(Invoice::getClient));
		for (Map.Entry<Client, List<Invoice>> entry : groupedInvoicesByClient.entrySet()) {
			BillingListTurnoverWrapper billingListTurnoverWrapper = new BillingListTurnoverWrapper();
			String clientName = "";
			 if(!ValidationHelper.isNullOrEmpty(entry.getKey())
	                    && !ValidationHelper.isNullOrEmpty(entry.getKey().getClientName())){
				 clientName = entry.getKey().getClientName().toUpperCase();
	         }
			 
			if (!ValidationHelper.isNullOrEmpty(clientName) && groupedInvoicesByClient.containsKey(entry.getKey())) {
				billingListTurnoverWrapper.setClientName(clientName);
				List<Invoice> invoicesForMonth = entry.getValue();
				double totalTax = 0d;
				totalTax = invoicesForMonth.stream().filter(i -> taxMapping.containsKey(i.getId()))
						.mapToDouble(o -> taxMapping.get(o.getId())).sum();
				billingListTurnoverWrapper.setTotalTax(0d);
				if(!ValidationHelper.isNullOrEmpty(totalTax))
					billingListTurnoverWrapper.setTotalTax(totalTax);

				double nonTotalTax = 0d;
				nonTotalTax = invoicesForMonth.stream().filter(i -> nonTaxMapping.containsKey(i.getId()))
						.mapToDouble(o -> nonTaxMapping.get(o.getId())).sum();
				billingListTurnoverWrapper.setNonTotalTax(0d);
				if(!ValidationHelper.isNullOrEmpty(nonTotalTax))
					billingListTurnoverWrapper.setNonTotalTax(nonTotalTax);

				double totalIva = 0d;
				totalIva = invoicesForMonth.stream().filter(i -> ivaMapping.containsKey(i.getId()))
						.mapToDouble(o -> ivaMapping.get(o.getId())).sum();
				billingListTurnoverWrapper.setTotalIva(0d);
				if(!ValidationHelper.isNullOrEmpty(totalIva))
					billingListTurnoverWrapper.setTotalIva(totalIva);

				double total = totalTax + nonTotalTax + totalIva;
				BigDecimal tot = BigDecimal.valueOf(total);
	            tot = tot.setScale(2, RoundingMode.HALF_UP);
	            total = tot.doubleValue();
				billingListTurnoverWrapper.setTotal(total);

				billingListTurnoverWrapperList.add(billingListTurnoverWrapper);
			} 
		}
		billingListTurnoverWrapperList.sort(Comparator.comparing(BillingListTurnoverWrapper::getTotal,
                Comparator.nullsLast(Comparator.reverseOrder())));
		return billingListTurnoverWrapperList;
	}

	private List<GoodsServicesFieldWrapper> goodsServicesFields(Invoice invoice, InvoiceHelper invoiceHelper)
			throws PersistenceBeanException, IllegalAccessException {
		List<GoodsServicesFieldWrapper> wrapperList = new ArrayList<>();
		int counter = 1;
		List<InvoiceItem> invoiceItems = DaoManager.load(InvoiceItem.class,
				new Criterion[] { Restrictions.eq("invoice", invoice) });
		for (InvoiceItem invoiceItem : invoiceItems) {
			GoodsServicesFieldWrapper wrapper = invoiceHelper.createGoodsServicesFieldWrapper();
			wrapper.setCounter(counter);
			wrapper.setInvoiceTotalCost(invoiceItem.getInvoiceTotalCost());
			wrapper.setSelectedTaxRateId(invoiceItem.getTaxRate().getId());
			wrapper.setPercentage(invoiceItem.getTaxRate().getPercentage());
			wrapper.setInvoiceItemAmount(
					ValidationHelper.isNullOrEmpty(invoiceItem.getAmount()) ? 0.0 : invoiceItem.getAmount());
			double totalcost = !(ValidationHelper.isNullOrEmpty(invoiceItem.getInvoiceTotalCost()))
					? invoiceItem.getInvoiceTotalCost().doubleValue()
					: 0.0;
			double amount = !(ValidationHelper.isNullOrEmpty(invoiceItem.getAmount()))
					? invoiceItem.getAmount().doubleValue()
					: 0.0;
			double totalLine;
			if (amount != 0.0) {
				totalLine = totalcost * amount;
			} else {
				totalLine = totalcost;
			}
			wrapper.setTotalLine(totalLine);
			if (!ValidationHelper.isNullOrEmpty(invoiceItem.getDescription()))
				wrapper.setDescription(invoiceItem.getDescription());
			wrapperList.add(wrapper);
			counter = counter + 1;
		}
		return wrapperList;
	}

	public List<BillingTurnoverData> getTurnoversPerMonthData(int year, BillingDashboard billingDashboard)
			throws HibernateException, IllegalAccessException, PersistenceBeanException, InstantiationException {
		Date startDate = DateTimeHelper.getFirstDateOfMonth("01" + "/" + year);
		Date endDate = DateTimeHelper.getLastDateOfMonth("12" + "/" + year);
		List<Invoice> invoices = DaoManager.load(Invoice.class,
				new Criterion[] { Restrictions.between("date", startDate, endDate) });

		InvoiceHelper invoiceHelper = new InvoiceHelper();
		Map<Long, Double> taxMapping = new HashMap<>();
		Map<Long, Double> nonTaxMapping = new HashMap<>();
		Map<Long, Double> ivaMapping = new HashMap<>();
		for (Invoice invoice : invoices) {
			List<GoodsServicesFieldWrapper> wrapperList = goodsServicesFields(invoice, invoiceHelper);
			Double value = invoiceHelper.getNonZeroTotalLine(wrapperList);
			taxMapping.put(invoice.getId(), value);

			value = invoiceHelper.getZeroTotalLine(wrapperList);
			nonTaxMapping.put(invoice.getId(), value);

			value = invoiceHelper.getNonZeroTotalVat(wrapperList);
			ivaMapping.put(invoice.getId(), value);
		}

		List<BillingTurnoverData> billingTurnoverDataList = new ArrayList<>();
		Map<Integer, List<Invoice>> groupedInvoicesByMonth = invoices.stream()
				.filter(i -> !ValidationHelper.isNullOrEmpty(i.getDate()))
				.collect(Collectors.groupingBy(i -> DateTimeHelper.getMonth(i.getDate())));
		String[] months = new String[] { "Gennaio", "Febbraio", "Marzo", "Aprile", "Maggio", "Giugno", "Luglio",
				"Agosto", "Settembre", "Ottobre", "Novembre", "Dicembre" };
		for (int m = 0; m < months.length; m++) {
			BillingTurnoverData billingTurnoverData = DaoManager.get(BillingTurnoverData.class, new CriteriaAlias[]{
					new CriteriaAlias("billingDashboard", "b", JoinType.INNER_JOIN)
			},new Criterion[]{
					Restrictions.eq("month", m + 1),
					Restrictions.eq("b.id", billingDashboard.getId()),});
			if(ValidationHelper.isNullOrEmpty(billingTurnoverData)){
				billingTurnoverData = new BillingTurnoverData();
				billingTurnoverData.setBillingDashboard(billingDashboard);
			}
			if (groupedInvoicesByMonth.containsKey(m + 1)) {
				billingTurnoverData.setMonth(m + 1);
				List<Invoice> invoicesForMonth = groupedInvoicesByMonth.get(m + 1);
				double totalTax = 0d;
				totalTax = invoicesForMonth.stream().filter(i -> taxMapping.containsKey(i.getId()))
						.mapToDouble(o -> taxMapping.get(o.getId())).sum();
				billingTurnoverData.setTotalTax(totalTax);

				double nonTotalTax = 0d;
				nonTotalTax = invoicesForMonth.stream().filter(i -> nonTaxMapping.containsKey(i.getId()))
						.mapToDouble(o -> nonTaxMapping.get(o.getId())).sum();
				billingTurnoverData.setNonTotalTax(nonTotalTax);

				double totalIva = 0d;
				totalIva = invoicesForMonth.stream().filter(i -> ivaMapping.containsKey(i.getId()))
						.mapToDouble(o -> ivaMapping.get(o.getId())).sum();
				billingTurnoverData.setTotalIva(totalIva);

				double total = totalTax + nonTotalTax + totalIva;
				BigDecimal tot = BigDecimal.valueOf(total);
				tot = tot.setScale(2, RoundingMode.HALF_UP);
				total = tot.doubleValue();
				billingTurnoverData.setTotal(total);
			} else {
				billingTurnoverData.setMonth(m + 1);
				billingTurnoverData.setTotalTax(0d);
				billingTurnoverData.setNonTotalTax(0d);
				billingTurnoverData.setTotalIva(0d);
				billingTurnoverData.setTotal(0d);

			}
			DaoManager.save(billingTurnoverData, true);
			billingTurnoverDataList.add(billingTurnoverData);
		}
		return billingTurnoverDataList;
	}

	public List<BillingTurnoverData> getTurnoversPerClientData(int year, BillingDashboard billingDashboard)
			throws HibernateException, IllegalAccessException, PersistenceBeanException, InstantiationException {
		Date startDate = DateTimeHelper.getFirstDateOfMonth("01" + "/" + year);
		Date endDate = DateTimeHelper.getLastDateOfMonth("12" + "/" + year);
		List<Invoice> invoices = DaoManager.load(Invoice.class,
				new Criterion[] { Restrictions.between("date", startDate, endDate) });

		InvoiceHelper invoiceHelper = new InvoiceHelper();
		Map<Long, Double> taxMapping = new HashMap<>();
		Map<Long, Double> nonTaxMapping = new HashMap<>();
		Map<Long, Double> ivaMapping = new HashMap<>();
		for (Invoice invoice : invoices) {
			List<GoodsServicesFieldWrapper> wrapperList = goodsServicesFields(invoice, invoiceHelper);
			Double value = invoiceHelper.getNonZeroTotalLine(wrapperList);
			taxMapping.put(invoice.getId(), value);

			value = invoiceHelper.getZeroTotalLine(wrapperList);
			nonTaxMapping.put(invoice.getId(), value);

			value = invoiceHelper.getNonZeroTotalVat(wrapperList);
			ivaMapping.put(invoice.getId(), value);
		}

		List<BillingTurnoverData> billingTurnoverDataList = new ArrayList<>();
		Map<Client, List<Invoice>> groupedInvoicesByClient = invoices.stream()
				.filter(i -> !ValidationHelper.isNullOrEmpty(i.getClient()))
				.collect(Collectors.groupingBy(Invoice::getClient));
		for (Map.Entry<Client, List<Invoice>> entry : groupedInvoicesByClient.entrySet()) {
			String clientName = "";
			if(!ValidationHelper.isNullOrEmpty(entry.getKey())
					&& !ValidationHelper.isNullOrEmpty(entry.getKey().getClientName())){
				clientName = entry.getKey().getClientName().toUpperCase();
			}
			BillingTurnoverData billingTurnoverData = null;

			if(StringUtils.isNotBlank(clientName)){
				billingTurnoverData = DaoManager.get(BillingTurnoverData.class, new CriteriaAlias[]{
						new CriteriaAlias("billingDashboard", "b", JoinType.INNER_JOIN)
				},new Criterion[]{
						Restrictions.eq("clientName", clientName),
						Restrictions.eq("b.id", billingDashboard.getId()),});
			}
			if(ValidationHelper.isNullOrEmpty(billingTurnoverData)){
				billingTurnoverData = new BillingTurnoverData();
				billingTurnoverData.setBillingDashboard(billingDashboard);
			}
			if (!ValidationHelper.isNullOrEmpty(clientName) && groupedInvoicesByClient.containsKey(entry.getKey())) {
				billingTurnoverData.setClientName(clientName);
				List<Invoice> invoicesForMonth = entry.getValue();
				double totalTax = 0d;
				totalTax = invoicesForMonth.stream().filter(i -> taxMapping.containsKey(i.getId()))
						.mapToDouble(o -> taxMapping.get(o.getId())).sum();
				billingTurnoverData.setTotalTax(0d);
				if(!ValidationHelper.isNullOrEmpty(totalTax))
					billingTurnoverData.setTotalTax(totalTax);
				double nonTotalTax = 0d;
				nonTotalTax = invoicesForMonth.stream().filter(i -> nonTaxMapping.containsKey(i.getId()))
						.mapToDouble(o -> nonTaxMapping.get(o.getId())).sum();
				billingTurnoverData.setNonTotalTax(0d);
				if(!ValidationHelper.isNullOrEmpty(nonTotalTax))
					billingTurnoverData.setNonTotalTax(nonTotalTax);

				double totalIva = 0d;
				totalIva = invoicesForMonth.stream().filter(i -> ivaMapping.containsKey(i.getId()))
						.mapToDouble(o -> ivaMapping.get(o.getId())).sum();
				billingTurnoverData.setTotalIva(0d);
				if(!ValidationHelper.isNullOrEmpty(totalIva))
					billingTurnoverData.setTotalIva(totalIva);

				double total = totalTax + nonTotalTax + totalIva;
				BigDecimal tot = BigDecimal.valueOf(total);
				tot = tot.setScale(2, RoundingMode.HALF_UP);
				total = tot.doubleValue();
				billingTurnoverData.setTotal(total);
				DaoManager.save(billingTurnoverData, true);
				billingTurnoverDataList.add(billingTurnoverData);
			}
		}
		billingTurnoverDataList.sort(Comparator.comparing(BillingTurnoverData::getTotal,
				Comparator.nullsLast(Comparator.reverseOrder())));
		return billingTurnoverDataList;
	}
}
