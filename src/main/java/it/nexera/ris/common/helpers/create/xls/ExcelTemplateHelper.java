package it.nexera.ris.common.helpers.create.xls;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import it.nexera.ris.common.enums.ExtraCostType;
import it.nexera.ris.common.enums.TypeActEnum;
import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.helpers.DateTimeHelper;
import it.nexera.ris.common.helpers.InvoiceHelper;
import it.nexera.ris.common.helpers.ResourcesHelper;
import it.nexera.ris.common.helpers.ValidationHelper;
import it.nexera.ris.persistence.beans.entities.domain.Client;
import it.nexera.ris.persistence.beans.entities.domain.ExtraCost;
import it.nexera.ris.persistence.beans.entities.domain.Request;
import it.nexera.ris.persistence.beans.entities.domain.RequestSubject;
import it.nexera.ris.web.beans.wrappers.logic.ExcelDataWrapper;
import it.nexera.ris.web.beans.wrappers.logic.TranscriptionWrapper;
import it.nexera.ris.web.exceltemplate.CertificationExpense;
import it.nexera.ris.web.exceltemplate.Cost;

public class ExcelTemplateHelper extends CreateExcelReportHelper {
	
	public CertificationExpense createExpenseReportCertification(ExcelDataWrapper excelDataWrapper, Request request) 
			throws IllegalAccessException, HibernateException, InstantiationException, PersistenceBeanException, FileNotFoundException, IOException {
		CertificationExpense certificationExpense = new CertificationExpense();
		String billingClient = "";
        String client = "";
        String office = excelDataWrapper.getOffice();
        String trust = "";
        String ndg = excelDataWrapper.getNdg() != null ? excelDataWrapper.getNdg() : "";
        String reportn = excelDataWrapper.getReportn() != null ? String.valueOf(excelDataWrapper.getReportn()) : "";
        String fatturaN = "";
        if(!ValidationHelper.isNullOrEmpty(excelDataWrapper.getInvoiceNumber()))
            fatturaN = excelDataWrapper.getInvoiceNumber();
        else
            fatturaN = excelDataWrapper.getFatturan() != null ? String.valueOf(excelDataWrapper.getFatturan()) : "";
        String data = excelDataWrapper.getData() != null ? DateTimeHelper.toFormatedString(excelDataWrapper.getData(), DateTimeHelper.getDatePattern()) : "";
        //String fatturaDiRiferimento = excelDataWrapper.getFatturaDiRiferimento() != null ? excelDataWrapper.getFatturaDiRiferimento() : "";

        String referenceRequest = excelDataWrapper.getReferenceRequest() != null ? excelDataWrapper.getReferenceRequest() : "";

        if (!ValidationHelper.isNullOrEmpty(excelDataWrapper.getManagers())) {
            client = excelDataWrapper.getManagers().stream().map(Client::toString).collect(Collectors.joining(", "));
        }

        if (!ValidationHelper.isNullOrEmpty(excelDataWrapper.getClientFiduciary())) {
            trust = excelDataWrapper.getClientFiduciary().toString();
        }else if (!ValidationHelper.isNullOrEmpty(excelDataWrapper.getFiduciary()))
            trust = excelDataWrapper.getFiduciary();

        if (!ValidationHelper.isNullOrEmpty(excelDataWrapper.getClientInvoice())) {
            billingClient = excelDataWrapper.getClientInvoice().toString();
        }
        
        certificationExpense.setBillingCustomer(ValidationHelper.isNullOrEmpty(billingClient) ? "" : billingClient);
        certificationExpense.setFatturaN(ValidationHelper.isNullOrEmpty(fatturaN) ? "" : fatturaN);
        certificationExpense.setData(ValidationHelper.isNullOrEmpty(data) ? "" : data);
        if(excelDataWrapper.getShowReport() != null && excelDataWrapper.getShowReport()){
            reportn = ResourcesHelper.getString("reportN") + " "+ reportn;
            certificationExpense.setReportn(reportn);
        }
        
        certificationExpense.setOffice(!ValidationHelper.isNullOrEmpty(office) ? office.trim() : "");
        certificationExpense.setManager(!ValidationHelper.isNullOrEmpty(client) ? client : "");
        certificationExpense.setTrust(!ValidationHelper.isNullOrEmpty(trust) ? trust : "");
        certificationExpense.setNdg(!ValidationHelper.isNullOrEmpty(ndg)? ndg : "");
        certificationExpense.setReferenceRequest(!ValidationHelper.isNullOrEmpty(referenceRequest) ? referenceRequest : "");
        
        certificationExpense.setRequestTitle(getCertificationRequestTitle(request));
        
        List<TranscriptionWrapper> transcriptionWrappers = createCertificationWrapperTable(request);
        List<Cost> costs = new ArrayList<>();
        for(TranscriptionWrapper transcriptionWrapper: transcriptionWrappers) {
        	Cost cost = new Cost();
        	cost.setCostType(transcriptionWrapper.getCostType());
        	cost.setRights(ValidationHelper.isNullOrEmpty(transcriptionWrapper.getRights()) 
        			? null : InvoiceHelper.format(transcriptionWrapper.getRights().doubleValue()));
        	cost.setExpenses(ValidationHelper.isNullOrEmpty(transcriptionWrapper.getExpenses()) 
        			? null : InvoiceHelper.format(transcriptionWrapper.getExpenses().doubleValue()));
        	//cost.setStamps(ValidationHelper.isNullOrEmpty(transcriptionWrapper.getStamps()) 
        			//? null : InvoiceHelper.format(transcriptionWrapper.getStamps().doubleValue()));
        	cost.setFees(ValidationHelper.isNullOrEmpty(transcriptionWrapper.getFees()) 
        			? null : InvoiceHelper.format(transcriptionWrapper.getFees().doubleValue()));
        	costs.add(cost);
        }
        certificationExpense.setCosts(costs);
        
        Double cadastralAssessment = getCostCadastralAndExtraCostRelated(request);
        Double mortgageInvestigations = getCostEstateFormalityAndExtraCostRelated(request);
        Double totalRights = cadastralAssessment.doubleValue() + mortgageInvestigations.doubleValue();
        certificationExpense.setTotalRights(totalRights != null ? "€ " + InvoiceHelper.format(totalRights) : null);
        
        Double totalExpenses;
        List<ExtraCost> extraCosts = getExtraCostsAltro(request);
		if(!ValidationHelper.isNullOrEmpty(extraCosts)) {
			double extraCostsAltro = 0.0;
			extraCostsAltro = extraCosts.stream().filter(x -> !ValidationHelper.isNullOrEmpty(x.getPrice()))
		    		  .map(x -> x.getPrice())
		    		  .collect(Collectors.summingDouble(Double::doubleValue));
			totalExpenses = getRequestExtraCostSumByType(request.getId(), ExtraCostType.POSTALE).doubleValue() 
					+ getTotalAnagraficoCost(request).doubleValue()
					+ extraCostsAltro;
			certificationExpense.setTotalExpenses(totalExpenses != null 
					? "€ " + InvoiceHelper.format(totalExpenses) : null);
		} else {
			Double postalExpenses = getRequestExtraCostSumByType(request.getId(), ExtraCostType.POSTALE) != 0d 
					? getRequestExtraCostSumByType(request.getId(), ExtraCostType.POSTALE) : 0d;
			Double anagraficoExpenses = getTotalAnagraficoCost(request);
			totalExpenses = postalExpenses + anagraficoExpenses;
			certificationExpense.setTotalExpenses(totalExpenses != 0d 
					? "€ " + InvoiceHelper.format(totalExpenses) : null);
		}
		
		//Double totalStamp = getTotalStampCost(request) != 0d ? getTotalStampCost(request).doubleValue() : null;
		//certificationExpense.setTotalStamp(totalStamp != null ? "€ " + InvoiceHelper.format(totalStamp) : null);
		
		
		
		Double totalCompensation = ValidationHelper.isNullOrEmpty(request.getCostPay()) ? null : request.getCostPay();
		certificationExpense.setTotalFees(totalCompensation != null ? "€ " + InvoiceHelper.format(totalCompensation) : null);
        
        return certificationExpense;
	}
	
