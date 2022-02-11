package it.nexera.ris.common.enums;

/**
 * (non-Javadoc)
 *
 * @author Vlad Strunenko
 * @see it.nexera.ris.common.enums.PropertyXMLElements
 */
public enum TagsForPDFXMLElements {

    PROVINCE("ElencoSintetico.DescrizioneUfficio", "%PROVINCE%", true),
    CONSERVATORIA_NAME("ElencoSintetico.DescrizioneUfficio", "%CONSERVATORIA_NAME%", false),
    COMMUNICATON_DATE("ElencoSintetico.DataEsecuzione", "%COMMUNICATON_DATE%", true),
    DOCUMENT_CREATE_DATE("", "%DOCUMENT_CREATE_DATE%", true),
    DOCUMENT_CREATE_TIME("", "%DOCUMENT_CREATE_TIME%", true),
    NUMBER_INSPECTION("", "%NUMBER_INSPECTION%", false),
    DATE_INSPECTION("ElencoSintetico.DataRichiesta", "%DATE_INSPECTION%", true),
    APPLICANT_DATA("", "%APPLICANT_DATA%", true),
    SUBJECT_ROW("", "%SUBJECT_ROW%", true),
    SUBJECT_FISCAL_CODE("SoggettoN.CodiceFiscale", "%SUBJECT_FISCAL_CODE%", true),
    SUBJECT_SURNAME("SoggettoF.Cognome", "%SUBJECT_SURNAME%", true),
    SUBJECT_NAME("SoggettoF.Nome", "%SUBJECT_NAME%", true),
    SUBJECT_BIRTH_DATE("SoggettoF.DataNascita", "%SUBJECT_BIRTH_DATE%", true),
    TYPE_DISPLAYED_FORMALITIES("DatiFormalita.Trascrizioni DatiFormalita.Iscrizioni DatiFormalita.Annotazioni", "%TYPE_DISPLAYED_FORMALITIES%", true),
    RESTRICTIONS("DatiFormalita.EsclusioneIpotecheCancellate DatiFormalita.EsclusioneTrascrizioniNonRinnovate", "%RESTRICTIONS%", true),
    PERIOD_FROM_COMPUTERIZED("SituazioneAggiornamento.DataDal", "%PERIOD_FROM_COMPUTERIZED%", true),
    PERIOD_TO_COMPUTERIZED("SituazioneAggiornamento.DataAl", "%PERIOD_TO_COMPUTERIZED%", true),
    PERIOD_FROM_VALIDATED("SituazioneAggiornamento.DataDal", "%PERIOD_FROM_VALIDATED%", true),
    PERIOD_TO_VALIDATED("SituazioneAggiornamento.DataAl", "%PERIOD_TO_VALIDATED%", true),
    ADDITIONAL_INFORMATION("SoggettiImmobiliSelezionati.InformazioniAggiuntive", "%ADDITIONAL_INFORMATION%", false),
    VOLUMES_REPERTORIES("VolumiRepertori.InformazioniAggiuntive", "%VOLUMES_REPERTORIES%", true),
    SUBJECT_TABLE("", "%SUBJECT_TABLE%", true),
    SYNTHETIC_TABLE("", "%SYNTHETIC_TABLE%", true),
    SYNTHETIC_HEADER("", "%SYNTHETIC_HEADER%", true),
    SERVICE_TYPE_C("TitoloVisura.TipoServizio", "%SERVICE_TYPE_C%", false),
    PROVENANCE_C("TitoloVisura.Provenienza", "%PROVENANCE_C%", false),
    VISURA_TITLE_C("TitoloVisura.Titolo", "%VISURA_TITLE_C%", false),
    SITUATION_TO_C("TitoloVisura.SituazioneAl", "%SITUATION_TO_C%", true),
    VISURA_TITLE_DATE_C("TitoloVisura.Data", "%VISURA_TITLE_DATE_C%", true),
    VISURA_TITLE_TIME_C("TitoloVisura.Ora", "%VISURA_TITLE_TIME_C%", true),
    SUBJECT_TABLE_C("", "%SUBJECT_TABLE_C%", true),
    SUBJECT_SURNAME_C("SoggettoPF.Cognome", "%SUBJECT_SURNAME_C%", false),
    SUBJECT_NAME_C("SoggettoPF.Nome", "%SUBJECT_NAME_C%", false),
    DATA_REQUEST_PROVINCE_C("DatiRichiesta.Provincia", "%DATA_REQUEST_PROVINCE_C%", true),
    DATA_REQUEST_COMUNE_C("DatiRichiesta.Comune", "%DATA_REQUEST_COMUNE_C%", true),
    DATA_REQUEST_CODICE_COMUNE_C("DatiRichiesta.CodiceComune", "%DATA_REQUEST_CODICE_COMUNE_C%", true),
    DATA_REQUEST_FOGLIO_C("DatiRichiesta.Foglio", "%DATA_REQUEST_FOGLIO_C%", true),
    DATA_REQUEST_PARTICELLANUM_C("DatiRichiesta.ParticellaNum", "%DATA_REQUEST_PARTICELLANUM_C%", true),
    DATA_REQUEST_SUBALTERNO_C("DatiRichiesta.Subalterno", "%DATA_REQUEST_SUBALTERNO_C%", true),
    PROPERTY_INSTAT_TABLE_C("", "%PROPERTY_INSTAT_TABLE_C%", true),
    SUBJECT_CITY_C("SoggettoPF.ComuneNascita", "%SUBJECT_CITY_C%", false),
    SUBJECT_BIRTHDATE_C("SoggettoPF.DataNascita", "%SUBJECT_BIRTHDATE_C%", true),
    SUBJECT_FISCALCODE_C("SoggettoPF.CodiceFiscale", "%SUBJECT_FISCALCODE_C%", false),
    SUBJECT_DENOMINATION_C("SoggettoPNF.Denominazione", "%SUBJECT_DENOMINATION_C%", false),
    SUBJECT_SEAT_C("SoggettoPNF.Sede", "%SUBJECT_SEAT_C%", false),
    SUBJECT_FISCALCODE_C_L("SoggettoPNF.CodiceFiscale", "%SUBJECT_FISCALCODE_C_L%", false),
    PROPERTY_BUILDING_TABLE("ImmobileFabbricati", "%PROPERTY_BUILDING_TABLE%", true),
    PROPERTY_LAND_TABLE("ImmobileTerreni", "%PROPERTY_LAND_TABLE%", true),
    PROPERTY_TABLES("", "%PROPERTY_TABLES%", true),
    RESIDENTIAL_UNITS("DatiLiquidazione.UnitaImmobiliari", "%RESIDENTIAL_UNITS%", false),
    CENTRAL_GOVERNMENT_TAXES("DatiLiquidazione.TributiErariali", "%CENTRAL_GOVERNMENT_TAXES%", false),
    SUMMARY_ROWS("", "%SUMMARY_ROWS%", true),
    HISTORY_ROWS("", "%HISTORY_ROWS%", true),
    PROPERTY_HISTORY_TABLE("MutazioneSoggettiva", "%PROPERTY_HISTORY_TABLE%", true),
    PADRI_ROWS("", "%PADRI_ROWS%", true),

    /*Added for handling new format*/
    PROPERTY_BUILDING_TABLE_ALT("ImmobileFabbricatiS", "%PROPERTY_BUILDING_TABLE_ALT%", true),
    PROPERTY_LAND_TABLE_ALT("ImmobileTerreniS", "%PROPERTY_LAND_TABLE_ALT%", true);
    private String elementXML;

    private String elementHTML;

    private boolean specialFlow;

    private TagsForPDFXMLElements(String elementXML, String elementHTML,
                                  boolean specialFlow) {
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
