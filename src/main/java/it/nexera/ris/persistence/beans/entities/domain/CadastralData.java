package it.nexera.ris.persistence.beans.entities.domain;

import it.nexera.ris.common.helpers.ValidationHelper;
import it.nexera.ris.persistence.beans.dao.ConnectionManager;
import it.nexera.ris.persistence.beans.entities.IndexedEntity;
import org.hibernate.Session;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "cadastral_data")
public class CadastralData extends IndexedEntity {

    private static final long serialVersionUID = 5252651481527077112L;

    @Column(name = "section", length = 10)
    private String section;

    @Column(name = "sheet", length = 30)
    private String sheet;

    @Column(name = "particle", length = 30)
    private String particle;

    @Column(name = "sub", length = 30)
    private String sub;

    @Column(name = "scheda")
    private String scheda;

    @Column(name = "data_scheda")
    private String dataScheda;

    @ManyToMany(mappedBy = "cadastralData")
    private List<Property> propertyList;

    @ManyToOne
    @JoinColumn(name = "request_print_property_id")
    private RequestPrintProperty requestPrintProperty;

    @Transient
    private Integer tempId;

    public CadastralData() {
    }

    public CadastralData loadOrCopy(Session session) throws IllegalAccessException, InstantiationException {
        CadastralData cd = null;
        if (!ValidationHelper.isNullOrEmpty(getSection()) || !ValidationHelper.isNullOrEmpty(getSheet())
                || !ValidationHelper.isNullOrEmpty(getParticle()) || !ValidationHelper.isNullOrEmpty(getSub())) {
            List<Criterion> restrictions = new ArrayList<>();

            if (!ValidationHelper.isNullOrEmpty(getParticle())) {
                restrictions.add(Restrictions.eq("particle", getParticle()));
            } else {
                restrictions.add(Restrictions.or(
                        Restrictions.isNull("particle"),
                        Restrictions.eq("particle", "")));
            }

            if (!ValidationHelper.isNullOrEmpty(getSection())) {
                restrictions.add(Restrictions.eq("section", getSection()));
            } else {
                restrictions.add(Restrictions.or(
                        Restrictions.isNull("section"),
                        Restrictions.eq("section", "")));
            }

            if (!ValidationHelper.isNullOrEmpty(getSheet())) {
                restrictions.add(Restrictions.eq("sheet", getSheet()));
            } else {
                restrictions.add(Restrictions.or(
                        Restrictions.isNull("sheet"),
                        Restrictions.eq("sheet", "")));
            }

            if (!ValidationHelper.isNullOrEmpty(getSub())) {
                restrictions.add(Restrictions.eq("sub", getSub()));
            } else {
                restrictions.add(Restrictions.or(
                        Restrictions.isNull("sub"),
                        Restrictions.eq("sub", "")));
            }

            List<CadastralData> list = ConnectionManager.load(CadastralData.class, restrictions.toArray(new Criterion[0]),
                    session);
            if (!ValidationHelper.isNullOrEmpty(list)) {
                cd = list.get(0);
            }
        }
        if (cd == null) {
            cd = new CadastralData();
            cd.setSection(getSection());
            cd.setSheet(getSheet());
            cd.setParticle(getParticle());
            cd.setSub(getSub());
            cd.setScheda(getScheda());
            cd.setDataScheda(getDataScheda());
        }
        return cd;
    }

    public CadastralData(CadastralData other) {
        this.section = other.section;
        this.sheet = other.sheet;
        this.particle = other.particle;
        this.sub = other.sub;
        this.scheda = other.scheda;
        this.dataScheda = other.dataScheda;
        this.propertyList = other.propertyList;
        this.requestPrintProperty = other.requestPrintProperty;
        this.tempId = other.tempId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CadastralData data = (CadastralData) o;
        return Objects.equals(section, data.section) &&
                Objects.equals(sheet, data.sheet) &&
                Objects.equals(particle, data.particle) &&
                Objects.equals(sub, data.sub);
    }

    public boolean isPresumable(CadastralData cd) {
        return ValidationHelper.areStringsEqualWithNullOrEmpty(this.getSection(), cd.getSection())
                && ValidationHelper.areStringsEqualWithNullOrEmpty(this.getSheet(), cd.getSheet())
                && ValidationHelper.areStringsEqualWithNullOrEmpty(this.getParticle(), cd.getParticle())
                && ((!ValidationHelper.isNullOrEmpty(this.getScheda()) && !ValidationHelper.isNullOrEmpty(this.getDataScheda()))
                    ? this.getScheda().equals(cd.getScheda()) && this.getDataScheda().equals(cd.getDataScheda())
                    : ValidationHelper.areStringsEqualWithNullOrEmpty(this.getSub(), cd.getSub()));
    }

    @Override
    public int hashCode() {

        return Objects.hash(section, sheet, particle, sub);
    }

    public String getSection() {
        return section;
    }

    public void setSection(String section) {
        this.section = section;
    }

    public String getSheet() {
        return sheet;
    }

    public void setSheet(String sheet) {
        this.sheet = sheet;
    }

    public String getParticle() {
        return particle;
    }

    public void setParticle(String particle) {
        this.particle = particle;
    }

    public String getSub() {
        return sub;
    }

    public void setSub(String sub) {
        this.sub = sub;
    }

    public List<Property> getPropertyList() {
        return propertyList;
    }

    public void setPropertyList(List<Property> propertyList) {
        this.propertyList = propertyList;
    }

    public Integer getTempId() {
        return tempId;
    }

    public void setTempId(Integer tempId) {
        this.tempId = tempId;
    }

    public RequestPrintProperty getRequestPrintProperty() {
        return requestPrintProperty;
    }

    public void setRequestPrintProperty(
            RequestPrintProperty requestPrintProperty) {
        this.requestPrintProperty = requestPrintProperty;
    }

    public String getScheda() {
        return scheda;
    }

    public void setScheda(String scheda) {
        this.scheda = scheda;
    }

    public String getDataScheda() {
        return dataScheda;
    }

    public void setDataScheda(String dataScheda) {
        this.dataScheda = dataScheda;
    }
}
