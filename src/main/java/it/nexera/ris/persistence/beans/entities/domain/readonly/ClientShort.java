package it.nexera.ris.persistence.beans.entities.domain.readonly;

import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import it.nexera.ris.common.enums.ClientType;
import it.nexera.ris.common.helpers.ResourcesHelper;
import it.nexera.ris.common.helpers.ValidationHelper;
import it.nexera.ris.persistence.beans.entities.IndexedEntity;
import it.nexera.ris.persistence.beans.entities.domain.Client;
import it.nexera.ris.persistence.beans.entities.domain.ClientEmail;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.City;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.Country;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.Office;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.Province;

@Entity
@Table(name = "client")
public class ClientShort extends IndexedEntity implements Serializable {

    private static final long serialVersionUID = 6171602283864285979L;

    @Column(name = "name_of_the_company", insertable = false, updatable = false)
    private String nameOfTheCompany;

    @Column(name = "number_vat", insertable = false, updatable = false)
    private String numberVAT;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "address_province_id")
    private Province addressProvinceId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "address_city_id")
    private City addressCityId;

    @Column(name = "address_street", insertable = false, updatable = false)
    private String addressStreet;

    @Column(name = "address_house_number", insertable = false, updatable = false)
    private String addressHouseNumber;

    @Column(name = "phone", insertable = false, updatable = false)
    private String phone;

    @Column(name = "mail", insertable = false, updatable = false)
    private String mail;

    @Column(name = "type_id")
    private Long typeId;

    @Column(name = "is_deleted")
    private Boolean deleted;

    @Column(name = "name_professional")
    private String nameProfessional;

    @Column(name = "foreign_country")
    private Boolean foreignCountry;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "country_id")
    private Country country;

    @OneToMany(mappedBy = "client")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private List<ClientEmail> emails;

    @Column(name = "manager")
    private Boolean manager;

    @Column(name = "fiduciary")
    private Boolean fiduciary;

    @Column(name = "external")
    private Boolean external;

    @ManyToOne
    @JoinColumn(name="client_id")
    private Client client;

    @Column(name = "client_name")
    private String clientName;
    
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "client_reference", joinColumns = {
            @JoinColumn(name = "client_id", table = "client")
    }, inverseJoinColumns = {
            @JoinColumn(name = "reference_id", table = "client")
    })
    private List<Client> referenceClients;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "office_id")
    private Office office;

    private Boolean brexa;
    
    public Office getOffice() {
        return office;
    }

    public void setOffice(Office office) {
        this.office = office;
    }

    @Override
    public String toString() {
        if (ClientType.PROFESSIONAL.getId().equals(getTypeId())) {
            return getNameProfessional() == null ? "" : getNameProfessional();
        } else {
            return getNameOfTheCompany() == null ? "" : getNameOfTheCompany();
        }
    }

    public String getFullAddress() {
        if (getForeignCountry() != null && getForeignCountry()) {
            return String.format("%s %s %s", this.getAddressStreet(),
                    this.getAddressHouseNumber(),
                    this.getCountry().getDescription());
        } else {
            return String.format("%s %s %s", this.getAddressStreet(),
                    this.getAddressHouseNumber(),
                    this.getAddressCityId().getDescription());
        }
    }

    public String getClients() {
        if(!ValidationHelper.isNullOrEmpty(getReferenceClients())) {
            return getReferenceClients()
                    .stream()
                    .map(Client::toString)
                    .collect(Collectors.joining(", "));
        }
        return "";
    }

    public String getKind() {
        String result = "";

        if (Boolean.TRUE.equals(getManager())) {
            result += ResourcesHelper.getString("clientManager").toUpperCase() + " ";
        }
        if (Boolean.TRUE.equals(getFiduciary())) {
            result += ResourcesHelper.getString("clientTrust").toUpperCase() + " ";
        }
        if (Boolean.TRUE.equals(getExternal())) {
            result += ResourcesHelper.getString("clientExternal").toUpperCase() + " ";
        }

        return result.trim();
    }

    public String getNameOfTheCompany() {
        return nameOfTheCompany;
    }

    public void setNameOfTheCompany(String nameOfTheCompany) {
        this.nameOfTheCompany = nameOfTheCompany;
    }

    public String getNumberVAT() {
        return numberVAT;
    }

    public void setNumberVAT(String numberVAT) {
        this.numberVAT = numberVAT;
    }

    public Province getAddressProvinceId() {
        return addressProvinceId;
    }

    public void setAddressProvinceId(Province addressProvinceId) {
        this.addressProvinceId = addressProvinceId;
    }

    public City getAddressCityId() {
        return addressCityId;
    }

    public void setAddressCityId(City addressCityId) {
        this.addressCityId = addressCityId;
    }

    public String getAddressStreet() {
        return addressStreet;
    }

    public void setAddressStreet(String addressStreet) {
        this.addressStreet = addressStreet;
    }

    public String getAddressHouseNumber() {
        return addressHouseNumber;
    }

    public void setAddressHouseNumber(String addressHouseNumber) {
        this.addressHouseNumber = addressHouseNumber;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    public Long getTypeId() {
        return typeId;
    }

    public void setTypeId(Long typeId) {
        this.typeId = typeId;
    }

    public String getNameProfessional() {
        return nameProfessional;
    }

    public void setNameProfessional(String nameProfessional) {
        this.nameProfessional = nameProfessional;
    }

    public Boolean getForeignCountry() {
        return foreignCountry;
    }

    public void setForeignCountry(Boolean foreignCountry) {
        this.foreignCountry = foreignCountry;
    }

    public Country getCountry() {
        return country;
    }

    public void setCountry(Country country) {
        this.country = country;
    }

    public Boolean getDeleted() {
        return deleted;
    }

    public void setDeleted(Boolean deleted) {
        this.deleted = deleted;
    }

    public List<ClientEmail> getEmails() {
        return emails;
    }

    public void setEmails(List<ClientEmail> emails) {
        this.emails = emails;
    }

    public Boolean getManager() {
        return manager;
    }

    public void setManager(Boolean manager) {
        this.manager = manager;
    }

    public Boolean getFiduciary() {
        return fiduciary;
    }

    public void setFiduciary(Boolean fiduciary) {
        this.fiduciary = fiduciary;
    }

    public Boolean getExternal() {
        return external;
    }

    public void setExternal(Boolean external) {
        this.external = external;
    }

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    public String getClientName() {
        return clientName != null ? clientName.toLowerCase() : clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }
    
    public List<Client> getReferenceClients() {
        return referenceClients;
    }

    public void setReferenceClients(List<Client> referenceClients) {
        this.referenceClients = referenceClients;
    }

    public Boolean getBrexa() {
        return brexa;
    }

    public void setBrexa(Boolean brexa) {
        this.brexa = brexa;
    }
}
