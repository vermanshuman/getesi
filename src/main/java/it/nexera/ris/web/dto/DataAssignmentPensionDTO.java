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
public class DataAssignmentPensionDTO {
    @SerializedName("note")
    private String retirementTransferNote;;

    @SerializedName("in_corso")
    private Boolean retirementTransferProgress;

    @SerializedName("importo")
    private BigDecimal retirementTransferAmount;

    @SerializedName("termine")
    private String retirementTransferEnd;
}
