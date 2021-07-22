package it.nexera.ris.web.beans.wrappers.logic;

import java.io.Serializable;

import it.nexera.ris.persistence.beans.entities.domain.ItemGroupOmi;

public class CategoryGroupItemWrapper implements Serializable{

    private static final long serialVersionUID = 2862304699058505647L;

    private Integer position;

    private String categoryCode;
    
    private ItemGroupOmi itemGroupOmi;

    public Integer getPosition() {
        return position;
    }

    public void setPosition(Integer position) {
        this.position = position;
    }

    public String getCategoryCode() {
        return categoryCode;
    }

    public void setCategoryCode(String categoryCode) {
        this.categoryCode = categoryCode;
    }

    public ItemGroupOmi getItemGroupOmi() {
        return itemGroupOmi;
    }

    public void setItemGroupOmi(ItemGroupOmi itemGroupOmi) {
        this.itemGroupOmi = itemGroupOmi;
    }
}
