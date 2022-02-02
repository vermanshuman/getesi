package it.nexera.ris.web.converters;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

/**
 * Long converter
 */
@FacesConverter(value = "longConverter")
public class LongConverter implements Converter {

    @Override
    public Object getAsObject(FacesContext context, UIComponent component, String value) {
    	try {
    		return Long.parseLong(value);
    	}
    	
    	catch(Exception e) {
    		
    	}
    	
    	return null;
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Object value) {
        return ((Long) value).toString();
    }

}
