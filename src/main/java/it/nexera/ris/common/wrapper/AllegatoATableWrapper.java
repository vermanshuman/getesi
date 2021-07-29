package it.nexera.ris.common.wrapper;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class AllegatoATableWrapper {
    private String cityName;
    private String proviceName;
    private Double totalMq;
    private Double totalOmi;
    List<AllegatoAWrapper> tableData;
    List<AllegatoAWrapper> nonDisponTableData;
}
