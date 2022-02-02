package it.nexera.ris.common.xml.wrappers;

import it.nexera.ris.persistence.beans.entities.domain.dictionary.CadastralTopology;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "cadastral_topology")
public class CadastralTopolodyList {

    @XmlElement(name = "data_entry", type = CadastralTopology.class)
    private List<CadastralTopology> cadastralTopologies;

    public CadastralTopolodyList() {
        super();
    }

    public List<CadastralTopology> getCadastralTopologies() {
        return cadastralTopologies;
    }

    public void setCadastralTopologies(List<CadastralTopology> cadastralTopologies) {
        this.cadastralTopologies = cadastralTopologies;
    }
}
