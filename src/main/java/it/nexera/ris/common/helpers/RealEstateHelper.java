package it.nexera.ris.common.helpers;

import it.nexera.ris.common.enums.RelationshipType;
import it.nexera.ris.persistence.beans.dao.ConnectionManager;
import it.nexera.ris.persistence.beans.dao.CriteriaAlias;
import it.nexera.ris.persistence.beans.entities.domain.CadastralData;
import it.nexera.ris.persistence.beans.entities.domain.CommercialValueHistory;
import it.nexera.ris.persistence.beans.entities.domain.EstimateOMIHistory;
import it.nexera.ris.persistence.beans.entities.domain.Property;
import it.nexera.ris.persistence.beans.entities.domain.Relationship;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.City;
import it.nexera.ris.web.beans.wrappers.logic.SubjectRelationshipWrapper;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;
import org.hibernate.sql.JoinType;
import org.hibernate.type.LongType;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.persistence.PersistenceException;

public class RealEstateHelper {

    public static Property getExistingPropertyByKey(Session session, Property baseProperty, Long typeId,
                                                    Long provinceId, Long cityId)
            throws IllegalAccessException, InstantiationException {
        List<Criterion> restrictions = new ArrayList<>();
        restrictions.add(Restrictions.eq("type", typeId));
        restrictions.add(Restrictions.eq("c.description",
                ConnectionManager.get(City.class, cityId, session).getDescription()));
        restrictions.add(Restrictions.eq("province.id", provinceId));
        if (ValidationHelper.isNullOrEmpty(baseProperty.getFloor())) {
            restrictions.add(Restrictions.or(
                    Restrictions.isNull("floor"),
                    Restrictions.eq("floor", "")
            ));
        } else {
            restrictions.add(Restrictions.like("floor", baseProperty.getFloor()));
        }
        if (ValidationHelper.isNullOrEmpty(baseProperty.getInterno())) {
            restrictions.add(Restrictions.or(
                    Restrictions.isNull("interno"),
                    Restrictions.eq("interno", "")
            ));
        } else {
            restrictions.add(Restrictions.like("interno", baseProperty.getInterno()));
        }
        if (ValidationHelper.isNullOrEmpty(baseProperty.getScala())) {
            restrictions.add(Restrictions.or(
                    Restrictions.isNull("scala"),
                    Restrictions.eq("scala", "")
            ));
        } else {
            restrictions.add(Restrictions.like("scala", baseProperty.getScala()));
        }
        if (ValidationHelper.isNullOrEmpty(baseProperty.getAddress())) {
            restrictions.add(Restrictions.or(
                    Restrictions.isNull("address"),
                    Restrictions.eq("address", "")
            ));
        } else {
            restrictions.add(Restrictions.like("address", baseProperty.getAddress()));
        }
        if (ValidationHelper.isNullOrEmpty(baseProperty.getConsistency())) {
            restrictions.add(Restrictions.or(
                    Restrictions.isNull("consistency"),
                    Restrictions.eq("consistency", "")
            ));
        } else {
            restrictions.add(Restrictions.like("consistency", baseProperty.getConsistency()));
        }
        if (ValidationHelper.isNullOrEmpty(baseProperty.getHectares())) {
            restrictions.add(Restrictions.isNull("hectares"));
        } else {
            restrictions.add(Restrictions.eq("hectares", baseProperty.getHectares()));
        }
        if (ValidationHelper.isNullOrEmpty(baseProperty.getAres())) {
            restrictions.add(Restrictions.isNull("ares"));
        } else {
            restrictions.add(Restrictions.eq("ares", baseProperty.getAres()));
        }
        if (ValidationHelper.isNullOrEmpty(baseProperty.getCentiares())) {
            restrictions.add(Restrictions.isNull("centiares"));
        } else {
            restrictions.add(Restrictions.eq("centiares", baseProperty.getCentiares()));
        }

        return ConnectionManager.get(Property.class, new CriteriaAlias[]{
                new CriteriaAlias("city", "c", JoinType.INNER_JOIN)
        }, restrictions.toArray(new Criterion[0]), session);
    }


