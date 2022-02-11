package it.nexera.ris.web.services;

import it.nexera.ris.common.enums.SessionNames;
import it.nexera.ris.web.services.base.BaseDBService;

import java.io.Serializable;

public class ShowMemoryService extends BaseDBService implements Serializable {

    private static final long serialVersionUID = -8162648523887224720L;

    public ShowMemoryService() {
        super(SessionNames.ShowMemoryService);
    }

    @Override
    protected void routineFuncInternal() {
        log.info(Runtime.getRuntime().freeMemory() + "/" + Runtime.getRuntime().maxMemory() + " bytes memory available");
    }

    @Override
    protected int getPollTimeKey() {
        return 30   ;
    }
}
