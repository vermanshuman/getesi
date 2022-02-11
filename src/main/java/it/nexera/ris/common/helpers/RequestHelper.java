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
import it.nexera.ris.persistence.beans.entities.domain.InputCard;
import it.nexera.ris.persistence.beans.entities.domain.InputCardManageField;
import it.nexera.ris.persistence.beans.entities.domain.Request;
import it.nexera.ris.persistence.beans.entities.domain.User;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.DataGroup;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.Service;
import it.nexera.ris.web.beans.wrappers.logic.RequestStateWrapper;
import it.nexera.ris.web.beans.wrappers.logic.RequestTypeFilterWrapper;
import it.nexera.ris.web.beans.wrappers.logic.ServiceFilterWrapper;
import it.nexera.ris.web.beans.wrappers.logic.UserFilterWrapper;

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

    public static List<SelectItem> onRequestTypeChange(Long requestTypeId, boolean multiple)
            throws IllegalAccessException, PersistenceBeanException {
        if (!ValidationHelper.isNullOrEmpty(requestTypeId)) {
            return ComboboxHelper.fillList(Service.class, Order.asc("name"), new Criterion[]{
                    Restrictions.eq("requestType.id", requestTypeId)
            }, !multiple);
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

    public static List<InputCard> onMultipleServiceChange(List<Long> serviceId , boolean isMultiple)
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
                        if ((field.getField() == ManageTypeFields.SUBJECT_MASTERY)
                                || (isMultiple && (field.getField()==ManageTypeFields.CDR
                                || field.getField()== ManageTypeFields.NDG
                                || field.getField()== ManageTypeFields.POSITION_PRACTICE))){
                            continue;
                        }
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
}
