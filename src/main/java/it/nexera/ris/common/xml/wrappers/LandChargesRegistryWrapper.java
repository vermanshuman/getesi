package it.nexera.ris.common.xml.wrappers;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "data_entry")
public class LandChargesRegistryWrapper {

    @XmlElement(name = "name")
    private String name;

    @XmlElement(name = "symbol")
    private String cityCfis;

    @XmlElement(name = "city")
    private String cityDescription;

    @XmlElement(name = "province")
    private String provinceDescription;

    @XmlElement(name = "type")
    private String typeName;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCityCfis() {
        return cityCfis;
    }

    public void setCityCfis(String cityCfis) {
        this.cityCfis = cityCfis;
    }

    public String getCityDescription() {
        return cityDescription;
    }

    public void setCityDescription(String cityDescription) {
        this.cityDescription = cityDescription;
    }

    public String getProvinceDescription() {
        return provinceDescription;
    }

    public void setProvinceDescription(String provinceDescription) {
        this.provinceDescription = provinceDescription;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }
}
