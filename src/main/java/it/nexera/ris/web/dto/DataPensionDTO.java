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
public class DataPensionDTO {
    private String note;

    @SerializedName("pensionato")
    private Boolean retired;

    @SerializedName("importo_mensile_netto")
    private BigDecimal netAmount;

    @SerializedName( "importo_mensile_lordo")
    private BigDecimal grossAmount;

    @SerializedName( "ultima_data")
    private String lastDate;

    @SerializedName( "reddito_annuale")
    private BigDecimal retirementAmount;

    @SerializedName( "data_decorrenza")
    private String retirementStartDate;

    @SerializedName( "tipologia")
    private String type;

    @SerializedName( "ragione_sociale_datore")
    private String retirementName;

    @SerializedName( "partita_iva_ente")
    private String retirementVat;

    @SerializedName("codice_fiscale_ente")
    private String retirementFiscalCode;

    @SerializedName("indirizzo_ente_toponimo")
    private String institutionToponymAddress;

    @SerializedName("indirizzo_ente_via")
    private String institutionStreetAddress;

    @SerializedName("indirizzo_ente_civico")
    private String institutionCivicAddress;

    @SerializedName("indirizzo_ente_presso")
    private String institutionAddressAt;

    @SerializedName("indirizzo_ente_comune")
    private String institutionCityAddress;

    @SerializedName("indirizzo_ente_cap")
    private String institutionPostalAddress;

    @SerializedName("indirizzo_ente_provincia")
    private String institutionAddressProvince;

    @SerializedName("indirizzo_ente_stato")
    private String institutionAddressStatus;

    @SerializedName("indirizzo_sede_operativa_toponimo")
    private String operationalToponymAddress;

    @SerializedName("indirizzo_sede_operativa_via")
    private String operationalStreetAddress;

    @SerializedName("indirizzo_sede_operativa_civico")
    private String operationalCivicAddress;

    @SerializedName("indirizzo_sede_operativa_presso")
    private String operationalAddressAt;

    @SerializedName("indirizzo_sede_operativa_comune")
    private String operationalCityAddress;

    @SerializedName("indirizzo_sede_operativa_cap")
    private String operationalPostalAddress;

    @SerializedName("indirizzo_sede_operativa_provincia")
    private String operationalAddressProvince;

    @SerializedName("indirizzo_sede_operativa_stato")
    private String operationalAddressStatus;
}
