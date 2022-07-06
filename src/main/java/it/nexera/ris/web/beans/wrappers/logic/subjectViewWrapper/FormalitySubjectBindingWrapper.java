package it.nexera.ris.web.beans.wrappers.logic.subjectViewWrapper;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.primefaces.component.column.Column;
import org.primefaces.model.LazyDataModel;

import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.helpers.ResourcesHelper;
import it.nexera.ris.common.helpers.ValidationHelper;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.domain.DocumentSubject;
import it.nexera.ris.persistence.beans.entities.domain.ReportFormalitySubject;
import it.nexera.ris.persistence.beans.entities.domain.Subject;
import it.nexera.ris.persistence.beans.entities.domain.VisureRTF;
import it.nexera.ris.web.common.EntityLazyListModel;
import it.nexera.ris.web.converters.DateConverter;


public class FormalitySubjectBindingWrapper extends BaseTab implements Serializable {

    private static final long serialVersionUID = -6547841494395752324L;

    private List<Long> listIds;
    
    List<String> list;
    
    public FormalitySubjectBindingWrapper(List<Long> ids) {
        this.listIds = ids;
        try {
            List<Criterion> criteriaList = new ArrayList<>();
           
            criteriaList.add(Restrictions.in("id", getListIds()));
            List<Subject> subjectList =
                    DaoManager.load(Subject.class,
            criteriaList.toArray(new Criterion[0]));
            
            List<String> cfPIva = subjectList
                    .stream()
                    .filter(s -> !ValidationHelper.isNullOrEmpty(s.getFiscalCodeVATNamber()))
                    .map(Subject:: getFiscalCodeVATNamber)
                    .map(String::trim)
                    .distinct()
                    .collect(Collectors.toList());
            setList(cfPIva);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } 
    }

    @Override
    public String getTabTitle() {
        return ResourcesHelper.getString("subjectViewValidated");
    }

    @Override
    public LazyDataModel getLazyDataModel() {
        return new EntityLazyListModel(ReportFormalitySubject.class, 
                new Criterion[]{
                        Restrictions.or(
                                Restrictions.in("fiscalCode", getList()),
                                Restrictions.in("numberVAT", getList())   
                                )}, new Order[]{Order.asc("createDate")});
    }

    @Override
    public Long getCountTable() throws PersistenceBeanException, IllegalAccessException {
        return DaoManager.getCount(ReportFormalitySubject.class, "id", new Criterion[]{
                Restrictions.or(
                        Restrictions.in("fiscalCode", getList()),
                        Restrictions.in("numberVAT", getList())   
                        )});
    }

    @Override
    public List<Column> getColumns() {
        List<Column> columns = new ArrayList<>();
        columns.add(getTextColumn("subjectViewTFConservatoria", "landChargesRegistryName"));
        columns.add(getTextColumn("subjectViewTFTypeFormality", "typeFormality"));
        columns.add(getTextColumn("subjectViewTFDate", "date", new DateConverter(), Date.class, ""));
        columns.add(getTextColumn("subjectViewTFNumber", "number"));
        return columns;
    }

    public List<Long> getListIds() {
        return listIds;
    }

    public void setListIds(List<Long> listIds) {
        this.listIds = listIds;
    }
    
    public void setList(List<String> list) {
        this.list = list;
    }

    public List<String> getList() {
        return list;
    }
}