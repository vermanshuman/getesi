package it.nexera.ris.web.beans.wrappers.logic;

import java.io.Serializable;

import it.nexera.ris.persistence.beans.entities.domain.CategoryItemGroupOmi;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.CadastralCategory;

public class CategoryColumnWrapper implements Serializable{


    private static final long serialVersionUID = -2404123646569274160L;

    private CategoryItemGroupOmi categoryItemGroupOmi;

    private Boolean selected;

    public CategoryColumnWrapper() {
	}
    
    public CategoryColumnWrapper(CategoryItemGroupOmi categoryItemGroupOmi) {
        this.selected = true;
        this.categoryItemGroupOmi = categoryItemGroupOmi;
    }

    public CategoryColumnWrapper(CadastralCategory cadastralCategory) {
        CategoryItemGroupOmi categoryItemGroupOmi = new CategoryItemGroupOmi();
        categoryItemGroupOmi.setCategory(cadastralCategory);
        this.categoryItemGroupOmi = categoryItemGroupOmi;
    }

    public Boolean getSelected() {
        return selected;
    }

    public void setSelected(Boolean selected) {
        this.selected = selected;
    }

   

    public CategoryItemGroupOmi getCategoryItemGroupOmi() {
        return categoryItemGroupOmi;
    }

    public void setCategoryItemGroupOmi(CategoryItemGroupOmi categoryItemGroupOmi) {
        this.categoryItemGroupOmi = categoryItemGroupOmi;
    }

    @Override
    public String toString() {
        return getCategoryItemGroupOmi().getCategory().toString();
    }
}
