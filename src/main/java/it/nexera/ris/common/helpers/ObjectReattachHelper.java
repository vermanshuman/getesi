package it.nexera.ris.common.helpers;

import it.nexera.ris.common.annotations.ReattachIgnore;
import it.nexera.ris.persistence.PersistenceSessionManager;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.IEntity;
import it.nexera.ris.persistence.beans.entities.IndexedView;
import it.nexera.ris.persistence.beans.entities.domain.Agency;
import it.nexera.ris.persistence.beans.entities.domain.CadastralData;
import it.nexera.ris.persistence.beans.entities.domain.Subject;
import it.nexera.ris.web.beans.PageBean;
import it.nexera.ris.web.beans.pages.ClientEditBean;
import it.nexera.ris.web.beans.pages.DatabaseListBean;
import it.nexera.ris.web.beans.pages.RealEstateBean;
import it.nexera.ris.web.beans.pages.RequestEditBean;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Criteria;
import org.hibernate.collection.internal.PersistentBag;
import org.hibernate.criterion.Criterion;
import org.hibernate.proxy.HibernateProxy;
import org.hibernate.proxy.pojo.javassist.JavassistLazyInitializer;

import javax.persistence.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ObjectReattachHelper {

    private static Log log;

    static {
        log = LogFactory.getLog(ObjectReattachHelper.class);
    }

    @SuppressWarnings("unused")
    private static ObjectReattachHelper instance;

    private List<ReatachedObject> list = new ArrayList<ObjectReattachHelper.ReatachedObject>();

    private PageBean bean;

    public static synchronized ObjectReattachHelper getInstance() {
        return new ObjectReattachHelper();
    }

    public IEntity mergeAllFields(IEntity newObj, IEntity oldObj)
            throws Exception {
        if (newObj instanceof HibernateProxy) {
            try {
                newObj = (IEntity) ((JavassistLazyInitializer) newObj)
                        .getImplementation();
            } catch (ClassCastException e) {
                HibernateProxy proxy = (HibernateProxy) newObj;
                newObj = (IEntity) (proxy.writeReplace());
            }
        }
        if (oldObj != null && oldObj.isNew()) {
            newObj = oldObj;
        }
        if (newObj == null && oldObj != null) {
            newObj = oldObj.getClass().newInstance();
        } else if (oldObj == null && newObj != null) {
            newObj = null;
            return newObj;
        } else if (oldObj == null && newObj == null) {
            return newObj;
        }

        if (wasReattached(newObj) || oldObj instanceof HibernateProxy) {
            return newObj;
        }

        if (newObj instanceof Agency) {
            ((Agency) newObj).setTempId(((Agency) oldObj).getTempId());
        } else if (newObj instanceof CadastralData) {
            ((CadastralData) newObj)
                    .setTempId(((CadastralData) oldObj).getTempId());
        } else if (newObj instanceof Subject) {
            ((Subject) newObj).setTempId(((Subject) oldObj).getTempId());
        }

        list.add(new ReatachedObject(newObj.getClass(), newObj.getId()));
        for (Field field : getAllFields(oldObj.getClass())) {
            try {
                if (isEmbeddedId(field)) {
                    continue;
                } else if (!field.getType().equals(List.class)
                        && !isEntityClass(field.getType())) {
                    field.setAccessible(true);
                    try {
                        if (!newObj.getClass().equals(oldObj.getClass())) {
                            field.set(newObj, field.get(oldObj));
                        }
                    } catch (Exception e) {

                    }
                    field.setAccessible(false);
                } else if (isEntityClass(field.getType())) {
                    field.setAccessible(true);
                    {
                        IEntity ent = (IEntity) field.get(newObj);
                        if (isTransientField(field)) {
                            ent = mergeTransientEnity(
                                    (IEntity) field.get(oldObj));
                        } else {
                            ManyToOne annotation = field
                                    .getAnnotation(ManyToOne.class);
                            IEntity oldEnt = (IEntity) field.get(oldObj);
                            if (oldEnt != null && annotation != null
                                    && annotation.fetch()
                                    .equals(FetchType.LAZY)) {
                                Field lazyField = oldEnt.getClass()
                                        .getDeclaredField("handler");
                                lazyField.setAccessible(true);

                                JavassistLazyInitializer handler = ((JavassistLazyInitializer) lazyField
                                        .get(oldEnt));
                                if (!handler.isUninitialized()) {
                                    JavassistLazyInitializer newHandler = null;
                                    try {
                                        newHandler = ((JavassistLazyInitializer) lazyField
                                                .get(ent));
                                    } catch (Exception e) {
                                    }

                                    if (newHandler != null) {
                                        newHandler.initialize();
                                    }
                                }

                                lazyField.setAccessible(false);
                            } else if (ent != null) {
                                IEntity oldEntity = (IEntity) field.get(oldObj);
                                if (!(oldEntity instanceof HibernateProxy)) {
                                    ent = mergeAllFields(ent,
                                            (IEntity) field.get(oldObj));
                                }
                            }
                        }
                        field.set(newObj, ent);
                    }

                    field.setAccessible(false);
                } else if (field.getType().equals(List.class)) {
                    field.setAccessible(true);
                    if (isEntityClass((Class<?>) ((ParameterizedType) field
                            .getGenericType()).getActualTypeArguments()[0])) {
                        mergeLists(newObj, oldObj, field);
                    } else {
                        field.set(newObj, field.get(oldObj));
                    }

                    field.setAccessible(false);
                }
            } catch (Exception e) {

            }
        }

        return newObj;
    }

    /**
     * @param newObj
     * @param oldObj
     * @param field
     * @throws IllegalAccessException
     * @throws InstantiationException
     */
    @SuppressWarnings("unchecked")
    private void mergeLists(Object newObj, Object oldObj, Field field)
            throws Exception {
        List<IEntity> list = (List<IEntity>) field.get(newObj);
        List<IEntity> oldList = (List<IEntity>) field.get(oldObj);
        if (oldList == null || (oldList instanceof PersistentBag
                && !((PersistentBag) oldList).wasInitialized())) {
            return;
        }
        List<Long> ind = new ArrayList<Long>();
        if (list == null) {
            list = (List<IEntity>) field.get(oldObj);
            if (isTransientField(field)) {
                mergeTransientList(list, null);
            }
        } else {
            for (int i = 0; i < list.size(); i++) {
                IEntity ent = list.get(i);
                if (!ent.isNew() && ListHelper.contains(oldList, ent.getId())) {
                    ent = mergeAllFields(ent,
                            ListHelper.get(oldList, ent.getId()));
                    list.set(i, ent);
                } else {
                    ind.add(ent.getId());
                }
            }
            for (Long i : ind) {
                ListHelper.remove(list, i);
            }

            for (IEntity ent : oldList) {
                if (ent.isNew()) {
                    list.add(ent);
                } else if (!ListHelper.contains(list, ent.getId())) {
                    list.add(ent);
                }
            }
        }

        field.set(newObj, list);
    }

    @SuppressWarnings("unchecked")
    private void mergeTransientList(List<IEntity> list, PageBean bean) {
        List<Long> savedEntitiesIds = new ArrayList<Long>();
        Class<? extends IEntity> clazz = null;
        for (int i = 0; i < list.size(); i++) {
            try {
                if (!list.get(i).isNew()) {
                    savedEntitiesIds.add(list.get(i).getId());
                    if (clazz == null) {
                        clazz = list.get(i).getClass();
                    }
                } else {
                    IEntity newEntity = list.get(i).getClass().newInstance();
                    newEntity = mergeAllFields(newEntity, list.get(i));
                    if (newEntity != list.get(i)) {
                        list.set(i, newEntity);
                    }
                }
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }

        if (!savedEntitiesIds.isEmpty()) {
            try {
                Criteria criteria = PersistenceSessionManager.getBean()
                        .getSession().createCriteria(getEntityClass(clazz));

                List<Criterion> criterions = new ArrayList<>();

                if (savedEntitiesIds.size() > 900) {
                    LogHelper.log(log,
                            "\n _________________ merging on class = " + clazz
                                    + " on bean = " + bean
                                    + " more than 900 objects, it's not ok _________________ \n");
                }

                DaoManager.addCriteriaIn("id", savedEntitiesIds, criterions);

                if (!criterions.isEmpty()) {
                    for (Criterion criterion : criterions) {
                        criteria.add(criterion);
                    }
                }

                List<IEntity> newEntities = criteria.list();

                Map<Long, IEntity> map = new HashMap<Long, IEntity>(
                        newEntities.size());

                for (IEntity entity : newEntities) {
                    map.put(entity.getId(), entity);
                }

                for (int i = 0; i < list.size(); i++) {
                    if (!list.get(i).isNew()) {
                        IEntity newEntity = map.get(list.get(i).getId());
                        if (newEntity != null) {
                            newEntity = mergeAllFields(newEntity, list.get(i));
                            list.set(i, newEntity);
                        }
                    }
                }
            } catch (Exception e) {
                LogHelper.log(log, "Try reattach bean :" + getBean().getClass().getName());
                LogHelper.log(log, e);
                log.error(e.getMessage(), e);
            }
        }
    }

    private Class<?> getEntityClass(Class<?> clazz) {
        Class<?> resultClass = clazz;
        Class<?> tempClass = clazz.getSuperclass();
        while (!tempClass.equals(Object.class)) {
            if (tempClass.isAnnotationPresent(Entity.class)) {
                resultClass = tempClass;
            }
            tempClass = tempClass.getSuperclass();
        }

        return resultClass;
    }

    private List<Field> getAllFields(Class<?> clazz) {
        List<Field> list = new ArrayList<Field>();
        while (clazz != null) {
            for (Field f : clazz.getDeclaredFields()) {
                list.add(f);
            }

            clazz = clazz.getSuperclass();
        }

        return list;
    }

    private IEntity mergeTransientEnity(IEntity oldEntity) throws Exception {
        IEntity newEntity = null;
        if (!oldEntity.isNew()) {
            newEntity = reattachObject(oldEntity);
        } else {
            newEntity = mergeAllFields(newEntity, oldEntity);
        }

        return newEntity;
    }

    public IEntity reattachObject(IEntity oldObj) throws Exception {
        IEntity newObj = null;

        newObj = (IEntity) PersistenceSessionManager.getBean().getSession()
                .merge(oldObj);

        if (!(oldObj instanceof HibernateProxy)) {
            newObj = mergeAllFields(newObj, oldObj);
        }
        return newObj;
    }


    @SuppressWarnings({
            "unchecked", "rawtypes"
    })
    public PageBean reattachBean(PageBean bean) throws Exception {
        setBean(bean);
        for (Field field : getAllFields(bean.getClass())) {
            try {
                if (field.isAnnotationPresent(ReattachIgnore.class)) {
                    continue;
                }
                if (isEntityClass(field.getType())) {
                    field.setAccessible(true);
                    IEntity ent = (IEntity) field.get(bean);
                    ent = mergeTransientEnity((IEntity) field.get(bean));
                    field.set(bean, ent);

                    field.setAccessible(false);
                } else if (field.getType().equals(List.class)) {
                    field.setAccessible(true);

                    @SuppressWarnings("rawtypes")
                    List list = (List) field.get(bean);
                    if (list != null && list.size() > 0
                            && isEntityClass(list.get(0).getClass())
                            && !(((bean instanceof ClientEditBean) && (field
                            .getName().equals("invoicesRecipients")
                            || field.getName().equals("agencies")))
                            || (bean instanceof RealEstateBean
                            && field.getName()
                            .equals("cadastralDataForDelete"))
                            || ((bean instanceof DatabaseListBean
                            || bean instanceof RequestEditBean)
                            && field.getName()
                            .equals("xmlSubjects")))) {
                        mergeTransientList((List<IEntity>) field.get(bean),
                                bean);
                    }

                    field.setAccessible(false);
                }
            } catch (Exception e) {

            }
        }

        return bean;
    }


    public boolean isTransientField(Field field) {
        for (Annotation annotation : field.getAnnotations()) {
            if (annotation instanceof Transient) {
                return true;
            }
        }
        return false;
    }

    public boolean isEmbeddedId(Field field) {
        for (Annotation annotation : field.getAnnotations()) {
            if (annotation instanceof EmbeddedId) {
                return true;
            }
        }
        return false;
    }

    private boolean isEntityClass(Class<?> clazz) {
        while (clazz != null) {
            if (clazz.equals(IndexedView.class)) {
                return false;
            }
            for (Class<?> interfaze : clazz.getInterfaces()) {
                if (interfaze == IEntity.class) {
                    return true;
                }
            }

            clazz = clazz.getSuperclass();
        }

        return false;
    }

    private boolean wasReattached(IEntity ent) {
        if (ent.isNew()) {
            return false;
        }
        for (ReatachedObject obj : list) {
            if (obj.getId() != null && obj.getClazz().equals(ent.getClass())
                    && obj.getId().equals(ent.getId())) {
                return true;
            }
        }

        return false;
    }

    public PageBean getBean() {
        return bean;
    }

    public void setBean(PageBean bean) {
        this.bean = bean;
    }

    private class ReatachedObject {

        private Class<?> clazz;

        private Long id;

        public ReatachedObject(Class<?> clazz, Long id) {
            this.setClazz(clazz);
            this.setId(id);
        }

        public Class<?> getClazz() {
            return clazz;
        }

        public void setClazz(Class<?> clazz) {
            this.clazz = clazz;
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }
    }
}
