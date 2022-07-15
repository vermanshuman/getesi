package it.nexera.ris.persistence.beans.entities.domain.dictionary;

import it.nexera.ris.persistence.beans.entities.IndexedEntity;
import it.nexera.ris.persistence.beans.entities.domain.DocumentTemplate;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "dic_model")
@SequenceGenerator(name = "ID_SEQ_GEN", initialValue = 1, sequenceName = "MODEL_SEQ", allocationSize = 1)
@Getter
@Setter
public class TemplateDocumentModel extends IndexedEntity {
    private static final long serialVersionUID = -1585382014917551349L;

    @Column
    private String name;

    @OneToMany(mappedBy = "model")
    private List<DocumentTemplate> documentTemplates;

    @OneToMany(mappedBy = "model")
    private List<InstancePhases> instansePhases;

    @Column(name = "is_deleted")
    private Boolean isDeleted;

    public String toString() {
        return name;
    }
}
