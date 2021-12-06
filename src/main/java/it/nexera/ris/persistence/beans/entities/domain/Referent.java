package it.nexera.ris.persistence.beans.entities.domain;

import it.nexera.ris.common.enums.ReferentType;
import it.nexera.ris.common.helpers.ValidationHelper;
import it.nexera.ris.persistence.beans.entities.IndexedEntity;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "referent")
public class Referent extends IndexedEntity implements Serializable {

    private static final long serialVersionUID = -5503596775117967907L;

    @Column(name = "surname")
    private String surname;

    @Column(name = "name")
    private String name;

    @Column(name = "phone")
    private String phone;

    @Column(name = "email")
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(name = "type")
    private ReferentType type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id")
    private Client client;

    public String getClientName() {
        if (!ValidationHelper.isNullOrEmpty(getClient())) {
            return getClient().toString();
        }
        return "";
    }

    @Override
    public String toString() {
        return this.getSurname();
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    public ReferentType getType() {
        return type;
    }

    public void setType(ReferentType type) {
        this.type = type;
    }
}
