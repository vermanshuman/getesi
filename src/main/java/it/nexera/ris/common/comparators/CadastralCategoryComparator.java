package it.nexera.ris.common.comparators;

import it.nexera.ris.persistence.beans.entities.domain.dictionary.CadastralCategory;

import java.util.Comparator;

public class CadastralCategoryComparator implements Comparator<CadastralCategory> {
    @Override
    public int compare(CadastralCategory o1, CadastralCategory o2) {
        Comparator<CadastralCategory> cs = Comparator.comparing(str -> str.getCode().replaceAll("\\d", ""));
        if (!o1.getCode().replaceAll("\\D", "").equals("")
                && !o2.getCode().replaceAll("\\D", "").equals("")) {
            Comparator<CadastralCategory> ci = Comparator.comparingInt(str ->
                    Integer.parseInt(str.getCode().replaceAll("\\D", "")));
            return cs.thenComparing(ci).compare(o1, o2);
        }
        return cs.compare(o1, o2);
    }
}
