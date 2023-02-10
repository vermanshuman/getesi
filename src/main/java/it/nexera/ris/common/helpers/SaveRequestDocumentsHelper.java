package it.nexera.ris.common.helpers;

import it.nexera.ris.common.enums.DocumentType;
import it.nexera.ris.common.enums.RequestState;
import it.nexera.ris.common.enums.SectionCType;
import it.nexera.ris.common.enums.SubjectType;
import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.persistence.Action;
import it.nexera.ris.persistence.TransactionExecuter;
import it.nexera.ris.persistence.UserHolder;
import it.nexera.ris.persistence.beans.dao.CriteriaAlias;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.domain.*;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.TypeFormality;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pdfbox.io.MemoryUsageSetting;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.hibernate.HibernateException;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;
import org.hibernate.sql.JoinType;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.StringJoiner;
import java.util.stream.Collectors;

public class SaveRequestDocumentsHelper {

    public static transient final Log log = LogFactory.getLog(SaveRequestDocumentsHelper.class);

    public static void saveRequestDocuments(Request request, List<Document> documents,boolean isConfirmed) throws Exception {
        TransactionExecuter.execute(new Action() {
            @Override
            public void execute() throws Exception {
                manageFormalitiesForExternal(request, documents);
                for (Document document : documents) {
                    if (!ValidationHelper.isNullOrEmpty(document.getSelectedForDialogList()) &&
                            document.getSelectedForDialogList()) {
                        document.setSelectedForEmail(true);
                        document.setRequest(request);
                        DaoManager.save(document);
                    } else {
                        if (DocumentType.FORMALITY.getId().equals(document.getTypeId())) {
                            saveRequestFormalityPDF(document, request);
                        }
                        document.setSelectedForEmail(false);
                        document.setRequest(request);
                        DaoManager.save(document);
                    }
                }
                setParamsAndSaveRequest(request,isConfirmed);
            }
        });
    }

    private static void manageFormalitiesForExternal(Request request, List<Document> documents) throws PersistenceBeanException {
        if (!ValidationHelper.isNullOrEmpty(request.getSubject())) {
            if (ValidationHelper.isNullOrEmpty(request.getSubject().getFormalityExternalList())) {
                request.getSubject().setFormalityExternalList(new ArrayList<>());
            }

            for (Document document : documents) {
                if (!ValidationHelper.isNullOrEmpty(document.getSelectedFormalityForExternal())) {

                    if (document.getSelectedFormalityForExternal()) {
                        for (Formality formality : document.getFormality()) {
                            if (!request.getSubject().getFormalityExternalList().contains(formality))
                                request.getSubject().getFormalityExternalList().add(formality);
                        }
                    } else {
                        for (Formality formality : document.getFormality()) {
                            request.getSubject().getFormalityExternalList().remove(formality);
                        }
                    }
                    DaoManager.save(request.getSubject());
                }
            }
        }
    }

    private static void saveRequestFormalityPDF(Document document, Request request) throws PersistenceBeanException {
        for (Formality formality : document.getFormality()) {
            if (request.getFormalityPdfList().contains(formality)) {
                request.getFormalityPdfList().remove(formality);
                DaoManager.save(request);
            }
        }
    }

    private static void setParamsAndSaveRequest(Request request,boolean isConfirmed) throws PersistenceBeanException,
            IllegalAccessException, InstantiationException {
        if (request.getRequestPrint() == null) {
            request.setRequestPrint(new RequestPrint());
        }
        request.getRequestPrint().setDocumentAttached(true);
        DaoManager.save(request.getRequestPrint());
        if(isConfirmed) {
        	request.setEvasionDate(new Date());
        }
        
        request.setSent(true);
        if (!ValidationHelper.isNullOrEmpty(request.getMail())) {
            request.setStateId(RequestState.TO_BE_SENT.getId());
            request.setCertificationStateId(RequestState.TO_BE_SENT.getId());
        } else {
            request.setStateId(RequestState.EVADED.getId());
            request.setCertificationStateId(RequestState.EVADED.getId());
            request.setUser(DaoManager.get(User.class, UserHolder.getInstance().getCurrentUser().getId()));
        }
        DaoManager.save(request);
    }

