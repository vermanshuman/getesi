package it.nexera.ris.persistence;

import it.nexera.ris.persistence.beans.dao.DaoManager;
import org.hibernate.Session;
import org.hibernate.Transaction;

/**
 * @author Alex Chelombitko
 * 23.01.2013
 */
public abstract class TransactionExecuter {
    private static Object monitor = new Object();

    public static void execute(IAction action) throws Exception {
        execute(action, DaoManager.getSession());
    }

    public static synchronized void execute(IAction action, Session session)
            throws Exception {
        synchronized (monitor) {
            Transaction tr = null;
            try {
                tr = session.beginTransaction();

                action.execute(session);
            } catch (Exception e) {
                if (tr != null && tr.isActive()) {
                    tr.rollback();
                }

                action.onException(e);
            } finally {
                if (tr != null && tr.isActive() && !tr.wasRolledBack()) {
                    try {
                        tr.commit();
                        action.onSuccess();
                    } catch (Exception e) {
                        action.onException(e);
                    }
                }
                action.onExecuted();
            }
        }
    }
}
