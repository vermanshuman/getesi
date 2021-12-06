package it.nexera.ris.common.xml.wrappers;

import it.nexera.ris.persistence.beans.entities.domain.dictionary.TypeAct;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "type_act")
public class TypeActList {

    @XmlElement(name = "data_entry", type = TypeAct.class)
    private List<TypeAct> typeActs;

    public TypeActList() {
        super();
    }

    public List<TypeAct> getTypeActs() {
        return typeActs;
    }

    public void setTypeActs(List<TypeAct> typeActs) {
        this.typeActs = typeActs;
    }
}
