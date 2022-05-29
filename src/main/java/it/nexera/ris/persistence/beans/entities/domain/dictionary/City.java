package it.nexera.ris.persistence.beans.entities.domain.dictionary;

import it.nexera.ris.common.helpers.ValidationHelper;
import it.nexera.ris.persistence.beans.entities.Dictionary;
import it.nexera.ris.persistence.beans.entities.domain.LandOmi;
import org.apache.commons.lang3.text.WordUtils;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@Entity
@Table(name = "dic_city")
@SequenceGenerator(name = "ID_SEQ_GEN", initialValue = 1, sequenceName = "CITY_SEQ", allocationSize = 1)
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "data_entry")
public class City extends Dictionary {

    private static final long serialVersionUID = -9206231689506545191L;

    @ManyToOne
    @JoinColumn(name = "province_id")
    private Province province;

    @ManyToOne
    @JoinColumn(name = "asl_id")
    private Asl asl;

    @Column(name = "client_instance")
    private Boolean clientInstance;

    @ManyToOne
    @JoinColumn(name = "asl_region_id")
    private AslRegion aslRegion;

    @Column
    private String cap;

    @Column
    private String cfis;

    @Column
    private Boolean obsolete;

    @ManyToMany(mappedBy = "cities", fetch = FetchType.LAZY)
    private List<LandChargesRegistry> landChargesRegistries;

    @Column(name = "external")
    private Boolean external;


    @ManyToMany(mappedBy = "cities")
    private List<LandOmi> landOmis;

    @Column(name = "is_deleted")
    private Boolean isDeleted;

    @Transient
    @XmlElement(name = "asl_region_code")
    private String asl_region_code;

    @Transient
    @XmlElement(name = "asl_code")
    private String asl_code;

    @Transient
    @XmlElement(name = "province_code")
    private String province_code;

    @Transient
    public String getCamelCityDescription() {
        if (!ValidationHelper.isNullOrEmpty(getDescription())) {
            return WordUtils.capitalizeFully(getDescription(), ' ');
        }
        return "";
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (obj == this) {
            return true;
        }

        if (this.getId() == null) {
            return obj == this;
        }

        City city = (City) obj;

        return this.getId().equals(city.getId());
    }

    @Override
    public String toString() {
        return String.format("%s %s", getCfis(), getDescription());
    }

    public String getNameForConservatory() {
        return String.format("%s %s", getCfis(), getDescription());
    }

    public Province getProvince() {
        return province;
    }

    public void setProvince(Province province) {
        this.province = province;
    }

    public Boolean getClientInstance() {
        return clientInstance;
    }

    public void setClientInstance(Boolean clientInstance) {
        this.clientInstance = clientInstance;
    }

    public Asl getAsl() {
        return asl;
    }

    public void setAsl(Asl asl) {
        this.asl = asl;
    }

    public AslRegion getAslRegion() {
        return aslRegion;
    }

    public void setAslRegion(AslRegion aslRegion) {
        this.aslRegion = aslRegion;
    }

    public String getCap() {
        return cap;
    }

    public void setCap(String cap) {
        this.cap = cap;
    }

    public String getCfis() {
        return cfis;
    }

    public void setCfis(String cfis) {
        this.cfis = cfis;
    }

    public Boolean getObsolete() {
        return obsolete;
    }

    public void setObsolete(Boolean obsolete) {
        this.obsolete = obsolete;
    }

    public String getAsl_region_code() {
        return asl_region_code;
    }

    public void setAsl_region_code(String asl_region_code) {
        this.asl_region_code = asl_region_code;
    }

    public String getAsl_code() {
        return asl_code;
    }

    public void setAsl_code(String asl_code) {
        this.asl_code = asl_code;
    }

    public String getProvince_code() {
        return province_code;
    }

    public void setProvince_code(String province_code) {
        this.province_code = province_code;
    }

    public List<LandChargesRegistry> getLandChargesRegistries() {
        return landChargesRegistries;
    }

    public void setLandChargesRegistries(List<LandChargesRegistry> landChargesRegistries) {
        this.landChargesRegistries = landChargesRegistries;
    }

    public Boolean getExternal() {
        return external;
    }

    public void setExternal(Boolean external) {
        this.external = external;
    }

    public List<LandOmi> getLandOmis() {
        return landOmis;
    }

    public void setLandOmis(List<LandOmi> landOmis) {
        this.landOmis = landOmis;
    }

    public Boolean getIsDeleted() {
        return isDeleted == null ? Boolean.FALSE : isDeleted;
    }

    public void setIsDeleted(Boolean isDeleted) {
        this.isDeleted = isDeleted;
    }
}
