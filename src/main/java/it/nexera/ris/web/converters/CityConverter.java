package it.nexera.ris.web.converters;

import it.nexera.ris.common.helpers.LogHelper;
import it.nexera.ris.common.helpers.ValidationHelper;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.City;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

@FacesConverter(value = "cityConverter")
public class CityConverter implements Converter {

    public transient final Log log = LogFactory.getLog(CityConverter.class);

    @Override
    public Object getAsObject(FacesContext context, UIComponent component,
                              String value) {
        try {
            String[] val = value.split(" ", 2);

            if (!ValidationHelper.isNullOrEmpty(val)) {
                return DaoManager.get(City.class, new Criterion[]
                        {
                                Restrictions.eq("cfis", val[0]),
                                Restrictions.eq("description", val[1])
                        });
            }
        } catch (Exception e) {
            LogHelper.log(log, e);
        }

        return null;
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component,
                              Object value) {
        City city = ((City) value);
        return String.format("%s %s", city.getCfis(), city.getDescription());
    }

}
