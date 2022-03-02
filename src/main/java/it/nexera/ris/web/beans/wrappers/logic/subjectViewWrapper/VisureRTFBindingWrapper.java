package it.nexera.ris.web.beans.wrappers.logic.subjectViewWrapper;

import it.nexera.ris.common.enums.DocumentType;
import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.helpers.ResourcesHelper;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.domain.DocumentSubject;
import it.nexera.ris.persistence.beans.entities.domain.Subject;
import it.nexera.ris.persistence.beans.entities.domain.VisureRTF;
import it.nexera.ris.web.common.EntityLazyListModel;
import it.nexera.ris.web.converters.DateConverter;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.primefaces.component.column.Column;
import org.primefaces.component.commandbutton.CommandButton;
import org.primefaces.model.LazyDataModel;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class VisureRTFBindingWrapper extends BaseTab implements Serializable {

    private static final long serialVersionUID = 3419737923499817710L;

    private Subject subject;

    public VisureRTFBindingWrapper(Subject subject) {
        this.subject = subject;
    }

    public String getTabTitle() {
        return ResourcesHelper.getString("subjectViewVisureRTF");
    }

    public List<Column> getColumns() {
        List<Column> columns = new ArrayList<>();
        columns.add(getTextColumn("subjectViewVisureRTFUpdateDate", "updateDateString"));
        columns.add(getTextColumn("subjectViewVisureRTFNumFormality", "numFormality"));
        columns.add(getTextColumn("subjectViewVisureRTFConservatory", "landChargesRegistry"));

        CommandButton commandButton = new CommandButton();
        commandButton.setActionExpression(createMethodExpression(String.format("#{subjectBean.%s}",
                "downloadVisureRTF(tableVar.id)"), new Class[]{Long.class}));
        commandButton.setAjax(false);
        commandButton.setIcon("fa fa-download");
        columns.add(getButtonColumn("subjectViewVisureRTFFile", commandButton, "", "action_column"));

        return columns;
    }

    @Override
    LazyDataModel getLazyDataModel() {
        return new EntityLazyListModel(VisureRTF.class, new Criterion[]{
                Restrictions.eq("fiscalCodeVat", getSubject().getFiscalCodeVATNamber())},
                new Order[]{Order.asc("id")});
    }

    @Override
    public Long getCountTable() throws PersistenceBeanException, IllegalAccessException {
        return DaoManager.getCount(VisureRTF.class, "id", new Criterion[]{
                Restrictions.eq("fiscalCodeVat", getSubject().getFiscalCodeVATNamber())});
    }

    public Subject getSubject() {
        return subject;
    }

    public void setSubject(Subject subject) {
        this.subject = subject;
    }
}