    public static String generateCostByRequests(List<Request> requests) {
        return String.valueOf(requests.stream()
                .mapToDouble(r -> !ValidationHelper.isNullOrEmpty(r.getTotalCost()) ?
                        Double.parseDouble(r.getTotalCostDouble()) : 0).sum());
    }

    public static Long getLastInvoiceNumber() {
        Long lastInvoiceNumber = null;
        try {
            lastInvoiceNumber = (Long) DaoManager.getMax(Document.class, "invoiceNumber",
                    new Criterion[]{Restrictions.eq("typeId", DocumentType.INVOICE_REPORT.getId())});
        } catch (PersistenceBeanException | IllegalAccessException e) {
            LogHelper.log(log, e);
        }
        return lastInvoiceNumber == null ? 0 : lastInvoiceNumber;
    }
    
    public static void saveRequestDocumentsSingleFile(Request request, List<Document> documents,boolean isConfirmed) throws Exception {
        TransactionExecuter.execute(new Action() {
        	
            @Override
            public void execute() throws Exception {
                manageFormalitiesForExternal(request, documents);

                for (Document document : documents) {

                    if (!ValidationHelper.isNullOrEmpty(document.getSelectedForDialogList()) &&
                            document.getSelectedForDialogList()) {

                        document.setSelectedForEmail(true);
                        document.setRequest(request);
                        DaoManager.save(document);
                    } else {
                        if (DocumentType.FORMALITY.getId().equals(document.getTypeId())) {
                            saveRequestFormalityPDF(document, request);
                        }
                        document.setSelectedForEmail(false);
                        document.setRequest(request);
                        DaoManager.save(document);
                    }
                }
                PDFMergerUtility obj = new PDFMergerUtility();
                String fileName = generatePdfName(request);
                
                String pathToFile = FileHelper.getApplicationProperties().getProperty("requestReportSavePath")
                        + File.separator + request.getId() + File.separator + "single_file" + File.separator + fileName + ".pdf" ;
                Path pdfFilePath = Paths.get(pathToFile);
                log.info("single file :: "+pathToFile);
                if (Files.notExists(pdfFilePath.getParent()))
                    Files.createDirectories(pdfFilePath.getParent());
                obj.setDestinationFileName(pathToFile); 
                
                List<Document> combinedDocuments = new LinkedList<>();
                List<Document> requestReportDocuments = new LinkedList<>();
                List<Document> dicTypeFormalityDocuments = new LinkedList<>();
                List<Document> otherDocuments = new LinkedList<>();
                List<Document> allOtherDocuments = new LinkedList<>();
                List<Formality> prejudicialFormailities = new LinkedList<>();
                List<Formality> salesDevelopmentFormailities = new LinkedList<>();
                List<Formality> allFormalities = new ArrayList<>();
                
                for (Document document : documents) {
                	if (!ValidationHelper.isNullOrEmpty(document.getSelectedForDialogList()) && document.getSelectedForDialogList()) {
	                	if(document.getTypeId().equals(DocumentType.FORMALITY.getId())) {
	                		if(!ValidationHelper.isNullOrEmpty(document.getFormality())) {
	                			allFormalities.addAll(document.getFormality());
	                		}
	                	}
                	}
                }
                
                if(!ValidationHelper.isNullOrEmpty(allFormalities)) {
                	allFormalities.sort(Comparator.comparing(Formality::getPresentationDate).reversed());
                	for (Formality formality : allFormalities) {
                        if (request.getFormalityPdfList().contains(formality)) {
                        	TypeFormality dicTypeFormality = formality.getDicTypeFormality();
                        	if(!ValidationHelper.isNullOrEmpty(dicTypeFormality)) {
                        		if(!ValidationHelper.isNullOrEmpty(dicTypeFormality.getPrejudicial()) && dicTypeFormality.getPrejudicial()) {
                        			prejudicialFormailities.add(formality);
                        		} else if((!ValidationHelper.isNullOrEmpty(dicTypeFormality.getSalesDevelopment()) && dicTypeFormality.getSalesDevelopment())
                        				|| (!ValidationHelper.isNullOrEmpty(dicTypeFormality.getSalesDevelopmentOMI()) && dicTypeFormality.getSalesDevelopmentOMI())) {
                        			salesDevelopmentFormailities.add(formality);
                        		}
                        	}
                        }
            		}
                }
                
                for (Document document : documents) {
                    if (!ValidationHelper.isNullOrEmpty(document.getSelectedForDialogList()) && document.getSelectedForDialogList()) {
                    	if(document.getTypeId().equals(DocumentType.REQUEST_REPORT.getId())) {
                    		requestReportDocuments.add(document);
                    	}
                    	
                    	else if(document.getTypeId().equals(DocumentType.OTHER.getId())) {
                    		otherDocuments.add(document);
                    	}
                    	else if(!document.getTypeId().equals(DocumentType.REQUEST_REPORT.getId()) 
                    			&& !document.getTypeId().equals(DocumentType.FORMALITY.getId())
                    			&& !document.getTypeId().equals(DocumentType.OTHER.getId())
                    			&& !document.getTypeId().equals(DocumentType.SINGLE_FULFILMENT_FILE.getId())){
                    		allOtherDocuments.add(document);
                    	}
                    }
                }
                
                for(Formality prejudicial : prejudicialFormailities) {
        			dicTypeFormalityDocuments.add(prejudicial.getDocument());
        		}
        		for(Formality salesDevelopmentFormality : salesDevelopmentFormailities) {
        			dicTypeFormalityDocuments.add(salesDevelopmentFormality.getDocument());
        		}
        		
        		combinedDocuments.addAll(requestReportDocuments);
        		combinedDocuments.addAll(dicTypeFormalityDocuments);
        		combinedDocuments.addAll(otherDocuments);
        		combinedDocuments.addAll(allOtherDocuments);
        		List<Document> uniqueCombinedDocuments = combinedDocuments.stream().distinct().collect(Collectors.toList()); 
        		
        		try {
	        		for (Document document : uniqueCombinedDocuments) {
	        			log.info("file :: "+document.getPath());
	        			if(document.getPath().contains(".pdf")) {
	        				log.info("file considered for single file generation :: "+document.getPath());
	        				File file = new File(document.getPath());
	        				if(file.exists()) {
	        					log.info("File exists");
	        		        } else {
	        		        	log.info("File does not exists");
	        		        	continue;
	        		        }
	        				if (file.canRead()) {
	        					log.info("File is Readable");
	        		        } else {
	        		        	log.info("File not readable");
	        		        	file.setReadable(true);
	        		        }
	        				
	        				if (file.canWrite()) {
	        					log.info("Can Write file ");
	        		        } else {
	        		        	log.info("Cannot Write file ");
	        		        	file.setWritable(true);
	        		        }
	        				
	        				if (file.canExecute()) {
	        					log.info("Can Execute file ");
	        		        } else {
	        		        	log.info("Cannot Execute file");
	        		        	file.setExecutable(true);
	        		        }
	        				obj.addSource(file);
	        			}
	        		}
	                
	                for (Document document : uniqueCombinedDocuments) {
	                    if (DocumentType.FORMALITY.getId().equals(document.getTypeId())) {
	                        saveRequestFormalityPDF(document, request);
	                    }
	                    if(!document.getTypeId().equals(DocumentType.ATTACHMENT_C.getId()))
	                        document.setSelectedForEmail(false);
	                    document.setRequest(request);
	                    DaoManager.save(document);
	                }
                    obj.mergeDocuments(MemoryUsageSetting.setupTempFileOnly());
                    Document document = DaoManager.get(Document.class, new Criterion[]{
                            Restrictions.eq("request", request),
                            Restrictions.eq("typeId", DocumentType.SINGLE_FULFILMENT_FILE.getId())});
                    if(ValidationHelper.isNullOrEmpty(document)) {
                    	document = new Document();
                    }
                    document.setSelectedForEmail(true);
                    document.setRequest(request);
                    document.setPath(pathToFile);
                    document.setTitle(fileName);
                    document.setTypeId(DocumentType.SINGLE_FULFILMENT_FILE.getId());
                    DaoManager.save(document);
                    
                    setParamsAndSaveRequest(request,isConfirmed);
                } catch (Exception e) {
                	log.error(e.getMessage());
                }
            }
        });
    }
    
