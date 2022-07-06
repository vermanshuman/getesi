package it.nexera.ris.web.converters;


import it.nexera.ris.common.helpers.ValidationHelper;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.IndexedEntity;
import it.nexera.ris.persistence.beans.entities.domain.TaxRate;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;
import javax.faces.convert.FacesConverter;


@FacesConverter(value = "taxRateConverter")
public class TaxRateConverter implements Converter {

    protected transient final Log log = LogFactory.getLog(getClass());

    @Override
    public Object getAsObject(FacesContext facesContext, UIComponent component,
                              String taxRateId) {
        if (ValidationHelper.isNullOrEmpty(taxRateId) || taxRateId.equalsIgnoreCase("- Non selezionato -")) {
            return null;
        } else {
            try {
                long id = Long.parseLong(taxRateId);
                return DaoManager.get(TaxRate.class, id);
            } catch (Exception exception) {
                log.error(exception);
            }
        }
        return null;
    }

    @Override
    public String getAsString(FacesContext facesContext, UIComponent component,
                              Object value) {
        if (value == null || StringUtils.isBlank(value.toString())) {
            return null;
        } else {
            return String.valueOf(value);
        }
    }

}
