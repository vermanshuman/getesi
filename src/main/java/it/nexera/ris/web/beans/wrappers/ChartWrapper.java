package it.nexera.ris.web.beans.wrappers;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
public class ChartWrapper implements Serializable {

    private static final long serialVersionUID = 478871911547616909L;

    private String type;
    private List<String> labels;
    private List<ChartDataWrapper> datasets;
}
