package it.nexera.ris.web.dto;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class GetInboxByIdRequestDTO {
    @SerializedName("wlg_inbox_id")
    private Long wlgInboxId;

    public GetInboxByIdRequestDTO(Long wlgInboxId) {
        this.wlgInboxId = wlgInboxId;
    }
}