package it.nexera.ris.web.dto;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GetDocumentByIdRequestDTO {
    @SerializedName("document_id")
    private Long documentId;

    private String document;

    @SerializedName("file_name")
    private String fileName;

    private String type;
}
