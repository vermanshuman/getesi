/**
 *
 */
package it.nexera.ris.persistence.beans.dao;

import it.nexera.ris.common.helpers.BaseHelper;
import it.nexera.ris.common.helpers.LogHelper;
import it.nexera.ris.persistence.beans.entities.Entity;
import it.nexera.ris.persistence.beans.entities.IEntity;
import it.nexera.ris.persistence.interfaces.BeforeSave;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.*;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.SimpleExpression;
import org.hibernate.sql.JoinType;

import javax.persistence.PersistenceException;
import java.io.Serializable;
import java.lang.InstantiationException;
import java.util.Date;
import java.util.List;

/**
 * Helper class for working with database outside of the request context
 */
public class ConnectionManager {

    protected static transient final Log log = LogFactory
            .getLog(BaseHelper.class);
    /**
     * @param query
     * @param objs
     * @param session
     * @return
     * @throws HibernateException
     * @throws PersistenceException
     */
    public static List<?> find(String query, Object[] objs, Session session)
            throws HibernateException, PersistenceException {
        Query queryObject = session.createQuery(query);

        if (objs != null) {
            for (int i = 0; i < objs.length; i++) {
                queryObject.setParameter(i, objs[i]);
            }
        }

        return queryObject.list();
    }

    /**
     * @param object
     * @param session
     * @param currentUser
     * @throws HibernateException
     * @throws PersistenceException
     */
    public static void save(Entity object, Long currentUser, Session session)
            throws HibernateException, PersistenceException {
        save(object, currentUser, false, session);
    }

    public static Long getCount(Class<? extends IEntity> clazz, String field, Session session) {
        return getCount(clazz, field, new CriteriaAlias[]{}, new Criterion[]{}, session);
    }

    public static Long getCount(Class<? extends IEntity> clazz, String field, Criterion[] criterions, Session session) {
        return getCount(clazz, field, new CriteriaAlias[]{}, criterions, session);
    }

    public static Long getCount(Class<? extends IEntity> clazz, String field,
                                CriteriaAlias[] criteriaAlias, Criterion[] criterions, Session session) {
        Criteria criteria = session.createCriteria(clazz);
        criteria.setProjection(Projections.countDistinct(field));
        for (Criterion criterion : criterions) {
            criteria.add(criterion);
        }

        for (CriteriaAlias ca : criteriaAlias) {
            criteria.createAlias(ca.getTable(), ca.getAliasName(),
                    ca.getJoinType());
        }
        Object result = criteria.uniqueResult();
        long resultLong = 0L;
        try {
            resultLong = Long.parseLong(String.valueOf(result));

        } catch (NumberFormatException e) {
            LogHelper.log(log, e);
        }
        return resultLong;
    }

    /**
     * @param object
     * @param session
     * @throws HibernateException
     * @throws PersistenceException
     */
    public static void save(Entity object, Session session)
            throws HibernateException, PersistenceException {
        save(object, 0l, session);
    }

    /**
     * @param object
     * @param session
     * @param currentUserId
     * @throws HibernateException
     * @throws PersistenceException
     */
    public static void save(Entity object, Long currentUserId,
                              boolean startTransaction, Session session)
            throws HibernateException, PersistenceException {
        Transaction tr = null;
        try {
            if (startTransaction) {
                tr = session.beginTransaction();
            }
            beforeSaveOrUpdate(object, currentUserId);

            if (session.contains(object)) {
                session.merge(object);
            } else {
                session.saveOrUpdate(object);
            }
        } catch (Exception e) {
            if (startTransaction && tr != null && tr.isActive()) {
                tr.rollback();
            }
        } finally {
            if (startTransaction && tr != null && tr.isActive()
                    && !tr.wasRolledBack()) {
                tr.commit();
            }
        }
    }

    public static void saveObject(Object object, boolean startTransaction, Session session)
            throws HibernateException, PersistenceException {
        Transaction tr = null;
        try {
            if (startTransaction) {
                tr = session.beginTransaction();
            }

            if (session.contains(object)) {
                session.merge(object);
            } else {
                session.saveOrUpdate(object);
            }
        } catch (Exception e) {
            if (startTransaction && tr != null && tr.isActive()) {
                tr.rollback();
            }
        } finally {
            if (startTransaction && tr != null && tr.isActive()
                    && !tr.wasRolledBack()) {
                tr.commit();
            }
        }
    }