    @SuppressWarnings("unchecked")
    public static Property getExistingPropertyByCD(List<CadastralData> cadastralDataList, Session session) {
        List<Long> propertyList = getExistingPropertiesIdsByCD(cadastralDataList, session, false);
        return findProperty(cadastralDataList, session, propertyList);
    }

    @SuppressWarnings("unchecked")
    public static Property getExistingPropertyByCDAndPropertyFields(List<CadastralData> cadastralDataList, Property currentProperty, Session session) {
        List<Long> propertyList = getExistingPropertiesIdsByCDAndPropertyFields(cadastralDataList, currentProperty, session);
        return findProperty(cadastralDataList, session, propertyList);
    }

    private static Property findProperty(List<CadastralData> cadastralDataList, Session session, List<Long> propertyList) {
        Optional<CadastralData> cadastralDataWithSameSchedaData = cadastralDataList.stream()
                .filter(x -> !ValidationHelper.isNullOrEmpty(x.getScheda())
                        || !ValidationHelper.isNullOrEmpty(x.getDataScheda())).findAny();

        if (!ValidationHelper.isNullOrEmpty(propertyList)) {
            for (Property property : ConnectionManager.load(Property.class, new Criterion[]{
                    Restrictions.in("id", propertyList)
            }, session)) {
                boolean equals = false;
                for (CadastralData data : property.getCadastralData()) {
                    if (property.getCadastralData().size() != cadastralDataList.size()) {
                        continue;
                    }
                    equals = cadastralDataList.stream().anyMatch(d ->
                            (
                                    (ValidationHelper.isNullOrEmpty(d.getSection())
                                            && ValidationHelper.isNullOrEmpty(data.getSection()))
                                            ||	(!ValidationHelper.isNullOrEmpty(d.getSection())
                                                    && !ValidationHelper.isNullOrEmpty(data.getSection())
                                                    && d.getSection().equals(data.getSection())))
                                    && ((ValidationHelper.isNullOrEmpty(d.getSheet())
                                            && ValidationHelper.isNullOrEmpty(data.getSheet()))
                                            || d.getSheet().equals(data.getSheet()))
                                    && ((ValidationHelper.isNullOrEmpty(d.getParticle())
                                            && ValidationHelper.isNullOrEmpty(data.getParticle()))
                                            || d.getParticle().equals(data.getParticle()))
                                    && ((ValidationHelper.isNullOrEmpty(d.getSub())
                                            && ValidationHelper.isNullOrEmpty(data.getSub()))
                                            || d.getSub().equals(data.getSub())));

                    if (equals && cadastralDataWithSameSchedaData.isPresent()) {
                        equals = cadastralDataList.stream().anyMatch(d ->
                                (
                                        (ValidationHelper.isNullOrEmpty(d.getScheda())
                                                && ValidationHelper.isNullOrEmpty(data.getScheda()))
                                                || d.getScheda().equals(data.getScheda()))
                                        && ((ValidationHelper.isNullOrEmpty(d.getDataScheda())
                                                && ValidationHelper.isNullOrEmpty(data.getDataScheda()))
                                                || d.getDataScheda().equals(data.getDataScheda())));
                    }
                }
                if (equals) {
                    return property;
                }
            }
        }
        return null;
    }

    private static StringBuffer formExistingPropertiesIdsByCDQuery(
            List<CadastralData> cadastralDataList, Session session, boolean flag) {
        boolean hasNull;
        boolean hasNotNull;
        StringBuffer sb = new StringBuffer();
        sb.append("select p.id from property p inner join cadastral_property cp on p.id = cp.property_id " +
                "  inner join cadastral_data cd on cp.cadastral_data_id = cd.id where ");
        if (flag) {
            sb.append(" modified is null and ");
        }
        cadastralPart(cadastralDataList, sb);
        return sb;
    }

