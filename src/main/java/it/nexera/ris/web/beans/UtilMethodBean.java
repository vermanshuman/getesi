package it.nexera.ris.web.beans;

import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.helpers.LogHelper;
import it.nexera.ris.common.helpers.ValidationHelper;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.RequestType;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.HibernateException;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import java.io.Serializable;

@ManagedBean(name = "utilMethodBean")
@ViewScoped
public class UtilMethodBean implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4860295805654763117L;
	
	private static transient final Log log = LogFactory.getLog(UtilMethodBean.class);

    public String getItemIconStyleClass(Long requestTypeId) {
    	String iconStyleClass = "";
    	if (!ValidationHelper.isNullOrEmpty(requestTypeId)) {    		
    		try {
				RequestType requestTypeDTO = DaoManager.get(RequestType.class, requestTypeId);
				iconStyleClass = requestTypeDTO.getIcon();
				if(iconStyleClass.startsWith("fa")) {
					iconStyleClass = "fa " + iconStyleClass;
				}
			} catch (HibernateException | InstantiationException | IllegalAccessException | PersistenceBeanException e) {
                LogHelper.log(log, e);
            }
    	}
    	return iconStyleClass;
    }
    
}
