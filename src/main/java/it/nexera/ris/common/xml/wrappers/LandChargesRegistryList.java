package it.nexera.ris.common.xml.wrappers;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "land_charges_registry")
public class LandChargesRegistryList {
    @XmlElement(name = "data_entry", type = LandChargesRegistryWrapper.class)
    private List<LandChargesRegistryWrapper> landChargesRegistries;

    public LandChargesRegistryList() {
        super();
    }

    public List<LandChargesRegistryWrapper> getLandChargesRegistries() {
        return landChargesRegistries;
    }

    public void setLandChargesRegistries(List<LandChargesRegistryWrapper> landChargesRegistries) {
        this.landChargesRegistries = landChargesRegistries;
    }
}
