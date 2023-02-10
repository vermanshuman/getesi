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
public class DataSocietyDTO {
    private String note;

    @SerializedName("ragione_sociale")
    private String businessName;

    @SerializedName( "data_revoca_procedura_concorsuale")
    private String revokeDateProceeding;

    @SerializedName( "data_chiusura_procedura_concorsuale")
    private String closeDateProceeding;

    @SerializedName( "procedure_concorsuali")
    private String insolvencyProceeding;

    @SerializedName( "importo_protestato")
    private BigDecimal importTracking;

    @SerializedName( "codice_ateco")
    private String codeAteco;

    @SerializedName( "data_inizio_attivita")
    private String dateStart;

    @SerializedName( "data_cessazione_liquidazione")
    private String dateEnd;

    @SerializedName( "data_costituzione")
    private String dateCreation;

    @SerializedName( "natura_giuridica")
    private String giuridicNature;

    @SerializedName( "data_status")
    private String dataStatus;

    private String status;

    @SerializedName( "tipo")
    private String type;

    @SerializedName( "partita_iva")
    private String numberVat;

    @SerializedName("indirizzo_toponimo")
    private String toponymAddress;

    @SerializedName("indirizzo_via")
    private String streetAddress;

    @SerializedName("indirizzo_civico")
    private String civicAddress;

    @SerializedName("indirizzo_presso")
    private String addressAt;

    @SerializedName("indirizzo_comune")
    private String cityAddress;

    @SerializedName("indirizzo_cap")
    private String postalAddress;

    @SerializedName("indirizzo_provincia")
    private String addressProvince;

    @SerializedName("indirizzo_stato")
    private String addressStatus;

    @SerializedName("attiva")
    private Boolean active;

    @SerializedName( "protesti")
    private String protest;

    @SerializedName( "pregiudizievoli")
    private String prejudicial;

    @SerializedName( "condominio")
    private String condominium;

    private String cciaa;

    @SerializedName( "capitale_sociale")
    private BigDecimal socialAmount;

    private String pec;

}
