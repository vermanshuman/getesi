package it.nexera.ris.web.common;

import static it.nexera.ris.common.helpers.TemplatePdfTableHelper.distinctByKey;

import it.nexera.ris.common.enums.DbEnum;
import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.helpers.DateTimeHelper;
import it.nexera.ris.common.helpers.LogHelper;
import it.nexera.ris.common.helpers.ValidationHelper;
import it.nexera.ris.persistence.beans.dao.CriteriaAlias;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.IEntity;
import it.nexera.ris.persistence.beans.entities.domain.EstateFormality;
import it.nexera.ris.persistence.beans.entities.domain.EstateSituation;
import it.nexera.ris.persistence.beans.entities.domain.Request;
import it.nexera.ris.persistence.beans.entities.domain.Subject;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import javax.persistence.Column;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.sql.JoinType;
import org.primefaces.model.LazyDataModel;
import org.hibernate.criterion.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.primefaces.model.SortOrder;
import it.nexera.ris.persistence.beans.entities.domain.Property;

public class WrapperLazyModel<T extends IEntity, T2 extends IEntity> extends LazyDataModel<T> {

    Class<? extends IEntity> clazz;

    private Order[] orders;

    private Criterion[] restrictions;

    private CriteriaAlias[] criteriaAliases;

    private List<String> aliases;

    private int rowIndex = -1;

    private int totalNumRows;

    private List<T> list;

    protected transient final Log log = LogFactory
        .getLog(getClass());

    private boolean calculated;

    private Request request;

    private Subject subject;

    private EstateSituation estateSituation;

    private static final long serialVersionUID = 5148271666683822739L;

    public WrapperLazyModel(Class<? extends IEntity> clazz, Order[] orders) {
        this(clazz, null, orders);
    }

    public WrapperLazyModel(Class<? extends IEntity> clazz,
        Criterion[] restrictions, Order[] orders) {
        this(clazz, restrictions, orders, null);
    }

    public WrapperLazyModel(Class<? extends IEntity> clazz,
        Criterion[] restrictions, Order[] orders,
        CriteriaAlias[] criteriaAliases) {
        super();
        this.clazz = clazz;
        this.orders = orders;
        this.restrictions = restrictions;
        this.criteriaAliases = criteriaAliases;
    }

    public WrapperLazyModel(Class<? extends IEntity> clazz,
        Criterion[] restrictions, Order[] orders,
        CriteriaAlias[] criteriaAliases, List<Long> fromCross) {
        super();
        this.clazz = clazz;
        this.orders = orders;
        this.restrictions = restrictions;
        this.criteriaAliases = criteriaAliases;
    }

