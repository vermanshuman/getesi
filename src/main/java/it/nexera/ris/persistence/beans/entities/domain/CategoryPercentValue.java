package it.nexera.ris.persistence.beans.entities.domain;

import it.nexera.ris.common.helpers.ValidationHelper;
import it.nexera.ris.persistence.beans.entities.IndexedEntity;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.CadastralCategory;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@Table(name = "category_percent_value")
public class CategoryPercentValue extends IndexedEntity {

    private static final long serialVersionUID = -3044462388692093561L;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private CadastralCategory cadastralCategory;

    @Column(name = "percent_omi")
    private Long percentOmi;

    @Column(name = "percent_commercial")
    private Long percentCommercial;

    public String getCategoryCode() {
        if (!ValidationHelper.isNullOrEmpty(getCadastralCategory())) {
            return getCadastralCategory().getCode();
        } else {
            return null;
        }
    }
}
