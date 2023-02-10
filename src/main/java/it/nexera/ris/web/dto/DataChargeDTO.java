package it.nexera.ris.web.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.util.Date;

@Getter
@Setter
@JsonIgnoreProperties
@ToString(callSuper = true)
public class DataChargeDTO {
    private String note;

    @SerializedName("partita_iva_societa")
    private String positionVat;

    @SerializedName("ragione_sociale")
    private String positionName;

    @SerializedName( "presenti")
    private String positionActive ;

    @SerializedName( "stato_cariche")
    private String statusPosition;

    private Boolean socio;

    @SerializedName( "natura_giuridica")
    private String giuridicNature;

    @SerializedName( "data_inizio_attivita_societa")
    private String startDate;

    @SerializedName( "stato_societa")
    private String statusSociety;

    @SerializedName( "indirizzo_societa")
    private String positionLegalAddress;

    @SerializedName("cod_carica")
    private String code;

    @SerializedName("data_nomina")
    private String dateNomination;

    @SerializedName("cessata")
    private Boolean stopped;
}
