package it.nexera.ris.common.enums;


public enum EstateFormalityXMLElements implements XMLElements {
    DESCRIPTION("Descrizione"),
    PROVENANCE("Provenienza"),
    PROCEDURE_TYPE("TipoQualifica"),
    DATE("Data"),
    NUM_RPB("NumRPBis"),
    NUM_RP("NumRP"),
    NUM_RG("NumRG"),
    REPERTOIRE("Repertorio"),
    ACT_CODE("CodiceAtto"),
    SPECIES_ACT("SpecieAtto"),
    NOTE_TYPE("TipoNota"),
    TITLE_DATE("DataTitolo"),
    DENOMINATION_PU("DenominazionePU"),
    RIF_ANNO("RifAnno");

    private String element;

    EstateFormalityXMLElements(String element) {
        this.element = element;
    }

    public String getElement() {
        return element;
    }
}
