package it.nexera.ris.common.helpers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.stream.Collectors;

import javax.faces.model.SelectItem;

import it.nexera.ris.persistence.beans.entities.domain.*;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.Office;
import it.nexera.ris.persistence.beans.entities.domain.readonly.RequestShort;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.hibernate.sql.JoinType;

import it.nexera.ris.common.enums.ManageTypeFields;
import it.nexera.ris.common.enums.ManageTypeFieldsState;
import it.nexera.ris.common.enums.ServiceReferenceTypes;
import it.nexera.ris.common.enums.UserCategories;
import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.persistence.beans.dao.CriteriaAlias;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.DataGroup;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.Service;
import it.nexera.ris.web.beans.wrappers.logic.RequestStateWrapper;
import it.nexera.ris.web.beans.wrappers.logic.RequestTypeFilterWrapper;
import it.nexera.ris.web.beans.wrappers.logic.ServiceFilterWrapper;
import it.nexera.ris.web.beans.wrappers.logic.UserFilterWrapper;
import static it.nexera.ris.common.helpers.TemplatePdfTableHelper.distinctByKey;

public class RequestHelper {

    public static List<Criterion> filterTableFromPanel(Date dateFrom, Date dateTo,
                                                       Date dateFromEvasion, Date dateToEvasion,
                                                       Long selectedClientId,
                                                       Long selectedRequestTypeId,
                                                       List<RequestStateWrapper> stateWrappers,
                                                       List<UserFilterWrapper> userWrappers,
                                                       Long selectedServiceType,
                                                       String selectedUserType) {
        List<Criterion> restrictions = new ArrayList<>();

        restrictions.add(
                Restrictions.or(Restrictions.eq("isDeleted", Boolean.FALSE),
                        Restrictions.isNull("isDeleted")));

        if (!ValidationHelper.isNullOrEmpty(dateFrom)) {
            restrictions.add(Restrictions.ge("createDate",
                    DateTimeHelper.getDayStart(dateFrom)));
        }

        if (!ValidationHelper.isNullOrEmpty(dateTo)) {
            restrictions.add(Restrictions.le("createDate",
                    DateTimeHelper.getDayEnd(dateTo)));
        }

        if (!ValidationHelper.isNullOrEmpty(dateFromEvasion)) {
            restrictions.add(Restrictions.ge("evasionDate",
                    DateTimeHelper.getDayStart(dateFromEvasion)));
        }

        if (!ValidationHelper.isNullOrEmpty(dateToEvasion)) {
            restrictions.add(Restrictions.le("evasionDate",
                    DateTimeHelper.getDayEnd(dateToEvasion)));
        }

        if (!ValidationHelper.isNullOrEmpty(selectedClientId)) {
            restrictions.add(
                    Restrictions.eq("clientId", selectedClientId));
        }

        if (!ValidationHelper.isNullOrEmpty(selectedRequestTypeId)) {
            restrictions.add(Restrictions.eq("requestTypeId",
                    selectedRequestTypeId));
        }

        if (!ValidationHelper.isNullOrEmpty(selectedServiceType)) {
            restrictions.add(Restrictions.eq("serviceId", selectedServiceType));
        }

        if (!ValidationHelper.isNullOrEmpty(stateWrappers)
                && !getSelectedAllStatesOnPanel(stateWrappers)) {
            List<Long> stateIds = new ArrayList<>();

            stateWrappers.stream().filter(RequestStateWrapper::getSelected).forEach(state -> stateIds.add(state.getId()));

            if (!ValidationHelper.isNullOrEmpty(stateIds)) {
                restrictions.add(Restrictions.in("stateId", stateIds));
            }
        }

        if (!ValidationHelper.isNullOrEmpty(userWrappers)) {
            List<Long> userIds = new ArrayList<>();

            userWrappers.stream().filter(UserFilterWrapper::getSelected).forEach(user -> userIds.add(user.getId()));

            if (!ValidationHelper.isNullOrEmpty(userIds)) {
                restrictions.add(Restrictions.in("userId", userIds));
            } else {
                restrictions.add(Restrictions.or(Restrictions.isNotNull("userId"),
                        Restrictions.isNull("userId")));
            }
        }

        if (!ValidationHelper.isNullOrEmpty(selectedUserType)) {
            if (UserCategories.ESTERNO.name().equals(selectedUserType)) {
                restrictions.add(Restrictions.and(Restrictions.isNotNull("userOfficeId"),
                        Restrictions.isNotNull("userAreaId")));
            } else {
                restrictions.add(Restrictions.and(Restrictions.isNull("userOfficeId"),
                        Restrictions.isNull("userAreaId")));
            }
        }

        return restrictions;
    }

