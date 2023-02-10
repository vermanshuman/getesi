package it.nexera.ris.web.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;

@Getter
@Setter
@JsonIgnoreProperties
@ToString(callSuper = true)
public class DataWorkDTO {
    private String note;

    @SerializedName("importo_mensile_netto")
    private BigDecimal netAmount;

    @SerializedName( "importo_mensile_lordo")
    private BigDecimal grossAmount;

    @SerializedName( "ultima_data_nota")
    private String lastDate;

    @SerializedName( "data_inizio")
    private String startDate;

    @SerializedName( "tipologia")
    private String type;

    @SerializedName( "ragione_sociale_datore")
    private String employer ;

    @SerializedName( "employer_fiscal_code")
    private String employerFiscalCode;

    @SerializedName( "partita_iva_datore")
    private String employerVat;

    @SerializedName("indirizzo_legale_toponimo")
    private String legalToponymAddress;

    @SerializedName("indirizzo_legale_via")
    private String legalStreetAddress;

    @SerializedName("indirizzo_legale_civico")
    private String legalCivicAddress;

    @SerializedName("indirizzo_legale_presso")
    private String legalAddressAt;

    @SerializedName("indirizzo_legale_comune")
    private String legalCityAddress;

    @SerializedName("indirizzo_legale_cap")
    private String legalPostalAddress;

    @SerializedName("indirizzo_legale_provincia")
    private String legalAddressProvince;

    @SerializedName("indirizzo_legale_stato")
    private String legalAddressStatus;

    @SerializedName("telefono_sede_legale")
    private String cellLegalAddress;

    @SerializedName("telefono2_sede_legale")
    private String cell2LegalAddress;

    @SerializedName("fax_sede_legale")
    private String faxLegalAddress;

    @SerializedName("indirizzo_operativa_toponimo")
    private String operationalToponymAddress;

    @SerializedName("indirizzo_operativa_via")
    private String operationalStreetAddress;

    @SerializedName("indirizzo_operativa_civico")
    private String operationalCivicAddress;

    @SerializedName("indirizzo_operativa_presso")
    private String operationalAddressAt;

    @SerializedName("indirizzo_operativa_comune")
    private String operationalCityAddress;

    @SerializedName("indirizzo_operativa_cap")
    private String operationalPostalAddress;

    @SerializedName("indirizzo_operativa_provincia")
    private String operationalAddressProvince;

    @SerializedName("indirizzo_operativa_stato")
    private String operationalAddressStatus;

    @SerializedName("telefono_sede_operativa")
    private String cellHeadquarter;

    @SerializedName("fax_sede_operativa")
    private String faxHeadquarter;

    @SerializedName("dipendente")
    private String employee;

    @SerializedName("tipologia_contratto")
    private String contractType;

    @SerializedName("data_scadenza_contratto")
    private String expirationContract;

    @SerializedName("reddito_annuale")
    private BigDecimal salary;

    @SerializedName("disoccupato")
    private String unemployed;

    @SerializedName("professionista")
    private String professional;

    @SerializedName("mansione")
    private String function;
}
