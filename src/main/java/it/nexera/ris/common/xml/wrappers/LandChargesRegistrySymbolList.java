package it.nexera.ris.common.xml.wrappers;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "land_charges_registry_symbol")
public class LandChargesRegistrySymbolList {

    @XmlElement(name = "data_entry", type = LandChargesRegistrySymbolWrapper.class)
    private List<LandChargesRegistrySymbolWrapper> landChargesRegistries;

    public LandChargesRegistrySymbolList() {
        super();
    }

    public List<LandChargesRegistrySymbolWrapper> getLandChargesRegistries() {
        return landChargesRegistries;
    }

    public void setLandChargesRegistries(List<LandChargesRegistrySymbolWrapper> landChargesRegistries) {
        this.landChargesRegistries = landChargesRegistries;
    }
}
