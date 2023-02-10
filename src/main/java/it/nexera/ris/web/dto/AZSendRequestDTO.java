package it.nexera.ris.web.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class AZSendRequestDTO {
    private String service;

    private String fiscalCode;

    private List<Long> customCode;
}
