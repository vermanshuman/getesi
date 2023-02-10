package it.nexera.ris.persistence.beans.dao;

import it.nexera.ris.common.enums.ApplicationSettingsKeys;
import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.helpers.*;
import it.nexera.ris.persistence.PersistenceSessionManager;
import it.nexera.ris.persistence.SessionManager;
import it.nexera.ris.persistence.UserHolder;
import it.nexera.ris.persistence.beans.entities.Entity;
import it.nexera.ris.persistence.beans.entities.IEntity;
import it.nexera.ris.persistence.beans.entities.domain.*;
import it.nexera.ris.persistence.interfaces.AfterSave;
import it.nexera.ris.persistence.interfaces.BeforeSave;
import it.nexera.ris.settings.ApplicationSettingsHolder;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.*;
import org.hibernate.criterion.*;
import org.hibernate.exception.GenericJDBCException;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import java.io.Serializable;
import java.lang.InstantiationException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Main class for working with database
 */
public class DaoManager {

    protected static transient final Log log = LogFactory
            .getLog(BaseHelper.class);

    public static Session getSession()
            throws PersistenceBeanException, IllegalAccessException {
        if (FacesContext.getCurrentInstance() == null) {
            throw new IllegalAccessException("");
        }
        return PersistenceSessionManager.getBean().getSession();
    }

    public static Object getMax(Class<? extends IEntity> clazz, String maxField,
                                Criterion[] criterions)
            throws PersistenceBeanException, IllegalAccessException {
        return getMax(clazz, maxField, null, criterions);
    }

    public static Object getMax(Class<? extends IEntity> clazz, String maxField,
                                CriteriaAlias[] aliases, Criterion[] criterions)
            throws PersistenceBeanException, IllegalAccessException {
        Criteria criteria = getSession().createCriteria(clazz);
        criteria.setProjection(Projections.max(maxField));
        if (aliases != null) {
            for (CriteriaAlias ca : aliases) {
                criteria.createAlias(ca.getTable(), ca.getAliasName(),
                        ca.getJoinType());
            }
        }
        for (Criterion criterion : criterions) {
            criteria.add(criterion);
        }
        return criteria.uniqueResult();
    }

    public static Object getMin(Class<? extends IEntity> clazz, String maxField,
                                CriteriaAlias[] aliases, Criterion[] criterions)
            throws PersistenceBeanException, IllegalAccessException {
        Criteria criteria = getSession().createCriteria(clazz);
        criteria.setProjection(Projections.min(maxField));
        if (aliases != null) {
            for (CriteriaAlias ca : aliases) {
                criteria.createAlias(ca.getTable(), ca.getAliasName(),
                        ca.getJoinType());
            }
        }
        for (Criterion criterion : criterions) {
            criteria.add(criterion);
        }
        return criteria.uniqueResult();
    }

    public static Long getCount(Class<? extends IEntity> clazz, String field,
                                Criterion[] criterions)
            throws PersistenceBeanException, IllegalAccessException {
        Criteria criteria = getSession().createCriteria(clazz);
        criteria.setProjection(Projections.countDistinct(field));
        for (Criterion criterion : criterions) {
            criteria.add(criterion);
        }
        return Long.parseLong(String.valueOf(criteria.uniqueResult()));
    }

    public static Long getCountNotAnEntity(Class clazz, String field,
                                           Criterion[] criterions)
            throws PersistenceBeanException, IllegalAccessException {
        Criteria criteria = getSession().createCriteria(clazz);
        criteria.setProjection(Projections.countDistinct(field));
        for (Criterion criterion : criterions) {
            criteria.add(criterion);
        }
        return Long.parseLong(String.valueOf(criteria.uniqueResult()));
    }

