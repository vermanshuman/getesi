package it.nexera.ris.common.enums;

public enum ApplicationInstance {
	GETESI(1),
	BREXA(2);
	
	private Integer id;
	
	private ApplicationInstance(Integer id) {
        this.id = id;
    }
	
	public Integer getId() {
        return id;
    }
}