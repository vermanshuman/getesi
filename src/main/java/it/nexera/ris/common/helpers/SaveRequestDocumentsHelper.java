package it.nexera.ris.common.helpers;

import it.nexera.ris.common.enums.DocumentType;
import it.nexera.ris.common.enums.RequestState;
import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.persistence.Action;
import it.nexera.ris.persistence.TransactionExecuter;
import it.nexera.ris.persistence.UserHolder;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.domain.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class SaveRequestDocumentsHelper {

    public static transient final Log log = LogFactory.getLog(SaveRequestDocumentsHelper.class);

    public static void saveRequestDocuments(Request request, List<Document> documents,boolean isConfirmed) throws Exception {
        TransactionExecuter.execute(new Action() {
            @Override
            public void execute() throws Exception {
                manageFormalitiesForExternal(request, documents);
                for (Document document : documents) {
                    if (!ValidationHelper.isNullOrEmpty(document.getSelectedForDialogList())
                            && document.getSelectedForDialogList()) {
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
        } else {
            request.setStateId(RequestState.EVADED.getId());
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
}
