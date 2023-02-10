package it.nexera.ris.web.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@JsonIgnoreProperties
@ToString(callSuper=true)
public class AZReceiveDataResponseDTO {
    private List<AZSendRequestResponseDataDTO> data;
}