    public static List<Criterion> filterTableFromPanel(Date dateFrom, Date dateTo,
            Date dateFromEvasion, Date dateToEvasion,
            Long selectedClientId,
            List<RequestTypeFilterWrapper> requestTypeWrappers,
            List<RequestStateWrapper> stateWrappers,
            List<UserFilterWrapper> userWrappers,
            List<ServiceFilterWrapper> serviceWrappers,
            String selectedUserType, Long aggregationFilterId, Long selectedService, Boolean isBilling) {
        List<Criterion> restrictions = new ArrayList<>();

        restrictions.add(
                Restrictions.or(Restrictions.eq("isDeleted", Boolean.FALSE),
                        Restrictions.isNull("isDeleted")));

        if (!ValidationHelper.isNullOrEmpty(dateFrom)) {
            restrictions.add(Restrictions.ge("createDate",
                    DateTimeHelper.getDayStart(dateFrom)));
        }

        if (!ValidationHelper.isNullOrEmpty(dateTo)) {
            restrictions.add(Restrictions.le("createDate",
                    DateTimeHelper.getDayEnd(dateTo)));
        }

        if (!ValidationHelper.isNullOrEmpty(dateFromEvasion)) {
            restrictions.add(Restrictions.ge("evasionDate",
                    DateTimeHelper.getDayStart(dateFromEvasion)));
        }

        if (!ValidationHelper.isNullOrEmpty(dateToEvasion)) {
            restrictions.add(Restrictions.le("evasionDate",
                    DateTimeHelper.getDayEnd(dateToEvasion)));
        }

        if (!ValidationHelper.isNullOrEmpty(selectedClientId)) {
            restrictions.add(
                    Restrictions.eq("clientId", selectedClientId));
        }

        if (!ValidationHelper.isNullOrEmpty(stateWrappers)
                && (isBilling || !getSelectedAllStatesOnPanel(stateWrappers))) {
            List<Long> stateIds = new ArrayList<>();


            stateWrappers.stream().filter(RequestStateWrapper::getSelected).forEach(state -> stateIds.add(state.getId()));
            if (!ValidationHelper.isNullOrEmpty(stateIds)) {
                restrictions.add(Restrictions.in("stateId", stateIds));
            }
        }

        if (!ValidationHelper.isNullOrEmpty(userWrappers)) {
            List<Long> userIds = new ArrayList<>();

            userWrappers.stream().filter(UserFilterWrapper::getSelected).forEach(user -> userIds.add(user.getId()));

            if (!ValidationHelper.isNullOrEmpty(userIds)) {
                restrictions.add(Restrictions.in("userId", userIds));
            } else {
                restrictions.add(Restrictions.or(Restrictions.isNotNull("userId"),
                        Restrictions.isNull("userId")));
            }
        }
        
        if (!ValidationHelper.isNullOrEmpty(serviceWrappers)) {
            List<Long> serviceIds = new ArrayList<>();

            serviceWrappers.stream().filter(ServiceFilterWrapper::getSelected).forEach(service -> serviceIds.add(service.getId()));
            
            if (!ValidationHelper.isNullOrEmpty(serviceIds)) {
                restrictions.add(Restrictions.in("serviceId", serviceIds));
            } else {
                restrictions.add(Restrictions.or(Restrictions.isNotNull("serviceId"),
                        Restrictions.isNull("serviceId")));
            }
            
            List<ServiceFilterWrapper> filterWrappers = 
                    serviceWrappers.stream().
                    filter(sw -> Objects.nonNull(
                            sw.getService().getServiceReferenceType()))
                    .filter(ListHelper.distinctByKey(sw -> sw.getService().getServiceReferenceType()))
                    .collect(Collectors.toList());
            
            if(ValidationHelper.isNullOrEmpty(filterWrappers) && !ValidationHelper.isNullOrEmpty(aggregationFilterId)) {
                restrictions.add(Restrictions.eq("aggregationLandChargesRegistryId",aggregationFilterId));
            }else if(!ValidationHelper.isNullOrEmpty(aggregationFilterId)){
                boolean isConservatory = false;
                boolean isComuni = false;
                for (ServiceFilterWrapper wkrsw : filterWrappers) {
                    if(wkrsw.getSelected()) {
                        if(wkrsw.getService().getServiceReferenceType() == ServiceReferenceTypes.COMMON) {
                            isComuni = true;
                        }else {
                            isConservatory = true;
                        }
                    }
                }
                if(isComuni && isConservatory) {
                    restrictions.add(
                            Restrictions.or(Restrictions.eq("cityId",aggregationFilterId),
                            Restrictions.eq("aggregationLandChargesRegistryId",aggregationFilterId)
                    ));
                }else if(isComuni) {
                    restrictions.add(Restrictions.eq("cityId", aggregationFilterId));
                }else {
                    restrictions.add(Restrictions.eq("aggregationLandChargesRegistryId",aggregationFilterId));
                }
            }
        }else if (!ValidationHelper.isNullOrEmpty(aggregationFilterId)) {
            restrictions.add(Restrictions.eq("aggregationLandChargesRegistryId",
                    aggregationFilterId));
        }
        
        if (!ValidationHelper.isNullOrEmpty(requestTypeWrappers)) {
            List<Long> requestTypeIds = new ArrayList<>();

            requestTypeWrappers.stream().filter(RequestTypeFilterWrapper::getSelected).forEach(requestType -> requestTypeIds.add(requestType.getId()));

            if (!ValidationHelper.isNullOrEmpty(requestTypeIds)) {
                restrictions.add(Restrictions.in("requestTypeId", requestTypeIds));
            } else {
                restrictions.add(Restrictions.or(Restrictions.isNotNull("requestTypeId"),
                        Restrictions.isNull("requestTypeId")));
            }
        }

        if (!ValidationHelper.isNullOrEmpty(selectedUserType)) {
            if (UserCategories.ESTERNO.name().equals(selectedUserType)) {
                restrictions.add(Restrictions.and(Restrictions.isNotNull("userOfficeId"),
                        Restrictions.isNotNull("userAreaId")));
            } else {
                restrictions.add(Restrictions.and(Restrictions.isNull("userOfficeId"),
                        Restrictions.isNull("userAreaId")));
            }
        }
        
        if(!ValidationHelper.isNullOrEmpty(selectedService)) {
            restrictions.add(Restrictions.eq("serviceId",selectedService));
        }

        return restrictions;
    }
    private static boolean getSelectedAllStatesOnPanel(List<RequestStateWrapper> stateWrappers) {
        if (stateWrappers != null) {

            for (RequestStateWrapper wlrsw : stateWrappers) {
                if (!wlrsw.getSelected()) {
                    return false;
                }
            }
        }

        return true;
    }

