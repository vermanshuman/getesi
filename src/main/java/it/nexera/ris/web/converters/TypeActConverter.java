package it.nexera.ris.web.converters;


import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.IndexedEntity;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.TypeAct;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;
import javax.faces.convert.FacesConverter;


@FacesConverter(value = "typeActConverter")
public class TypeActConverter implements Converter {

    protected transient final Log log = LogFactory.getLog(getClass());

    @Override
    public Object getAsObject(FacesContext facesContext, UIComponent component,
                              String submittedValue) {
        if (submittedValue.trim().equals("")) {
            return null;
        } else {
            try {
                long id = Long.parseLong(submittedValue);
                return DaoManager.get(TypeAct.class, id);
            } catch (Exception exception) {
                log.error(exception);

                throw new ConverterException(new FacesMessage(
                        FacesMessage.SEVERITY_ERROR, "Conversion Error",
                        "Not a valid entity"));
            }
        }
    }

    @Override
    public String getAsString(FacesContext facesContext, UIComponent component,
                              Object value) {
        if (value == null) {
            return null;
        } else {
            return String.valueOf(((IndexedEntity) value).getId());
        }
    }

}
