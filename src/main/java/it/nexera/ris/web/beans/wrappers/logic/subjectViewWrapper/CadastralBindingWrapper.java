package it.nexera.ris.web.beans.wrappers.logic.subjectViewWrapper;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.faces.component.html.HtmlOutputText;

import it.nexera.ris.common.helpers.ValidationHelper;
import it.nexera.ris.persistence.view.FormalityView;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.primefaces.component.column.Column;
import org.primefaces.component.commandbutton.CommandButton;
import org.primefaces.component.datatable.DataTable;
import org.primefaces.component.tabview.Tab;
import org.primefaces.model.LazyDataModel;

import it.nexera.ris.common.enums.DocumentType;
import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.helpers.ResourcesHelper;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.domain.DocumentSubject;
import it.nexera.ris.persistence.beans.entities.domain.Subject;
import it.nexera.ris.web.common.EntityLazyListModel;
import it.nexera.ris.web.converters.DateConverter;


public class CadastralBindingWrapper extends BaseTab implements Serializable {

    private static final long serialVersionUID = 8771232801101335683L;

    private DocumentType type;

    private FormalityBindingWrapper formalityBindingWrapper;

    private List<Long> listIds;

    private LazyDataModel<FormalityView> lazyModel;

    public CadastralBindingWrapper(List<Long> ids, Subject subject, DocumentType type) {
        this.listIds = ids;
        this.type = type;
        setFormalityBindingWrapper(new FormalityBindingWrapper(subject,ids));
    }

    public void loadData(Boolean forCadastralBindingWrapper){
        getFormalityBindingWrapper().loadData(forCadastralBindingWrapper);
    }
    @Override
    public Tab getTab() throws PersistenceBeanException, IllegalAccessException {
    	Tab tab = super.getTab();
    	HtmlOutputText text = new HtmlOutputText();

    	if (DocumentType.CADASTRAL != getType()) {
    	   	text.setValue(ResourcesHelper.getString("subjectViewIndirectCadasters"));
    	
        	tab.getChildren().add(0, text);
        	
        	DataTable table = formalityBindingWrapper.getTable();
        	
        	table.getChildren().addAll(formalityBindingWrapper.getColumns());
        	
        	tab.getChildren().add(0, table);
        	
        	text = new HtmlOutputText();
        	text.setValue(ResourcesHelper.getString("subjectViewCadastralRequest"));
        	
        	tab.getChildren().add(0, text);
    	}
    	    	
    	return tab;
    }

    public String getTabTitle() {
        if (DocumentType.CADASTRAL == getType()) {
            return ResourcesHelper.getString("subjectViewCadastral");
        } else {
            return ResourcesHelper.getString("subjectViewDocuments");
        }
    }

    public List<Column> getColumns() {
        List<Column> columns = new ArrayList<>();
        columns.add(getTextColumn("subjectViewCadastralOffice", "province"));
        columns.add(getTextColumn("subjectViewCadastralDate", "date", new DateConverter(), Date.class, ""));

        CommandButton commandButton = new CommandButton();
        commandButton.setActionExpression(createMethodExpression(String.format("#{subjectBean.%s}",
                "downloadPropertyPDF(tableVar.document.id)"), new Class[]{Long.class}));
        commandButton.setAjax(false);
        commandButton.setIcon("fa fa-fw new-pdf-icon");
        commandButton.setStyleClass("hide-pdf-bg");
        columns.add(getButtonColumn("subjectViewCadastralPDF", commandButton, "", "action_column"));
        columns.add(getTextColumn("subjectViewCadastralNote", null));
        return columns;
    }

    @Override
    LazyDataModel getLazyDataModel() {
        return new EntityLazyListModel(DocumentSubject.class, new Criterion[]{
                Restrictions.in("subject.id", getListIds()),
                Restrictions.eq("type", getType())
        }, new Order[]{Order.asc("id")});
    }

    @Override
    public Long getCountTable() throws PersistenceBeanException, IllegalAccessException {
        return DaoManager.getCount(DocumentSubject.class, "id", new Criterion[]{
                Restrictions.in("subject.id", getListIds()),
                Restrictions.eq("type", getType())
        }) + formalityBindingWrapper.getCountTable();
    }

    public List<Long> getListIds() {
        return listIds;
    }

    public void setListIds(List<Long> listIds) {
        this.listIds = listIds;
    }

    public DocumentType getType() {
        return type;
    }

    public void setType(DocumentType type) {
        this.type = type;
    }

	public FormalityBindingWrapper getFormalityBindingWrapper() {
		return formalityBindingWrapper;
	}

	public void setFormalityBindingWrapper(FormalityBindingWrapper formalityBindingWrapper) {
		this.formalityBindingWrapper = formalityBindingWrapper;
	}

    public LazyDataModel<FormalityView> getLazyModel() {
        return new EntityLazyListModel(DocumentSubject.class, new Criterion[]{
                Restrictions.in("subject.id", getListIds()),
                Restrictions.eq("type", getType())
        }, new Order[]{Order.asc("id")});
    }

    public void setLazyModel(LazyDataModel<FormalityView> lazyModel) {
        this.lazyModel = lazyModel;
    }
}
