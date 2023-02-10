package it.nexera.ris.web.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GetDocumentByIdResponseDTO extends ResponseDto{
    private String document;
    private String contentType;
    private String fileName;
}
