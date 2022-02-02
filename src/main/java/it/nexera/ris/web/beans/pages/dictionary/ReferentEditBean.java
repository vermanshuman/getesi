package it.nexera.ris.web.beans.pages.dictionary;

import it.nexera.ris.common.enums.ReferentType;
import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.helpers.*;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.domain.Referent;
import it.nexera.ris.web.beans.EntityEditPageBean;
import org.hibernate.HibernateException;
import org.hibernate.StaleObjectStateException;
import org.primefaces.context.RequestContext;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.model.SelectItem;
import java.io.IOException;
import java.io.Serializable;
import java.util.List;

@ManagedBean(name = "referentEditBean")
@ViewScoped
public class ReferentEditBean extends EntityEditPageBean<Referent>
        implements Serializable {

    private static final long serialVersionUID = 2539755768338506547L;

    private boolean onlyView;

    private Boolean fromClient;

    private List<SelectItem> referentTypes;

    @Override
    protected void preLoad() throws PersistenceBeanException {
        if ("true".equalsIgnoreCase(
                this.getRequestParameter(RedirectHelper.ONLY_VIEW))) {
            setOnlyView(true);
        }
    }

    @Override
    protected void pageLoadStatic() throws PersistenceBeanException {
        if (SessionHelper.get("fromClient") != null
                && ((Boolean) SessionHelper.get("fromClient"))) {
            SessionHelper.removeObject("fromClient");
            setFromClient(Boolean.TRUE);
        } else {
            setFromClient(Boolean.FALSE);
        }
    }

    @Override
    public void onLoad() throws NumberFormatException, HibernateException,
            PersistenceBeanException, InstantiationException,
            IllegalAccessException {
        setReferentTypes(ComboboxHelper.fillList(ReferentType.class, false, false));
    }

    @Override
    public void onValidate() throws PersistenceBeanException,
            HibernateException, IllegalAccessException {
        if (ValidationHelper.isNullOrEmpty(this.getEntity().getSurname())) {
            addRequiredFieldException("form:surname");
        }

        if (ValidationHelper.isNullOrEmpty(this.getEntity().getName())) {
            addRequiredFieldException("form:name");
        }

        if (!ValidationHelper.isNullOrEmpty(this.getEntity().getEmail())
                && !ValidationHelper
                .checkMailCorrectFormat(this.getEntity().getEmail())) {
            addFieldException("form:email", "emailWrongFormat");
        }
    }

    @Override
    public void onSave() throws HibernateException, PersistenceBeanException,
            NumberFormatException, IOException, InstantiationException,
            IllegalAccessException {
        DaoManager.save(getEntity());
    }

    public void closeDialog() {
        RequestContext.getCurrentInstance().closeDialog(getEntity());
    }

    public void saveFromDialog() {
        if (this.getSaveFlag() == 0) {
            try {
                this.cleanValidation();
                this.setValidationFailed(false);
                this.onValidate();
                if (this.getValidationFailed()) {
                    return;
                }
            } catch (Exception e) {
                LogHelper.log(log, e);
                return;
            }

            try {
                this.tr = DaoManager.getSession().beginTransaction();

                this.setSaveFlag(1);

                this.onSave();
            } catch (Exception e) {
                if (this.tr != null) {
                    this.tr.rollback();
                }
                LogHelper.log(log, e);
                MessageHelper.addGlobalMessage(FacesMessage.SEVERITY_ERROR, "",
                        ResourcesHelper.getValidation("objectEditedException"));
            } finally {
                if (this.tr != null && !this.tr.wasRolledBack()
                        && this.tr.isActive()) {
                    try {
                        this.tr.commit();
                    } catch (StaleObjectStateException e) {
                        MessageHelper.addGlobalMessage(
                                FacesMessage.SEVERITY_ERROR, "",
                                ResourcesHelper.getValidation(
                                        "exceptionOccuredWhileSaving"));
                        LogHelper.log(log, e);
                    } catch (Exception e) {
                        LogHelper.log(log, e);
                    }
                }
                this.setSaveFlag(0);
                closeDialog();
            }
        }
    }

    public void editEntity() {
        this.setOnlyView(false);
    }

    public boolean isOnlyView() {
        return onlyView;
    }

    public void setOnlyView(boolean onlyView) {
        this.onlyView = onlyView;
    }

    public Boolean getFromClient() {
        return fromClient;
    }

    public void setFromClient(Boolean fromClient) {
        this.fromClient = fromClient;
    }

    public List<SelectItem> getReferentTypes() {
        return referentTypes;
    }

    public void setReferentTypes(List<SelectItem> referentTypes) {
        this.referentTypes = referentTypes;
    }
}
