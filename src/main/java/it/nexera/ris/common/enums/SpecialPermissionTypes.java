package it.nexera.ris.common.enums;

import it.nexera.ris.common.helpers.EnumHelper;
import it.nexera.ris.common.helpers.ResourcesHelper;

public enum SpecialPermissionTypes {
    CAN_START_PRACTICE(null, PermissionType.ACTION_AVAILABILITY),
    CAN_CHANGE_MAIL_STATE(null, PermissionType.ACTION_AVAILABILITY),
    CAN_ASSIGN_PRACTICE(null, PermissionType.ACTION_AVAILABILITY),
    CAN_MANAGE_COSTS(null, PermissionType.ACTION_AVAILABILITY),
    CAN_SUSPEND_REQUEST(null, PermissionType.ACTION_AVAILABILITY),
    CAN_CHANGE_REQUEST_USER(null, PermissionType.ACTION_AVAILABILITY),
    CAN_ARCHIVE_MAIL(null, PermissionType.ACTION_AVAILABILITY),
    CAN_SEE_ALL_USERS(null, PermissionType.ACTION_AVAILABILITY),
    CAN_IMPORT_PROPERTY(PageTypes.IMPORT_PROPERTY_SETTINGS, PermissionType.PAGE_ACCESS),
    CAN_IMPORT_ESTATE_FORMALITY(PageTypes.IMPORT_ESTATE_FORMALITY_SETTINGS, PermissionType.PAGE_ACCESS),
    CAN_IMPORT_FORMALITY(PageTypes.IMPORT_FORMALITY_SETTINGS, PermissionType.PAGE_ACCESS),
    CAN_IMPORT_REPORT_FORMALITY_SUBJECT(PageTypes.IMPORT_REPORT_FORMALITY_SUBJECT_SETTINGS, PermissionType.PAGE_ACCESS),
    CAN_IMPORT_VISURE_RTF(PageTypes.IMPORT_VISURE_RTF_SETTINGS, PermissionType.PAGE_ACCESS),
    CAN_IMPORT_VISURE_DH(PageTypes.IMPORT_VISURE_DH_SETTINGS, PermissionType.PAGE_ACCESS),
    CAN_IMPORT_REQUEST_OLD(PageTypes.IMPORT_REQUEST_OLD_SETTINGS, PermissionType.PAGE_ACCESS);

    public enum PermissionType {
        PAGE_ACCESS,
        ACTION_AVAILABILITY;
    }

    private PageTypes pageType;

    private PermissionType permissionType;

    private SpecialPermissionTypes(PageTypes pageType,
                                   PermissionType permissionType) {
        this.pageType = pageType;
        this.permissionType = permissionType;
    }

    @Override
    public String toString() {
        return ResourcesHelper.getEnum(EnumHelper.toStringFormatter(this));
    }

    public PageTypes getPageType() {
        return pageType;
    }

    public void setPageType(PageTypes pageType) {
        this.pageType = pageType;
    }

    public PermissionType getPermissionType() {
        return permissionType;
    }

    public void setPermissionType(PermissionType permissionType) {
        this.permissionType = permissionType;
    }

}