    private static String generatePdfName(Request request) throws HibernateException, IllegalAccessException, PersistenceBeanException {
        String separator = "-";
        String spaceVal = "\\s";
        StringJoiner joiner = new StringJoiner(separator);
        String prefix = "";

        if (!ValidationHelper.isNullOrEmpty(request.getDistraintFormality())) {
            StringBuilder sb = new StringBuilder();
            sb.append("Certificazione notarile_");

            List<Subject> subjects = DaoManager.load(Subject.class, new CriteriaAlias[]{
                            new CriteriaAlias("sectionC", "sc", JoinType.INNER_JOIN)},
                    new Criterion[]{Restrictions.eq("sc.formality", request.getDistraintFormality()),
                            Restrictions.eq("sc.sectionCType", SectionCType.CONTRO.getName())});

            if (subjects != null && subjects.size() == 1) {
                Subject s = subjects.get(0);
                if (s.getTypeIsPhysicalPerson()) {
                    sb.append(s.getSurname().toUpperCase());
                } else if (SubjectType.LEGAL_PERSON.getId().equals(s.getTypeId())) {
                    sb.append(s.getBusinessName().toUpperCase());
                }
            } else {
                subjects.stream().forEach(s -> {
                    String fullName = "";
                    if (s.getTypeIsPhysicalPerson()) {
                        fullName = s.getSurname().toUpperCase();
                    } else if (SubjectType.LEGAL_PERSON.getId().equals(s.getTypeId())) {
                        fullName = s.getBusinessName().toUpperCase();
                    }
                    if (!ValidationHelper.isNullOrEmpty(fullName)) {
                        if (sb.length() > 24)
                            sb.append("-");
                        sb.append(fullName);
                    }
                });
            }
            joiner.add(sb.toString());
        } else if (!ValidationHelper.isNullOrEmpty(request.getSubject())) {
            if (request.isPhysicalPerson()) {
                if (!ValidationHelper.isNullOrEmpty(request.getSubject().getSurname())) {
                    joiner.add(request.getSubject().getSurname()
                            .toUpperCase().replaceAll(spaceVal, separator));
                }
                if (!ValidationHelper.isNullOrEmpty(request.getSubject().getName())) {
                    joiner.add(request.getSubject().getName()
                            .toUpperCase().replaceAll(spaceVal, separator));
                }
                if (!ValidationHelper.isNullOrEmpty(request.getSubject().getBirthCity())) {
                    joiner.add(request.getSubject().getBirthCity().getDescription()
                            .toUpperCase().replaceAll(spaceVal, separator));
                }
                if (!ValidationHelper.isNullOrEmpty(request.getSubject().getBirthDate())) {
                    joiner.add(DateTimeHelper.toFormatedString(request.getSubject().getBirthDate(),
                            DateTimeHelper.getXmlDatePattert()));
                }
            } else {
                if (!ValidationHelper.isNullOrEmpty(request.getSubject().getBusinessName())) {
                    joiner.add(request.getSubject().getBusinessName()
                            .toUpperCase().replaceAll(spaceVal, separator));
                }
                if (!ValidationHelper.isNullOrEmpty(request.getSubject().getNumberVAT())) {
                    joiner.add(request.getSubject().getNumberVAT()
                            .toUpperCase().replaceAll(spaceVal, separator));
                }
            }
        }

        joiner.add("Cons");
        if (!ValidationHelper.isNullOrEmpty(request.getAggregationLandChargesRegistry())) {
            joiner.add(request.getAggregationLandChargesRegistry().getName()
                    .toUpperCase().replaceAll(spaceVal, separator));
        }

        if (joiner.toString().toUpperCase().startsWith("CON.") || joiner.toString().equalsIgnoreCase("CON")) {
            prefix = "-";
        }
        return prefix + joiner.toString().replaceAll("[^\\w\\s\\-_.]", "");
    }
}
