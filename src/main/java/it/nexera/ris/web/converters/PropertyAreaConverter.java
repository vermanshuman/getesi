package it.nexera.ris.web.converters;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

@FacesConverter(value = "propertyAreaConverter")
public class PropertyAreaConverter implements Converter {

    @Override
    public Object getAsObject(FacesContext context, UIComponent component, String value) {
        if (value != null) {
            return Double.parseDouble(value);
        } else {
            return null;
        }
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Object value) {
        if (value != null && Integer.toString(((Double) value).intValue()).length() == 1) {
            return "0" + Integer.toString(((Double) value).intValue());
        } else if (value != null) {
            return Integer.toString(((Double) value).intValue());
        } else {
            return null;
        }
    }

    public static String getString(Double value) {
        if (value != null && Integer.toString(value.intValue()).length() == 1) {
            return "0" + Integer.toString(value.intValue());
        } else if (value != null) {
            return Integer.toString(value.intValue());
        } else {
            return "";
        }
    }
}
