package it.nexera.ris.web.converters;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;

import it.nexera.ris.common.enums.BillingTypeFields;
import it.nexera.ris.common.helpers.LogHelper;
import it.nexera.ris.common.helpers.ValidationHelper;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.domain.ClientInvoiceManageColumn;
import it.nexera.ris.web.beans.wrappers.logic.InvoiceColumnWrapper;

@FacesConverter(value = "clientInvoiceColumnConverter")
public class ClientInvoiceColumnConverter implements Converter {
	public transient final Log log = LogFactory
			.getLog(ClientInvoiceColumnConverter.class);

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
					for (BillingTypeFields field : BillingTypeFields.values()) {
						if(field.toString().equalsIgnoreCase(value)) {
							InvoiceColumnWrapper invoiceColumnWrapper = new InvoiceColumnWrapper(field);
							invoiceColumnWrapper.setSelected(Boolean.TRUE);
							return invoiceColumnWrapper;
						}
					}
				}else {
					ClientInvoiceManageColumn clientInvoiceManageColumn = DaoManager.get(ClientInvoiceManageColumn.class, new Criterion[]
							{Restrictions.eq("id", Long.parseLong(value))});
					if(clientInvoiceManageColumn != null) {
						InvoiceColumnWrapper invoiceColumnWrapper = new InvoiceColumnWrapper();
						invoiceColumnWrapper.setSelected(Boolean.TRUE);
						invoiceColumnWrapper.setField(clientInvoiceManageColumn);
						return invoiceColumnWrapper;
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
