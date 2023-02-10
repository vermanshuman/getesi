package it.nexera.ris.common.utils;

import it.nexera.ris.common.helpers.ResourcesHelper;

public class Constants {

    public static final String AUTHORIZATION_HEADER_NAME = "Authorization";

    public static final String API_SUCCESS_CODE = "00";
    public static final String API_FAILURE_CODE = "01";
    public static final String API_FAILURE_MISSING_PARAMETER = "02";


    public static final String API_CREDENTIALS_FAILURE = ResourcesHelper.getString("apiWrongLoginInfo");
    public static final String API_CREDENTIALS_WRONG_STATUS = ResourcesHelper.getString("apiWrongLoginStatus");

    public static final String API_GET_DOCUMENT_BY_ID_MISSING = ResourcesHelper.getString("getDocumentByIdNoDocument");
    public static final String API_GET_DOCUMENT_BY_ID_FILE_MISSING = ResourcesHelper.getString("getDocumentByIdNoDocumentFile");
    public static final String API_GET_DOCUMENT_BY_ID_MISSING_DATA = ResourcesHelper.getString("getDocumentByIdData");
    public static final String API_UPDATE_DOCUMENT_BY_ID_MISSING_DOCUMENT = ResourcesHelper.getString("updateDocumentByIdFileMissing");
    public static final String API_UPDATE_DOCUMENT_BY_ID_MISSING_FILE_NAME = ResourcesHelper.getString("updateDocumentByIdFileNameMissing");
    public static final String API_GET_DOCUMENT_BY_ID_SUCCESS = ResourcesHelper.getString("getDocumentByIdSuccess");


    public static final String API_RECIEVE_DATA_MISSING_DATA = ResourcesHelper.getString("recieveDataMissingInput");
    public static final String API_RECIEVE_DATA_SUCCESS = ResourcesHelper.getString("recieveDataSuccess");

}
