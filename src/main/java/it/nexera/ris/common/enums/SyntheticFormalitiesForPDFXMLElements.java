package it.nexera.ris.common.enums;

/**
 * (non-Javadoc)
 *
 * @author Vlad Strunenko
 * @see it.nexera.ris.common.enums.PropertyXMLElements
 */
public enum SyntheticFormalitiesForPDFXMLElements {
    REFERENCE_FORMALITIES("", "%REFERENCE_FORMALITIES%", true),
    ACT_CODE_STRING("DocumentoIpotecario.CodiceAtto", "%ACT_CODE_STRING%", true),
    FORMALITY_NUMBER("", "%FORMALITY_NUMBER%", true),
    FORMALITY_TYPE("DocumentoIpotecario.TipoNota", "%FORMALITY_TYPE%", true),
    QUALIFICATION_TYPE("DocumentoIpotecario.TipoQualifica", "%QUALIFICATION_TYPE%", true),
    QUALIFICATION("DocumentoIpotecario.Qualifica", "%QUALIFICATION%", true),
    FORMALITY_DATE("DocumentoIpotecario.Data", "%FORMALITY_DATE%", true),
    RECORD_DETAIL("DocumentoIpotecario.NumRP", "%RECORD_DETAIL%", false),
    REGISTRAR_GENERAL("DocumentoIpotecario.NumRG", "%REGISTRAR_GENERAL%", false),
    ACT_TYPE("DocumentoIpotecario.SpecieAtto DocumentoIpotecario.Descrizione", "%ACT_TYPE%", true),
    PROPERTY_LOCATION("UbicazioneImmobili.Descrizione", "%PROPERTY_LOCATION%", true),
    PUBLIC_OFFICIAL("", "%PUBLIC_OFFICIAL%", true),
    PROVENANCE("DocumentoIpotecario.Provenienza", "%PROVENANCE%", true),
    AC_ROW("", "%AC_ROW%", true);

    private String elementXML;

    private String elementHTML;

    private boolean specialFlow;

    private SyntheticFormalitiesForPDFXMLElements(String elementXML,
                                                  String elementHTML, boolean specialFlow) {
        this.elementXML = elementXML;
        this.elementHTML = elementHTML;
        this.specialFlow = specialFlow;
    }

    public String getElementXML() {
        return elementXML;
    }

    public String getElementHTML() {
        return elementHTML;
    }

    public boolean isSpecialFlow() {
        return specialFlow;
    }
}
