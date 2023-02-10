package it.nexera.ris.common.enums;

import it.nexera.ris.common.helpers.EnumHelper;
import it.nexera.ris.common.helpers.ResourcesHelper;

public enum DocumentGenerationTags {

    GENERAL_CURRENT_DATE("general_current_date"),
    GENERAL_CURRENT_USER("general_current_user"),
    REQUEST_CONSERVATORY("request_conservatory"),
    CONSERVATION_DATE("conservation_date"),
    SUBJECT_REGISTRY("subject_registry"),
    CLIENT_NAME("client_name"),
    CLIENT_FISCAL_CODE("client_fiscal_code"),
    CLIENT_ADDRESS_NUMBER("client_address_number"),
    CLIENT_ADDRESS_CITY("client_city"),
    CLIENT_ADDRESS_PROVINCE("client_province"),
    CLIENT_PHONE("client_phone"),
    CLIENT_CELL("client_cell"),
    CLIENT_MAIL("client_mail"),
    CLIENT_ADDRESS_INFO("client_address"),
    CLIENT_NAME_PROFESSIONAL("client_name_professional"),
    AGGREGATE_CONSERVATORY_NAME("aggregate_conservatory_name"),
    SERVICE_REQUEST("service_request"),
    EMERGENCY_REQUEST("emergency_request"),
    REQUESTED_DATE("requested_date"),
    MOVEMENTS_AUTHORIZED_REQUEST("movements_authorized_request"),
    NOTES_REQUEST("notes_request"),
    SUBJECT_S_FORMALITIES("subject_s_formalities"),
    SUBJECT_S_CADASTRAL("subject_s_cadastral"),
    REAL_ESTATE_RELATIONSHIP_TABLE("real_estate_relationship_table"),
    ALIENATED_TABLE("alienated_table"),
    CERTIFICAZIONE_TABLE("certificazione_table"),
    DECEASED_TABLE("deceased_table"),
    NEGATIVE_TABLE("negative_table"),
    NO_ASSETS_TABLE("no_assets_table"),
    SUBJECT_LAND_REGISTRATION("subject_land_registration"),
    REAL_ESTATE_REPORT_NOTE("real_estate_report_note"),
    RICHIEDENTE_RICHIESTA("richiedente richiesta"),
    FILIALE_RICHIEDENTE_RICHIESTA("filiale_richiedente_richiesta"),
    NUMERO_ATTI("numero_atti"),
    COSTO_RICHIESTA("costo_richiesta"),
    TRASCRIZIONE_REPERTORIO("trascrizione_repertorio"),
    TRASCRIZIONE_CODICEFISCALEPU("trascrizione_codiceFiscalepu"),
    TRASCRIZIONE_PUBBLICO_UFFICIALE("trascrizione_pubblico_ufficiale"),
    TRASCRIZIONE_CF_RICHIEDENTE("trascrizione_cf_richiedente"),
    TRASCRIZIONE_NOME_RICHIEDENTE("trascrizione_nome_richiedente"),
    TRASCRIZIONE_INDIRIZZO_RICHIEDENTE("trascrizione_indirizzo_richiedente"),
    TRASCRIZIONE_DESCRIZIONE_TITOLO("trascrizione_descrizione_titolo"),
    TRASCRIZIONE_DATA_TITOLO("trascrizione_data_titolo"),
    TRASCRIZIONE_ATTO_CODICE_ATTO("trascrizione_atto_codice_atto"),
    TRASCRIZIONE_ATTO_DESCRIZIONE_ATTO("trascrizione_atto_descrizione_atto"),
    TRASCRIZIONE_SPECIE_ATTO("trascrizione_specie_atto"),
    TRASCRIZIONE_TIPO_NOTA("trascrizione_tipo_nota"),
    TRASCRIZIONE_CODICECONSERVATORIA("trascrizione_codiceConservatoria"),
    TRASCRIZIONE_UNITA_NEGOZIALI("trascrizione_unita_negoziali"),
    TRASCRIZIONE_DATI_ATTO("trascrizione_dati_atto"),
    TRASCRIZIONE_DATI_ASSOCIAZIONE("trascrizione_dati_associazione"),
    TRASCRIZIONE_QUADROD("trascrizione_quadrod"),
    TRASCRIZIONE_DATII_MMOBILI("trascrizione_dati_immobili"),
    TASCRIZIONE_DATI_SOGGETTI("tascrizione_dati_soggetti"),
    TASCRIZIONE_DTD("tascrizione_dtd"),
    INIT_TEXT_REGISTRY_OR_TABLE("init_text_registry_or_table"),
    ATTACHMENT_INDICATION("Indicazione allegati"),
    ATTACHMENT_A("Allegato A"),
    ATTACHMENT_B("Allegato B"),
    ATTACHMENT_C("Allegato C");


    private String tag;

    DocumentGenerationTags(String editorValue) {
        this.tag = editorValue;
    }

    @Override
    public String toString() {
        return ResourcesHelper.getEnum(EnumHelper.toStringFormatter(this));
    }

    public static DocumentGenerationTags getByTag(String tag) {
        for (DocumentGenerationTags item : DocumentGenerationTags.values()) {
            if (item.getTag().equals(tag)) {
                return item;
            }
        }

        return null;
    }

    public String getTag() {
        return tag.contains("%") ? tag : '%' + tag + '%';
    }

    public void setTag(String editorValue) {
        this.tag = editorValue.contains("%") ? editorValue
                : '%' + editorValue + '%';
    }
}
