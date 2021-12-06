package it.nexera.ris.common.enums;

/**
 * (non-Javadoc)
 *
 * @author Vlad Strunenko
 * @see it.nexera.ris.common.enums.PropertyXMLElements
 */
public enum SyntheticFormalityXMLElements implements XMLElements {

    DATE("DocumentoIpotecario.Data"),
    FORMALITY_TYPE("DocumentoIpotecario.TipoNota"),
    GENERAL_REGISTER("DocumentoIpotecario.NumRG"),
    NOTE("DocumentoIpotecario.Provenienza"),
    PARTICULAR_REGISTER("DocumentoIpotecario.NumRP"),
    ACT_TYPE_TYPE("DocumentoIpotecario.SpecieAtto"),
    ACT_TYPE_DESCRIPTION("DocumentoIpotecario.Descrizione"),
    SUBJECT_TYPE("DocumentoIpotecario.Qualifica"),
    QUALIFICATION_TYPE("DocumentoIpotecario.TipoQualifica");

    private String element;

    private SyntheticFormalityXMLElements(String element) {
        this.element = element;
    }

    public String getElement() {
        return element;
    }

}
