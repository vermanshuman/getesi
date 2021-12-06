package it.nexera.ris.common.enums;

/**
 * (non-Javadoc)
 *
 * @author Vlad Strunenko
 * @see it.nexera.ris.common.enums.PropertyXMLElements
 */
public enum CadastralDataXMLElements implements XMLElements {

    SECTION("SezUrbana"),
    SHEET("Foglio"),
    SUB("Subalterno"),
    PARTICLE("ParticellaNum");

    private String element;

    private CadastralDataXMLElements(String element) {
        this.element = element;
    }

    public String getElement() {
        return element;
    }

}
