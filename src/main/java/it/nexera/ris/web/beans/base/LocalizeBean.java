package it.nexera.ris.web.beans.base;

import it.nexera.ris.common.enums.LocaleType;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import java.io.Serializable;
import java.util.Locale;

@ManagedBean(name = "localizeBean")
@SessionScoped
public class LocalizeBean implements Serializable {
    private static final long serialVersionUID = -4670177084406774037L;
    
    /*private Locale            userLocale       = FacesContext
                                                       .getCurrentInstance()
                                                       .getExternalContext()
                                                       .getRequestLocale();*/

    private String locale;

    public LocalizeBean() {
        /*if (userLocale.getLanguage().equals(LocaleType.IT.getValue()))
        {
            setLocale(LocaleType.IT.getValue());
        }
        else if (userLocale.getLanguage().equals(LocaleType.EN.getValue()))
        {
            this.setLocale(LocaleType.EN.getValue());
        }
        else
        {
            setLocale(LocaleType.IT.getValue());
        }*/
        setLocale(LocaleType.IT.getValue());
    }

    //#{localizeBean.locale}
    public void setLocale(String locale) {
        this.locale = locale;
    }

    public String getLocale() {
        return locale;
    }

    public LocaleType getLocaleType() {
        return LocaleType.fromString(locale);
    }

    public Locale getCurrentLocale() {
        return new Locale(locale);
    }
}
