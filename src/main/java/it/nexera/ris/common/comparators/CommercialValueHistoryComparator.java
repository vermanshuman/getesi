package it.nexera.ris.common.comparators;

import it.nexera.ris.persistence.beans.entities.domain.CommercialValueHistory;

import java.util.Comparator;

public class CommercialValueHistoryComparator
        implements Comparator<CommercialValueHistory> {

    @Override
    public int compare(CommercialValueHistory o1, CommercialValueHistory o2) {
        if(o1.getId() == null || o2.getId() == null){
            return 0;
        }
        return o2.getId().compareTo(o1.getId());
    }

}