    public static Long getCount(Class<? extends IEntity> clazz, String field,
                                CriteriaAlias[] criteriaAlias, Criterion[] criterions)
            throws PersistenceBeanException, IllegalAccessException {
        Criteria criteria = getSession().createCriteria(clazz);
        criteria.setProjection(Projections.countDistinct(field));
        for (Criterion criterion : criterions) {
            criteria.add(criterion);
        }
        if (!ValidationHelper.isNullOrEmpty(criteriaAlias)) {
            for (CriteriaAlias ca : criteriaAlias) {
                criteria.createAlias(ca.getTable(), ca.getAliasName(),
                        ca.getJoinType());
            }
        }

        return Long.parseLong(String.valueOf(criteria.uniqueResult()));
    }

    public static Long getCount(Class<? extends IEntity> clazz, String field)
            throws PersistenceBeanException, IllegalAccessException {
        return getCount(clazz, field, new Criterion[]
                {});
    }

    public static BigInteger countQuery(String query) throws PersistenceBeanException, IllegalAccessException {
        Query queryObject = getSession().createSQLQuery(query);
        return (BigInteger) queryObject.uniqueResult();
    }

    public static List<?> find(String query, Object[] objs)
            throws HibernateException, PersistenceBeanException,
            IllegalAccessException {
        Query queryObject = getSession().createQuery(query);

        if (objs != null) {
            for (int i = 0; i < objs.length; i++) {
                queryObject.setParameter(i, objs[i]);
            }
        }

        return queryObject.list();
    }

    @SuppressWarnings("unchecked")
    public static <T> T get(Class<T> clazz, Serializable id)
            throws HibernateException, PersistenceBeanException,
            InstantiationException, IllegalAccessException {
        if (id == null || String.valueOf(id).isEmpty()) {
            return (T) clazz.newInstance();
        }

        return (T) getSession().get(clazz, Long.parseLong(String.valueOf(id)));
    }

    public static <T> T get(Class<T> clazz, Criterion[] criterions)
            throws HibernateException, PersistenceBeanException,
            InstantiationException, IllegalAccessException {
        return get(clazz, new CriteriaAlias[]
                {}, criterions);
    }

    @SuppressWarnings("unchecked")
    public static <T> T get(Class<T> clazz, CriteriaAlias[] criteriaAlias,
                            Criterion[] criterions)
            throws HibernateException, PersistenceBeanException,
            InstantiationException, IllegalAccessException {
        Criteria criteria = getSession().createCriteria(clazz);
        if (criterions != null) {
            for (Criterion crit : criterions) {
                criteria.add(crit);
            }
        }

        for (CriteriaAlias ca : criteriaAlias) {
            criteria.createAlias(ca.getTable(), ca.getAliasName(),
                    ca.getJoinType());
        }

        criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);

