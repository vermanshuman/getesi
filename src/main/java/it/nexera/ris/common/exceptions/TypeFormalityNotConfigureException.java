package it.nexera.ris.common.exceptions;

import it.nexera.ris.common.enums.TypeActEnum;

public class TypeFormalityNotConfigureException extends Exception {

    private static final long serialVersionUID = 3296332230874803136L;

    private TypeActEnum type;
    private String code;

    public TypeFormalityNotConfigureException() {
    }

    public TypeFormalityNotConfigureException(String message) {
        super(message);
    }

    public TypeFormalityNotConfigureException(String message, TypeActEnum type, String code) {
        super(message);
        this.type = type;
        this.code = code;
    }

    public TypeFormalityNotConfigureException(String message, Throwable cause) {
        super(message, cause);
    }

    public TypeActEnum getType() {
        return type;
    }

    public void setType(TypeActEnum type) {
        this.type = type;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
