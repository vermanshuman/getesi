package it.nexera.ris.web.beans.wrappers.logic.subjectViewWrapper;

import it.nexera.ris.common.enums.DocumentType;
import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.helpers.ResourcesHelper;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.domain.Document;
import it.nexera.ris.persistence.beans.entities.domain.DocumentSubject;
import it.nexera.ris.persistence.beans.entities.domain.Subject;
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


public class SyntheticBindingWrapper extends BaseTab implements Serializable {

    private static final long serialVersionUID = -7247560203810610222L;

    private List<Long> listIds;

    public SyntheticBindingWrapper(List<Long> ids) {
        this.listIds = ids;
    }

    public String getTabTitle() {
        return ResourcesHelper.getString("subjectViewSynthetic");
    }

    public List<Column> getColumns() {
        List<Column> columns = new ArrayList<>();
        columns.add(getTextColumn("subjectViewSyntheticConservatory", "office"));
        columns.add(getTextColumn("subjectViewSyntheticDate", "date", new DateConverter(), Date.class, ""));

        CommandButton commandButton = new CommandButton();
        commandButton.setActionExpression(createMethodExpression(String.format("#{subjectBean.%s}",
                "downloadEstateFormalityPDF(tableVar.document.id)"), new Class[]{Long.class}));
        commandButton.setIcon("fa fa-fw fa-file-pdf-o");
        commandButton.setAjax(false);
        columns.add(getButtonColumn("subjectViewSyntheticPDF", commandButton));

        columns.add(getTextColumn("subjectViewSyntheticNote", null));
        return columns;
    }

    @Override
    LazyDataModel getLazyDataModel() {
        return new EntityLazyListModel(DocumentSubject.class, new Criterion[]{
                Restrictions.in("subject.id", getListIds()),
                Restrictions.eq("type", DocumentType.ESTATE_FORMALITY),
                Restrictions.or(
                        Restrictions.isNull("useSubjectFromXml"),
                        Restrictions.eq("useSubjectFromXml", false)
                )
        }, new Order[]{Order.asc("id")});
    }

    @Override
    public Long getCountTable() throws PersistenceBeanException, IllegalAccessException {
        return DaoManager.getCount(DocumentSubject.class, "id", new Criterion[]{
                Restrictions.in("subject.id", getListIds()),
                Restrictions.eq("type", DocumentType.ESTATE_FORMALITY),
                Restrictions.or(
                        Restrictions.isNull("useSubjectFromXml"),
                        Restrictions.eq("useSubjectFromXml", false)
                )
        });
    }

    public List<Long> getListIds() {
        return listIds;
    }

    public void setListIds(List<Long> listIds) {
        this.listIds = listIds;
    }
}
