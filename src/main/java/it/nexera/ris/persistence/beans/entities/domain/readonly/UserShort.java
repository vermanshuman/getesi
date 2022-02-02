package it.nexera.ris.persistence.beans.entities.domain.readonly;

import it.nexera.ris.persistence.beans.entities.IndexedEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.io.Serializable;

@Entity
@Table(name = "acl_user")
public class UserShort extends IndexedEntity implements Serializable {

    private static final long serialVersionUID = -2689940595951222721L;

    @Column(name = "first_name", insertable = false, updatable = false)
    private String firstName;

    @Column(name = "last_name", insertable = false, updatable = false)
    private String lastName;

    public String getFullname() {
        return String.format("%s %s",
                this.getLastName() == null ? "" : this.getLastName(),
                this.getFirstName() == null ? "" : this.getFirstName());
    }

    @Override
    public String toString() {
        return getFullname();
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

}
