package it.nexera.ris.web.converters;


import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@FacesConverter("mailLetterConverter")
public class MailLetterConverter implements Converter {

    @Override
    public Object getAsObject(FacesContext facesContext, UIComponent uiComponent, String s) {
        return null; // stub
    }

    @Override
    public String getAsString(FacesContext facesContext, UIComponent uiComponent, Object o) {
        String mailName = o.toString();
        Pattern pattern = Pattern.compile("\\w");
        Matcher matcher = pattern.matcher(mailName);
        return matcher.find() ? matcher.group().toUpperCase() : "E";
    }
}
