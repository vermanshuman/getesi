package it.nexera.ris.web.converters;

import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.helpers.LogHelper;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.CostConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.HibernateException;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

@FacesConverter(value = "costConfigurationConverter")
public class CostConfigurationConverter implements Converter {

    public transient final Log log = LogFactory
            .getLog(CostConfigurationConverter.class);

    @Override
    public Object getAsObject(FacesContext context, UIComponent component,
                              String value) {
        try {
            return DaoManager.get(CostConfiguration.class, new Criterion[]
                    {Restrictions.eq("name", value)});
        } catch (HibernateException | InstantiationException
                | IllegalAccessException | PersistenceBeanException e) {
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
