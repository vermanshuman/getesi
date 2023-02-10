package it.nexera.ris.web.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@JsonIgnoreProperties
@ToString(callSuper=true)
public class AZSendRequestResponseDTO {
    private Boolean success;
    private AZSendRequestResponseDataDTO data;
}
