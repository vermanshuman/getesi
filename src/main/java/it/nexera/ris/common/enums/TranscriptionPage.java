package it.nexera.ris.common.enums;

public enum TranscriptionPage {
	TRANSCRIPTION("transcription"),
	CERTIFICATION("certification"),
	TRANSCRIPTIONCERTIFICATION("transcriptionCertification"),
	RENEWAL("renewal"),;
	
	private String page;       

    private TranscriptionPage(String s) {
        page = s;
    }

	public String getPage() {
		return page;
	}

    
}