    private static boolean getSelectedAllUsersOnPanel(List<UserFilterWrapper> userWrappers) {
        if (userWrappers != null) {
            for (UserFilterWrapper wlrsw : userWrappers) {
                if (!wlrsw.getSelected()) {
                    return false;
                }
            }
        }
        return true;
    }

    public static List<SelectItem> onRequestTypeChange(Long requestTypeId, boolean multiple, Long selectedClientId)
            throws IllegalAccessException, PersistenceBeanException, InstantiationException {
        if (!ValidationHelper.isNullOrEmpty(requestTypeId) && !ValidationHelper.isNullOrEmpty(selectedClientId)) {
            List<Service> services = DaoManager.load(Service.class,new Criterion[]{
                    Restrictions.eq("requestType.id", requestTypeId)
            },  Order.asc("name"));

            List<Service> filteredServices = new ArrayList<>();

            Client client = DaoManager.get(Client.class, selectedClientId);

            if(!ValidationHelper.isNullOrEmpty(client)){
                if(!ValidationHelper.isNullOrEmpty(client.getLandOmi()) && client.getLandOmi()){
                    filteredServices.addAll(services
                            .stream()
                            .filter(s -> !ValidationHelper.isNullOrEmpty(s.getLandOmi()) && s.getLandOmi())
                            .collect(Collectors.toList()));
                }
                if(!ValidationHelper.isNullOrEmpty(client.getSalesDevelopment()) && client.getSalesDevelopment()){
                    filteredServices.addAll(services
                            .stream()
                            .filter(s -> !ValidationHelper.isNullOrEmpty(s.getSalesDevelopment()) && s.getSalesDevelopment())
                            .collect(Collectors.toList()));
                }
                filteredServices.addAll(services
                        .stream()
                        .filter(s -> ValidationHelper.isNullOrEmpty(s.getSalesDevelopment())
                                && ValidationHelper.isNullOrEmpty(s.getLandOmi()))
                        .collect(Collectors.toList()));
            }
            if(!ValidationHelper.isNullOrEmpty(filteredServices)){
                return ComboboxHelper.fillList(filteredServices.stream()
                        .filter(distinctByKey(c -> c.getId()))
                        .collect(Collectors.toList()), !multiple);
            }else {
                return (ComboboxHelper.fillList(new ArrayList<>(), !multiple));
            }

//            return ComboboxHelper.fillList(Service.class, Order.asc("name"), new Criterion[]{
//                    Restrictions.eq("requestType.id", requestTypeId)
//            }, !multiple);
        } else {
            return (ComboboxHelper.fillList(new ArrayList<>(), !multiple));
        }
    }

