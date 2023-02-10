package it.nexera.ris.persistence.beans.entities.domain.dictionary;

import it.nexera.ris.common.helpers.ValidationHelper;
import it.nexera.ris.persistence.beans.entities.IndexedEntity;
import it.nexera.ris.persistence.beans.entities.domain.AggregationLandChargesRegistryNote;
import it.nexera.ris.persistence.beans.entities.domain.AggregationLandChargesRegistryReference;
import it.nexera.ris.persistence.beans.entities.domain.Document;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

import javax.persistence.*;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Entity
@Table(name = "dic_aggregation_land_char_reg")
public class AggregationLandChargesRegistry extends IndexedEntity {

    private static final long serialVersionUID = 1533180820445303590L;

    @Column(name = "name")
    private String name;

    @ManyToMany
    @JoinTable(name = "aggregation_land", joinColumns =
            {@JoinColumn(name = "aggregation_id", table = "dic_aggregation_land_char_reg")}, inverseJoinColumns =
            {@JoinColumn(name = "land_id", table = "dic_land_charges_registry")})
    private List<LandChargesRegistry> landChargesRegistries;

    @Column(name = "is_deleted")
    private Boolean isDeleted;

    @Column(name = "national")
    private Boolean national;

    @Column(name = "code_office")
    private String codeOffice;

    @Column(name = "address")
    private String address;

    @Column(name = "phone1")
    private String phone1;

    @Column(name = "phone2")
    private String phone2;

    @Column(name = "mail")
    private String mail;

    @Column(name = "pec")
    private String pec;

    @Column(name = "cell_phone")
    private String cellPhone;

    @Column(name = "stamp")
    private Boolean stamp;

    @Column(name = "penalty")
    private Boolean penalty;

    @Column(name = "address_number")
    private String addressNumber;

    @Column(name = "address_cap")
    private String addressCap;

    @Column(name = "address_city")
    private String addressCity;

    @OneToMany(mappedBy = "aggregationLandChargesRegistry", cascade = CascadeType.ALL, orphanRemoval = true)
    @LazyCollection(LazyCollectionOption.FALSE)
    private List<AggregationLandChargesRegistryReference> aggregationLandChargesRegistryReferences;

    @OneToMany(mappedBy = "aggregationLandChargesRegistry", cascade = CascadeType.ALL, orphanRemoval = true)
    @LazyCollection(LazyCollectionOption.FALSE)
    private List<AggregationLandChargesRegistryNote> aggregationLandChargesRegistryNotes;

    private String type;

    @OneToMany(mappedBy = "aggregationLandChargesRegistry")
    @LazyCollection(LazyCollectionOption.FALSE)
    private List<Document> documents;

    @Override
    public String toString() {
        return getName();
    }

    public String getLandChargesRegistriesStr() {
        StringBuffer sb = new StringBuffer();

        if (!ValidationHelper.isNullOrEmpty(getLandChargesRegistries())) {
            for (LandChargesRegistry landReg : getLandChargesRegistries()) {
                sb.append(landReg);
                sb.append(" + ");
            }

            sb = new StringBuffer(sb.substring(0, sb.length() - 3));
        }

        return sb.toString();
    }

    public int getNumberOfVisualizedLandChargesRegistries() {
        int result = 0;

        for (LandChargesRegistry registry : getLandChargesRegistries()) {
            if (!ValidationHelper.isNullOrEmpty(registry.getVisualize()) && registry.getVisualize()) {
                result += 1;
            }
        }
        return result;
    }

    public List<Long> getAggregationLandChargesRegistersIds() {
        List<Long> chargesRegistryIds;
        if (!ValidationHelper.isNullOrEmpty(getLandChargesRegistries())) {
            chargesRegistryIds = getLandChargesRegistries().stream()
                    .map(LandChargesRegistry::getId).collect(Collectors.toList());
        } else {
            chargesRegistryIds = Collections.singletonList(0L);
        }
        return chargesRegistryIds;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<LandChargesRegistry> getLandChargesRegistries() {
        return landChargesRegistries;
    }

    public void setLandChargesRegistries(
            List<LandChargesRegistry> landChargesRegistries) {
        this.landChargesRegistries = landChargesRegistries;
    }

    public Boolean getIsDeleted() {
        return isDeleted;
    }

    public void setIsDeleted(Boolean isDeleted) {
        this.isDeleted = isDeleted;
    }

    public Boolean getNational() {
        return national;
    }

    public void setNational(Boolean national) {
        this.national = national;
    }

    public String getCodeOffice() {
        return codeOffice;
    }

    public void setCodeOffice(String codeOffice) {
        this.codeOffice = codeOffice;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhone1() {
        return phone1;
    }

    public void setPhone1(String phone1) {
        this.phone1 = phone1;
    }

    public String getPhone2() {
        return phone2;
    }

    public void setPhone2(String phone2) {
        this.phone2 = phone2;
    }

    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    public String getPec() {
        return pec;
    }

    public void setPec(String pec) {
        this.pec = pec;
    }

    public String getCellPhone() {
        return cellPhone;
    }

    public void setCellPhone(String cellPhone) {
        this.cellPhone = cellPhone;
    }

    public Boolean getStamp() {
        return stamp;
    }

    public void setStamp(Boolean stamp) {
        this.stamp = stamp;
    }

    public Boolean getPenalty() {
        return penalty;
    }

    public void setPenalty(Boolean penalty) {
        this.penalty = penalty;
    }

    public String getAddressNumber() {
        return addressNumber;
    }

    public void setAddressNumber(String addressNumber) {
        this.addressNumber = addressNumber;
    }

    public String getAddressCap() {
        return addressCap;
    }

    public void setAddressCap(String addressCap) {
        this.addressCap = addressCap;
    }

    public String getAddressCity() {
        return addressCity;
    }

    public void setAddressCity(String addressCity) {
        this.addressCity = addressCity;
    }

    public List<AggregationLandChargesRegistryReference> getAggregationLandChargesRegistryReferences() {
        return aggregationLandChargesRegistryReferences;
    }

    public void setAggregationLandChargesRegistryReferences(List<AggregationLandChargesRegistryReference> aggregationLandChargesRegistryReferences) {
        this.aggregationLandChargesRegistryReferences = aggregationLandChargesRegistryReferences;
    }

    public List<AggregationLandChargesRegistryNote> getAggregationLandChargesRegistryNotes() {
        return aggregationLandChargesRegistryNotes;
    }

    public void setAggregationLandChargesRegistryNotes(List<AggregationLandChargesRegistryNote> aggregationLandChargesRegistryNotes) {
        this.aggregationLandChargesRegistryNotes = aggregationLandChargesRegistryNotes;
    }

    public String getType() {

        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<Document> getDocuments() {
        return documents;
    }

    public void setDocuments(List<Document> documents) {
        this.documents = documents;
    }
}
