package it.nexera.ris.common.helpers;

import it.nexera.ris.common.comparators.CommercialValueHistoryComparator;
import it.nexera.ris.common.comparators.EstimateOMIComparator;
import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.persistence.beans.dao.CriteriaAlias;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.domain.CategoryFiscalValue;
import it.nexera.ris.persistence.beans.entities.domain.CommercialValueHistory;
import it.nexera.ris.persistence.beans.entities.domain.EstimateOMIHistory;
import it.nexera.ris.persistence.beans.entities.domain.Property;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;
import org.hibernate.sql.JoinType;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class PropertyEntityHelper {

    protected static transient final Log log = LogFactory.getLog(PropertyEntityHelper.class);

    private static List<Property> managePropertyListOMIComm(Property property) {
        if (!property.getCadastralData().isEmpty()) {
            try {
                List<Property> properties = DaoManager.load(Property.class, new CriteriaAlias[]{
                                new CriteriaAlias("cadastralData", "cData", JoinType.INNER_JOIN)},
                        new Criterion[]{
                                Restrictions.eq("province", property.getProvince()),
                                Restrictions.eq("city", property.getCity()),
                                Restrictions.eq("type", property.getType()),
                                Restrictions.eq("category", property.getCategory()),
                                Restrictions.eq("cData.id", property.getCadastralData().stream().findFirst().orElse(null).getId())});

                if (!ValidationHelper.isNullOrEmpty(properties)) {
                    return properties;
                }
            } catch (Exception e) {
                log.error(log, e);
            }
        }
        return new ArrayList<>();
    }

    public static String getEstimateOMIRequestText(Property property) {
        List<EstimateOMIHistory> list = managePropertyListOMIComm(property).stream().map(Property::getEstimateOMIHistory)
                .flatMap(List::stream).sorted(new EstimateOMIComparator()).collect(Collectors.toList());

        return ValidationHelper.isNullOrEmpty(list) ? "" : list.get(0).getEstimateOMI();
    }

    public static String getLastEstimateOMIRequestText(Property property) {
        List<EstimateOMIHistory> list = property.getEstimateOMIHistory();
        return ValidationHelper.isNullOrEmpty(list) ? "" : list.get(list.size()-1).getEstimateOMI();
    }

    public static List<String> getTwoEstimateOMIRequestText(Property property) {
        List<String> result = new LinkedList<>();
        List<EstimateOMIHistory> list = managePropertyListOMIComm(property).stream().map(Property::getEstimateOMIHistory)
                .flatMap(List::stream).sorted(new EstimateOMIComparator()).collect(Collectors.toList());

        if (ValidationHelper.isNullOrEmpty(list)) {
            return result;
        } else {
            if (list.size() != 1) {
                result.add(list.get(1).getEstimateOMI());
            }
            result.add(list.get(0).getEstimateOMI());
        }
        return result;
    }

    public static String getEstimateLastCommercialValueRequestText(Property property) {
        List<CommercialValueHistory> list = managePropertyListOMIComm(property).stream().map(Property::getCommercialValueHistory)
                .flatMap(List::stream).sorted(new CommercialValueHistoryComparator()).collect(Collectors.toList());

        return ValidationHelper.isNullOrEmpty(list) ? "" : list.get(0).getCommercialValue();
    }

    public static String getLastEstimateLastCommercialValueRequestText(Property property) {
        List<CommercialValueHistory> list = property.getCommercialValueHistory();
        return ValidationHelper.isNullOrEmpty(list) ? "" : list.get(list.size()-1).getCommercialValue();
    }

    public static List<Property> getPropertiesByFormalityIdThroughSectionB(Long formalityId)
            throws PersistenceBeanException, IllegalAccessException {
        return DaoManager.load(Property.class, new CriteriaAlias[]{
                new CriteriaAlias("sectionB", "sB", JoinType.INNER_JOIN)
        }, new Criterion[]{
                Restrictions.eq("sB.formality.id", formalityId)
        });
    }

    public static String getFiscalValue(Property property) {

        List<CategoryFiscalValue> categoryFiscalValues = null;
        try {
            categoryFiscalValues = DaoManager.load(CategoryFiscalValue.class, new CriteriaAlias[]{
                    new CriteriaAlias("cadastralCategory", "cC", JoinType.INNER_JOIN)
            }, new Criterion[]{
                    Restrictions.eq("cC.id", property.getCategory().getId())
            });
            if(categoryFiscalValues != null && categoryFiscalValues.size() > 0){
                String revenue = property.getRevenue()
                        .replaceAll("\\.", "").replaceAll("\\,", "\\.");

                return InvoiceHelper.format(categoryFiscalValues.get(0).getValue()
                        * Double.parseDouble(revenue));
            }

        } catch (Exception e) {
            e.printStackTrace();
            log.error(log, e);
        }

        return null;
    }
}
