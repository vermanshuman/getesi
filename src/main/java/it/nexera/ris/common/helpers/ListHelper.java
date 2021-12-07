package it.nexera.ris.common.helpers;

import it.nexera.ris.persistence.beans.entities.IEntity;

import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ListHelper {
    public static <T extends IEntity> boolean contains(List<T> list, T obj) {
        return list.stream().anyMatch(item -> item.getId().equals(obj.getId()));
    }

    public static <T extends IEntity> boolean contains(List<T> list, Long id) {
        return list.stream().anyMatch(item -> item.getId().equals(id));
    }

    public static <T extends IEntity> T get(List<T> list, Long id) {
        return list.stream().filter(item -> item.getId().equals(id)).findFirst().orElse(null);
    }

    public static <T extends IEntity> int getIndex(List<T> list, Long id) {
        T item = get(list, id);
        if (item == null) {
            return -1;
        } else {
            return list.indexOf(item);
        }
    }

    public static <T extends IEntity> void remove(List<T> list, Long id) {
        T item = get(list, id);
        if (item != null) {
            list.remove(item);
        }
    }

    public static <T extends IEntity> Long getMaxId(List<T> list) {
        T item = list.stream().max(Comparator.comparing(IEntity::getId)).orElse(null);
        return item != null ? item.getId() : 1L;
    }

    public static <T extends IEntity> boolean add(List<T> list, T obj) {
        if (!contains(list, obj)) {
            list.add(obj);
            return true;
        }
        return false;
    }

    public static <T extends IEntity> boolean add(List<T> listDest, List<T> listSrc) {
        listDest.addAll(listSrc);
        return listDest.size() > listSrc.size();
    }

    public static <T> String toString(List<T> list) {
        StringBuilder stringBuilder = new StringBuilder();
        if (!ValidationHelper.isNullOrEmpty(list)) {
            Iterator<T> iterator = list.iterator();
            stringBuilder.append(iterator.next().toString());
            while (iterator.hasNext()) {
                stringBuilder.append("\r\n");
                stringBuilder.append(iterator.next().toString());
            }
        }
        return stringBuilder.toString();
    }

    public static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
        Set<Object> seen = ConcurrentHashMap.newKeySet();
        return t -> seen.add(keyExtractor.apply(t));
    }
    
    public static String ignoreCaseReplace(String source, String target, String replacement) {
        return Pattern.compile(target, Pattern.LITERAL | Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE).matcher(source)
                  .replaceAll(Matcher.quoteReplacement(replacement));
    }
}
