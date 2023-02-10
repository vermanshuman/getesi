package it.nexera.ris.web.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@JsonIgnoreProperties
@ToString(callSuper = true)
public class DataBankAccountDTO {
    private String note;

    @SerializedName("presente")
    private String checked;

    @SerializedName("ragione_sociale_banca")
    private String name;

    @SerializedName("partita_iva_banca")
    private String vat;

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

    private String abi;

    private String cab;

    @SerializedName("attivo")
    private String active;

    @SerializedName("protesti")
    private String protests;
}
