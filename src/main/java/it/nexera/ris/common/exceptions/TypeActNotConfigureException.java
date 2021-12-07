package it.nexera.ris.common.exceptions;

public class TypeActNotConfigureException extends Exception {

    private static final long serialVersionUID = 1161953637449223157L;

    private String actCode;
    private String noteType;
    private String description;

    public TypeActNotConfigureException() {
    }

    public TypeActNotConfigureException(String message) {
        super(message);
    }

    public TypeActNotConfigureException(String message, Throwable cause) {
        super(message, cause);
    }

    public String getActCode() {
        return actCode;
    }

    public void setActCode(String actCode) {
        this.actCode = actCode;
    }

    public String getNoteType() {
        return noteType;
    }

    public void setNoteType(String noteType) {
        this.noteType = noteType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
