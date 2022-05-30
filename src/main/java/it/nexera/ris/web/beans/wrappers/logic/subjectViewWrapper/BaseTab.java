package it.nexera.ris.web.beans.wrappers.logic.subjectViewWrapper;


import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.helpers.ResourcesHelper;
import org.primefaces.component.column.Column;
import org.primefaces.component.commandbutton.CommandButton;
import org.primefaces.component.datatable.DataTable;
import org.primefaces.component.tabview.Tab;
import org.primefaces.model.LazyDataModel;

import javax.el.MethodExpression;
import javax.el.ValueExpression;
import javax.faces.component.html.HtmlOutputText;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import java.util.List;

public abstract class BaseTab {

    private static final String TABLE_VAR = "#{tableVar.%s}";

    abstract String getTabTitle();

    abstract List<Column> getColumns();

    abstract LazyDataModel getLazyDataModel();

    abstract Long getCountTable() throws PersistenceBeanException, IllegalAccessException;

    public Tab getTab() throws PersistenceBeanException, IllegalAccessException {
        Tab tab = new Tab();
        if (getCountTable() != 0L) {
            tab.setTitle(String.format("%s (%d)", getTabTitle(), getCountTable()));
            tab.setTitleStyle("font-weight: bold;");
            DataTable dataTable = getTable();
            dataTable.getChildren().addAll(getColumns());
            tab.getChildren().add(dataTable);
        } else {
            tab.setTitle(getTabTitle());
            tab.setTitleStyle("font-weight: normal;");
        }
        return tab;
    }

    DataTable getTable() {
        DataTable dataTable = new DataTable();
        dataTable.setValue(getLazyDataModel());
        dataTable.setValueExpression("value", createValueExpression("#{tabItem.lazyDataModel}", LazyDataModel.class));
        dataTable.setPaginatorTemplate("{FirstPageLink} {PreviousPageLink} {RowsPerPageDropdown} {CurrentPageReport} {NextPageLink} {LastPageLink}");
        dataTable.setRows(10);
        dataTable.setPaginator(true);
        dataTable.setVar("tableVar");
        dataTable.setLazy(true);
        return dataTable;
    }

    private ValueExpression createValueExpression(String valueExpression, Class<?> valueType) {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        return facesContext.getApplication().getExpressionFactory().createValueExpression(facesContext.getELContext(),
                valueExpression, valueType);
    }

    MethodExpression createMethodExpression(String valueExpression, Class<?>[] valueType) {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        return facesContext.getApplication().getExpressionFactory().createMethodExpression(facesContext.getELContext(),
                valueExpression, void.class, valueType);
    }

    Column getTextColumn(String titleResourceId, String expression) {
        return getTextColumn(titleResourceId, expression, null, String.class, "");
    }

    Column getTextColumn(String titleResourceId, String expression, String width) {
        return getTextColumn(titleResourceId, expression, null, String.class, width);
    }

    Column getTextColumn(String titleResourceId, String expression, Converter converter, String width) {
        return getTextColumn(titleResourceId, expression, converter, String.class, width);
    }

    Column getTextColumn(String titleResourceId, String expression, Converter converter, Class classValue, String width) {
        Column column = new Column();
        column.setHeaderText(ResourcesHelper.getString(titleResourceId));
        column.setWidth(width);
        HtmlOutputText text = new HtmlOutputText();
        if (expression != null) {
            text.setValueExpression("value", createValueExpression(String.format(TABLE_VAR, expression), classValue));
        }
        if (converter != null) {
            text.setConverter(converter);
        }
        column.getChildren().add(text);
        return column;
    }

    Column getButtonColumn(String titleResourceId, CommandButton button) {
        return getButtonColumn(titleResourceId, button, "text-align:center;", "action_button_column");
    }

    Column getButtonColumn(String titleResourceId, CommandButton button, String style, String styleClass) {
        Column column = new Column();
        column.setStyleClass(styleClass);
        column.setStyle(style);
        if (titleResourceId != null) {
            column.setHeaderText(ResourcesHelper.getString(titleResourceId));
        }
        column.getChildren().add(button);
        return column;
    }

    Column getButtonColumn(String titleResourceId, CommandButton button, String width) {
        return getButtonColumn(titleResourceId, button, "text-align:center;", "action_button_column", width);
    }

    Column getButtonColumn(String titleResourceId, CommandButton button, String style, String styleClass, String width) {
        Column column = new Column();
        column.setStyleClass(styleClass);
        column.setStyle(style);
        column.setWidth(width);
        if (titleResourceId != null) {
            column.setHeaderText(ResourcesHelper.getString(titleResourceId));
        }
        column.getChildren().add(button);
        return column;
    }
}