    @Override
    public Object getRowKey(T entity) {
        return entity.getId();
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<T> load(int first, int pageSize, String sortField,
        SortOrder sortOrder, Map<String, Object> filters) {
        try {
            this.list = new ArrayList<T>();
            this.list = (List<T>) loadList(first, pageSize, sortField, sortOrder, filters);
            if (this.list != null && !this.list.isEmpty()) {
                if (clazz.equals(Property.class)) {
                    if (!ValidationHelper.isNullOrEmpty(request)) {
                        this.list.stream().filter(p -> !ValidationHelper
                            .isNullOrEmpty(((Property) p).getEstateSituationList()))
                            .forEach(p -> ((Property) p)
                                .setUsed(((Property) p).getEstateSituationList().stream()
                                    .anyMatch(
                                        es -> es.getRequest() != null && es.getRequest().getId()
                                            .equals(request.getId()))));
                        this.list.forEach(p -> {
                            ((Property) p).setCurrentRequest(request);
                            if (request.getAggregationLandChargesRegistry() != null)
                                ((Property) p).setAggregationLandChargedRegistryId(
                                    request.getAggregationLandChargesRegistry().getId());
                        });
                    }
                    if (!ValidationHelper.isNullOrEmpty(subject)) {
                        this.list.forEach(p -> ((Property) p).setCurrentRequest(request));
                    }
                    if(!ValidationHelper.isNullOrEmpty(estateSituation)){
                        this.list.forEach(p -> ((Property)p).setVisible(true));
                    }
                }
                if (clazz.equals(EstateFormality.class)) {
                    if (!ValidationHelper.isNullOrEmpty(request)) {
                        this.list.stream()
                            .filter(ef -> ((EstateFormality) ef).getAccountable() == null)
                            .forEach(ef -> ((EstateFormality) ef).setAccountable(true));
                        this.list.stream()
                            .filter(ef -> ((EstateFormality) ef).getEstateSituationList() != null)
                            .forEach(ef -> ((EstateFormality) ef)
                                .setUsed(((EstateFormality) ef).getEstateSituationList().stream()
                                    .filter(es -> es.getRequest() != null)
                                    .filter(es -> request.getId().equals(es.getRequest().getId()))
                                    .count() > 0));
                        this.list = this.list.stream().filter(
                            (distinctByKey(
                                x -> (((EstateFormality) x).getNumRG() + "-" + ((EstateFormality) x)
                                    .getNumRP() + "-" + ((EstateFormality) x).getDate() + "-"
                                    + ((EstateFormality) x).getLandChargesRegistry())))).collect(
                            Collectors.toList());

                        List<EstateFormality> estateFormalitiesUpdated = DaoManager
                            .load(EstateFormality.class, new CriteriaAlias[]{
                                new CriteriaAlias("requestListUpdate", "request",
                                    JoinType.INNER_JOIN)
                            }, new Criterion[]{
                                Restrictions.eq("request.id", request.getId())
                            });

                        if (!ValidationHelper.isNullOrEmpty(estateFormalitiesUpdated)) {
                            this.list.forEach(ef -> ((EstateFormality) ef).setUpdated(
                                estateFormalitiesUpdated.contains((EstateFormality) ef)));
                        }
                    }
                    if(!ValidationHelper.isNullOrEmpty(estateSituation)){
                        this.list.forEach(es -> ((EstateFormality)es).setVisible(true));
                    }
                }
            }
            this.aliases = new ArrayList<String>();
            Criteria countCriteria = DaoManager.getSession().createCriteria(
                clazz);

            countCriteria.setProjection(Projections.rowCount());

            for (Criterion criterion : getCriterion(filters, countCriteria)) {
                countCriteria.add(criterion);
            }

            if (restrictions != null) {
                for (Criterion criterion : restrictions) {
                    countCriteria.add(criterion);
                }
            }

            if (criteriaAliases != null) {
                for (CriteriaAlias ca : criteriaAliases) {
                    countCriteria.createAlias(ca.getTable(), ca.getAliasName(),
                        ca.getJoinType());
                }
            }
            Long rowCount = (Long) countCriteria.uniqueResult();

            this.setRowCount(rowCount == null ? 0 : rowCount.intValue());
        } catch (Exception e) {
            LogHelper.log(log, e);
        }

        this.calculated = true;
        return list;
    }

    @SuppressWarnings("unchecked")
    private List<T2> loadList(Integer first, Integer pageSize, String sortField,
        SortOrder sortOrder, Map<String, Object> filters)
        throws PersistenceBeanException, IllegalAccessException {
        Criteria criteria = DaoManager.getSession().createCriteria(clazz);
        if (pageSize != null) {
            criteria.setMaxResults(pageSize);
        }
        if (first != null) {
            criteria.setFirstResult(first);
        }
        if (criteriaAliases != null) {
            for (CriteriaAlias ca : criteriaAliases) {
                criteria.createAlias(ca.getTable(), ca.getAliasName(),
                    ca.getJoinType());
            }
        }

        this.aliases = new ArrayList<String>();

        for (Criterion criterion : getCriterion(filters, criteria)) {
            criteria.add(criterion);
        }

        if (restrictions != null) {
            for (Criterion criterion : restrictions) {
                criteria.add(criterion);
            }
        }

        if (sortField == null) {
            if(!ValidationHelper.isNullOrEmpty(this.orders)) {
                for (Order order : this.orders) {
                    criteria.addOrder(order);
                }
            }
        } else {
            if (sortOrder.equals(SortOrder.ASCENDING)) {
                criteria.addOrder(Order.asc(this.getCorrectFieldName(sortField,
                    criteria)));
            } else {
                criteria.addOrder(Order.desc(this.getCorrectFieldName(
                    sortField, criteria)));
            }
        }

        criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
        return criteria.list();
    }

    /* (non-Javadoc)
     * @see org.primefaces.model.LazyDataModel#setRowCount(int)
     */
    @Override
    public void setRowCount(int rowCount) {
        this.totalNumRows = rowCount;
        super.setRowCount(rowCount);
    }

    private String getCorrectFieldName(String oldFieldName, Criteria criteria)
        throws HibernateException, PersistenceBeanException {
        if (!oldFieldName.contains(".")) {
            return oldFieldName;
        }
        String fieldName = oldFieldName;
        if (oldFieldName.replace(".strId", ".id").endsWith(".id")) {
            fieldName = oldFieldName.replace(".strId", ".id");
        }
        if (oldFieldName.contains("[0]")) {
            fieldName = oldFieldName.replace("[0]", "");
        }
        String tmp = fieldName;
        int aliasCtr = 0;
        while (tmp.contains(".")
            && !tmp.substring(tmp.indexOf('.')).equals(".id")) {
            aliasCtr++;
            tmp = tmp.substring(tmp.indexOf('.') + 1);
        }
        if (aliasCtr > 0) {
            String aliasField = '.' + tmp;
            String alias = "";
            String table = "";
            String field = "";
            for (int i = 0; i < aliasCtr; i++) {
                if (field.length() > 0) {
                    field = fieldName.substring(
                        field.length() + 1,
                        field.length()
                            + fieldName.substring(field.length() + 1)
                            .indexOf('.') + 1);
                    table = alias + '.' + field;
                } else {
                    table = field = fieldName.substring(0,
                        fieldName.indexOf('.'));
                }
                if (alias.length() > 0) {
                    alias += "_alias_" + field;
                } else {
                    alias = "alias_" + field;
                }

                if (!this.aliases.contains(alias)) {
                    criteria.createAlias(table, alias, JoinType.INNER_JOIN);
                    this.aliases.add(alias);
                }
            }
            fieldName = alias + aliasField;
        }
        return fieldName;
    }

    private Criterion[] getCriterion(Map<String, Object> filters,
        Criteria criteria) throws HibernateException,
        PersistenceBeanException {
        List<Criterion> cr = new ArrayList<Criterion>();
        for (Entry<String, Object> set : filters.entrySet()) {
            String fieldName = getCorrectFieldName(set.getKey(), criteria);
            if (fieldName.endsWith(".id")) {
                cr.add(Restrictions.eq(fieldName,
                    Long.parseLong(set.getValue().toString())));
            } else {
                boolean criterionAdded = false;
                try {
                    Class<?> fieldType = clazz.getDeclaredField(fieldName)
                        .getType();
                    if (fieldType.equals(Long.class)
                        || fieldType.equals(long.class)) {
                        criterionAdded = true;
                        cr.add(Restrictions.eq(fieldName,
                            Long.parseLong(set.getValue().toString())));
                    } else if (fieldType.equals(Integer.class)
                        || fieldType.equals(int.class)) {
                        criterionAdded = true;
                        cr.add(Restrictions.eq(fieldName,
                            Integer.parseInt(set.getValue().toString())));
                    } else if (fieldType.equals(Boolean.class)
                        || fieldType.equals(boolean.class)) {
                        criterionAdded = true;
                        cr.add(Restrictions.eq(fieldName, (set.getValue()
                            .equals("true")) ? Boolean.TRUE : Boolean.FALSE));
                    } else if (fieldType.equals(Date.class)) {
                        criterionAdded = true;
                        cr.add(Restrictions.sqlRestriction("to_char("
                            + (ValidationHelper.isNullOrEmpty(clazz
                            .getDeclaredField(fieldName)
                            .getAnnotation(Column.class).name()) ? fieldName
                            : clazz.getDeclaredField(fieldName)
                                .getAnnotation(Column.class)
                                .name()) + ", '"
                            + DateTimeHelper.getDatePattern() + "') like '"
                            + set.getValue() + "%'"));
                    } else if (fieldType.isEnum()) {
                        boolean checkInstanceOf = false;
                        for (Object o : fieldType.getEnumConstants()) {
                            if (!checkInstanceOf) {
                                checkInstanceOf = o instanceof DbEnum;
                                if (!checkInstanceOf) {
                                    break;
                                }
                            }
                            if (((DbEnum) o).getRealName().equals(
                                set.getValue())) {
                                cr.add(Restrictions.eq(fieldName,
                                    ((DbEnum) o).getRealObject()));
                                criterionAdded = true;
                                break;
                            }
                        }
                    }
                } catch (SecurityException e) {
                    LogHelper.log(log, e);
                } catch (NumberFormatException e) {
                    cr.add(Restrictions.eq("id", -1l));
                } catch (NoSuchFieldException e) {
                }


                if (!criterionAdded) {
                    if(fieldName.endsWith("_exact")) {
                        String name = fieldName.substring(0,fieldName.lastIndexOf("_exact"));
                        cr.add(Restrictions.like(name,
                            set.getValue().toString(), MatchMode.EXACT)
                            .ignoreCase());
                    } else {
                        cr.add(Restrictions.like(fieldName,
                            set.getValue().toString(), MatchMode.ANYWHERE)
                            .ignoreCase());
                    }
                }
            }
        }
        return cr.toArray(new Criterion[0]);
    }

    @Override
    public boolean isRowAvailable() {
        if (list == null)
            return false;

        int rowIndex = getRowIndex();
        if (rowIndex >= 0 && rowIndex < list.size())
            return true;
        else
            return false;
    }

    public int getRowCount() {
        if (!this.calculated) {
            try {
                if (restrictions == null) {
                    this.totalNumRows = DaoManager.getCount(this.clazz, "id")
                        .intValue();
                } else {
                    if (criteriaAliases != null && criteriaAliases.length != 0) {
                        this.totalNumRows = DaoManager.getCount(this.clazz,
                            "id", criteriaAliases, restrictions).intValue();
                    } else {
                        this.totalNumRows = DaoManager.getCount(this.clazz,
                            "id", restrictions).intValue();
                    }
                }
            } catch (Exception e) {
                LogHelper.log(log, e);
            }
        }
        return this.totalNumRows;
    }

    @Override
    public T getRowData() {
        if (list == null) {
            return null;
        } else if (!isRowAvailable()) {
            throw new IllegalArgumentException();
        } else {
            int dataIndex = getRowIndex();

            return list.get(dataIndex);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public T getRowData(String rowKey) {
        try {
            return (T) DaoManager.get(clazz, Long.parseLong(rowKey));
        } catch (Exception e) {
            LogHelper.log(log, e);
        }
        return null;
    }

    public int getRowIndex() {
        if (getPageSize() != 0) {
            return (rowIndex % getPageSize());
        } else {
            return 0;
        }
    }

    @Override
    public void setRowIndex(int rowIndex) {
        this.rowIndex = rowIndex;
    }

    @Override
    public Object getWrappedData() {
        return list;
    }

    @Override
    @SuppressWarnings({
        "rawtypes", "unchecked"
    })
    public void setWrappedData(Object list) {
        this.list = (List) list;
    }

    public Request getRequest() {
        return request;
    }

    public void setRequest(Request request) {
        this.request = request;
    }

    public Subject getSubject() {
        return subject;
    }

    public void setSubject(Subject subject) {
        this.subject = subject;
    }

    public EstateSituation getEstateSituation() {
        return estateSituation;
    }

    public void setEstateSituation(
        EstateSituation estateSituation) {
        this.estateSituation = estateSituation;
    }
}
