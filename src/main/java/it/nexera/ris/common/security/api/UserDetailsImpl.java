package it.nexera.ris.common.security.api;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

/**
 * Class encapsulates user security info, permissions
 */
public class UserDetailsImpl implements UserDetails {
    private static final long serialVersionUID = -1275986526924009634L;

    private String _userName = null;

    private String _password = null;

    private Collection<? extends GrantedAuthority> _grantedAuthority = null;

    public UserDetailsImpl(String userName, String password,
                           Collection<? extends GrantedAuthority> grantedAuthority) {
        this._userName = userName;

        this._password = password;

        this._grantedAuthority = grantedAuthority;
    }

    /* (non-Javadoc)
     * @see org.springframework.security.core.userdetails.UserDetails#getAuthorities()
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return this._grantedAuthority;
    }

    public String getPassword() {
        return this._password;
    }

    public String getUsername() {
        return this._userName;
    }

    public boolean isAccountNonExpired() {
        return true;
    }

    public boolean isAccountNonLocked() {
        return true;
    }

    public boolean isCredentialsNonExpired() {
        return true;
    }

    public boolean isEnabled() {
        return true;
    }

}
