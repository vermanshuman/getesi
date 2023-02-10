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
public class DataPensionForeclosureDTO {
    @SerializedName("note")
    private String distraintRetirementNote;

    @SerializedName("in_corso")
    private Boolean distraintRetirementProgress;

    @SerializedName("importo")
    private BigDecimal distraintRetirementAmount;

    @SerializedName("data_termine")
    private String distraintRetirementEnd;
}
