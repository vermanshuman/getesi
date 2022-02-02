package it.nexera.ris.persistence.beans.entities.domain.dictionary;

import it.nexera.ris.common.helpers.ValidationHelper;
import it.nexera.ris.persistence.beans.entities.Dictionary;

import javax.persistence.Entity;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang3.text.WordUtils;

@Entity
@Table(name = "dic_country")
@SequenceGenerator(name = "ID_SEQ_GEN", initialValue = 1, sequenceName = "COUNTRY_SEQ", allocationSize = 1)
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "data_entry")
public class Country extends Dictionary {

    private static final long serialVersionUID = -6817812197958619229L;
    
    @Transient
    public String getCamelCountryDescription(){
        if(!ValidationHelper.isNullOrEmpty(getDescription())) {
           return WordUtils.capitalizeFully(getDescription(), ' ');
        }
        return "";
    }

}