	public List<TranscriptionWrapper> createCertificationWrapperTable(Request request)
			throws IllegalAccessException, PersistenceBeanException, HibernateException, InstantiationException {
		List<TranscriptionWrapper> transcriptionWrapperList = new ArrayList<>();
		Boolean transcriptionTab = null;
		Boolean certificationTab = null;
		if(request.getService() != null && request.getService().getManageTranscription() != null
				&& request.getService().getManageTranscription())
			transcriptionTab = Boolean.TRUE;
		if(request.getService() != null && request.getService().getManageCertification() != null
				&& request.getService().getManageCertification())
			certificationTab = Boolean.TRUE;
		TranscriptionWrapper cadastralAssessment = new TranscriptionWrapper();
		cadastralAssessment.setCostType(ResourcesHelper.getString("cadastralAssessment"));
		Double rights = getCostCadastralAndExtraCostRelated(request, transcriptionTab, certificationTab);
		cadastralAssessment.setRights(rights != 0d ?
				new BigDecimal(getCostCadastralAndExtraCostRelated(request)).setScale(2, BigDecimal.ROUND_HALF_EVEN) : null);
		cadastralAssessment.setTempId(1);
		transcriptionWrapperList.add(cadastralAssessment);
		
		TranscriptionWrapper mortgageInvestigations = new TranscriptionWrapper();
		mortgageInvestigations.setCostType(ResourcesHelper.getString("mortgageInvestigations"));
		Double result = getCostEstateFormalityAndExtraCostRelated(request, transcriptionTab, certificationTab);
		mortgageInvestigations.setRights(result.doubleValue() != 0d ? new BigDecimal(result).setScale(2, BigDecimal.ROUND_HALF_EVEN) : null);
		mortgageInvestigations.setTempId(2);
		transcriptionWrapperList.add(mortgageInvestigations);

		TranscriptionWrapper notarialCertification = new TranscriptionWrapper();
		notarialCertification.setCostType(ResourcesHelper.getString("notarialCertification"));
		result = getTotalStampCost(request, transcriptionTab, certificationTab);
		notarialCertification.setStamps(result.doubleValue() != 0d ? new BigDecimal(result).setScale(2,
				BigDecimal.ROUND_HALF_EVEN) : null);
		notarialCertification
				.setFees(!ValidationHelper.isNullOrEmpty(request.getCostPay()) ? 
						(request.getCostPay() != 0d ? new BigDecimal(request.getCostPay()).setScale(2, BigDecimal.ROUND_HALF_EVEN) : null) : null);
		notarialCertification.setTempId(3);
		transcriptionWrapperList.add(notarialCertification);

		TranscriptionWrapper personalCosts = new TranscriptionWrapper();
		personalCosts.setCostType(ResourcesHelper.getString("personalCosts"));
		result = getTotalAnagraficoCost(request);
		personalCosts.setExpenses(result.doubleValue() != 0d ? new BigDecimal(result).setScale(2,
				BigDecimal.ROUND_HALF_EVEN) : null);
		personalCosts.setTempId(4);
		transcriptionWrapperList.add(personalCosts);
		
		TranscriptionWrapper postal = new TranscriptionWrapper();
		postal.setCostType(ResourcesHelper.getString("postal"));
		result = getRequestExtraCostSumByType(request.getId(), ExtraCostType.POSTALE, transcriptionTab, certificationTab);
		postal.setExpenses(result != 0d ? new BigDecimal(result).setScale(2, BigDecimal.ROUND_HALF_EVEN) : null);
		postal.setTempId(5);
		transcriptionWrapperList.add(postal);
		
		List<ExtraCost> extraCosts = getExtraCostsAltro(request);
		if(!ValidationHelper.isNullOrEmpty(extraCosts)) {
			int tempId = 5;
			for(ExtraCost extraCost : extraCosts) {
				if((transcriptionTab != null && transcriptionTab && (
						extraCost.getTranscription() == null || !extraCost.getTranscription())) ||
						(certificationTab != null && certificationTab && (
								extraCost.getCertification() == null || !extraCost.getCertification()))){
					break;
				}
				if(!ValidationHelper.isNullOrEmpty(extraCost.getPrice())) {
					tempId = tempId + 1;
					TranscriptionWrapper transcriptionWrapperExtraCost = new TranscriptionWrapper();
					transcriptionWrapperExtraCost.setTempId(tempId);
					transcriptionWrapperExtraCost.setCostType(!ValidationHelper.isNullOrEmpty(extraCost.getNote()) ? extraCost.getNote() : ResourcesHelper.getString("other"));
					transcriptionWrapperExtraCost.setExpenses(extraCost.getPrice() != 0d ? new BigDecimal(extraCost.getPrice()).setScale(2, BigDecimal.ROUND_HALF_EVEN) : null);
					transcriptionWrapperList.add(transcriptionWrapperExtraCost);
				}
			}
		}

		return transcriptionWrapperList;
	}
	
