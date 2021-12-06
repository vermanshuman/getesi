package it.nexera.ris.persistence.beans.entities.domain;

import it.nexera.ris.common.enums.EmailType;
import it.nexera.ris.persistence.beans.entities.IndexedEntity;

import javax.persistence.*;

@Entity
@Table(name = "client_email")
public class ClientEmail extends IndexedEntity {

    private static final long serialVersionUID = -3639307529902311797L;

    @Column(name = "email")
    private String email;

    @Column(name = "description")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id")
    private Client client;

    @Column(name = "type_id")
    private Long typeId;

    public EmailType getType() {
        return EmailType.findById(getTypeId());
    }

    public boolean isAdditional() {
        return EmailType.ADDITIONAL.equals(getType());
    }

    public boolean isPersonal() {
        return EmailType.PERSONAL.equals(getType());
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

    public Long getTypeId() {
        return typeId;
    }

    public void setTypeId(Long typeId) {
        this.typeId = typeId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
