package it.nexera.ris.web.beans.wrappers;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

@Getter
@Setter
@Builder
public class MixChartDataWrapper implements Serializable {

    private static final long serialVersionUID = -6277707881880747431L;

    private String type;
    private String label;
    private LinkedList<Integer> data;
    private List<String> backgroundColor;
    private List<String> borderColor;
    private int borderWidth;
    private LinkedList<List<String>> tooltip;
}