    private static StringBuffer formExistingPropertiesIdsByCDAndPropertyFieldsQuery(
            List<CadastralData> cadastralDataList, Property property) {
        StringBuffer sb = new StringBuffer();
        sb.append("select p.id from property p inner join cadastral_property cp on p.id = cp.property_id " +
                "  inner join cadastral_data cd on cp.cadastral_data_id = cd.id where ");

        cadastralPart(cadastralDataList, sb);
        sb.append(" and ");
        if (property.getCategory() == null) {
            sb.append(" p.cadastral_category_id is null ");
        } else {
            sb.append(" p.cadastral_category_id = ").append(property.getCategory().getId());
        }
        sb.append(" and ");
        if (ValidationHelper.isNullOrEmpty(property.getConsistency())) {
            sb.append(" p.consistency is null ");
        } else {
            sb.append(" p.consistency = \"").append(property.getConsistency()).append("\" ");
        }
        sb.append(" and ");
        if (ValidationHelper.isNullOrEmpty(property.getCadastralArea())) {
            sb.append(" p.cadastral_area is null ");
        } else {
            sb.append(" p.cadastral_area = \"").append(property.getCadastralArea()).append("\" ");
        }
        sb.append(" and ");
        if (ValidationHelper.isNullOrEmpty(property.getRevenue())) {
            sb.append(" p.revenue is null ");
        } else {
            sb.append(" p.revenue = \"").append(property.getRevenue()).append("\" ");
        }
        sb.append(" and ");
        if (ValidationHelper.isNullOrEmpty(property.getAddress())) {
            sb.append(" p.address is null ");
        } else {
            sb.append(" p.address = \"").append(property.getAddress()).append("\" ");
        }
        sb.append(" and ");
        if (ValidationHelper.isNullOrEmpty(property.getFloor())) {
            sb.append(" p.floor is null ");
        } else {
            sb.append(" p.floor = \"").append(property.getFloor()).append("\" ");
        }
        sb.append(" and ");
        if (ValidationHelper.isNullOrEmpty(property.getInterno())) {
            sb.append(" p.interno is null ");
        } else {
            sb.append(" p.interno = \"").append(property.getInterno()).append("\" ");
        }
        sb.append(" and ");
        if (ValidationHelper.isNullOrEmpty(property.getHectares())) {
            sb.append(" p.hectares is null ");
        } else {
            sb.append(" p.hectares = \"").append(property.getHectares()).append("\" ");
        }
        sb.append(" and ");
        if (ValidationHelper.isNullOrEmpty(property.getAres())) {
            sb.append(" p.ares is null ");
        } else {
            sb.append(" p.ares = \"").append(property.getAres()).append("\" ");
        }
        sb.append(" and ");
        if (ValidationHelper.isNullOrEmpty(property.getCentiares())) {
            sb.append(" p.centiares is null ");
        } else {
            sb.append(" p.centiares = \"").append(property.getCentiares()).append("\" ");
        }
        return sb;
    }

