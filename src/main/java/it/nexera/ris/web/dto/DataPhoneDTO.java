package it.nexera.ris.web.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.Column;
import java.math.BigDecimal;

@Getter
@Setter
@JsonIgnoreProperties
@ToString(callSuper=true)
public class DataPhoneDTO {
    private String note;

    @SerializedName("numero")
    private String number;
}
