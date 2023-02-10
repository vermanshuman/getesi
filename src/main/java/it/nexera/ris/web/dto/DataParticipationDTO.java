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
public class DataParticipationDTO {
    private String note;

    @SerializedName("partita_iva_societa")
    private String participationVat;

    @SerializedName("ragione_sociale")
    private String participationName;

    @SerializedName( "presenti")
    private String participationActive ;

    @SerializedName( "stato_partecipazioni")
    private String participationStatus;

    @SerializedName( "socio")
    private Boolean participationSocio;
}