    public static void refresh(Object object, Session session) {
        Transaction tr = null;
        try {
            if (session.isOpen()) {
                tr = session.getTransaction();
            } else {
                tr = session.beginTransaction();
            }
            session.refresh(object);
        } catch (Exception e) {
            LogHelper.log(log, e);
            if (tr != null && tr.isActive()) {
                tr.rollback();
            }
        } finally {
            if (tr != null && tr.isActive() && !tr.wasRolledBack()) {
                tr.commit();
            }
        }
    }

    /**
     * @param object
     * @param session
     * @throws HibernateException
     * @throws PersistenceException
     */
    public static void save(Entity object, boolean startTransaction,
                            Session session) throws HibernateException,
            PersistenceException {
        save(object, 0l, startTransaction, session);
    }

    /**
     * @param object
     * @param currentUserId
     */
    public static void beforeSaveOrUpdate(Entity object, Long currentUserId) {
        if (object.getCreateDate() == null) {
            object.setCreateDate(new Date());
            try {
                object.setCreateUserId(currentUserId);
            } catch (Exception e) {
            }
        } else {
            object.setUpdateDate(new Date());
            try {
                object.setUpdateUserId(currentUserId);
            } catch (Exception e) {
            }
        }
        if (object instanceof BeforeSave) {
            ((BeforeSave) object).beforeSave();
        }
    }

    /**
     * @param clazz
     * @param criterions
     * @param session
     * @return
     * @throws HibernateException
     * @throws PersistenceException
     * @throws InstantiationException
     * @throws IllegalAccessException
     */
    @SuppressWarnings("unchecked")
    public static <T> T get(Class<T> clazz, Criterion[] criterions,
                            Session session) throws HibernateException,
            PersistenceException, InstantiationException,
            IllegalAccessException {
        return get(clazz, null, criterions, session);
    }

    @SuppressWarnings("unchecked")
    public static <T> T get(Class<T> clazz, CriteriaAlias[] criteriaAliases, Criterion[] criterions,
                            Session session) throws HibernateException,
            PersistenceException, InstantiationException,
            IllegalAccessException {

        Criteria criteria = session.createCriteria(clazz);
        if (criteriaAliases != null) {
            for (CriteriaAlias ca : criteriaAliases) {
                criteria.createAlias(ca.getTable(), ca.getAliasName(),
                        ca.getJoinType());
            }

            criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
        }
        if (criterions != null) {
            for (Criterion crit : criterions) {
                criteria.add(crit);
            }
        }

        return (T) criteria.uniqueResult();
    }

    /**
     * @param clazz
     * @param id
     * @param session
     * @return
     * @throws HibernateException
     * @throws PersistenceException
     * @throws InstantiationException
     * @throws IllegalAccessException
     */
    @SuppressWarnings("unchecked")
    public static <T> T get(Class<T> clazz, Serializable id, Session session)
            throws HibernateException, PersistenceException,
            InstantiationException, IllegalAccessException {
        if (id == null || String.valueOf(id).isEmpty()) {
            return (T) clazz.newInstance();
        }

        return (T) session.get(clazz, Long.parseLong(String.valueOf(id)));
    }

    /**
     * @param clazz
     * @param expression
     * @param session
     * @return
     * @throws HibernateException
     * @throws PersistenceException
     * @throws InstantiationException
     * @throws IllegalAccessException
     */
    public static <T> T get(Class<T> clazz, SimpleExpression expression,
                            Session session) throws HibernateException,
            PersistenceException, InstantiationException,
            IllegalAccessException {
        return get(clazz, new Criterion[]{
                expression
        }, session);
    }

    /**
     * @param clazz
     * @param maxField
     * @param criterions
     * @param session
     * @return
     * @throws PersistenceException
     */
    public static Object getMax(Class<?> clazz, String maxField,
                                Criterion[] criterions, Session session)
            throws PersistenceException {
        Criteria criteria = session.createCriteria(clazz);
        criteria.setProjection(Projections.max(maxField));
        for (Criterion criterion : criterions) {
            criteria.add(criterion);
        }
        return criteria.uniqueResult();
    }

