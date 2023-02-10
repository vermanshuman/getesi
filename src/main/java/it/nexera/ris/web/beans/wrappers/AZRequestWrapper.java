package it.nexera.ris.web.beans.wrappers;

import com.sun.org.apache.xpath.internal.operations.Bool;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.Service;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class AZRequestWrapper {

    private Integer id;

    private List<Service> azServices;

    private String subject;

    private Service service;

    private Boolean multiple;

    private int span;

    private Long requestId;

    private Integer index;

    private Boolean differentSupplier;

    private Boolean selected;

    private Long serviceId;
}
