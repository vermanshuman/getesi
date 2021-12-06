package it.nexera.ris.web.beans;

import it.nexera.ris.persistence.beans.entities.Entity;
import it.nexera.ris.web.common.EntityLazyListModel;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.primefaces.model.LazyDataModel;

public abstract class EntityLazyInListEditPageBean<T extends Entity> extends
        EntityInListEditPageBean<T> {

    private static final long serialVersionUID = 425884437868463221L;

    private LazyDataModel<T> lazyModel;

    protected void loadList(Class<T> clazz, Order[] orders) {
        this.setLazyModel(new EntityLazyListModel<T>(clazz, orders));
    }

    protected void loadList(Class<T> clazz, Criterion[] criterion,
                            Order[] orders) {
        this.setLazyModel(new EntityLazyListModel<T>(clazz, criterion, orders));
    }

    protected void loadList(Class<T> clazz, Criterion[] criterion, Criterion[] innerCriterion,
                            Order[] orders) {
        this.setLazyModel(new EntityLazyListModel<T>(clazz, criterion, innerCriterion, orders));
    }

    public LazyDataModel<T> getLazyModel() {
        return lazyModel;
    }

    public void setLazyModel(LazyDataModel<T> lazyModel) {
        this.lazyModel = lazyModel;
    }
}
