package it.nexera.ris.web.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GetInboxByIdResponseDTO extends ResponseDto {
    private String mailContent;

    private String fileName;
}
