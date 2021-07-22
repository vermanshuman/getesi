package it.nexera.ris.persistence.beans.entities.domain.dictionary;

import it.nexera.ris.persistence.beans.entities.Dictionary;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "no_camel_case_word")
public class NoCamelCaseWord extends Dictionary {

    private static final long serialVersionUID = 4085709564323279763L;

    @Override
    public String toString() {
        return getDescription();
    }


}
