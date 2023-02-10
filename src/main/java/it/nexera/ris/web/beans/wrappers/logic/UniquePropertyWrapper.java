package it.nexera.ris.web.beans.wrappers.logic;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UniquePropertyWrapper implements Serializable {

	private static final long serialVersionUID = 4602991605087009687L;
	
	private Long estateSituationId;
	
	private Long propertyId;
	
	private Long cadastralDataId;
	
	private Long cityId;
	
	private String section;
	
	private String sheet;
	
	private String particle;
	
	private String sub;
	
	

}
