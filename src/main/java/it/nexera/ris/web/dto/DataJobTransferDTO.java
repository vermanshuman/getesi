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
public class DataJobTransferDTO {
    @SerializedName("note")
    private String workTransferNote;

    @SerializedName("in_corso")
    private Boolean workTransferProgress;

    @SerializedName("importo")
    private BigDecimal workTransferAmount;

    @SerializedName("termine")
    private String workTransferEnd;
}
