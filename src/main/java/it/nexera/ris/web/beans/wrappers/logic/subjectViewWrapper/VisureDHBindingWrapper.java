package it.nexera.ris.web.beans.wrappers.logic.subjectViewWrapper;

import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.helpers.ResourcesHelper;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.domain.Subject;
import it.nexera.ris.persistence.beans.entities.domain.VisureDH;
import it.nexera.ris.persistence.beans.entities.domain.VisureRTF;
import it.nexera.ris.persistence.view.FormalityView;
import it.nexera.ris.web.common.EntityLazyListModel;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.primefaces.component.column.Column;
import org.primefaces.component.commandbutton.CommandButton;
import org.primefaces.model.LazyDataModel;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


public class VisureDHBindingWrapper extends BaseTab implements Serializable {

    private static final long serialVersionUID = 3419737923499817710L;

    private Subject subject;

    private LazyDataModel<FormalityView> lazyModel;

    public VisureDHBindingWrapper(Subject subject) {
        this.subject = subject;
    }

    public String getTabTitle() {
        return ResourcesHelper.getString("subjectViewVisureDH");
    }

    public List<Column> getColumns() {
        List<Column> columns = new ArrayList<>();
        columns.add(getTextColumn("subjectViewVisureDHType", "type"));
        columns.add(getTextColumn("subjectViewVisureDHUpdateDate", "updateDateString"));
        columns.add(getTextColumn("subjectViewVisureDHNumFormality", "numFormality"));
        columns.add(getTextColumn("subjectViewVisureDHNumberPractice", "numberPractice"));

        return columns;
    }

    @Override
    LazyDataModel getLazyDataModel() {
        return new EntityLazyListModel(VisureDH.class, new Criterion[]{
                Restrictions.eq("fiscalCodeVat", getSubject().getFiscalCodeVATNamber())},
                new Order[]{Order.asc("id")});
    }

    @Override
    public Long getCountTable() throws PersistenceBeanException, IllegalAccessException {
        return DaoManager.getCount(VisureDH.class, "id", new Criterion[]{
                Restrictions.eq("fiscalCodeVat", getSubject().getFiscalCodeVATNamber())});
    }

    public Subject getSubject() {
        return subject;
    }

    public void setSubject(Subject subject) {
        this.subject = subject;
    }

    public LazyDataModel<FormalityView> getLazyModel() {
        return new EntityLazyListModel(VisureDH.class, new Criterion[]{
                Restrictions.eq("fiscalCodeVat", getSubject().getFiscalCodeVATNamber())},
                new Order[]{Order.asc("id")});
    }

    public void setLazyModel(LazyDataModel<FormalityView> lazyModel) {
        this.lazyModel = lazyModel;
    }
}
