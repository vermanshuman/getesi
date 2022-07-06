package it.nexera.ris.persistence.beans.entities.domain;

import it.nexera.ris.persistence.beans.entities.IndexedEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "wlg_server")
public class WLGServer extends IndexedEntity {

    private static final long serialVersionUID = -1030811327181647075L;

    @Column(name = "name", length = 300, nullable = false)
    private String name;

    @Column(name = "host", length = 200)
    private String host;

    @Column(name = "port")
    private Long port;

    @Column(name = "login", length = 200)
    private String login;

    @Column(name = "password", length = 100)
    private String password;

    @Column(name = "keep_messages", columnDefinition = "BOOLEAN", nullable = false)
    private Boolean keepMassages;

    @Column(name = "use_secured_connection", columnDefinition = "BOOLEAN", nullable = false)
    private Boolean useSecuredConnection;

    @Column(name = "timeout")
    private Long timeout;

    @Column(name = "type", nullable = false)
    private Long type;

    @Column(name = "alias")
    private String alias;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public Long getPort() {
        return port;
    }

    public void setPort(Long port) {
        this.port = port;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Boolean getKeepMassages() {
        return keepMassages;
    }

    public void setKeepMassages(Boolean keepMassages) {
        this.keepMassages = keepMassages;
    }

    public Boolean getUseSecuredConnection() {
        return useSecuredConnection;
    }

    public void setUseSecuredConnection(Boolean useSecuredConnection) {
        this.useSecuredConnection = useSecuredConnection;
    }

    public Long getTimeout() {
        return timeout;
    }

    public void setTimeout(Long timeout) {
        this.timeout = timeout;
    }

    public Long getType() {
        return type;
    }

    public void setType(Long type) {
        this.type = type;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

}
