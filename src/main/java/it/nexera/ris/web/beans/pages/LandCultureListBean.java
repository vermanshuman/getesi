package it.nexera.ris.web.beans.pages;

import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.helpers.LogHelper;
import it.nexera.ris.common.helpers.ValidationHelper;
import it.nexera.ris.persistence.PersistenceSessionManager;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.domain.LandCadastralCulture;
import it.nexera.ris.persistence.beans.entities.domain.LandCulture;
import it.nexera.ris.persistence.beans.entities.domain.Property;
import it.nexera.ris.web.beans.EntityLazyListPageBean;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.collections4.CollectionUtils;
import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.Transaction;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.primefaces.context.RequestContext;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Setter
@Getter
@ManagedBean(name = "landCultureListBean")
@ViewScoped
public class LandCultureListBean extends EntityLazyListPageBean<LandCulture>
        implements Serializable {

    private static final long serialVersionUID = 7141646207349762514L;

    private LandCulture entity;

    private String landCultureName;

    private List<String> selectedQuality;

    private List<String> selectedFilterQuality;

    private List<String> qualities;

    private List<LandCadastralCulture> landCadastralCultures;

    @Override
    public void onLoad() throws NumberFormatException, HibernateException, PersistenceBeanException,
            InstantiationException, IllegalAccessException, IOException {
        filterTableFromPanel();
        setEntity(new LandCulture());
        setQualities(DaoManager.loadDistinctfield(Property.class, "quality", String.class,
                null, new Criterion[]{
                        Restrictions.isNotNull("quality")
                }));
    }

    public void filterTableFromPanel() throws PersistenceBeanException, IllegalAccessException, InstantiationException {
        List<Criterion> restrictions = new ArrayList<>();

        if (!ValidationHelper.isNullOrEmpty(getLandCultureName())) {
            restrictions.add(Restrictions.eq("name", getLandCultureName()));
        }
        if (!ValidationHelper.isNullOrEmpty(getSelectedFilterQuality())) {
            restrictions.add(Restrictions.in("landCadastralCultures", getSelectedFilterQuality()));
        }
        restrictions.add(Restrictions.or(Restrictions.eq("isDeleted", Boolean.FALSE),
                Restrictions.isNull("isDeleted")));

        this.loadList(LandCulture.class, restrictions.toArray(new Criterion[0]), new Order[]
                {Order.asc("name")});
    }

    public void clearFilterPanel() throws PersistenceBeanException, IllegalAccessException, InstantiationException {
        filterTableFromPanel();
    }

    @Override
    public void editEntity() throws HibernateException, InstantiationException, IllegalAccessException, PersistenceBeanException {
        if (this.getCanEdit()) {
            this.cleanValidation();
            if (!ValidationHelper.isNullOrEmpty(this.getEntityEditId())) {
                try {
                    this.setEntity(DaoManager.get(getType(), this.getEntityEditId()));
                    if(!Hibernate.isInitialized(this.getEntity().getLandCadastralCultures()))
                        Hibernate.initialize(this.getEntity().getLandCadastralCultures());

                    DaoManager.getSession().evict(this.getEntity());


                    setLandCadastralCultures(getEntity().getLandCadastralCultures());
                    setSelectedQuality(new ArrayList<>());
                    CollectionUtils.emptyIfNull(getEntity().getLandCadastralCultures())
                            .stream()
                            .filter(lcc -> !ValidationHelper.isNullOrEmpty(lcc.getDescription()))
                            .forEach(lcc -> {
                                if(!getSelectedQuality().contains(lcc.getDescription()))
                                    getSelectedQuality().add(lcc.getDescription());
                            });
                } catch (Exception e) {
                    LogHelper.log(log, e);
                }
            }
            RequestContext.getCurrentInstance().update("addLandCultureDialog");
            executeJS("PF('addLandCultureDialogWV').show();");
        }
    }

    @Override
    public void addEntity() throws HibernateException, InstantiationException, IllegalAccessException, PersistenceBeanException {
        if (this.getCanCreate()) {
            setEntity(new LandCulture());
            this.cleanValidation();
            RequestContext.getCurrentInstance().update("addLandCultureDialog");
            executeJS("PF('addLandCultureDialogWV').show();");
        }
    }

    public void save() throws HibernateException, PersistenceBeanException, NumberFormatException, IOException,
            InstantiationException, IllegalAccessException {
        this.cleanValidation();
        this.setValidationFailed(false);

        try {
            this.validate();
        } catch (PersistenceBeanException e) {
            LogHelper.log(log, e);
        }
        if (this.getValidationFailed()) {
            return;
        }
        saveEntity();
        this.resetFields();
        executeJS("PF('addLandCultureDialogWV').hide()");
        executeJS("refreshTable()");
    }

    protected void validate() throws PersistenceBeanException {
        if(ValidationHelper.isNullOrEmpty(getEntity().getName())) {
            addRequiredFieldException("form:landCultureName");
        }
    }

    public void resetFields() throws PersistenceBeanException, IllegalAccessException, InstantiationException {
        setEntity(new LandCulture());
        setSelectedQuality(null);
        this.cleanValidation();
        this.filterTableFromPanel();
    }

    @Override
    public void deleteEntity() throws HibernateException, PersistenceBeanException, InstantiationException, IllegalAccessException, NumberFormatException, IOException {
        this.setEntity(DaoManager.get(getType(), this.getEntityDeleteId()));
        getEntity().setIsDeleted(Boolean.TRUE);
        saveEntity();
        filterTableFromPanel();
    }

    private void saveEntity() {
        Transaction tr = null;
        try {
            tr = PersistenceSessionManager.getBean().getSession()
                    .beginTransaction();
            DaoManager.save(this.getEntity());

            if(!ValidationHelper.isNullOrEmpty(getLandCadastralCultures())){
                List<LandCadastralCulture> toRemove = getLandCadastralCultures()
                        .stream()
                        .filter(lcc -> !getSelectedQuality().contains(lcc.getDescription()))
                        .collect(Collectors.toList());

                if(!ValidationHelper.isNullOrEmpty(toRemove)){
                    for(LandCadastralCulture landCadastralCulture : toRemove){
                        DaoManager.remove(landCadastralCulture);
                    }
                }
            }

            for (String quality : getSelectedQuality()) {
                List<LandCadastralCulture> landCadastralCultures = DaoManager.load(LandCadastralCulture.class,
                        new Criterion[]{
                                Restrictions.eq("landCulture.id",getEntity().getId()),
                                Restrictions.eq("description", quality)
                        });

                if(ValidationHelper.isNullOrEmpty(landCadastralCultures)){
                    LandCadastralCulture landCadastralCulture = new LandCadastralCulture();
                    landCadastralCulture.setLandCulture(getEntity());
                    landCadastralCulture.setDescription(quality);
                    DaoManager.save(landCadastralCulture);
                }
            }
        } catch (Exception e) {
            if (tr != null) {
                tr.rollback();
            }
            LogHelper.log(log, e);
        } finally {
            if (tr != null && !tr.wasRolledBack()
                    && tr.isActive()) {
                tr.commit();
            }
        }
    }
}

