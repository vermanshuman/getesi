package it.nexera.ris.persistence.beans.entities.domain.dictionary;

import it.nexera.ris.persistence.beans.entities.IndexedEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "day_phrase")
public class DayPhrase extends IndexedEntity {

    @Column(name = "phrase")
    private String phrase;

    @Override
    public String toString() {
        return phrase;
    }

    public String getPhrase() {
        return phrase;
    }

    public void setPhrase(String phrase) {
        this.phrase = phrase;
    }
}
