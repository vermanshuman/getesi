package it.nexera.ris.common.helpers;

import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;

import it.nexera.ris.common.enums.ApplicationInstance;
import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.security.api.UserDetailsImpl;
import it.nexera.ris.common.security.crypto.MD5;
import it.nexera.ris.persistence.beans.dao.ConnectionManager;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.domain.User;

public class UserHelper {

	public static List<User> loadUsers() throws HibernateException, IllegalAccessException, PersistenceBeanException {
		Boolean getesi = Boolean.FALSE;
		Boolean brexa = Boolean.FALSE;
		Integer applicationInstance = FileHelper.getApplicationInstance();
		if (applicationInstance.equals(ApplicationInstance.GETESI.getId()))
			getesi = Boolean.TRUE;
		else if (applicationInstance.equals(ApplicationInstance.BREXA.getId()))
			brexa = Boolean.TRUE;
		return DaoManager.load(User.class, new Criterion[] {
				Restrictions.or(Restrictions.eq("getesi", getesi), Restrictions.eq("brexa", brexa)) });
	}
	
	public static User getUser(UserDetailsImpl userDetails) throws HibernateException, InstantiationException, IllegalAccessException, PersistenceBeanException {
		Boolean getesi = Boolean.FALSE;
        Boolean brexa = Boolean.FALSE;
        Integer applicationInstance = FileHelper.getApplicationInstance();
        if(applicationInstance.equals(ApplicationInstance.GETESI.getId()))
        	getesi = Boolean.TRUE;
        else if(applicationInstance.equals(ApplicationInstance.BREXA.getId()))
        	brexa = Boolean.TRUE;
        return DaoManager.get(User.class, new Criterion[]{Restrictions.eq("login", userDetails.getUsername()),
        		Restrictions.or(Restrictions.eq("getesi", getesi), Restrictions.eq("brexa", brexa))});
	}
	
	public static User getUser(UserDetailsImpl userDetails, Session session) throws HibernateException, InstantiationException, IllegalAccessException, PersistenceBeanException {
		Boolean getesi = Boolean.FALSE;
        Boolean brexa = Boolean.FALSE;
        Integer applicationInstance = FileHelper.getApplicationInstance();
        if(applicationInstance.equals(ApplicationInstance.GETESI.getId()))
        	getesi = Boolean.TRUE;
        else if(applicationInstance.equals(ApplicationInstance.BREXA.getId()))
        	brexa = Boolean.TRUE;
        return ConnectionManager.get(User.class, new Criterion[]{Restrictions.eq("login", userDetails.getUsername()),
            	Restrictions.or(Restrictions.eq("getesi", getesi), Restrictions.eq("brexa", brexa))}, session);
	}
	
	public static User getUser(String username, String password) throws HibernateException, InstantiationException, IllegalAccessException, PersistenceBeanException {
		Boolean getesi = Boolean.FALSE;
        Boolean brexa = Boolean.FALSE;
        Integer applicationInstance = FileHelper.getApplicationInstance();
        if(applicationInstance.equals(ApplicationInstance.GETESI.getId()))
        	getesi = Boolean.TRUE;
        else if(applicationInstance.equals(ApplicationInstance.BREXA.getId()))
        	brexa = Boolean.TRUE;
        return DaoManager.get(User.class, new Criterion[] { Restrictions.eq("login", username), 
        		Restrictions.or(Restrictions.eq("getesi", getesi), Restrictions.eq("brexa", brexa)),
        		Restrictions.eq("password", MD5.encodeString(password, null))});
	}
	
	public static User getUser(String userLogin) throws HibernateException, InstantiationException, IllegalAccessException, PersistenceBeanException {
		Boolean getesi = Boolean.FALSE;
        Boolean brexa = Boolean.FALSE;
        Integer applicationInstance = FileHelper.getApplicationInstance();
        if(applicationInstance.equals(ApplicationInstance.GETESI.getId()))
        	getesi = Boolean.TRUE;
        else if(applicationInstance.equals(ApplicationInstance.BREXA.getId()))
        	brexa = Boolean.TRUE;
        return DaoManager.get(User.class, new Criterion[]{Restrictions.eq("login", userLogin), 
			   Restrictions.or(Restrictions.eq("getesi", getesi), Restrictions.eq("brexa", brexa))});
	}
	
	public static User getUser(String userLogin, Session session) throws HibernateException, InstantiationException, IllegalAccessException, PersistenceBeanException {
		Boolean getesi = Boolean.FALSE;
        Boolean brexa = Boolean.FALSE;
        Integer applicationInstance = FileHelper.getApplicationInstance();
        if(applicationInstance.equals(ApplicationInstance.GETESI.getId()))
        	getesi = Boolean.TRUE;
        else if(applicationInstance.equals(ApplicationInstance.BREXA.getId()))
        	brexa = Boolean.TRUE;
        return ConnectionManager.get(User.class, new Criterion[]{Restrictions.eq("login", userLogin), 
               Restrictions.or(Restrictions.eq("getesi", getesi), Restrictions.eq("brexa", brexa))}, session);
	}
}
