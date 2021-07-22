package it.nexera.ris.common.comparators;

import it.nexera.ris.persistence.beans.entities.domain.EstimateOMIHistory;

import java.util.Comparator;

public class EstimateOMIComparator implements Comparator<EstimateOMIHistory> {

    @Override
    public int compare(EstimateOMIHistory o1, EstimateOMIHistory o2) {
        return o2.getId().compareTo(o1.getId());
    }

}
