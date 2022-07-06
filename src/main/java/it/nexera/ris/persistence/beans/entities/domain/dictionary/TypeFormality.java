package it.nexera.ris.persistence.beans.entities.domain.dictionary;

import it.nexera.ris.common.enums.TypeActEnum;
import it.nexera.ris.persistence.beans.entities.Dictionary;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@Entity
@Table(name = "dic_type_formality")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "data_entry")
@Getter
@Setter
public class TypeFormality extends Dictionary {

    private static final long serialVersionUID = 6749546567948835245L;

    @Column(name = "text_in_visura")
    private String textInVisura;

    @Column(name = "type_act")
    private TypeActEnum type;

    @Column(name = "init_text")
    private String initText;

    @Column(name = "final_text")
    private String finalText;

    @Transient
    @XmlElement(name = "type_act")
    private String type_act;

    @Column(name = "prejudicial")
    private Boolean prejudicial;
    
    @Column(name = "certification_text")
    private String certificationText;
    
    @Column(name = "verb_alienated")
    private String verbAlienated;

    @Column(name = "sales_development")
    private Boolean salesDevelopment;

    @Column(name = "sales_development_omi")
    private Boolean salesDevelopmentOMI;

    private Boolean renewal;

    @Column(name = "is_deleted")
    private Boolean isDeleted;

    @Override
    public String toString() {
        return getCode();
    }

}
