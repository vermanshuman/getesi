package it.nexera.ris.web.beans;

import it.nexera.ris.common.helpers.*;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public abstract class BaseValidationPageBean extends PageBean {

    public void addFieldException(String id, String message, Boolean showMessage) {
        id = fixComponentId(id);
        this.addFieldException(this.getComponentById(id), message, showMessage);
    }

    public void addException(String message) {
        addException(message, false);
    }

    public void addException(String message, boolean fullMessage) {
        this.setValidationFailed(true);
        this.getExceptions().add(fullMessage ? message : ResourcesHelper.getValidation(message));
    }

    public void markInvalid(UIComponent component, String message) {
        this.getMarkedIvalidFields().add(completeId(component));
        this.setValidationFailed(true);
        ValidatorHelper.markNotValid(component,
                ResourcesHelper.getValidation(message), this.getContext(),
                this.getTabs());
    }

    public void addFieldException(UIComponent component, String message,
                                  Boolean showMessage) {
        this.getMarkedIvalidFields().add(completeId(component));
        this.setValidationFailed(true);
        ValidatorHelper.markNotValid(component,
                ResourcesHelper.getValidation(message), this.getContext(),
                this.getTabs());
        if (Boolean.TRUE.equals(showMessage)) {
            this.getExceptions().add(ResourcesHelper.getValidation(message));
        }
    }
}
