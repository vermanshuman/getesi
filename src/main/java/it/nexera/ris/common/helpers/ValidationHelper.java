package it.nexera.ris.common.helpers;

import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.persistence.beans.dao.ConnectionManager;
import it.nexera.ris.persistence.beans.dao.CriteriaAlias;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.Entity;
import it.nexera.ris.persistence.beans.entities.IndexedEntity;
import it.nexera.ris.persistence.beans.entities.domain.EstateFormality;
import it.nexera.ris.persistence.beans.entities.domain.Request;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;
import org.hibernate.criterion.SimpleExpression;
import org.hibernate.sql.JoinType;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Validation helper class Used for server-side validation
 */
public class ValidationHelper {

    public transient final static Log log = LogFactory
            .getLog(ValidationHelper.class);

    public static String EMPTY = "";

    public static boolean isNullOrEmptyMultiple(Object... args) {
        for (Object obj : args) {
            if (obj == null) {
                return true;
            } else {
                if (obj instanceof String) {
                    String str = (String) obj;
                    if (str.trim().isEmpty()) {
                        return true;
                    }
                }
                if (obj instanceof List<?>) {
                    List<?> list = (List<?>) obj;
                    if (list.isEmpty()) {
                        return true;
                    }
                }
                if (obj instanceof IndexedEntity) {
                    IndexedEntity indexedEntity = (IndexedEntity) obj;
                    if (isNullOrEmpty(indexedEntity.getId())) {
                        return true;
                    }
                }
                if (obj instanceof Long) {
                    Long lStr = (Long) obj;
                    if (lStr.equals(0L)) {
                        return true;
                    }
                }
                if (obj instanceof Integer) {
                    Integer integer = (Integer) obj;
                    if (integer.equals(0)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static boolean isNullOrEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }

    public static boolean isNullOrEmpty(Object str) {
        return str == null;
    }

    public static boolean isNullOrEmpty(Boolean str) {
        return str == null;
    }

    public static boolean isNullOrEmpty(Date str) {
        return str == null;
    }

    public static boolean isNullOrEmpty(List<?> list) {
        return list == null || list.isEmpty();
    }

    public static boolean isNullOrEmpty(Set<?> set) {
        return set == null || set.isEmpty();
    }

    public static boolean isNullOrEmpty(Map<?, ?> map) {
        return map == null || map.isEmpty();
    }

    public static boolean isNullOrEmptyEditor(String str) {
        if (str.equals("<br>")) {
            str = null;
        }

        return str == null || str.trim().isEmpty();
    }

    public static boolean isNullOrEmpty(Long str) {
        return str == null || str.equals(0L);
    }

    public static boolean isNullOrEmpty(Integer str) {
        return str == null;
    }

    public static boolean isNullOrEmpty(Double str) {
        return str == null;
    }

    public static boolean isNullOrEmpty(IndexedEntity obj) {
        return obj == null || ValidationHelper.isNullOrEmpty(obj.getId());
    }

    public static boolean isNullOrEmpty(Object[] arr) {
        return arr == null || arr.length == 0;
    }

    public static boolean isAlphanumeric(String value) {
        for (Character c : value.toCharArray()) {
            if (!Character.isLetterOrDigit(c)) {
                return false;
            }
        }
        return true;
    }

    public static <T> boolean checkCriterions(Class<T> clazz,
                                              Criterion criterions[]) {
        try {
            return (DaoManager.load(clazz, criterions).size() != 0);
        } catch (HibernateException | IllegalAccessException | PersistenceBeanException ignored) {

        }

        return false;
    }

    public static <T extends Entity> boolean isUnique(Class<T> clazz,
                                                      String fieldname, String fieldvalue, Long id) {
        return isUnique(clazz, fieldname, fieldvalue, null, id);
    }

    public static <T extends Entity> boolean isUnique(Class<T> clazz,
                                                      String fieldname, String fieldvalue, SimpleExpression ex, Long id) {
        return isUnique(clazz, fieldname, fieldvalue, ex, null, id);
    }

    public static <T extends Entity> boolean isUnique(Class<T> clazz,
                                                      String fieldname, String fieldvalue, SimpleExpression ex,
                                                      CriteriaAlias[] aliases, Long id) {
        try {
            return (DaoManager.getCount(
                    clazz,
                    fieldname,
                    aliases,
                    new Criterion[]
                            {Restrictions.eq(fieldname, fieldvalue).ignoreCase(),
                                    Restrictions.ne("id", id == null ? 0 : id)}) == 0);
        } catch (Exception e) {
            LogHelper.log(log, e);
        }

        return false;
    }

    public static <T extends Entity> boolean isUnique(Class<T> clazz,
                                                      String fieldname, String fieldvalue, Criterion[] criterions,
                                                      CriteriaAlias[] aliases, Long id) {
        int size = 0;
        if (criterions != null) {
            size = criterions.length;
        }

        Criterion[] restrictions = new Criterion[size + 2];

        restrictions[0] = Restrictions.eq(fieldname, fieldvalue).ignoreCase();
        restrictions[1] = Restrictions.ne("id", id == null ? 0 : id);

        if (criterions != null) {
            System.arraycopy(criterions, 0, restrictions, 2, criterions.length);
        }

        try {
            return (DaoManager.load(clazz, aliases, restrictions).size() == 0);
        } catch (HibernateException | PersistenceBeanException | IllegalAccessException e) {
            LogHelper.log(log, e);
        }

        return false;
    }

    public static boolean areStringsEqualWithNullOrEmpty(String first, String second) {
        first = isNullOrEmpty(first) ? "" : first;
        second = isNullOrEmpty(second) ? "" : second;
        return first.equals(second);
    }

    public static boolean checkMailCorrectFormat(String value) {
        return checkCorrectFormatByExpression(
                "^([a-zA-Z0-9_\\-\\.]+)@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.)|(([a-zA-Z0-9\\-]+\\.)+))([a-zA-Z]{2,4}\\s{0,10}|[0-9]{1,3}\\s{0,10})(\\]?)$",
                value);
    }

    public static boolean checkCorrectFormatByExpression(String expression,
                                                         String value) {
        return Pattern.matches(expression, value);
    }

    public static boolean validPassword(String value) {
        if (value != null && (value.length() != 0) && (value.length() < 8)) {
            return false;
        }

        boolean digit = false;
        boolean upperCase = false;
        boolean lowerCase = false;

        if (value != null) {
            for (char ch : value.toCharArray()) {
                if (Character.isUpperCase(ch)) {
                    upperCase = true;
                }
                if (Character.isLowerCase(ch)) {
                    lowerCase = true;
                }
                if (Character.isDigit(ch)) {
                    digit = true;
                }
            }
        }
        return digit && lowerCase && upperCase;
    }

    public static boolean checkUserNameFormat(String value) {
        return value == null || (value.length() >= 3 && value.length() <= 20);
    }

    public static boolean checkFieldLengthFrom(String value, int from) {
        return value != null && value.length() >= from;

    }

    public static boolean checkFieldLengthTo(String value, int to) {
        return value != null && value.length() <= to;

    }

    public static boolean checkFieldLength(String value, int from, int to) {
        return checkFieldLengthFrom(value, from) && checkFieldLengthTo(value, to);

    }

    public static boolean isNumber(String value) {
        if (value == null || value.isEmpty()) {
            return true;
        }

        char[] valueMas = value.toCharArray();
        for (Character c : valueMas) {
            if (!Character.isDigit(c)) {
                return false;
            }
        }
        return true;
    }

    public static boolean checkTimeFormat(String value) {
        String[] strs = value.split(":");
        if (Integer.parseInt(strs[0]) > 23) {
            return false;
        }

        return Integer.parseInt(strs[1]) <= 59;
    }

    public static boolean checkURLFormat(String value) {
        try {
            @SuppressWarnings("unused")
            URL url = new URL(value);
            return true;
        } catch (MalformedURLException e) {
            return false;
        }
    }

    public static boolean isNull(Long str) {
        return str == null;
    }

    public static boolean isEstateFormalityExists(EstateFormality estateFormality, Request request, Session session)
            throws PersistenceBeanException, IllegalAccessException {
        return ConnectionManager.getCount(EstateFormality.class, "id", new CriteriaAlias[]{
                new CriteriaAlias("requestFormalities", "request", JoinType.INNER_JOIN)
        }, new Criterion[]{
                Restrictions.eq("landChargesRegistry", estateFormality.getLandChargesRegistry()),
                Restrictions.eq("date", estateFormality.getDate()),
                Restrictions.eq("numRG", estateFormality.getNumRG()),
                Restrictions.eq("numRP", estateFormality.getNumRP()),
                Restrictions.eq("request.request.id", request == null ? 0L : request.getId()),
                Restrictions.ne("id", estateFormality.isNew() ? 0L : estateFormality.getId())
        }, session) != 0;
    }

    public static String validateNumberVat(String str) {
        if (str != null) {
            int length = str.length();
            if (length < 11) {
                StringBuilder prefixZero = new StringBuilder();
                int addZeroNumber = 11 - length;
                for (int i = 0; i < addZeroNumber; i++) {
                    prefixZero.append("0");
                }
                return prefixZero.append(str).toString();
            }
        }
        return str;
    }

//    public static void main(String[] args) {
//        System.out.println(ValidationHelper.checkCorrectFormatByExpression(PERSON_PATTERN_ALT, "CITTER EDOARDO 25/07/1960; Comune DARFO BOARIO TERME (BS)"));
//    }
//
//    private static final String PERSON_PATTERN_ALT = "(([A-Z]{2,}\\s?){1,3})\\s(([a-zA-Z]{3,}\\s?){1,3})\\s(\\d*\\/\\d*\\/\\d*);\\sComune\\s(([A-Z]{2,}\\s?){1,3})(\\([A-Z]{2,}\\))";
}
