package it.nexera.ris.web.beans.pages.dictionary;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.model.SelectItem;

import org.hibernate.HibernateException;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;
import org.primefaces.event.CellEditEvent;

import it.nexera.ris.common.enums.TypeActEnum;
import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.helpers.ComboboxHelper;
import it.nexera.ris.common.helpers.LogHelper;
import it.nexera.ris.common.helpers.ValidationHelper;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.ImportF24;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.Service;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.TypeFormality;
import it.nexera.ris.web.beans.EntityEditPageBean;
import lombok.Getter;
import lombok.Setter;

@ManagedBean(name = "typeFormalityEditBean")
@ViewScoped
@Getter
@Setter
public class TypeFormalityEditBean extends EntityEditPageBean<TypeFormality> implements Serializable {

	private static final long serialVersionUID = 4032745881934617416L;
	
	private boolean onlyView;
	
	private List<SelectItem> typeActEnumList;
	
	private List<ImportF24> importsF24;

	@Override
	public void onLoad() throws NumberFormatException, HibernateException, PersistenceBeanException,
			InstantiationException, IllegalAccessException {
		setTypeActEnumList(ComboboxHelper.fillList(TypeActEnum.class, true, false));
		setImportsF24(new ArrayList<>());
		if(!ValidationHelper.isNullOrEmpty(getEntity().getImportF24List()))
			setImportsF24(getEntity().getImportF24List());
			
	}

	@Override
	public void onValidate() throws PersistenceBeanException, HibernateException, IllegalAccessException {
		try {
            if (DaoManager.getCount(TypeFormality.class, "id", new Criterion[]{
                    Restrictions.eq("code", getEntity().getCode()),
                    Restrictions.eq("type", getEntity().getType()),
                    Restrictions.ne("id", getEntity().isNew() ? 0L : getEntity().getId())
            }) > 0) {
                addException("typeFormalityInUse");
            }
        } catch (Exception e) {
            LogHelper.log(log, e);
        }
		
	}

	@Override
	public void onSave() throws HibernateException, PersistenceBeanException, NumberFormatException, IOException,
			InstantiationException, IllegalAccessException {
		DaoManager.save(this.getEntity());
		if(!ValidationHelper.isNullOrEmpty(getImportsF24())) {
			List<ImportF24> importF24List = new ArrayList<>();
			for(ImportF24 importF24: getImportsF24()) {
				importF24.setTypeFormaility(this.getEntity());
				importF24List.add(importF24);
			}
			getEntity().setImportF24List(importF24List);
			DaoManager.save(this.getEntity());
		}
	}
	
	public void onCellEdit(CellEditEvent event) {
        Object oldValue = event.getOldValue();
        Object newValue = event.getNewValue();
        if (newValue != null && !newValue.equals(oldValue)) {
            System.out.println("msg :: "+" Cell Changed Old: " + oldValue + ", New:" + newValue);
        }
    }
	
	public void onAddNew() {
        // Add one new importF24 to the table:
        getImportsF24().add(new ImportF24());
    }

}