    public static List<InputCard> onServiceChange(Long serviceId)
            throws PersistenceBeanException, IllegalAccessException, InstantiationException {
        if (!ValidationHelper.isNullOrEmpty(serviceId)) {
            Service service = DaoManager.get(Service.class, serviceId);
            if (service != null && service.getGroup() != null) {
                return DaoManager.load(InputCard.class, new CriteriaAlias[]{
                        new CriteriaAlias("dataGroupInputCardList", "dgic", JoinType.INNER_JOIN),
                        new CriteriaAlias("dgic.dataGroup", "group", JoinType.INNER_JOIN),
                }, new Criterion[]{
                        Restrictions.eq("group.id", service.getGroup().getId())
                });
            }
        }
        return null;
    }

    public static List<InputCard> onMultipleServiceChange(List<Long> serviceId , boolean isMultiple) throws PersistenceBeanException, IllegalAccessException {

         return onMultipleServiceChange(serviceId, isMultiple, false);
    }
    public static List<InputCard> onMultipleServiceChange(List<Long> serviceId , boolean isMultiple, boolean isHidden)
            throws PersistenceBeanException, IllegalAccessException {
        if (!ValidationHelper.isNullOrEmpty(serviceId)) {
            List<Service> serviceList = DaoManager.load(Service.class, new Criterion[]{
                    Restrictions.in("id", serviceId)
            });
            if (!ValidationHelper.isNullOrEmpty(serviceList)) {
                List<Long> inputCardIds = serviceList.stream().filter(s -> !ValidationHelper.isNullOrEmpty(s.getGroup()))
                        .map(Service::getGroup).map(DataGroup::getId).collect(Collectors.toList());
                if (!ValidationHelper.isNullOrEmpty(inputCardIds)) {
                    List<InputCard> inputCardList = DaoManager.load(InputCard.class, new CriteriaAlias[]{
                            new CriteriaAlias("dataGroupInputCardList", "dgic", JoinType.INNER_JOIN),
                            new CriteriaAlias("dgic.dataGroup", "group", JoinType.INNER_JOIN),
                    }, new Criterion[]{
                            Restrictions.in("group.id", inputCardIds)
                    });
                    InputCard inputCardOutput = new InputCard();
                    inputCardOutput.setFields(new LinkedList<>());
                    for (InputCardManageField field : inputCardList.stream().map(InputCard::getFields)
                            .flatMap(List::stream).distinct().collect(Collectors.toList())) {

                        if ((!isHidden && field.getState() == ManageTypeFieldsState.HIDDEN)
                                || (field.getField() == ManageTypeFields.CONSERVATORY_TALOVARE) ||
                                (field.getField() == ManageTypeFields.SUBJECT_MASTERY)
                                || (isMultiple && (field.getField()==ManageTypeFields.CDR
                                || field.getField()== ManageTypeFields.NDG
                                || field.getField()== ManageTypeFields.POSITION_PRACTICE
                                || field.getField()== ManageTypeFields.NOTE))){
                            continue;
                        }

                        if ((field.getField().equals(ManageTypeFields.MANAGER)
                                && field.getField().equals(ManageTypeFields.MANAGER))
                                || (field.getField().equals(ManageTypeFields.URGENT) &&
                                field.getField().equals(ManageTypeFields.URGENT))
                        ){
                            continue;
                        }

                        if(!isHidden){
                            InputCardManageField existingField = inputCardOutput.getFields().stream()
                                    .filter(f -> f.getField() == field.getField()).findAny().orElse(null);
                            if (existingField == null) {
                                inputCardOutput.getFields().add(field);
                            } else {
                                if ((existingField.getState() == ManageTypeFieldsState.ENABLE
                                        || existingField.getState() == ManageTypeFieldsState.HIDDEN)
                                        && (field.getState() == ManageTypeFieldsState.ENABLE
                                        || field.getState() == ManageTypeFieldsState.ENABLE_AND_MANDATORY)) {
                                    inputCardOutput.getFields().remove(existingField);
                                    inputCardOutput.getFields().add(field);
                                }
                            }
                        }else {
                            InputCardManageField existingField = inputCardOutput.getFields().stream()
                                    .filter(f -> f.getField() == field.getField()).findAny().orElse(null);
                            if (existingField == null && field.getState() == ManageTypeFieldsState.HIDDEN) {
                                inputCardOutput.getFields().add(field);
                            } else {
                                if (existingField != null && existingField.getState() == ManageTypeFieldsState.HIDDEN) {
                                    inputCardOutput.getFields().remove(existingField);
                                    inputCardOutput.getFields().add(field);
                                }
                            }
                        }

                    }
                    return Collections.singletonList(inputCardOutput);
                }
            }
        }
        return null;
    }

