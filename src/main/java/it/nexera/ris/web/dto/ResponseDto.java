package it.nexera.ris.web.dto;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class ResponseDto implements Serializable {
    private String resultCode;
    private String resultDescription;
}
