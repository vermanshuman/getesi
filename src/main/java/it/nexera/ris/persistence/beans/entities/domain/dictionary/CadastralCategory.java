package it.nexera.ris.persistence.beans.entities.domain.dictionary;

import it.nexera.ris.persistence.beans.entities.Dictionary;
import it.nexera.ris.persistence.beans.entities.domain.CategoryPercentValue;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@Table(name = "dic_cadastral_category")
public class CadastralCategory extends Dictionary {

    private static final long serialVersionUID = -2976132307840270459L;

    @Column(name = "text_in_tag")
    private String textInTag;

    @Column(name = "codeInVisura")
    private String codeInVisura;

    @Column(name = "is_deleted")
    private Boolean isDeleted;

    @OneToOne(mappedBy = "cadastralCategory", cascade = CascadeType.ALL)
    private CategoryPercentValue categoryPercentValue;

    @Override
    public String toString() {
        return getCode();
    }

}