    private static void cadastralPart(List<CadastralData> cadastralDataList, StringBuffer sb) {
        boolean hasNull;
        boolean hasNotNull;
        sb.append("(");
        hasNull = cadastralDataList.stream().map(CadastralData::getSection).anyMatch(Objects::isNull);
        hasNotNull = cadastralDataList.stream().map(CadastralData::getSection).anyMatch(Objects::nonNull);
        if (hasNull) {
            sb.append("section is null ");
            sb.append(" or ");
        }
        if (hasNotNull) {
            sb.append(" section in ");
            sb.append(cadastralDataList.stream().map(CadastralData::getSection).filter(Objects::nonNull).distinct()
                    .map(s -> "'" + s + "'").collect(Collectors.joining(",", "('', ", ")")));
        } else {
            sb.append(" section = '' ");
        }
        sb.append(")");
        sb.append(" and ");
        sb.append("(");
        hasNull = cadastralDataList.stream().map(CadastralData::getSheet).anyMatch(Objects::isNull);
        hasNotNull = cadastralDataList.stream().map(CadastralData::getSheet).anyMatch(Objects::nonNull);
        if (hasNull) {
            sb.append("sheet is null ");
            sb.append(" or ");
        }
        if (hasNotNull) {
            sb.append(" sheet in ");
            sb.append(cadastralDataList.stream().map(CadastralData::getSheet).filter(Objects::nonNull).distinct()
                    .map(s -> "'" + s + "'").collect(Collectors.joining(",", "('', ", ")")));
        } else {
            sb.append(" sheet = '' ");
        }
        sb.append(")");
        sb.append(" and ");
        sb.append("(");
        hasNull = cadastralDataList.stream().map(CadastralData::getParticle).anyMatch(Objects::isNull);
        hasNotNull = cadastralDataList.stream().map(CadastralData::getParticle).anyMatch(Objects::nonNull);
        if (hasNull) {
            sb.append("particle is null ");
            sb.append(" or ");
        }
        if (hasNotNull) {
            sb.append(" particle in ");
            sb.append(cadastralDataList.stream().map(CadastralData::getParticle).filter(Objects::nonNull).distinct()
                    .map(s -> "'" + s + "'").collect(Collectors.joining(",", "('', ", ")")));
        } else {
            sb.append(" particle = '' ");
        }
        sb.append(")");
        sb.append(" and ");
        sb.append("(");
        hasNull = cadastralDataList.stream().map(CadastralData::getSub).anyMatch(Objects::isNull);
        hasNotNull = cadastralDataList.stream().map(CadastralData::getSub).anyMatch(Objects::nonNull);
        if (hasNull) {
            sb.append("sub is null ");
            sb.append(" or ");
        }
        if (hasNotNull) {
            sb.append(" sub in ");
            sb.append(cadastralDataList.stream().map(CadastralData::getSub).filter(Objects::nonNull).distinct()
                    .map(s -> "'" + s + "'").collect(Collectors.joining(",", "('', ", ")")));
        } else {
            sb.append(" sub = '' ");
        }
        sb.append(")");
    }


    public static List<Long> getExistingPropertiesIdsByCD(List<CadastralData> cadastralDataList, Session session,
                                                          boolean flag) {
        StringBuffer sb = formExistingPropertiesIdsByCDQuery(cadastralDataList, session, flag);
        return getLongs(session, sb);
    }

    public static List<Long> getExistingPropertiesIdsByCDAndPropertyFields(List<CadastralData> cadastralDataList,
                                                                           Property property, Session session) {
        StringBuffer sb = formExistingPropertiesIdsByCDAndPropertyFieldsQuery(cadastralDataList, property);
        return getLongs(session, sb);
    }

    private static List<Long> getLongs(Session session, StringBuffer sb) {
    	String query = sb.toString();
    	if(!ValidationHelper.isNullOrEmpty(query)) {
    		query = query.replaceAll(":", "\\\\:");
    	}
        List<BigInteger> propertyList = session.createSQLQuery(query).list();
        List<Long> resultPropertyList = new ArrayList<>();
        for (BigInteger bigInteger : propertyList) {
            resultPropertyList.add(bigInteger.longValue());
        }
        return resultPropertyList;
    }

