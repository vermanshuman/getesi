package it.nexera.ris.common.helpers;

import java.util.List;

import lombok.Data;

@Data
public class BarGraph {
	
	private List<String> tooltip;
	private String chartXAxisData;
	private Integer data;

}