	public String getCertificationRequestTitle(Request request) {
		String title = "CERTIFICAZIONE NOTARILE - CONSERVATORIA DI ";
		if(!ValidationHelper.isNullOrEmpty(request.getAggregationLandChargesRegistry())
				&& !ValidationHelper.isNullOrEmpty(request.getAggregationLandChargesRegistry().getName())) {
			title += request.getAggregationLandChargesRegistry().getName();
		} else if(!ValidationHelper.isNullOrEmpty(request.getDistraintFormality()) 
				&& !ValidationHelper.isNullOrEmpty(request.getDistraintFormality().getReclamePropertyService())
				&& !ValidationHelper.isNullOrEmpty(request.getDistraintFormality().getReclamePropertyService().getName())) {
			title += request.getDistraintFormality().getReclamePropertyService().getName();
		} 
		if(!ValidationHelper.isNullOrEmpty(request.getDistraintFormality())) { 
			if(!ValidationHelper.isNullOrEmpty(request.getDistraintFormality().getProvincialOffice())
				&& !ValidationHelper.isNullOrEmpty(request.getDistraintFormality().getProvincialOffice().getName())) {
				title += request.getDistraintFormality().getProvincialOffice().getName() + " - ";
				if (!Hibernate.isInitialized(request.getRequestSubjects())) {
					try {
						request.reloadRequestSubjects();
					} catch (IllegalAccessException | PersistenceBeanException e) {
						e.printStackTrace();
					}
		        }
		    	
		    	if(!ValidationHelper.isNullOrEmpty(request.getRequestSubjects())) {
					String subjectNames = " - ";
					for(RequestSubject requestSubject : request.getRequestSubjects()) {
		                String subject = requestSubject.getSubject().getFullName() + (request.getRequestSubjects().size() > 1 ? " - " : "");
		                subjectNames = subjectNames + subject.toUpperCase();
		            }
					title += subjectNames;
				}
			} else if(!ValidationHelper.isNullOrEmpty(request.getDistraintFormality().getSecrionCAgainstSubjects())) {
				title += request.getDistraintFormality().getSecrionCAgainstSubjects();
			}
			
		}
    	return title.toUpperCase();
	}

}
