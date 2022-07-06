package it.nexera.ris.web.beans.wrappers;

import it.nexera.ris.common.helpers.ValidationHelper;
import it.nexera.ris.persistence.beans.entities.domain.Formality;

import java.util.ArrayList;
import java.util.List;

public class PartedPairsByCityWrapper {

    Formality formality;

    List<Pair<String, String>> pairs;

    List<List<Pair<String, String>>> patredList;


    public void fillPatredList() {
        setPatredList(new ArrayList<>());
        String city = "";
        List<Pair<String, String>> temp = new ArrayList<>();

        for (int i = 0, j = 0; i < getPairs().size(); i++) {
            String tempCity = getPairs().get(i).getFirst();
            if (!ValidationHelper.isNullOrEmpty(tempCity)) {
                if (!ValidationHelper.isNullOrEmpty(city) && !city.equals(tempCity)) {
                    for (; j < i-2; j++) {
                        temp.add(getPairs().get(j));
                    }
                    getPatredList().add(temp);
                    temp = new ArrayList<>();
                }
                city = tempCity;
            }
        }
        if(ValidationHelper.isNullOrEmpty(getPatredList())) {
            getPatredList().add(getPairs());
        }
        if (getPairs().size() != getPatredListsSize()) {
            addOtherElements(getPatredListsSize());
        }
    }

    private void addOtherElements(int patredListsSize) {
        List<Pair<String, String>> temp = new ArrayList<>();
        for (int i = patredListsSize; i < getPairs().size(); i++) {
            temp.add(getPairs().get(i));
        }
        if(!ValidationHelper.isNullOrEmpty(temp)) {
            getPatredList().add(temp);
        }
    }

    public int getPatredListsSize() {
        int result = 0;
        for (List<Pair<String, String>> list : getPatredList()) {
            result += list.size();
        }
        return result;
    }

    public PartedPairsByCityWrapper(Formality formality, List<Pair<String, String>> pairs) {
        this.formality = formality;
        this.pairs = pairs;
    }

    public PartedPairsByCityWrapper() {
    }

    public Formality getFormality() {
        return formality;
    }

    public void setFormality(Formality formality) {
        this.formality = formality;
    }

    public List<List<Pair<String, String>>> getPatredList() {
        return patredList;
    }

    public void setPatredList(List<List<Pair<String, String>>> patredList) {
        this.patredList = patredList;
    }

    public List<Pair<String, String>> getPairs() {
        return pairs;
    }

    public void setPairs(List<Pair<String, String>> pairs) {
        this.pairs = pairs;
    }
}
