package it.nexera.ris.web.converters;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

@FacesConverter(value = "uppercaseConverter")
public class UppercaseConverter implements Converter {

	@Override
	public Object getAsObject(FacesContext context, UIComponent component, String value) {
		if (value == null || ((String) value).isEmpty()) {
	        return null;
	    }
		return value;
	}

	@Override
	public String getAsString(FacesContext context, UIComponent component, Object value) {
		if (value == null || ((String) value).isEmpty()) {
	        return null;
	    }

	    String string = (String) value;
	    return string.toUpperCase();
	}

}
