package it.nexera.ris.web.converters;

import it.nexera.ris.common.helpers.LogHelper;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.Province;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

@FacesConverter(value = "provinceConverter")
public class ProvinceConverter implements Converter {

    public transient final Log log = LogFactory.getLog(ProvinceConverter.class);

    @Override
    public Object getAsObject(FacesContext context, UIComponent component,
                              String value) {
        try {
            return DaoManager.get(Province.class, new Criterion[]
                    {
                            Restrictions.eq("description", value)
                    });
        } catch (Exception e) {
            LogHelper.log(log, e);
        }

        return null;
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component,
                              Object value) {
        return value.toString();
    }

}
