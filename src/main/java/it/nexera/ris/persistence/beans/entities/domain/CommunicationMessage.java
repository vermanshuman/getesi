package it.nexera.ris.persistence.beans.entities.domain;

import it.nexera.ris.persistence.beans.entities.IndexedEntity;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "communication_message")
public class CommunicationMessage extends IndexedEntity {

	private static final long serialVersionUID = 8163472280590976315L;

	@Column(name = "start_date")
    private Date startDate;

    @Column(name = "end_date")
    private Date endDate;

    @Column(name = "message")
    private String message;
    
    @OneToMany(mappedBy = "communicationMessage",fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    private List<CommunicationMessageExport> attachedFiles;

    @ManyToMany
    @JoinTable(name = "communication_message_roles", joinColumns =
            {
                    @JoinColumn(name = "communication_message_id", table = "communication_message")
            }, inverseJoinColumns =
            {
                    @JoinColumn(name = "role_id", table = "acl_role")
            })
    private List<Role> assosiatedRoles;
    
    @ManyToMany
    @JoinTable(name = "communication_message_users", joinColumns =
            {
                    @JoinColumn(name = "communication_message_id", table = "communication_message")
            }, inverseJoinColumns =
            {
                    @JoinColumn(name = "user_id", table = "acl_user")
            })
    private List<User> assosiatedUsers;
}
