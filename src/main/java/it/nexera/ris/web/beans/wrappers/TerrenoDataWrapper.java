package it.nexera.ris.web.beans.wrappers;

import it.nexera.ris.persistence.beans.entities.domain.Property;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class TerrenoDataWrapper implements Serializable {

    private static final long serialVersionUID = -5602578498629673398L;

    Property property;
    String data;
    String sheet;

    public TerrenoDataWrapper(Property property, String sheet, String data) {
        this.property = property;
        this.sheet = sheet;
        this.data = data;
    }
}