package it.nexera.ris.common.enums;

/**
 * (non-Javadoc)
 *
 * @author Vlad Strunenko
 * @see it.nexera.ris.common.enums.PropertyXMLElements
 */
public enum AnnotationXMLElements implements XMLElements {

    FORMALITY_TYPE("FormalitaSuccessive.TipoNota"),
    RECORD_DATE("FormalitaSuccessive.Data"),
    PARTICULAR_REGISTER("FormalitaSuccessive.NumRP"),
    ACT_TYPE("FormalitaSuccessive.CodiceAtto FormalitaSuccessive.Descrizione");

    private String element;

    private AnnotationXMLElements(String element) {
        this.element = element;
    }

    public String getElement() {
        return element;
    }

}
