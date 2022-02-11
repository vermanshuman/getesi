package it.nexera.ris.common.helpers;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import java.util.Date;

@FacesConverter("fullDateTimeConverter")
public class FullDateTimeConverter implements Converter {

    @Override
    public Object getAsObject(FacesContext facesContext, UIComponent uiComponent, String s) {
        if (!ValidationHelper.isNullOrEmpty(s)) {
            Date date = DateTimeHelper.fromString(s, "dd/MM/yyyy");
            if (date == null) {
                date = DateTimeHelper.fromString(s);
            }
            return date;
        }
        return null;
    }

    @Override
    public String getAsString(FacesContext facesContext, UIComponent uiComponent, Object o) {
        if (o != null && o instanceof Date) {
            return DateTimeHelper.toFormatedString((Date) o, DateTimeHelper.getDatePattern());
        }
        return null;
    }
}
