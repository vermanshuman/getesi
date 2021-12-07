package it.nexera.ris.common.exceptions;

public class ServerNotFoundExceprion extends Exception {

    private static final long serialVersionUID = 1128887815532692307L;

    public ServerNotFoundExceprion() {
    }

    public ServerNotFoundExceprion(String message) {
        super(message);
    }

    public ServerNotFoundExceprion(String message, Throwable cause) {
        super(message, cause);
    }
}
