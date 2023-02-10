package it.nexera.ris.persistence.beans.entities.domain;

import it.nexera.ris.persistence.beans.entities.IndexedEntity;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.CadastralCategory;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@EqualsAndHashCode(callSuper = true)
@Getter
@Setter
@Entity
@Table(name = "category_fiscal_value")
public class CategoryFiscalValue extends IndexedEntity {

    private static final long serialVersionUID = -3364532093200018502L;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private CadastralCategory cadastralCategory;

    private Double value;
}
