package it.nexera.ris.web.beans.wrappers;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WorkLoadWrapper {

    private String icon;

    private String style;

    private String name;

    private int percentage;

    private long numberUnclosedRequestsInWork;
}
