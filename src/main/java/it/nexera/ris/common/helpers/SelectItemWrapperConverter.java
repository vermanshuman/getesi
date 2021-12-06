package it.nexera.ris.common.helpers;

import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.xml.wrappers.SelectItemWrapper;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.Entity;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;
import java.util.ArrayList;
import java.util.List;

public class SelectItemWrapperConverter<T extends Entity> implements Converter {

    private Class<T> clazz;

    private List<SelectItemWrapper<T>> wrapperList;

    private SelectItemWrapperConverter() {
    }

    public SelectItemWrapperConverter(Class<T> c) {
        this(c, new ArrayList<>());
    }

    public SelectItemWrapperConverter(Class<T> clazz, List<SelectItemWrapper<T>> wrapperList) {
        this.clazz = clazz;
        this.wrapperList = wrapperList;
    }

    @Override
    public Object getAsObject(FacesContext facesContext, UIComponent uiComponent, String s) {
        if (!ValidationHelper.isNullOrEmpty(s)) {
            if (!ValidationHelper.isNullOrEmpty(getWrapperList())) {
                SelectItemWrapper<T> wrapper = getWrapperList().stream()
                        .filter(x -> x.getId().equals(Long.parseLong(s))).findFirst().orElse(null);
                if (ValidationHelper.isNullOrEmpty(wrapper)) {
                    try {
                        wrapper = getNotExistingItemAndAddItToWrapperList(s);
                    } catch (PersistenceBeanException | InstantiationException | IllegalAccessException e) {
                        throw new ConverterException(new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                "Error", "Not a valid theme."));
                    }
                }
                return wrapper;
            } else {
                try {
                    return new SelectItemWrapper<>((T) DaoManager.get(getClazz(), Long.parseLong(s)));
                } catch (Exception e) {
                    throw new ConverterException(new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Error", "Not a valid theme."));
                }
            }
        } else {
            return null;
        }
    }

    private SelectItemWrapper<T> getNotExistingItemAndAddItToWrapperList(String s)
            throws IllegalAccessException, PersistenceBeanException, InstantiationException {
        SelectItemWrapper<T> wrapper;
        T item = (T) DaoManager.get(getClazz(), Long.parseLong(s));
        if (ValidationHelper.isNullOrEmpty(item)) {
            throw new ConverterException(new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Error", "Item was not found."));
        }
        wrapper = new SelectItemWrapper<>(item);
        getWrapperList().add(wrapper);
        return wrapper;
    }

    @Override
    public String getAsString(FacesContext facesContext, UIComponent uiComponent, Object o) {
        if (o != null) {
            return String.valueOf(((SelectItemWrapper) o).getId());
        } else {
            return null;
        }
    }

    public Class<T> getClazz() {
        return clazz;
    }

    public void setClazz(Class<T> clazz) {
        this.clazz = clazz;
    }

    public List<SelectItemWrapper<T>> getWrapperList() {
        return wrapperList;
    }

    public void setWrapperList(List<SelectItemWrapper<T>> wrapperList) {
        this.wrapperList = wrapperList;
    }
}
