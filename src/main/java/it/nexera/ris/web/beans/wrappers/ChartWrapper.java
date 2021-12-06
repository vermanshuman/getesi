package it.nexera.ris.web.beans.wrappers;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class ChartWrapper implements Serializable {

    private static final long serialVersionUID = 478871911547616909L;

    private String type;
    private String xLabel;
    private String yLabel;
    private List<String> labels;
    private List<MixChartDataWrapper> datasets;
    private Map<String,String> toolTipData;
}
