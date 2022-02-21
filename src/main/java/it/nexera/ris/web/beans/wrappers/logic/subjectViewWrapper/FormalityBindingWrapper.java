package it.nexera.ris.web.beans.wrappers.logic.subjectViewWrapper;

import it.nexera.ris.common.enums.DocumentType;
import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.helpers.EstateSituationHelper;
import it.nexera.ris.common.helpers.LogHelper;
import it.nexera.ris.common.helpers.ResourcesHelper;
import it.nexera.ris.common.helpers.ValidationHelper;
import it.nexera.ris.persistence.beans.dao.CriteriaAlias;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.domain.Formality;
import it.nexera.ris.persistence.beans.entities.domain.Subject;
import it.nexera.ris.persistence.view.FormalityView;
import it.nexera.ris.web.common.EntityLazyListModel;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.hibernate.sql.JoinType;
import org.primefaces.component.column.Column;
import org.primefaces.component.commandbutton.CommandButton;
import org.primefaces.model.LazyDataModel;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


public class FormalityBindingWrapper extends BaseTab implements Serializable {

    private static final long serialVersionUID = 2317727685232261426L;

    protected static transient final Log log = LogFactory.getLog(FormalityBindingWrapper.class);

    private Subject subject;

    List<Long> list;

    private List<Long> listIds;

    public FormalityBindingWrapper(Subject subject,List<Long> ids) {
        this.subject = subject;
        setListIds(ids);
        setList(new ArrayList<>());
    }

    public void loadData(Boolean forCadastralBindingWrapper){
        try {
            List<Criterion> criteria = new ArrayList<>();

            criteria.add(Restrictions.in("sub.id", getListIds()));
            List<Formality> unfilteredList =
                    DaoManager.load(Formality.class, new CriteriaAlias[]{new CriteriaAlias
                            ("sectionC", "sectionC", JoinType.INNER_JOIN),
                            new CriteriaAlias("sectionC.subject", "sub", JoinType.INNER_JOIN)
                    }, criteria.toArray(new Criterion[0]));

            List<Long> filteredList = new ArrayList<>();

            for(Formality f : unfilteredList) {
                if(!(forCadastralBindingWrapper && (f.getDocument() == null ||
                        f.getDocument().getTypeId() != DocumentType.INDIRECT_CADASTRAL_REQUEST.getId())))
                    filteredList.add(f.getId());
            }

            setList(filteredList);
        } catch (Exception e) {
            LogHelper.log(log, e);
        }
    }
    public String getTabTitle() {
        return ResourcesHelper.getString("subjectViewFormality");
    }

    public List<Column> getColumns() {
        List<Column> columns = new ArrayList<>();
        columns.add(getTextColumn("subjectViewFormalityConservatory", "conservatory", "15%"));
        columns.add(getTextColumn("subjectViewFormalityDate", "presentationDateStr", "6%"));
        columns.add(getTextColumn("subjectViewFormalityRegisterG", "generalRegister", "6%"));
        columns.add(getTextColumn("subjectViewFormalityRegisterP", "particularRegister", "6%"));
        columns.add(getTextColumn("subjectViewFormalityType",
                "getSpecialSubjectType(subjectBean.entity.id)", "3%"));
        columns.add(getTextColumn("subjectViewFormalityDescription", "actType"));
        columns.add(getTextColumn("status", "statusStr", "6%"));

        CommandButton commandButton = new CommandButton();
        commandButton.setActionExpression(createMethodExpression(String.format("#{subjectBean.%s}",
                "downloadFormalityPDF(tableVar.documentId)"), new Class[]{Long.class}));
        commandButton.setAjax(false);
        commandButton.setIcon("fa fa-fw fa-file-pdf-o red-file icon-align");
        columns.add(getButtonColumn("subjectViewFormalityPDF", commandButton, "", "action_column"));

        commandButton = new CommandButton();
        commandButton.setActionExpression(createMethodExpression(String.format("#{subjectBean.%s}",
                "goToFormality(tableVar.id)"), new Class[]{Long.class}));
        commandButton.setValue(ResourcesHelper.getString("subjectViewFormalityDetail"));
        columns.add(getButtonColumn("subjectViewFormalityDetail", commandButton));

        return columns;
    }


    @Override
    LazyDataModel getLazyDataModel() {
        return new EntityLazyListModel(FormalityView.class, new Criterion[]{
                (!ValidationHelper.isNullOrEmpty(getList()) ?
                        Restrictions.in("id", getList()) :
                        Restrictions.eq("id", 0L))
        }, new Order[]{Order.asc("conservatoryName"), Order.asc("presentationDate"), Order.asc("generalRegister")});
    }

    @Override
    public Long getCountTable() throws PersistenceBeanException, IllegalAccessException {
        return (long) getList().size();
    }

    public Subject getSubject() {
        return subject;
    }

    public void setSubject(Subject subject) {
        this.subject = subject;
    }

    public List<Long> getList() {
        return list;
    }

    public void setList(List<Long> list) {
        this.list = list;
    }

    public List<Long> getListIds() {
        return listIds;
    }

    public void setListIds(List<Long> listIds) {
        this.listIds = listIds;
    }
}
