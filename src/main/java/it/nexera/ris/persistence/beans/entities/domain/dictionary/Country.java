package it.nexera.ris.persistence.beans.entities.domain.dictionary;

import it.nexera.ris.common.helpers.ValidationHelper;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.Dictionary;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.text.WordUtils;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@Entity
@Table(name = "dic_country")
@SequenceGenerator(name = "ID_SEQ_GEN", initialValue = 1, sequenceName = "COUNTRY_SEQ", allocationSize = 1)
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "data_entry")
@Getter
@Setter
public class Country extends Dictionary {

    private static final long serialVersionUID = -6817812197958619229L;

    @Column(name = "external")
    private Boolean external;

    @Transient
    private String fiscalCode;

    @Transient
    public String getCamelCountryDescription(){
        if(!ValidationHelper.isNullOrEmpty(getDescription())) {
           return WordUtils.capitalizeFully(getDescription(), ' ');
        }
        return "";
    }

    public String getFiscalCode() {
        if (!ValidationHelper.isNullOrEmpty(getDescription())) {
            try {
                List<Nationality> nationalityList = DaoManager.load(Nationality.class, new Criterion[]{
                        Restrictions.eq("description", this.getDescription()).ignoreCase()
                });
                if (!ValidationHelper.isNullOrEmpty(nationalityList))
                    fiscalCode = nationalityList.get(0).getCfis();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return fiscalCode;
    }

    public void setFiscalCode(String fiscalCode) {
        this.fiscalCode = fiscalCode;
    }
}
