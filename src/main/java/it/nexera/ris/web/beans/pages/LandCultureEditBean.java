package it.nexera.ris.web.beans.pages;

import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.helpers.ValidationHelper;
import it.nexera.ris.common.xml.wrappers.CitySelectItem;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.domain.LandCadastralCulture;
import it.nexera.ris.persistence.beans.entities.domain.LandCulture;
import it.nexera.ris.persistence.beans.entities.domain.LandOmiValue;
import it.nexera.ris.persistence.beans.entities.domain.Property;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.City;
import it.nexera.ris.web.beans.EntityEditPageBean;
import lombok.Data;
import org.apache.commons.collections4.CollectionUtils;
import org.hibernate.HibernateException;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@ManagedBean(name = "landCultureEditBean")
@ViewScoped
@Data
public class LandCultureEditBean extends EntityEditPageBean<LandCulture>
        implements Serializable {

    private static final long serialVersionUID = 4004310257870599002L;

    private List<String> selectedQuality;

    private List<String> qualities;

    private List<LandCadastralCulture> landCadastralCultures;

    @Override
    public void onLoad() throws NumberFormatException, HibernateException,
            PersistenceBeanException, InstantiationException,
            IllegalAccessException {
        setQualities(new ArrayList<>());
        getQualities().addAll(DaoManager.loadDistinctfield(Property.class, "quality", String.class,
                null,  new Criterion[] {
                        Restrictions.isNotNull("quality")
                }));
        fillFields();
    }

    @Override
    public void onValidate() throws PersistenceBeanException,
            HibernateException, IllegalAccessException {
        if(ValidationHelper.isNullOrEmpty(getEntity().getName())) {
            addRequiredFieldException("form:landCultureName");
        }
    }

    @Override
    public void onSave() throws HibernateException, PersistenceBeanException,
            NumberFormatException, IOException, InstantiationException,
            IllegalAccessException {
        DaoManager.save(getEntity());

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
    }

    private void fillFields() {
        setLandCadastralCultures(getEntity().getLandCadastralCultures());
        setSelectedQuality(new ArrayList<>());
        CollectionUtils.emptyIfNull(getEntity().getLandCadastralCultures())
                .stream()
                .filter(lcc -> !ValidationHelper.isNullOrEmpty(lcc.getDescription()))
                .forEach(lcc -> {
                    if(!getSelectedQuality().contains(lcc.getDescription()))
                        getSelectedQuality().add(lcc.getDescription());
                });
    }
}