        return (T) criteria.uniqueResult();
    }

    public static <T> T get(Class<T> clazz, SimpleExpression expression)
            throws HibernateException, PersistenceBeanException,
            InstantiationException, IllegalAccessException {
        return get(clazz, new Criterion[]
                {
                        expression
                });
    }

    public static <T> List<T> load(Class<T> clazz) throws HibernateException,
            PersistenceBeanException, IllegalAccessException {
        return load(clazz, null, new Order[]
                {});
    }

    public static <T extends Entity> List<T> load(Class<T> clazz,
                                                  CriteriaAlias[] criteriaAlias, Criterion[] criterions)
            throws HibernateException, PersistenceBeanException,
            IllegalAccessException {
        return load(clazz, criteriaAlias, criterions, new Order[]
                {});
    }

    public static <T extends Entity> List<T> load(Class<T> clazz,
                                                  CriteriaAlias[] criteriaAlias, Criterion[] criterions, Order order)
            throws HibernateException, PersistenceBeanException,
            IllegalAccessException {
        return load(clazz, criteriaAlias, criterions, new Order[]
                {
                        order
                });
    }

    @SuppressWarnings("unchecked")
    public static <T extends Entity> List<T> load(Class<T> clazz,
                                                  CriteriaAlias[] criteriaAliases, Criterion[] criterions,
                                                  Order[] orders)
            throws PersistenceBeanException, IllegalAccessException {
        Criteria criteria = getSession().createCriteria(clazz);

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

    public static <T extends Entity> List<T> load(Class<T> clazz,
                                                  CriteriaAlias criteriaAlias, Criterion[] criterions)
            throws HibernateException, PersistenceBeanException,
            IllegalAccessException {
        return load(clazz, new CriteriaAlias[]
                {
                        criteriaAlias
                }, criterions);
    }

    public static <T> List<T> load(Class<T> clazz, Criterion[] criterions)
            throws HibernateException, PersistenceBeanException,
            IllegalAccessException {
        return load(clazz, criterions, new Order[]
                {});
    }

    public static <T> List<T> load(Class<T> clazz,
                                   SimpleExpression... restrictions) throws HibernateException,
            PersistenceBeanException, IllegalAccessException {
        List<Criterion> crits = new ArrayList<Criterion>();
        for (SimpleExpression item : restrictions) {
            crits.add(item);
        }
        return load(clazz, crits.toArray(new Criterion[0]), new Order[]
                {});
    }

    @SuppressWarnings("unchecked")
    public static <T> List<T> load(Class<T> clazz, String sqlQuery)
            throws HibernateException, PersistenceBeanException,
            IllegalAccessException {
        return getSession().createSQLQuery(sqlQuery).list();
    }

    public static <T> List<T> load(Class<T> clazz, Criterion[] criterions, CriteriaAlias[] criteriaAlias,
                                   Order[] orderList, Integer maxResult, Integer from, Projection projection)
            throws PersistenceBeanException, IllegalAccessException {
        Criteria criteria = getSession().createCriteria(clazz);
        if (maxResult != null) {
            criteria.setMaxResults(maxResult);
        }
        if (from != null) {
            criteria.setFirstResult(from);
        }
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

        if (orderList != null) {
            for (Order o : orderList) {
                criteria.addOrder(o);
            }
        }

        if (projection != null) {
            criteria.setProjection(projection);
        }

        try {
            return criteria.list();
        } catch (GenericJDBCException e) {
            LogHelper.log(log, e);
        }
        return new ArrayList<T>();
    }

    public static <T> List<T> load(Class<T> clazz, Criterion[] criterions, CriteriaAlias[] criteriaAlias)
            throws PersistenceBeanException, IllegalAccessException {
        return load(clazz, criterions, criteriaAlias, null, null, null, null);
    }


    public static List<Long> loadIds(Class<?> clazz, Criterion[] criterions)
            throws HibernateException, PersistenceBeanException,
            IllegalAccessException {
        return loadField(clazz, "id", Long.class, null, criterions, null);
    }

    public static List<Long> loadIds(Class<?> clazz,
                                     CriteriaAlias[] criteriaAlias, Criterion[] criterions)
            throws HibernateException, PersistenceBeanException,
            IllegalAccessException {
        return loadField(clazz, "id", Long.class, criteriaAlias, criterions, null);
    }

    public static <T> List<T> loadField(Class<?> clazz, String idField,
                                        Class<T> returnType, Criterion[] criterions)
            throws HibernateException, PersistenceBeanException,
            IllegalAccessException {
        return loadField(clazz, idField, returnType, null, criterions, null);
    }

    public static <T> List<T> loadField(Class<?> clazz, String idField,
                                        Class<T> returnType, Criterion[] criterions, Order order)
            throws PersistenceBeanException, IllegalAccessException {
        return loadField(clazz, idField, returnType, null, criterions, order);
    }

    public static <T> List<T> loadField(Class<?> clazz, String idField,
                                        Class<T> returnType, CriteriaAlias[] criteriaAlias,
                                        Criterion[] criterions) throws PersistenceBeanException, IllegalAccessException {
        return loadField(clazz, idField, returnType, criteriaAlias, criterions, null);
    }

    @SuppressWarnings("unchecked")
    public static <T> List<T> loadField(Class<?> clazz, String idField,
                                        Class<T> returnType, CriteriaAlias[] criteriaAlias,
                                        Criterion[] criterions, Order order) throws HibernateException,
            PersistenceBeanException, IllegalAccessException {
        Criteria criteria = getSession().createCriteria(clazz);
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

        if (order != null) {
            criteria.addOrder(order);
        }

        return criteria.list();
    }

    public static <T extends Entity> List<T> load(Class<T> clazz, Order order)
            throws HibernateException, PersistenceBeanException,
            IllegalAccessException {
        return load(clazz, null, order, null);
    }

    public static <T> List<T> load(Class<T> clazz, Order order,
                                   Integer maxRecords) throws HibernateException,
            PersistenceBeanException, IllegalAccessException {
        return load(clazz, null, order, maxRecords);
    }

    public static <T> List<T> load(Class<T> clazz, Criterion[] criterions, Order order)
            throws PersistenceBeanException, IllegalAccessException {
        return load(clazz, criterions, order, null);
    }

    public static <T> List<T> load(Class<T> clazz, Criterion[] criterions, Order order, Integer maxRecords)
            throws PersistenceBeanException, IllegalAccessException {
        if (order != null) {
            return load(clazz, criterions, new Order[]
                    {
                            order
                    }, maxRecords);
        } else {
            return load(clazz, criterions, new Order[]
                    {}, maxRecords);
        }
    }

    public static <T> List<T> load(Class<T> clazz, Criterion[] criterions,
                                   Order[] orderList)
            throws PersistenceBeanException, IllegalAccessException {
        return load(clazz, criterions, orderList, null);
    }

    public static <T> List<T> load(Class<T> clazz, Criterion[] criterions,
                                   Order[] orderList, Integer maxResult)
            throws PersistenceBeanException, IllegalAccessException {
        return load(clazz, criterions, orderList, maxResult, null);
    }

    @SuppressWarnings("unchecked")
    public static <T> List<T> load(Class<T> clazz, Criterion[] criterions,
                                   Order[] orderList, Integer maxResult, Projection projection)
            throws PersistenceBeanException, IllegalAccessException {
        Criteria criteria = getSession().createCriteria(clazz);
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

        if (projection != null) {
            criteria.setProjection(projection);
        }

        try {
            return criteria.list();
        } catch (GenericJDBCException e) {
            // HibernateUtil.cleanFactory();
        }
        return criteria.list();
    }

    @SuppressWarnings("unchecked")
    public static <T extends Entity> List<T> load(Class<T> clazz,
                                                  CriteriaAlias[] criteriaAliases, Criterion[] criterions,
                                                  Integer maxresults)
            throws PersistenceBeanException, IllegalAccessException {
        Criteria criteria = getSession().createCriteria(clazz);

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

        if (maxresults != null) {
            criteria.setMaxResults(maxresults);
        }

        return criteria.list();
    }

    public static void save(Entity object)
            throws HibernateException, PersistenceBeanException {
        save(object, false);
    }

    public static void save(Entity object, boolean beginTransaction)
            throws HibernateException, PersistenceBeanException {
        Transaction tr = null;
        try {
            if (beginTransaction) {
                tr = DaoManager.getSession().beginTransaction();
            }
            //sendNotification(object);
            beforeSaveOrUpdate(object);

            if (getSession().contains(object)) {
                getSession().merge(object);
            } else {
                getSession().saveOrUpdate(object);
            }

            if (object instanceof AfterSave) {
                ((AfterSave) object).afterSave();
            }
        } catch (NonUniqueObjectException e) {
            try {
                getSession().merge(object);
            } catch (Exception e1) {
                LogHelper.log(log, e1);
                if (beginTransaction) {
                    if (tr != null && tr.isActive()) {
                        tr.rollback();
                    }
                }
            }
        } catch (Exception e) {
            LogHelper.log(log, e);
            if (beginTransaction) {
                if (tr != null && tr.isActive()) {
                    tr.rollback();
                }
            }
        } finally {
            if (beginTransaction) {
                if (tr != null && tr.isActive() && !tr.wasRolledBack()) {
                    tr.commit();
                }
            }
        }
    }

    public static void saveWeak(Object object, boolean beginTransaction)
            throws HibernateException {
        Transaction tr = null;
        try {
            if (beginTransaction) {
                tr = DaoManager.getSession().beginTransaction();
            }
            if (getSession().contains(object)) {
                getSession().merge(object);
            } else {
                getSession().saveOrUpdate(object);
            }
        } catch (NonUniqueObjectException e) {
            try {
                getSession().merge(object);
            } catch (Exception e1) {
                LogHelper.log(log, e1);
                if (beginTransaction) {
                    if (tr != null && tr.isActive()) {
                        tr.rollback();
                    }
                }
            }
        } catch (Exception e) {
            LogHelper.log(log, e);
            if (beginTransaction) {
                if (tr != null && tr.isActive()) {
                    tr.rollback();
                }
            }
        } finally {
            if (beginTransaction) {
                if (tr != null && tr.isActive() && !tr.wasRolledBack()) {
                    tr.commit();
                }
            }
        }
    }

    public static void refresh(Object object) {
        Transaction tr = null;
        try {
            if(DaoManager.getSession().isOpen()) {
                tr = DaoManager.getSession().getTransaction();
            } else {
                tr = DaoManager.getSession().beginTransaction();
            }
            getSession().refresh(object);
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

    public static void beforeSaveOrUpdate(Entity object) {
        if (object.getCreateDate() == null) {
            object.setCreateDate(new Date());
            try {
                if (SessionManager.getInstance().getSessionBean() != null) {
                    object.setCreateUserId(
                            UserHolder.getInstance().getCurrentUser().getId());
                } else {
                    object.setCreateUserId(0L);
                }
            } catch (Exception e) {
            }
        } else {
            object.setUpdateDate(new Date());
            try {
                if (SessionManager.getInstance().getSessionBean() != null) {
                    object.setUpdateUserId(
                            UserHolder.getInstance().getCurrentUser().getId());
                } else {
                    object.setUpdateUserId(0L);
                }
            } catch (Exception e) {
            }
        }
        if (object instanceof BeforeSave) {
            ((BeforeSave) object).beforeSave();
        }
    }

    public static void sendNotification(Entity entity) {
        if (entity instanceof Request) {
            Long stateId = 0L;
            try {

                String value = getField(Request.class, "stateId", new Criterion[]{
                        Restrictions.eq("id", entity.getId())}, new CriteriaAlias[]{});

                if (!ValidationHelper.isNullOrEmpty(value)) {
                    stateId = Long.parseLong(value);
                }

                if (!stateId.equals(((Request) entity).getStateId())) {

                    WLGInbox wlgInbox = new WLGInbox();


                    String emailFrom = null;
                    try {
                        emailFrom = MailHelper.getEmailFrom();
                    } catch (PersistenceBeanException | IllegalAccessException e) {
                        LogHelper.log(log, e);
                    } catch (InstantiationException e) {
                        e.printStackTrace();
                    }
                    if (StringUtils.isNotBlank(emailFrom)) {
                        wlgInbox.setEmailFrom(emailFrom);
                    }

                    if (!ValidationHelper.isNullOrEmpty(((Request) entity).getClient().getEmails())) {
                        List<ClientEmail> emails = ((Request) entity).getClient().getEmails();
                        wlgInbox.setEmailTo(emails.stream().map(ClientEmail::getEmail).map(MailHelper::prepareEmailToSend)
                                .collect(Collectors.joining(", ")));
                    } else {
                        MessageHelper.addGlobalMessage(FacesMessage.SEVERITY_WARN,
                                ResourcesHelper.getValidation("warning"),
                                ResourcesHelper.getValidation("requestTextEditEvasionNotificationFailed"));
                        return;
                    }

                    wlgInbox.setEmailBody(ResourcesHelper.getString("requestTextEditEvasionNotificationPlain"));

                    String emailHtmlBoby = String.format(ResourcesHelper.getString("emailStructure"),
                            ResourcesHelper.getString("requestTextEditEvasionNotificationHtml"));
                    wlgInbox.setEmailBodyHtml(emailHtmlBoby);
                    wlgInbox.setEmailSubject(ResourcesHelper.getString("requestTextEditEvasionNotificationSubject"));

                    try {
                        MailHelper.sendMail(wlgInbox, null);
                        MessageHelper.addGlobalMessage(FacesMessage.SEVERITY_INFO, "",
                                ResourcesHelper.getString("requestTextEditEvasionNotificationSuccess"));
                    } catch (Exception e) {
                        LogHelper.log(log, e);
                        MessageHelper.addGlobalMessage(FacesMessage.SEVERITY_WARN,
                                ResourcesHelper.getValidation("warning"),
                                ResourcesHelper.getString("requestTextEditEvasionNotificationFailed"));
                    }
                }


            } catch (PersistenceBeanException | IllegalAccessException e) {
                LogHelper.log(log, e);
            }

        }

    }

    public static void merge(Entity object) throws HibernateException,
            PersistenceBeanException, IllegalAccessException {
        beforeSaveOrUpdate(object);

        getSession().merge(object);
    }

    public static <T extends IEntity> void remove(T object)
            throws HibernateException, PersistenceBeanException {
        remove(object, false);
    }

    @SuppressWarnings("unchecked")
    public static <T extends IEntity> void remove(T object,
                                                  boolean beginTransaction)
            throws HibernateException, PersistenceBeanException {
        Transaction tr = null;
        try {
            if (beginTransaction) {
                tr = DaoManager.getSession().beginTransaction();
            }

            getSession().delete(object);

        } catch (NonUniqueObjectException e) {
            try {
                object = (T) getSession().merge(object);
                getSession().delete(object);
            } catch (Exception e1) {
                LogHelper.log(log, e1);
                if (beginTransaction) {
                    if (tr != null && tr.isActive()) {
                        tr.rollback();
                    }
                }
            }
        } catch (Exception e) {
            LogHelper.log(log, e);
            if (beginTransaction) {
                if (tr != null && tr.isActive()) {
                    tr.rollback();
                }
            }
        } finally {
            if (beginTransaction) {
                if (tr != null && tr.isActive() && !tr.wasRolledBack()) {
                    tr.commit();
                }
            }
        }
    }

    public static void removeWeak(Object object, boolean beginTransaction)
            throws HibernateException {
        Transaction tr = null;
        try {
            if (beginTransaction) {
                tr = DaoManager.getSession().beginTransaction();
            }

            getSession().delete(object);

        } catch (NonUniqueObjectException e) {
            try {
                object = getSession().merge(object);
                getSession().delete(object);
            } catch (Exception e1) {
                LogHelper.log(log, e1);
                if (beginTransaction) {
                    if (tr != null && tr.isActive()) {
                        tr.rollback();
                    }
                }
            }
        } catch (Exception e) {
            LogHelper.log(log, e);
            if (beginTransaction) {
                if (tr != null && tr.isActive()) {
                    tr.rollback();
                }
            }
        } finally {
            if (beginTransaction) {
                if (tr != null && tr.isActive() && !tr.wasRolledBack()) {
                    tr.commit();
                }
            }
        }
    }

    public static <T extends IEntity> void removeWithRefreshRequest(Class<T> clazz,
                                                                    Serializable id, boolean beginTransaction)
            throws HibernateException, PersistenceBeanException,
            InstantiationException, IllegalAccessException {
        T object = get(clazz, id);
        if (object instanceof EstateSituation) {
            EstateSituation situation = (EstateSituation) object;
            for (SituationProperty situationProperty : situation.getSituationProperties()) {
                situationProperty.removeAllDatafromPropertiesAssociations();
                situationProperty.getProperty().getSituationProperties().remove(situationProperty);
                if (!ValidationHelper.isNullOrEmpty(situation.getPropertyListWithoutInit())) {
                    situation.getPropertyListWithoutInit().stream()
                            .filter(p -> !ValidationHelper.isNullOrEmpty(p.getSituationProperties()))
                            .forEach(p -> p.getSituationProperties().remove(situationProperty));
                }
                remove(situationProperty, false);
            }
            situation.setSituationProperties(null);
            DaoManager.refresh(situation.getRequest());
        }
        remove(object, beginTransaction);
    }

    public static <T extends IEntity> void remove(Class<T> clazz,
                                                  Serializable id, boolean beginTransaction)
            throws HibernateException, PersistenceBeanException,
            InstantiationException, IllegalAccessException {
        remove(get(clazz, id), beginTransaction);
    }

    public static <T extends IEntity> void remove(Class<T> clazz,
                                                  Serializable id)
            throws HibernateException, PersistenceBeanException,
            InstantiationException, IllegalAccessException {
        remove(clazz, id, false);
    }

    public static void addCriteriaIn(String propertyName, List<?> list,
                                     List<Criterion> criterions) {
        Disjunction or = Restrictions.disjunction();
        if (list.size() > 1000) {
            while (list.size() > 1000) {
                List<?> subList = list.subList(0, 1000);
                or.add(Restrictions.in(propertyName, subList));
                list.subList(0, 1000).clear();
            }
        }
        or.add(Restrictions.in(propertyName, list));
        criterions.add(or);
    }

    @SuppressWarnings("unchecked")
    public static String getField(Class<? extends IEntity> clazz, String field,
                                  Criterion[] criterions, CriteriaAlias[] criteriaAlias)
            throws PersistenceBeanException, IllegalAccessException {
        Criteria criteria = getSession().createCriteria(clazz);
        criteria.setProjection(Projections.property(field));

        if (criterions != null) {
            for (Criterion criterion : criterions) {
                criteria.add(criterion);
            }
        }

        if (criteriaAlias != null) {
            for (CriteriaAlias ca : criteriaAlias) {
                criteria.createAlias(ca.getTable(), ca.getAliasName(),
                        ca.getJoinType());
            }
        }

        List<Object> results = criteria.list();

        if (!ValidationHelper.isNullOrEmpty(results)) {
            return String.valueOf(results.get(0));
        } else {
            return "";
        }
    }

    @SuppressWarnings("unchecked")
    public static List<Object> getFields(Class<? extends IEntity> clazz,
                                         Criterion[] criterions, CriteriaAlias[] criteriaAlias,
                                         boolean isDistinct, Integer maxResults, String... fields)
            throws IllegalAccessException, PersistenceBeanException {
        Criteria criteria = getSession().createCriteria(clazz);
        ProjectionList proList = Projections.projectionList();

        for (String field : fields) {
            proList.add(Projections.property(field));
        }

        if (isDistinct) {
            criteria.setProjection(Projections.distinct(proList));
        } else {
            criteria.setProjection(proList);
        }

        if (maxResults != null) {
            criteria.setMaxResults(maxResults);
        }

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

    public static <T> List<T> loadDistinctfield(Class<?> clazz, String idField, Class<T> returnType, CriteriaAlias[] criteriaAlias,
                                                Criterion[] criterions) throws PersistenceBeanException, IllegalAccessException {
        Criteria criteria = getSession().createCriteria(clazz);
        criteria.setProjection(
                Projections.distinct(Projections.property(idField)));
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

}
