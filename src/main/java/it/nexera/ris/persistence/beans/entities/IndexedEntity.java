package it.nexera.ris.persistence.beans.entities;

import javax.persistence.*;

@MappedSuperclass
public abstract class IndexedEntity extends Entity {
    private static final long serialVersionUID = -7573995632754203108L;

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @javax.persistence.Version
    @Column(name = "version", columnDefinition = "NUMERIC(19, 0) DEFAULT 1")
    private Long version;

    @Transient
    private boolean customId;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
        this.customId = true;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public boolean isCustomId() {
        return customId;
    }

    public void setCustomId(boolean customId) {
        this.customId = customId;
    }

}
