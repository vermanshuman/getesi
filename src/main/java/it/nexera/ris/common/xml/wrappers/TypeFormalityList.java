package it.nexera.ris.common.xml.wrappers;

import it.nexera.ris.persistence.beans.entities.domain.dictionary.TypeFormality;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "type_formality")
public class TypeFormalityList {

    @XmlElement(name = "data_entry", type = TypeFormality.class)
    private List<TypeFormality> typeFormalities;

    public TypeFormalityList() {
        super();
    }

    public List<TypeFormality> getTypeFormalities() {
        return typeFormalities;
    }

    public void setTypeFormalities(List<TypeFormality> typeFormalities) {
        this.typeFormalities = typeFormalities;
    }
}
