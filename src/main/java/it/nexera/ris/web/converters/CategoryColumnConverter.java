package it.nexera.ris.web.converters;

import java.util.List;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;

import it.nexera.ris.common.helpers.LogHelper;
import it.nexera.ris.common.helpers.ValidationHelper;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.domain.CategoryItemGroupOmi;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.CadastralCategory;
import it.nexera.ris.web.beans.wrappers.logic.CategoryColumnWrapper;

@FacesConverter(value = "categoryColumnConverter")
public class CategoryColumnConverter implements Converter {
	public transient final Log log = LogFactory
			.getLog(CategoryColumnConverter.class);

	@Override
	public Object getAsObject(FacesContext context, UIComponent component,
			String value) {
		try {

			if(!ValidationHelper.isNullOrEmpty(value)) {
				Long  id = -1l;
				try {
					id = Long.parseLong(value);
				} catch (Exception e) {
				}
				if(id == -1 ) {
				    List<CadastralCategory> cadastralCategoryList = DaoManager.load(CadastralCategory.class);
				    for (CadastralCategory cadastralCategory : cadastralCategoryList) {
						if(cadastralCategory.toString().equalsIgnoreCase(value)) {
							CategoryColumnWrapper categoryColumnWrapper = new CategoryColumnWrapper(cadastralCategory);
							categoryColumnWrapper.setSelected(Boolean.TRUE);
							return categoryColumnWrapper;
						}
					}
				}else {
				    CategoryItemGroupOmi categoryItemGroupOmi = DaoManager.get(CategoryItemGroupOmi.class, new Criterion[]
							{Restrictions.eq("id", Long.parseLong(value))});
					if(categoryItemGroupOmi != null) {
					    CategoryColumnWrapper categoryColumnWrapper = new CategoryColumnWrapper();
					    categoryColumnWrapper.setSelected(Boolean.TRUE);
					    categoryColumnWrapper.setCategoryItemGroupOmi(categoryItemGroupOmi);
						return categoryColumnWrapper;
					}
				}
			}
		} catch (Exception e) {
			LogHelper.log(log, e);
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public String getAsString(FacesContext context, UIComponent component,
			Object value) {
		return value.toString();
	}

}
