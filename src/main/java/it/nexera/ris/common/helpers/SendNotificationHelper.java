package it.nexera.ris.common.helpers;

import it.nexera.ris.common.enums.RoleTypes;
import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.persistence.beans.dao.CriteriaAlias;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.domain.*;
import it.nexera.ris.web.beans.wrappers.logic.FileWrapper;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;
import org.hibernate.sql.JoinType;

import java.util.ArrayList;
import java.util.List;

public class SendNotificationHelper {

    public static void checkAndSendNotification(Request request) throws Exception {
        User createRequestUser = DaoManager.get(User.class, request.getCreateUserId());
        boolean externalUser = false;
        for (Role role : createRequestUser.getRoles()) {
            if (RoleTypes.EXTERNAL.equals(role.getType())) {
                externalUser = true;
                break;
            }
        }

        if (externalUser) {
            MailTemplate mailTemplate = DaoManager.get(MailTemplate.class, new Criterion[]{
                    Restrictions.eq("templateType", "evasione")});
            mailTemplate.setMailBodyHtml(replaceTagsByValues(mailTemplate, request));

            List<FileWrapper> files = new ArrayList<>();

            if (!ValidationHelper.isNullOrEmpty(createRequestUser.getNotificationOutput())
                    && createRequestUser.getNotificationOutput()) {
                List<Document> relatedDocuments = loadRelatedDocuments(request);
                for (Document document : relatedDocuments) {
                    files.add(new FileWrapper(document.getId(), document.getOlnyName(), document.getDocumentPath()));
                }
            }
            if (!ValidationHelper.isNullOrEmpty(createRequestUser.getNotificationEvasion())
                    && createRequestUser.getNotificationEvasion()) {
                MailHelper.sendMail(createRequestUser.getEmail(), mailTemplate, files);
            }
        }
    }

    private static String replaceTagsByValues(MailTemplate mailTemplate, Request request) {
        String result = "";
        if (!ValidationHelper.isNullOrEmpty(request.getSubject()) && !ValidationHelper.isNullOrEmpty(mailTemplate.getMailBodyHtml())) {

            result = mailTemplate.getMailBodyHtml();

            result = result.replaceAll("%subject_name%", request.getSubject().getFullName());
            result = result.replaceAll("%subject_fiscal_code%", request.getSubject().getFiscalCode() == null
                    ? request.getSubject().getNumberVAT() : request.getSubject().getFiscalCode());

            if(ValidationHelper.isNullOrEmpty(request.getMultipleServices()) 
                    && !ValidationHelper.isNullOrEmpty(request.getService())) {
                result = result.replaceAll("%dic_service_name%", request.getService().getName());
            }else {
                try {
                    result = result.replaceAll("%dic_service_name%", request.getMultipleServiceNames());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            result = result.replaceAll("%dic_service_email%", 
                    (!ValidationHelper.isNullOrEmpty(request.getService()) ? request.getService().getEmailText() : "")+ " "
                    + (request.getAggregationLandChargesRegistry() == null ?
                    request.getCityDescription() : request.getAggregationLandChargesRegistryName()));

            String clientName = "";
            if (!ValidationHelper.isNullOrEmpty(request.getClient())) {

                if (!ValidationHelper.isNullOrEmpty(request.getClient().getNameOfTheCompany())) {
                    clientName = request.getClient().getNameOfTheCompany();
                } else if (!ValidationHelper.isNullOrEmpty(request.getClient().getNameProfessional())) {
                    clientName = request.getClient().getNameProfessional();
                }

            }

            result = result.replaceAll("%client_id%", clientName);

            result = result.replaceAll("%current_user%", request.getCreateUserName());

        }

        return result;
    }

    private static List<Document> loadRelatedDocuments(Request request) throws PersistenceBeanException, IllegalAccessException {
        List<Document> documents = DaoManager.load(Document.class, new Criterion[]{
                Restrictions.eq("request.id", request.getId()),
                Restrictions.eq("selectedForEmail", true)
        });

        List<Document> formalities = DaoManager.load(Document.class, new CriteriaAlias[]{
                new CriteriaAlias("formality", "f", JoinType.INNER_JOIN),
                new CriteriaAlias("f.requestList", "r_f", JoinType.INNER_JOIN)
        }, new Criterion[]{
                Restrictions.eq("r_f.id", request.getId()),
                Restrictions.eq("selectedForEmail", true)
        });

        if (!ValidationHelper.isNullOrEmpty(formalities)) {
            for (Document temp : formalities) {
                if (!documents.contains(temp)) {
                    documents.add(temp);
                }
            }
        }
        return documents;
    }
}
