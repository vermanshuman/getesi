package it.nexera.ris.common.exceptions;

public class CannotProcessException extends Exception {

    private static final long serialVersionUID = -2828008490048880491L;

    public CannotProcessException() {
    }

    public CannotProcessException(String message) {
        super(message);
    }

    public CannotProcessException(String message, Throwable cause) {
        super(message, cause);
    }
}
