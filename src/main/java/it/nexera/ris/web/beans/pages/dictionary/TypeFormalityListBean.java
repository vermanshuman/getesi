package it.nexera.ris.web.beans.pages.dictionary;

import it.nexera.ris.common.enums.TypeActEnum;
import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.helpers.ComboboxHelper;
import it.nexera.ris.common.helpers.ValidationHelper;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.TypeFormality;
import it.nexera.ris.web.beans.EntityLazyListPageBean;
import org.hibernate.HibernateException;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.model.SelectItem;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@ManagedBean(name = "typeFormalityBean")
@ViewScoped
public class TypeFormalityListBean extends EntityLazyListPageBean<TypeFormality> implements Serializable {

    private static final long serialVersionUID = 4576200330090741930L;

    private String code;

    private String description;

    private String textInVisura;

    private TypeActEnum actType;

    private String initText;

    private String finalText;

    private String certificationText;

    private List<SelectItem> typeActEnumList;

    @Override
    public void onLoad() throws NumberFormatException, HibernateException, PersistenceBeanException,
            InstantiationException, IllegalAccessException, IOException {
        filterTableFromPanel();
        setTypeActEnumList(ComboboxHelper.fillList(TypeActEnum.class, true, false));
    }

    public void filterTableFromPanel() throws PersistenceBeanException, IllegalAccessException, InstantiationException {
        List<Criterion> restrictions = new ArrayList<>();
        if (!ValidationHelper.isNullOrEmpty(getCode())) {
            restrictions.add(Restrictions.eq("code", getCode()));
        }
        if (!ValidationHelper.isNullOrEmpty(getDescription())) {
            restrictions.add(Restrictions.ilike("description", getDescription(), MatchMode.ANYWHERE));
        }
        if (!ValidationHelper.isNullOrEmpty(getTextInVisura())) {
            restrictions.add(Restrictions.ilike("codeSDI", getTextInVisura(), MatchMode.ANYWHERE));
        }
        if (!ValidationHelper.isNullOrEmpty(getActType())) {
            restrictions.add(Restrictions.eq("type", getActType()));
        }
        if (!ValidationHelper.isNullOrEmpty(getInitText())) {
            restrictions.add(Restrictions.ilike("initText", getInitText(), MatchMode.ANYWHERE));
        }
        if (!ValidationHelper.isNullOrEmpty(getFinalText())) {
            restrictions.add(Restrictions.ilike("finalText", getFinalText(), MatchMode.ANYWHERE));
        }
        if (!ValidationHelper.isNullOrEmpty(getCertificationText())) {
            restrictions.add(Restrictions.ilike("certificationText", getCertificationText(), MatchMode.ANYWHERE));
        }

        this.loadList(TypeFormality.class, restrictions.toArray(new Criterion[0]), new Order[]
                {Order.asc("code")});
    }

    public void clearFilterPanel() throws PersistenceBeanException, IllegalAccessException, InstantiationException {
        setCode(null);
        setDescription(null);
        setTextInVisura(null);
        setCertificationText(null);
        setActType(null);
        setFinalText(null);
        setInitText(null);
        filterTableFromPanel();
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTextInVisura() {
        return textInVisura;
    }

    public void setTextInVisura(String textInVisura) {
        this.textInVisura = textInVisura;
    }

    public TypeActEnum getActType() {
        return actType;
    }

    public void setActType(TypeActEnum actType) {
        this.actType = actType;
    }

    public String getInitText() {
        return initText;
    }

    public void setInitText(String initText) {
        this.initText = initText;
    }

    public String getFinalText() {
        return finalText;
    }

    public void setFinalText(String finalText) {
        this.finalText = finalText;
    }

    public String getCertificationText() {
        return certificationText;
    }

    public void setCertificationText(String certificationText) {
        this.certificationText = certificationText;
    }

    public List<SelectItem> getTypeActEnumList() {
        return typeActEnumList;
    }

    public void setTypeActEnumList(List<SelectItem> typeActEnumList) {
        this.typeActEnumList = typeActEnumList;
    }
}