    public static boolean propertyChanged(Property propertyCD, Property propertyInWork) {
        if (!Objects.equals(propertyCD.getType(), propertyInWork.getType())) {
            return true;
        }
        if ((propertyCD.getCity() != null && propertyInWork.getCity() != null
                && !Objects.equals(propertyCD.getCity().getDescription(), propertyInWork.getCity().getDescription()))
                || (propertyCD.getCity() == null && propertyInWork.getCity() != null)
                || (propertyCD.getCity() != null && propertyInWork.getCity() == null)) {
            return true;
        }
        if (!Objects.equals(propertyCD.getProvince(), propertyInWork.getProvince())) {
            return true;
        }
        if (!Objects.equals(propertyCD.getFloor(), propertyInWork.getFloor())) {
            return true;
        }
        if (!Objects.equals(propertyCD.getInterno(), propertyInWork.getInterno())) {
            return true;
        }
        if (!Objects.equals(propertyCD.getScala(), propertyInWork.getScala())) {
            return true;
        }
        if (!Objects.equals(propertyCD.getAddress(), propertyInWork.getAddress())) {
            return true;
        }
        if (!Objects.equals(propertyCD.getConsistency(), propertyInWork.getConsistency())) {
            return true;
        }
        if (!Objects.equals(propertyCD.getHectares(), propertyInWork.getHectares())) {
            return true;
        }
        if (!Objects.equals(propertyCD.getAres(), propertyInWork.getAres())) {
            return true;
        }
        if (!Objects.equals(propertyCD.getCentiares(), propertyInWork.getCentiares())) {
            return true;
        }
        return false;
    }

    public static Property modifyExistingProperty(Property propertyDB, Property propertyView, Session session) {
        if (propertyDB != null) {
            propertyDB.setAddress(propertyView.getAddress());
            propertyDB.setFloor(propertyView.getFloor());
            propertyDB.setConsistency(propertyView.getConsistency());
            propertyDB.setRevenue(propertyView.getRevenue());
            propertyDB.setHectares(propertyView.getHectares());
            propertyDB.setAres(propertyView.getAres());
            propertyDB.setCentiares(propertyView.getCentiares());
            propertyDB.setNumberOfRooms(propertyView.getNumberOfRooms());
            propertyDB.setComment(propertyView.getComment());
            propertyDB.setCadastralArea(propertyView.getCadastralArea());
            propertyDB.setInterno(propertyView.getInterno());
            propertyDB.setExclusedArea(propertyView.getExclusedArea());
            if (!ValidationHelper.isNullOrEmpty(propertyDB.getRelationships())) {
                for (Relationship relationship : propertyDB.getRelationships()) {
                    if (RelationshipType.MANUAL_ENTRY.getId().equals(relationship.getRelationshipTypeId())) {
                        ConnectionManager.remove(relationship, session);
                    }
                }
            }
        }
        return propertyDB;
    }

    public static void saveCadastralData(Property property, List<CadastralData> cadastralDataList,
                                         List<CadastralData> cadastralDataToDelete, Session session)
            throws HibernateException, IllegalAccessException, InstantiationException {
        List<CadastralData> newList = new ArrayList<>(cadastralDataList.size());
        for (CadastralData data : cadastralDataList) {
            data = data.loadOrCopy(session);
            if (data.getPropertyList() == null) {
                data.setPropertyList(new ArrayList<>());
            }
            data.getPropertyList().add(property);
            ConnectionManager.save(data, session);
            newList.add(data);
        }

        property.setCadastralData(newList.stream().distinct().collect(Collectors.toList()));

        if (cadastralDataToDelete != null) {
            for (CadastralData data : cadastralDataToDelete) {
                if (data.getId() != null) {
                    ConnectionManager.remove(data, session);
                }
            }
        }
    }

    public static void saveHistory(Property property, List<EstimateOMIHistory> estimateOMIHistoryList,
                                   List<CommercialValueHistory> commercialValueHistoryList, Session session) {
        if (!ValidationHelper.isNullOrEmpty(estimateOMIHistoryList)) {
            for (EstimateOMIHistory h : estimateOMIHistoryList) {
                if (h.isNew()) {
                    EstimateOMIHistory history = h.copy();
                    history.setProperty(property);
                    ConnectionManager.save(history, session);
                }
            }
        }

        if (!ValidationHelper.isNullOrEmpty(commercialValueHistoryList)) {
            for (CommercialValueHistory h : commercialValueHistoryList) {
                if (h.isNew()) {
                    CommercialValueHistory history = h.copy();
                    history.setProperty(property);
                    ConnectionManager.save(history, session);
                }
            }
        }
    }

