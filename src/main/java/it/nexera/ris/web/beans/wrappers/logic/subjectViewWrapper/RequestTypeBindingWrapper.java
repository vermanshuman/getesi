package it.nexera.ris.web.beans.wrappers.logic.subjectViewWrapper;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.primefaces.component.column.Column;
import org.primefaces.component.commandbutton.CommandButton;
import org.primefaces.model.LazyDataModel;

import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.helpers.ResourcesHelper;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.domain.Request;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.RequestType;
import it.nexera.ris.web.common.EntityLazyListModel;
import it.nexera.ris.web.converters.DateConverter;


public class RequestTypeBindingWrapper extends BaseTab implements Serializable {

    private static final long serialVersionUID = -6547841494395752324L;

    private List<Long> listIds;
    
    private RequestType requestType;

    private boolean onlyView;

    public RequestTypeBindingWrapper(List<Long> ids, RequestType requestType) {
        this.listIds = ids;
        this.requestType = requestType;
    }

    public RequestTypeBindingWrapper(List<Long> ids, RequestType requestType, boolean onlyView) {
        this.listIds = ids;
        this.requestType = requestType;
        this.onlyView = onlyView;
    }

    @Override
    public String getTabTitle() {
        return this.getRequestType().getName();
    }

    @Override
    public LazyDataModel getLazyDataModel() {
        return new EntityLazyListModel(Request.class, 
                new Criterion[]{
                        Restrictions.and(Restrictions.in("subject.id", getListIds()),
                                Restrictions.eq("requestType", getRequestType())
                                ,
                                Restrictions.or(
                                        Restrictions.isNull("isDeleted"),
                                        Restrictions.eq("isDeleted", false)
                                        ))}, new Order[]{Order.asc("id")});
    }

    @Override
    public Long getCountTable() throws PersistenceBeanException, IllegalAccessException {
        return DaoManager.getCount(Request.class, "id", 
                new Criterion[]{
                        Restrictions.and(Restrictions.in("subject.id", getListIds()),
                                Restrictions.eq("requestType", getRequestType()),
                                Restrictions.or(
                                        Restrictions.isNull("isDeleted"),
                                        Restrictions.eq("isDeleted", false)
                                        ))});
    }

    @Override
    public List<Column> getColumns() {
        List<Column> columns = new ArrayList<>();
        columns.add(getTextColumn("subjectViewRequestDate", "createDate", new DateConverter(), Date.class, ""));
        if(!isOnlyView()) {
            columns.add(getTextColumn("subjectViewRequestService", "service"));
        }
        columns.add(getTextColumn("subjectViewRequestType", "service.name"));
        columns.add(getTextColumn("subjectViewRequestOffice", "aggregationLandChargesRegistry"));
        columns.add(getTextColumn("subjectViewRequestClient", "client"));

        CommandButton commandButton = new CommandButton();
        commandButton.setActionExpression(createMethodExpression(String.format("#{subjectBean.%s}", "showFile(tableVar)"), new Class[]{Request.class}));
        commandButton.setIcon("fa fa-fw fa-file-pdf-o red-file icon-align");

        columns.add(getButtonColumn("subjectViewRequestOutput", commandButton, "", "action_column"));

        columns.add(getTextColumn("subjectViewRequestBilling", "billingClient"));
        columns.add(getTextColumn("subjectViewRequestNote", "note"));
        return columns;
    }

    public List<Long> getListIds() {
        return listIds;
    }

    public void setListIds(List<Long> listIds) {
        this.listIds = listIds;
    }

    public RequestType getRequestType() {
        return requestType;
    }

    public void setRequestType(RequestType requestType) {
        this.requestType = requestType;
    }

    public boolean isOnlyView() {
        return onlyView;
    }

    public void setOnlyView(boolean onlyView) {
        this.onlyView = onlyView;
    }
}
