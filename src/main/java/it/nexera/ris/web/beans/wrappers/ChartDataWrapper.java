package it.nexera.ris.web.beans.wrappers;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
@Builder
public class ChartDataWrapper implements Serializable {

    private static final long serialVersionUID = 478871921547616909L;

    private String label;
    private List<Long> data;
    private String borderColor;
    private int borderWidth;
    private List<String> borderDash;
    private boolean fill;
    private int pointRadius;
}
