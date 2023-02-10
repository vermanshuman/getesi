package it.nexera.ris.api;

import it.nexera.ris.api.model.AZAPIManager;
import it.nexera.ris.web.dto.AZSendRequestDTO;
import it.nexera.ris.web.dto.AZSendRequestResponseDTO;
import it.nexera.ris.web.dto.GetInboxByIdRequestDTO;
import it.nexera.ris.web.dto.GetInboxByIdResponseDTO;

public class ApiFacade {
    public static final ApiFacade SINGLETON= new ApiFacade();

    public AZSendRequestResponseDTO sendAZRequest(AZSendRequestDTO requestDTO){
        return AZAPIManager.sendAZRequest(requestDTO);
    }

    public AZSendRequestResponseDTO getAZData(String ticketId){
        return AZAPIManager.getAZData(ticketId);
    }
}
