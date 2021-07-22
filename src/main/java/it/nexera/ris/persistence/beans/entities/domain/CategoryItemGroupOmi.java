package it.nexera.ris.persistence.beans.entities.domain;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import it.nexera.ris.persistence.beans.entities.IndexedEntity;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.CadastralCategory;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@Table(name = "category_item_gruop_omi")
public class CategoryItemGroupOmi extends IndexedEntity {

    private static final long serialVersionUID = 4310973354605417460L;


    @ManyToOne
    @JoinColumn(name = "cadastral_category_id")
    private CadastralCategory category;

    @ManyToOne
    @JoinColumn(name = "item_id")
    private ItemGroupOmi itemGroupOmi;

    public CadastralCategory getCategory() {
        return category;
    }

    public ItemGroupOmi getItemGroupOmi() {
        return itemGroupOmi;
    }

    public void setCategory(CadastralCategory category) {
        this.category = category;
    }

    public void setItemGroupOmi(ItemGroupOmi itemGroupOmi) {
        this.itemGroupOmi = itemGroupOmi;
    }
    
    @Override
    public String toString() {
        return this.getId() != null ? this.getStrId() : this.getCategory().toString();
    }

}
