package com.example.testAcl.model;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import lombok.Data;
import org.hibernate.annotations.Immutable;

@Data
@Entity
@Table(name = "acl_entry")
@Immutable
public class AclEntry implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ace_order", nullable = false, unique = false)
    private Integer aceOrder;

    @Column(name = "mask", nullable = false, unique = false)
    private Integer mask;

    @Column(name = "granting", nullable = false, unique = false)
    private Boolean granting;

    @Column(name = "audit_success", nullable = false, unique = false)
    private Boolean auditSuccess;

    @Column(name = "audit_failure", nullable = false, unique = false)
    private Boolean auditFailure;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "acl_object_identity", referencedColumnName = "id", nullable = false, unique = false, insertable = true, updatable = true)
    private AclObjectIdentity aclObjectIdentity;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sid", referencedColumnName = "id", nullable = false, unique = false, insertable = true, updatable = true)
    private AclSid sid;

}