    public static boolean isDifferent(Object currentField, Object potentialField) {
        if (currentField == null && potentialField == null) {
            return false;
        } else if (currentField != null && potentialField == null) {
            return true;
        } else if (currentField == null && potentialField != null) {
            return true;
        } else {
            return !currentField.equals(potentialField);
        }
    }

    public static boolean checkIfRequestHasDistraintFormality(Long requestId)
            throws PersistenceBeanException, IllegalAccessException {
        return DaoManager.getCount(Request.class, "distraintFormality", new Criterion[]{
                Restrictions.eq("id", requestId)
        }).equals(1L);
    }

    public static boolean checkIfRequestWasCreatedByUser(Long requestId, Long userId)
            throws PersistenceBeanException, IllegalAccessException {
        return DaoManager.getCount(Request.class, "id", new Criterion[]{
                Restrictions.eq("id", requestId),
                Restrictions.eq("createUserId", userId)
        }).equals(1L);
    }

    public static String prepareBusinessNameLike(String businessName) {
        if (!ValidationHelper.isNullOrEmpty(businessName)) {
            String output = businessName.trim().replaceAll("\\p{Punct}", "");
            StringJoiner result = new StringJoiner("%", "%", "%");
            for (Character str : output.toCharArray()) {
                result.add(str.toString());
            }
            return result.toString();
        }
        return "";
    }

    public static String prepareBusinessName(String businessName) {
        if (ValidationHelper.isNullOrEmpty(businessName))
            return "";

        return businessName.trim().replaceAll("\\p{Punct}", "").toLowerCase();
    }

    public static boolean isBusinessNameFunctionallyEqual(String businessName1,
                                                          String businessName2) {
        return prepareBusinessName(businessName1).equals(
                prepareBusinessName(businessName2));
    }

    public static void updateState(Long requestId, Long stateId)
            throws IllegalAccessException, PersistenceBeanException, InstantiationException {
        if (!ValidationHelper.isNullOrEmpty(requestId) && !ValidationHelper.isNullOrEmpty(stateId)) {
            Request request = DaoManager.get(Request.class, requestId);
            if (!ValidationHelper.isNullOrEmpty(request)) {
                request.setStateId(stateId);
                DaoManager.save(request, true);
            }
        }
    }

    public static void updateUser(Long requestId, Long userId)
            throws IllegalAccessException, PersistenceBeanException, InstantiationException {
        if (!ValidationHelper.isNullOrEmpty(requestId) && !ValidationHelper.isNullOrEmpty(userId)) {
            Request request = DaoManager.get(Request.class, requestId);
            if (!ValidationHelper.isNullOrEmpty(request)) {
                request.setUser(DaoManager.get(User.class, userId));
                DaoManager.save(request, true);
            }
        }
    }

    public static List<SelectItem> onRequestTypeChange(List<Long> requestTypeIds, boolean multiple)
            throws IllegalAccessException, PersistenceBeanException {
        if (!ValidationHelper.isNullOrEmpty(requestTypeIds)) {
            return ComboboxHelper.fillList(Service.class, Order.asc("name"), new Criterion[]{
                    Restrictions.in("requestType.id", requestTypeIds)
            }, !multiple);
        } else {
            return (ComboboxHelper.fillList(new ArrayList<>(), !multiple));
        }
    }

    public static String getPdfRequestBody(Request request) {
        return getFirstPart(request) + getSecondPart(request) +
                getThirdPart(request);
    }

    public static String getPdfRequestBody(Request request, Subject subject) {
        return getFirstPart(request, subject) + getSecondPart(request, subject) +
                getThirdPart(request, subject);
    }

    public static String getThirdPart(Request request) {
        return ((request == null) || (request.getSubject() == null)) ? "" :
                getThirdPart(request, request.getSubject());
    }

