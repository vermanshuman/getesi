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
public class DataWorkForeclosureDTO {
    @SerializedName("note")
    private String distraintWorkNote;

    @SerializedName("in_corso")
    private Boolean distraintWorkProgress;

    @SerializedName("importo")
    private BigDecimal distraintWorkAmount;

    @SerializedName("data_termine")
    private String distraintWorkEnd;
}
