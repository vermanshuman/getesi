package it.nexera.ris.persistence.beans.entities.domain.dictionary;

import it.nexera.ris.common.helpers.ValidationHelper;
import it.nexera.ris.persistence.beans.dao.CriteriaAlias;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.Dictionary;
import it.nexera.ris.persistence.beans.entities.domain.AggregationService;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;
import org.hibernate.sql.JoinType;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.util.stream.Collectors;

@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@Table(name = "aggregation_az")
public class AggregationAZ extends Dictionary {

    private static final long serialVersionUID = -6796159970132492880L;

    @Column(name = "is_deleted")
    private Boolean isDeleted;

    @Transient
    private String aggregationServiceName;

    public String getAggregationServiceName() {
        if (!isNew()) {
            try {
                AggregationService aggregationService = DaoManager.get(AggregationService.class,
                        new CriteriaAlias[]{
                                new CriteriaAlias("aggregationAZ", "a", JoinType.INNER_JOIN)
                        },
                        new Criterion[]{
                                Restrictions.eq("a.id", getId())});
                if (!ValidationHelper.isNullOrEmpty(aggregationService)
                        && !ValidationHelper.isNullOrEmpty(aggregationService.getServices()))
                    return aggregationService.getServices().stream()
                            .map( n -> n.getName() )
                            .collect( Collectors.joining( "," ));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public void setAggregationServiceName(String aggregationServiceName) {
        this.aggregationServiceName = aggregationServiceName;
    }

    @Override
    public String toString() {
        return getCode();
    }

}
