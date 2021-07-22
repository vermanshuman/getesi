package it.nexera.ris.persistence.beans.entities.domain.dictionary;

import it.nexera.ris.common.enums.TypeActEnum;
import it.nexera.ris.persistence.beans.entities.Dictionary;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Objects;

@Entity
@Table(name = "dic_type_act")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "data_entry")
public class TypeAct extends Dictionary {

    private static final long serialVersionUID = 7341809886279567072L;

    @Enumerated(EnumType.STRING)
    @Column(name = "type")
    private TypeActEnum type;

    @Column(name = "text_in_visura")
    private String textInVisura;

    @Column(name = "code_in_visura")
    private String codeInVisura;

    @Transient
    @XmlElement(name = "typeStr")
    private String typeStr;

    @Override
    public String toString() {
        return getCode()+ " - " + getDescription();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TypeAct typeAct = (TypeAct) o;
        return getId().equals(typeAct.getId());
    }

    @Override
    public int hashCode() {

        return Objects.hash(getId());
    }

    public TypeActEnum getType() {
        return type;
    }

    public void setType(TypeActEnum type) {
        this.type = type;
    }

    public String getTextInVisura() {
        return textInVisura;
    }

    public void setTextInVisura(String textInVisura) {
        this.textInVisura = textInVisura;
    }

    public String getCodeInVisura() {
        return codeInVisura;
    }

    public void setCodeInVisura(String codeInVisura) {
        this.codeInVisura = codeInVisura;
    }

    public String getTypeStr() {
        return typeStr;
    }

    public void setTypeStr(String typeStr) {
        this.typeStr = typeStr;
    }
}