    /**
     * @param clazz
     * @param session
     * @return
     * @throws HibernateException
     * @throws PersistenceException
     */
    public static <T> List<T> load(Class<T> clazz, Session session)
            throws HibernateException, PersistenceException {
        return load(clazz, null, new Order[]{}, session);
    }

    /**
     * @param clazz
     * @param alialTable
     * @param aliasName
     * @param joinType
     * @param criterions
     * @param session
     * @return
     * @throws HibernateException
     * @throws PersistenceException
     */
    @SuppressWarnings("unchecked")
    public static <T extends Entity> List<T> load(Class<T> clazz,
                                                  String alialTable, String aliasName, JoinType joinType,
                                                  Criterion[] criterions, Session session) throws HibernateException,
            PersistenceException {
        Criteria criteria = session.createCriteria(clazz).createAlias(
                alialTable, aliasName, joinType);
        if (criterions != null) {
            for (Criterion crit : criterions) {
                criteria.add(crit);
            }
        }

        return criteria.list();
    }

    /**
     * @param clazz
     * @param criterions
     * @param session
     * @return
     * @throws HibernateException
     * @throws PersistenceException
     */
    public static <T> List<T> load(Class<T> clazz, Criterion[] criterions,
                                   Session session) throws HibernateException,
            PersistenceException {
        return load(clazz, criterions, new Order[]{}, session);
    }

    /**
     * @param clazz
     * @param criterions
     * @param session
     * @return
     * @throws HibernateException
     * @throws PersistenceException
     */
    public static List<Long> loadIds(Class<?> clazz, Criterion[] criterions,
                                     Session session) throws HibernateException,
            PersistenceException {
        return loadIds(clazz, "id", criterions, session);
    }

    /**
     * @param clazz
     * @param sqlQuery
     * @param session
     * @return
     * @throws HibernateException
     * @throws PersistenceException
     */
    @SuppressWarnings("unchecked")
    public static <T> List<T> load(Class<T> clazz, String sqlQuery,
                                   Session session) throws HibernateException,
            PersistenceException {
        return session.createSQLQuery(sqlQuery).list();
    }

    /**
     * @param clazz
     * @param idField
     * @param criterions
     * @param session
     * @return
     * @throws HibernateException
     * @throws PersistenceException
     */
    @SuppressWarnings("unchecked")
    public static List<Long> loadIds(Class<?> clazz, String idField,
                                     Criterion[] criterions, Session session) throws HibernateException,
            PersistenceException {
        Criteria criteria = session.createCriteria(clazz);
        criteria.setProjection(Projections.property(idField));
        if (criterions != null) {
            for (Criterion crit : criterions) {
                criteria.add(crit);
            }
        }

        return criteria.list();
    }

    public static List<Long> loadIds(Class<?> clazz, CriteriaAlias[] aliases,
                                     Criterion[] criterions, Session session) {

        return loadField(clazz, "id", Long.class, aliases, criterions, session);
    }

    @SuppressWarnings("unchecked")
    public static <T> List<T> loadField(Class<?> clazz, String idField,
                                        Class<T> returnType, CriteriaAlias[] criteriaAlias,
                                        Criterion[] criterions, Session session) {
        Criteria criteria = session.createCriteria(clazz);
        criteria.setProjection(Projections.property(idField));
        if (criterions != null) {
            for (Criterion crit : criterions) {
                criteria.add(crit);
            }
        }

        if (criteriaAlias != null) {
            for (CriteriaAlias ca : criteriaAlias) {
                criteria.createAlias(ca.getTable(), ca.getAliasName(),
                        ca.getJoinType());
            }
        }


        return criteria.list();
    }

    /**
     * @param clazz
     * @param order
     * @param session
     * @return
     * @throws HibernateException
     * @throws PersistenceException
     */
    public static <T> List<T> load(Class<T> clazz, Order order, Session session)
            throws HibernateException, PersistenceException {
        return load(clazz, null, order, null, session);
    }

    /**
     * @param clazz
     * @param order
     * @param maxRecords
     * @param session
     * @return
     * @throws HibernateException
     * @throws PersistenceException
     */
    public static <T> List<T> load(Class<T> clazz, Order order,
                                   Integer maxRecords, Session session) throws HibernateException,
            PersistenceException {
        return load(clazz, null, order, maxRecords, session);
    }

