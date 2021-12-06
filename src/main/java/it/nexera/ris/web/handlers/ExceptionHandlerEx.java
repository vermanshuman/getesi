package it.nexera.ris.web.handlers;

import it.nexera.ris.common.enums.PageTypes;
import it.nexera.ris.common.helpers.LogHelper;
import it.nexera.ris.common.helpers.RedirectHelper;
import it.nexera.ris.web.beans.wrappers.ExceptionWrapper;

import javax.faces.FacesException;
import javax.faces.context.ExceptionHandler;
import javax.faces.context.ExceptionHandlerWrapper;
import javax.faces.context.FacesContext;
import javax.faces.event.ExceptionQueuedEvent;
import javax.faces.event.ExceptionQueuedEventContext;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ExceptionHandlerEx extends ExceptionHandlerWrapper {

    private ExceptionHandler exceptionHandler;

    public ExceptionHandlerEx(ExceptionHandler exceptionHandler) {
        this.exceptionHandler = exceptionHandler;
    }

    @Override
    public ExceptionHandler getWrapped() {
        return this.exceptionHandler;
    }

    @Override
    public void handle() throws FacesException {
        for (Iterator<ExceptionQueuedEvent> i = getUnhandledExceptionQueuedEvents()
                .iterator(); i.hasNext(); ) {
            ExceptionQueuedEvent exceptionQueuedEvent = i.next();

            ExceptionQueuedEventContext exceptionQueuedEventContext = (ExceptionQueuedEventContext) exceptionQueuedEvent
                    .getSource();

            Throwable throwable = exceptionQueuedEventContext.getException();
            PageTypes redirectPage = null;

            try {
                if (throwable instanceof Throwable) {
                    Throwable t = (Throwable) throwable;

                    FacesContext facesContext = FacesContext
                            .getCurrentInstance();

                    List<ExceptionWrapper> list = new ArrayList<ExceptionWrapper>();

                    while (t.getCause() != t) {
                        list.add(new ExceptionWrapper(t.getMessage(), LogHelper
                                .readStackTrace(t)));
                        if (t.getCause() == null) {
                            break;
                        }
                        t = t.getCause();
                    }

                    facesContext.getExternalContext().getSessionMap()
                            .put("exceptionList", list);

                    redirectPage = PageTypes.ERROR;
                }
            } finally {
                i.remove();
            }

            RedirectHelper.goTo(redirectPage);
            break;
        }
        getWrapped().handle();
    }

}
