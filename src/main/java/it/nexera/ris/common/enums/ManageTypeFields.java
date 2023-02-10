package it.nexera.ris.common.enums;

import it.nexera.ris.common.helpers.EnumHelper;
import it.nexera.ris.common.helpers.ResourcesHelper;
import it.nexera.ris.web.beans.wrappers.EnumPropsWrapper;

import java.util.Arrays;

public enum ManageTypeFields {

    PROPERTY_DATA(new EnumPropsWrapper(2l, true)),
    SUBJECT_MASTERY(new EnumPropsWrapper(3l, true)),
    CDR(new EnumPropsWrapper(4l)),
    NDG(new EnumPropsWrapper(5l)),
    MANAGER(new EnumPropsWrapper(6l)),
    POSITION_PRACTICE(new EnumPropsWrapper(7l)),
    FORMALITIES_AUTHORIZED(new EnumPropsWrapper(8l)),
    LEGAL(new EnumPropsWrapper(9l)),
    URGENT(new EnumPropsWrapper(10l)),
    NOTE(new EnumPropsWrapper(11l)),
    ATTACHED_DOCUMENTS(new EnumPropsWrapper(12l)),
    UPDATE_DATE(new EnumPropsWrapper(13l)),
    PROVINCE(new EnumPropsWrapper(14l, true)),
    ACT_TYPE(new EnumPropsWrapper(15l)),
    ACT_NUMBER(new EnumPropsWrapper(16l)),
    ACT_DATE(new EnumPropsWrapper(17l)),
    TERM_DATE(new EnumPropsWrapper(18l)),
    REA_NUMBER(new EnumPropsWrapper(19l)),
    NATURE_LEGAL(new EnumPropsWrapper(20l)),
    ISTAT(new EnumPropsWrapper(21l)),
    RESIDENCE(new EnumPropsWrapper(22l, true)),
    DOMICILE(new EnumPropsWrapper(23l, true)),
    CONSERVATORY(new EnumPropsWrapper(24l)),
    TALOVARE(new EnumPropsWrapper(25l)),
    ULTIMA_RESIDENZA(new EnumPropsWrapper(26l)),
    NOTARY(new EnumPropsWrapper(27l)),
    ACT_ATTACHMENT(new EnumPropsWrapper(28l)),
    SUBJECT_LIST(new EnumPropsWrapper(29l)),
    CONSERVATORY_TALOVARE(new EnumPropsWrapper(30l)),
    SPECIAL_FORMALITY(new EnumPropsWrapper(31l)),
    IDENTIFICATION_NUMBER_F24(new EnumPropsWrapper(32l)),
    LAST_NOTIFICATION_DATE(new EnumPropsWrapper(33l)),
    CONSERVANCY_VERIFICATION(new EnumPropsWrapper(34l)),
    REFERENCE_YEAR(new EnumPropsWrapper(35l)),
    MORTAGAGE_IMPORT(new EnumPropsWrapper(36l));

    private EnumPropsWrapper enumPropsWrapper;

    @Override
    public String toString() {
        return ResourcesHelper.getEnum(EnumHelper.toStringFormatter(this));
    }

    public String getPath() {
        return "/Pages/ManagementGroup/requestComponents/" + this.name() + ".xhtml";
    }

    private ManageTypeFields(EnumPropsWrapper enumPropsWrapper) {
        this.enumPropsWrapper = enumPropsWrapper;
    }

    public static ManageTypeFields getFieldById(Long id) {
        return Arrays.stream(values()).filter(f -> f.getEnumPropsWrapper().getId().equals(id))
                .findFirst().orElse(null);
    }

    public EnumPropsWrapper getEnumPropsWrapper() {
        return enumPropsWrapper;
    }

    public void setEnumPropsWrapper(EnumPropsWrapper enumPropsWrapper) {
        this.enumPropsWrapper = enumPropsWrapper;
    }

}

