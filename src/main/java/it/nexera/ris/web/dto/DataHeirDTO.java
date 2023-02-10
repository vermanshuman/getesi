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
public class DataHeirDTO {
    private String note;

    @SerializedName("date")
    private String deceasedDate;

    @SerializedName("codice_fiscale")
    private String codiceFiscale;

    @SerializedName("nome")
    private String name;

    @SerializedName("cognome")
    private String surname;

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

    @SerializedName("accettazione_eredita")
    private Boolean acceptInheritance;

    @SerializedName("grado_parentela")
    private String kin;

}
