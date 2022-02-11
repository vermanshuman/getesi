package it.nexera.ris.common.security.api;

/**
 * SecurityInfo class encapsulates user authentication details
 */
public class SecurityInfo {
    private Long _userID = 0L;

    private String _userName = null;

    private String _password = null;

    public SecurityInfo() {
    }

    public SecurityInfo(Long userID, String userName, String password) {
        this.set_userID(userID);
        this.set_userName(userName);
        this.set_password(password);
    }

    public Long get_userID() {
        return _userID;
    }

    public void set_userID(Long _userID) {
        this._userID = _userID;
    }

    public String get_userName() {
        return _userName;
    }

    public void set_userName(String _userName) {
        this._userName = _userName;
    }

    public String get_password() {
        return _password;
    }

    public void set_password(String _password) {
        this._password = _password;
    }
}