    public static void saveRelationships(Property property, List<SubjectRelationshipWrapper> subjectWrapperList,
                                         List<SubjectRelationshipWrapper> subjectWrapperToDeleteList, Session session) throws HibernateException, PersistenceException, InstantiationException, IllegalAccessException {
        for (SubjectRelationshipWrapper wrapper : subjectWrapperList) {
            
            wrapper.getRelationship().setQuote(wrapper.getQuote());
            wrapper.getRelationship().setPropertyType(wrapper.getPropertyTypeStr());
            wrapper.getRelationship().setProperty(property);

            if (wrapper.getRelationship().getRelationshipTypeId() == null) {
                wrapper.getRelationship().setRelationshipTypeId(RelationshipType.MANUAL_ENTRY.getId());
            }
            
            if(wrapper.getRelationship().getId() != null && wrapper.getRelationship().getId() > 0L) {
                Relationship relationship = ConnectionManager.get(Relationship.class, new Criterion[]{
                        Restrictions.eq("id", wrapper.getRelationship().getId()),
                        Restrictions.eq("property", property)
                }, session); 
                
                if(relationship == null || relationship.getId() < 0L) {
                    relationship = wrapper.getRelationship();
                    Relationship newRelation = new Relationship();
                    newRelation.setProperty(property);
                    newRelation.setSubject(relationship.getSubject());
                    if (relationship.getRelationshipTypeId() == null) {
                        newRelation.setRelationshipTypeId(RelationshipType.MANUAL_ENTRY.getId());
                    }else {
                        newRelation.setRelationshipTypeId(relationship.getRelationshipTypeId());
                    }
                    newRelation.setQuote(relationship.getQuote());
                    newRelation.setPropertyType(relationship.getPropertyType());
                    newRelation.setProperty(relationship.getProperty());
                    wrapper.setRelationship(newRelation);
                }
            }
            ConnectionManager.save(wrapper.getRelationship(), session);

        }

        for (SubjectRelationshipWrapper sw : subjectWrapperToDeleteList) {
            if (!sw.getRelationship().isNew()) {
                ConnectionManager.remove(sw.getRelationship(), session);
            }
        }
    }

    public static List<Property> getCadastralDatesEqualsProperties(Property property, Session session) {
        List<Property> propertyCadastralDatesEquals = new ArrayList<>();

        List<Long> longList = RealEstateHelper.getExistingPropertiesIdsByCD(property.getCadastralData(),
                session, false);

        if (!ValidationHelper.isNullOrEmpty(longList)) {
            for (Property prop : ConnectionManager.load(Property.class, new Criterion[]{
                    Restrictions.in("id", longList)
            }, session)) {
                boolean equals = false;
                for (CadastralData data : prop.getCadastralData()) {
                    if (prop.getCadastralData().size() != property.getCadastralData().size()) {
                        continue;
                    }
                    equals = property.getCadastralData().stream().anyMatch(d ->
                            ((ValidationHelper.isNullOrEmpty(d.getId()) && ValidationHelper.isNullOrEmpty(data.getId()))
                                    || d.getId().equals(data.getId())));
                }
                if (equals) {
                    propertyCadastralDatesEquals.add(prop);
                }
            }
        }
        return propertyCadastralDatesEquals;
    }

    public static List<Long> getCadastralDatesEqualsPropertiesIds(Property property, Session session) {
        List<Long> result;
        StringBuffer sb = new StringBuffer();
        sb.append("select property_id as num "
                + "from (" + formExistingPropertiesIdsByCDQuery(property.getCadastralData(), session, false) + ") tmp "
                + "inner join cadastral_property cp on tmp.id = cp.property_id "
                + "where cp.cadastral_data_id IN"
                + "(select cadastral_data_id from cadastral_property where property_id = " + property.getId() + ")");
        result = session.createSQLQuery(sb.toString()).addScalar("num", LongType.INSTANCE).list();
        if (result.contains(property.getId())) {
            result.remove(property.getId());
        }
        return result;
    }
}
