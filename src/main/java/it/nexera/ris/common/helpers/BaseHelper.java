package it.nexera.ris.common.helpers;

import it.nexera.ris.persistence.beans.entities.IndexedEntity;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class BaseHelper {
    protected static transient final Log log = LogFactory
            .getLog(BaseHelper.class);

    public static <T extends IndexedEntity> List<Long> getIds(List<T> list) {
        List<Long> ids = new ArrayList<>();

        if (!ValidationHelper.isNullOrEmpty(list)) {
            for (T item : list) {
                ids.add(item.getId());
            }
        }
        return ids;
    }

    public static <T> T getOptionOrNull(Optional<T> optional) {
        return optional.isPresent() ? optional.get() : null;
    }
}