    /**
     * @param clazz
     * @param criterions
     * @param order
     * @param session
     * @return
     * @throws HibernateException
     * @throws PersistenceException
     */
    public static <T> List<T> load(Class<T> clazz, Criterion[] criterions,
                                   Order order, Session session) throws HibernateException,
            PersistenceException {
        return load(clazz, criterions, order, null, session);
    }

    /**
     * @param clazz
     * @param criterions
     * @param order
     * @param maxRecords
     * @param session
     * @return
     * @throws HibernateException
     * @throws PersistenceException
     */
    public static <T> List<T> load(Class<T> clazz, Criterion[] criterions,
                                   Order order, Integer maxRecords, Session session)
            throws HibernateException, PersistenceException {
        if (order != null) {
            return load(clazz, criterions, new Order[]{
                    order
            }, maxRecords, session);
        } else {
            return load(clazz, criterions, new Order[]{}, maxRecords, session);
        }
    }

    /**
     * @param clazz
     * @param criterions
     * @param orderList
     * @param session
     * @return
     * @throws PersistenceException
     */
    public static <T> List<T> load(Class<T> clazz, Criterion[] criterions,
                                   Order[] orderList, Session session) throws PersistenceException {
        return load(clazz, criterions, orderList, null, session);
    }

    /**
     * @param clazz
     * @param criterions
     * @param orderList
     * @param maxResult
     * @param session
     * @return
     * @throws PersistenceException
     */
    @SuppressWarnings({
            "unchecked"
    })
    public static <T> List<T> load(Class<T> clazz, Criterion[] criterions,
                                   Order[] orderList, Integer maxResult, Session session)
            throws PersistenceException {
        Criteria criteria = session.createCriteria(clazz);
        if (maxResult != null) {
            criteria.setMaxResults(maxResult);
        }
        if (criterions != null) {
            for (Criterion crit : criterions) {
                criteria.add(crit);
            }
        }

        if (orderList != null) {
            for (Order o : orderList) {
                criteria.addOrder(o);
            }
        }

        return criteria.list();
    }

    /**
     * @param object
     * @param currentUserId
     * @param session
     * @throws HibernateException
     * @throws PersistenceException
     */
    public static void merge(Entity object, Long currentUserId, Session session)
            throws HibernateException, PersistenceException {
        beforeSaveOrUpdate(object, currentUserId);

        session.merge(object);
    }

    /**
     * @param object
     * @param session
     * @throws HibernateException
     * @throws PersistenceException
     */
    public static <T> void remove(T object, Session session)
            throws HibernateException, PersistenceException {
        session.delete(object);
    }

    /**
     * @param clazz
     * @param id
     * @param session
     * @throws HibernateException
     * @throws PersistenceException
     * @throws InstantiationException
     * @throws IllegalAccessException
     */
    public static <T> void remove(Class<T> clazz, Serializable id,
                                  Session session) throws HibernateException,
            PersistenceException, InstantiationException,
            IllegalAccessException {
        remove(get(clazz, id, session), session);
    }

    public static <T extends Entity> List<T> load(Class<T> clazz,
                                                  CriteriaAlias[] criteriaAliases, Criterion[] criterions,
                                                  Session session) throws PersistenceException {
        return load(clazz, criteriaAliases, criterions, null, session);
    }

    @SuppressWarnings("unchecked")
    public static <T extends Entity> List<T> load(Class<T> clazz,
                                                  CriteriaAlias[] criteriaAliases, Criterion[] criterions,
                                                  Order[] orders, Session session) throws PersistenceException {
        Criteria criteria = session.createCriteria(clazz);

        if (criteriaAliases != null) {
            for (CriteriaAlias ca : criteriaAliases) {
                criteria.createAlias(ca.getTable(), ca.getAliasName(),
                        ca.getJoinType());
            }

            criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
        }

        if (criterions != null) {
            for (Criterion crit : criterions) {
                if (crit != null) {
                    criteria.add(crit);
                }
            }
        }

        if (orders != null && orders.length > 0) {
            for (Order o : orders) {
                criteria.addOrder(o);
            }
        }

        return criteria.list();
    }
}
