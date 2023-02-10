package it.nexera.ris.persistence.beans.entities.domain;

import it.nexera.ris.common.enums.ReferentType;
import it.nexera.ris.common.helpers.ValidationHelper;
import it.nexera.ris.persistence.beans.entities.IndexedEntity;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "referent")
@Getter
@Setter
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

    @Column(name = "is_deleted")
    private Boolean isDeleted;

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

}
