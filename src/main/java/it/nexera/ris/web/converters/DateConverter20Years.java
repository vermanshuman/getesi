package it.nexera.ris.web.converters;

import it.nexera.ris.common.helpers.DateTimeHelper;

import java.util.Calendar;
import java.util.Date;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

/**
 * Date converter
 */
@FacesConverter(value = "dateTimeConverter20Years")
public class DateConverter20Years implements Converter {

    public Object getAsObject(FacesContext context, UIComponent component,
                              String value) {
        if ((value != null) && (!value.isEmpty()) && (value.length() > 2)) {
            return DateTimeHelper.fromString(value);
        } else {
            return null;
        }
    }

    public String getAsString(FacesContext context, UIComponent component,
                              Object value) {
        if (value != null) {
        	Calendar cal = Calendar.getInstance();
        	Date date = (Date) value;
        	cal.setTime(date);
        	cal.add(Calendar.YEAR, 20); 
        	date = cal.getTime();
        	value = date;
            return BaseConverter.convertToDateString(value);
        } else {
            return null;
        }
    }
}
