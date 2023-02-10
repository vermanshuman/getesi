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
@ToString(callSuper=true)
public class DataPropertyDTO {
    private String note;

    private Boolean pra;

    @SerializedName("atto_cautelare")
    private String precautionaryAct;

    @SerializedName("pignoramenti")
    private String foreclosures;

    @SerializedName("importo_preg")
    private BigDecimal importPrejudicial;

    @SerializedName("preg")
    private String prejudicial;

    @SerializedName("valore_quota")
    private String quoteValue;

    @SerializedName("valore_libero")
    private String freeValue;

    @SerializedName("valore_ipoteche")
    private BigDecimal valueMortgage;

    @SerializedName("valore_quota_proprieta")
    private String valueRelationship;

    @SerializedName("gravame_volontario")
    private String intentionalBurden;

    @SerializedName("gravame_giudiziale")
    private String judicialBurden;

    @SerializedName("valore_immobiliare")
    private String propertyValue;

    @SerializedName("numero_immobili_liberi")
    private Integer numberFreeProperty;

    @SerializedName("conservatoria")
    private String landRegistry;

    @SerializedName("numero_terreni")
    private Integer numberLand;

    @SerializedName("numero_immobili")
    private Integer numberProperty;

    @SerializedName("immobile_di_proprieta")
    private String propertyOwned;

    @SerializedName("catasto")
    private String cadastral;

    @SerializedName("titolarita")
    private String ownership;

    @SerializedName("indirizzo_toponimo")
    private String toponymAddress;

    @SerializedName("indirizzo_via")
    private String streetAddress;

    @SerializedName("indirizzo_civico")
    private String civicAddress;

    @SerializedName("indirizzo_presso")
    private String addressAt;

    @SerializedName("indirizzo_comune")
    private String cityAdress;

    @SerializedName("indirizzo_cap")
    private String postalAddress;

    @SerializedName("indirizzo_provincia")
    private String addressProvince;

    @SerializedName("indirizzo_stato")
    private String addressStatus;

    @SerializedName("foglio")
    private String sheet;

    @SerializedName("particella")
    private String particle;

    @SerializedName("classamento")
    private String classification;

    @SerializedName("consistenza")
    private String consistency;

    @SerializedName("rendita")
    private String revenue;

    @SerializedName("partita")
    private String match;

    @SerializedName("id_catasto")
    private String idCadastral;

    @SerializedName("altre")
    private String other;

    @SerializedName("data_ispezione")
    private String inspectionDate;

    @SerializedName("classe")
    private String propertyClass;

    @SerializedName("repertorio")
    private String repertoire;

    @SerializedName("pubblico_ufficiale")
    private String publicOfficial;

    private String sub;

    @SerializedName("note_da_conservatoria")
    private String notesLandRegistry;
}