    public static String getThirdPart(Request request, Subject subject) {
        String thirdPart = "";

        try {

            if (!ValidationHelper.isNullOrEmpty(subject)) {

                thirdPart += "<hr/>";
                thirdPart += "<b>Richieste</b>:<br/>";

                List<RequestShort> requestList;
                List<Criterion> criteria = new ArrayList<Criterion>();

                List<Long> subjectsIds = EstateSituationHelper.getIdSubjects(request);
                subjectsIds.add(subject.getId());

                criteria.add(Restrictions.in("subject.id", subjectsIds));

                if (request != null)
                    criteria.add(Restrictions.ne("id", request.getId()));

                criteria.add(Restrictions.or(Restrictions.eq("isDeleted", false),
                        Restrictions.isNull("isDeleted")));

                requestList = DaoManager.load(RequestShort.class, criteria.toArray(new Criterion[0]),
                        Order.desc("createDate"));


                for (RequestShort r : requestList) {
                    thirdPart +=
                            (request.getSubject().getId().equals(r.getSubject().getId()) ? "" : "PRES - ") +
                                    r.getCreateDateStr() +
                                    " - " +
                                    r.getClientName() +
                                    " - " +
                                    r.getServiceName() +
                                    " - " +
                                    r.getAggregationLandChargesRegistryName() +
                                    "<br/>";

                    if (!ValidationHelper.isNullOrEmpty(r.getMultipleServices())) {
                        thirdPart += "<ul>";
                        for (Service service : r.getMultipleServices()) {
                            thirdPart += "<li>";
                            thirdPart += service.getName();
                            thirdPart += "</li>";
                        }
                        thirdPart += "</ul>";
                    }
                }

                List<RequestOLD> requestOLDS = DaoManager.load(RequestOLD.class, new Criterion[]{
                        subject.getTypeIsPhysicalPerson() ?
                                Restrictions.eq("fiscalCodeVat", subject.getFiscalCode()) :
                                Restrictions.eq("fiscalCodeVat", subject.getNumberVAT())});

                for (RequestOLD old : requestOLDS) {
                    thirdPart +=
                            old.getRequestDateString() + " - " +
                                    old.getClient() + " - " +
                                    old.getType() + " - " +
                                    old.getLandChargesRegistry() +
                                    "<br/>";
                }
//
//                if(!ValidationHelper.isNullOrEmpty(request.getMultipleServices())) {
//                    thirdPart += "<ul>";
//                    for(Service service : request.getMultipleServices()) {
//                        thirdPart += "<li>";
//                        thirdPart += service.getName();
//                        thirdPart += "</li>";
//                    }
//                    thirdPart += "</ul>";
//                }

                thirdPart += "<br/>";

                thirdPart += "<hr/>";

                thirdPart += "<b>Visure a testo:</b><br/>";

                List<VisureRTF> visureRTFS = DaoManager.load(VisureRTF.class, new Criterion[]{
                        subject.getTypeIsPhysicalPerson() ?
                                Restrictions.eq("fiscalCodeVat", subject.getFiscalCode()) :
                                Restrictions.eq("fiscalCodeVat", subject.getNumberVAT())});

                for (VisureRTF rtf : visureRTFS) {
                    thirdPart +=
                            DateTimeHelper.toString(rtf.getUpdateDate()) + " - " +
                                    rtf.getNumFormality() + " - " +
                                    rtf.getLandChargesRegistry() +
                                    "<br/>";
                }

                thirdPart += "<br/>";

                thirdPart += "<hr/>";

                thirdPart += "<b>Visure DH:</b><br/>";

                List<VisureDH> visureDHS = DaoManager.load(VisureDH.class, new Criterion[]{
                        subject.getTypeIsPhysicalPerson() ?
                                Restrictions.eq("fiscalCodeVat", subject.getFiscalCode()) :
                                Restrictions.eq("fiscalCodeVat", subject.getNumberVAT())});

                for (VisureDH dh : visureDHS) {
                    thirdPart +=
                            dh.getType() + " - " +
                                    DateTimeHelper.toString(dh.getUpdateDate()) + " - " +
                                    dh.getNumFormality() + " - " +
                                    dh.getNumberPractice() + " - " +
                                    dh.getLandChargesRegistry() +
                                    "<br/>";
                }

                thirdPart += "<br/>";

                thirdPart += "<hr/>";

                thirdPart += "<b>Formalit&agrave;:</b><br/>";

                int countOfRequests = 0;

                if (request != null) {
                    countOfRequests = 1;
                } else {
                    countOfRequests = requestList.size();
                }

                List<Formality> formalityList = new ArrayList<>();

                for (int i = 0; i < countOfRequests; ++i) {

                    List<Long> listIds = EstateSituationHelper.getIdSubjects(subject);
                    listIds.add(subject.getId());
                    criteria = new ArrayList<>();

                    criteria.add(Restrictions.in("sub.id", listIds));
                    List<Formality> list =
                            DaoManager.load(Formality.class, new CriteriaAlias[]{new CriteriaAlias
                                    ("sectionC", "sectionC", JoinType.INNER_JOIN),
                                    new CriteriaAlias("sectionC.subject", "sub", JoinType.INNER_JOIN)
                            }, criteria.toArray(new Criterion[0]));

                    formalityList.addAll(list);
                }

                for (Formality f : formalityList) {
                    boolean isPresumptive = f.getSectionC().stream().map(SectionC::getSubject).flatMap(List::stream)
                            .noneMatch(x -> x.getId().equals(request.getSubject().getId()));

                    thirdPart +=
                            (isPresumptive ? "PRES - " : "") +
                                    f.getConservatoryStr() + " - " +
                                    DateTimeHelper.toString(f.getPresentationDate()) + " - " +
                                    (f.getType() == null || "null".equalsIgnoreCase(f.getType()) ? "" : f.getType().toUpperCase() + " - ") +
                                    (f.getParticularRegister() == null ? "" : f.getParticularRegister() + " - ") +
                                    (f.getGeneralRegister() == null ? "" : f.getGeneralRegister() + " - ") +
                                    f.getActType();

                    thirdPart += "<br/>";
                }

                thirdPart += "<br/>";

                thirdPart += "<hr/>";

                thirdPart += "<b>Segnalazioni:</b><br/>";

                List<ReportFormalitySubject> rfsList =
                        DaoManager.load(ReportFormalitySubject.class,
                                new Criterion[]{
                                        subject.getTypeIsPhysicalPerson() ?
                                                Restrictions.eq("fiscalCode", subject.getFiscalCode()) :
                                                Restrictions.eq("numberVAT", subject.getNumberVAT())
                                }, Order.desc("createDate"));

                for (ReportFormalitySubject rfs : rfsList) {
                    if (rfs.getTypeFormalityId().equals(1L)) {
                        thirdPart += "Trascrizione - ";
                    } else if (rfs.getTypeFormalityId().equals(2L)) {
                        thirdPart += "Iscrizione - ";
                    } else {
                        thirdPart += "Annotamento - ";
                    }
                    thirdPart +=
                            DateTimeHelper.toString(rfs.getDate()) + " - " +
                                    (rfs.getNumber() == null ? "" : rfs.getNumber() + " - ") +
                                    ((rfs.getLandChargesRegistry() == null) ? "" : rfs.getLandChargesRegistry().getName()) +
                                    "<br/>";
                }


                if (subject.getTypeIsPhysicalPerson()) {
                    thirdPart += "<hr/>";
                    thirdPart += "<b>Presumibili:</b><br/>";


                    List<Subject> subjects = SubjectHelper.getPresumablesForSubject(
                            subject);

                    subjects.removeIf(s -> s.equals(subject));
                    for (Subject s : subjects) {

                        thirdPart += s.getFullName() + " - " + s.getSexType().getShortValue() + " - " +
                                DateTimeHelper.toString(s.getBirthDate()) + " - " +
                                ((s.getForeignCountry() != null && s.getForeignCountry())
                                        ? (s.getCountry().getDescription() + " (EE) ")
                                        : (s.getBirthCityDescription()
                                        + (s.getBirthProvince() != null ? s.getBirthProvince().getCode() : " ")))
                                + "nato il " + DateTimeHelper.toString(s.getBirthDate()) + " - " +
                                s.getFiscalCode() +
                                "<br/>";
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "ERROR IN THIRD PART";
        }

        return thirdPart;
    }

    private static String getSecondPart(Request request) {
        return ((request == null) || (request.getSubject() == null)) ? "" :
                getSecondPart(request, request.getSubject());
    }

    private static String getSecondPart(Request request, Subject subject) {
        String secondPart = "";

        if (!ValidationHelper.isNullOrEmpty(request)) {

            if (!ValidationHelper.isNullOrEmpty(request.getNdg())) {
                secondPart += "NDG: " + request.getNdg() + "<br/>";
            }
            if (!ValidationHelper.isNullOrEmpty(request.getPosition())) {
                secondPart += "Posizione: " + request.getPosition() + "<br/>";
            }
            if (!ValidationHelper.isNullOrEmpty(request.getCreateUserId())) {
                secondPart += "Utente: " + request.getCreateUserName() + "<br/>";
            }
            if (!ValidationHelper.isNullOrEmpty(request.getUserOfficeId())) {
                Office office = new Office();
                try {
                    office = DaoManager.get(Office.class, request.getUserOfficeId());
                } catch (PersistenceBeanException | InstantiationException | IllegalAccessException e) {
                    //  LogHelper.log(log, e);
                }
                if (!ValidationHelper.isNullOrEmpty(office)) {
                    secondPart += "Filiale: " + office.getCode() + " " + office.getDescription() + "<br/>";
                }
            }
            if (!ValidationHelper.isNullOrEmpty(request.getNote())) {
                secondPart += "Note: " + request.getNote() + "<br/>";
            } else if (!ValidationHelper.isNullOrEmpty(request.getUltimaResidenza())) {
                secondPart += "Note: " + request.getUltimaResidenza() + "<br/>";
            }

        }

        return secondPart;
    }

    public static String getFirstPart(Request request) {
        return ((request == null) || (request.getSubject() == null)) ? "" :
                getFirstPart(request, request.getSubject());
    }

    private static String getFirstPart(Request request, Subject subject) {
        String result = "";

        if (!ValidationHelper.isNullOrEmpty(request)) {

            if (!ValidationHelper.isNullOrEmpty(request.getClientName())) {
                result += "Cliente: " + request.getClientName() + "<br/>";
            }
            if (!ValidationHelper.isNullOrEmpty(request.getCreateDate())) {
                result += "Data richiesta: " + request.getCreateDateStr() + "<br/>";
            }
            if (!ValidationHelper.isNullOrEmpty(request.getRequestType())) {
                result += "Servizio: " + request.getRequestTypeName() + "<br/>";
            }
            if (!ValidationHelper.isNullOrEmpty(request.getService())) {
                result += "Tipo Richiesta: " + request.getServiceName() + "<br/>";
                result += "Ufficio: " + request.getService().getEmailTextCamelCase() + " ";
            }else if (!ValidationHelper.isNullOrEmpty(request.getMultipleServices())) {
                result += "Tipo Richiesta: " +  request.getMultipleServices()
                        .stream()
                        .filter(s -> !ValidationHelper.isNullOrEmpty(s.getName()))
                        .map(s -> s.toString())
                        .collect(Collectors.joining(","));
                result += "<br/>";

                result += "Ufficio: " + request.getMultipleServices()
                        .stream()
                        .filter(s -> !ValidationHelper.isNullOrEmpty(s.getEmailTextCamelCase()))
                        .map(s -> s.getEmailTextCamelCase())
                        .collect(Collectors.joining(","));
                result += " ";
            }
            if (!ValidationHelper.isNullOrEmpty(request.getAggregationLandChargesRegistry())) {
                result += request.getAggregationLandChargesRegistryName() + "<br/>";
            } else if (!ValidationHelper.isNullOrEmpty(request.getCity())) {
                result += request.getCityDescription() + "<br/>";
            }

            if (!ValidationHelper.isNullOrEmpty(request.getUrgent()) && request.getUrgent()) {
                result += "Urgente: <b>S</b> <br/>";
            } else {
                result += "Urgente: <b>N</b> <br/>";
            }
        }

        if (!ValidationHelper.isNullOrEmpty(subject)) {
            if (subject.getTypeIsPhysicalPerson()) {
                result += "Soggetto: " + subject.getSurnameUpper() + " "
                        + subject.getNameUpper() + "<br/>";
                result += "Tipo: " + subject.getSexType().getShortValue() + "<br/>";
            } else if (!ValidationHelper.isNullOrEmpty(subject.getBusinessName())) {
                result += "Soggetto: " + subject.getBusinessName() + "<br/>";

            }
            if (!ValidationHelper.isNullOrEmpty(subject.getBirthCity()) &&
                    !ValidationHelper.isNullOrEmpty(subject.getBirthProvince())) {

                result += "Dati Anagrafici: " + (subject.getTypeIsPhysicalPerson() ? "nato a " : "con sede in ")
                        +
                        ((subject.getForeignCountry() != null &&
                                subject.getForeignCountry()) ?
                                (subject.getCountry().getDescription() + " (EE) ") :

                                (subject.getBirthCityDescription() + " ( "
                                        + subject.getBirthProvince().getCode() + " ) "));

                if (!ValidationHelper.isNullOrEmpty(subject.getBirthDate())) {
                    result += "il " + DateTimeHelper.toString(subject.getBirthDate());
                }
            } else if (!ValidationHelper.isNullOrEmpty(subject.getCountry())) {
                result += "Dati Anagrafici: " + (subject.getTypeIsPhysicalPerson() ? "nato in " : "con sede in ")
                        + (subject.getCountry().getDescription() + " (EE) ");
            }

            result += "<br/>";
            if (!ValidationHelper.isNullOrEmpty(subject.getFiscalCode())) {
                result += "C.F. " + subject.getFiscalCode() + "<br/>";
            } else if (!ValidationHelper.isNullOrEmpty(subject.getNumberVAT())) {
                result += " P.IVA: " + subject.getNumberVAT() + "<br/>";
            }
        }

        return result;
    }
}
