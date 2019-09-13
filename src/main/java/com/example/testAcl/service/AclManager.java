package com.example.testAcl.service;

import java.util.List;
import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.acls.domain.ObjectIdentityImpl;
import org.springframework.security.acls.domain.PrincipalSid;
import org.springframework.security.acls.jdbc.JdbcMutableAclService;
import org.springframework.security.acls.model.Acl;
import org.springframework.security.acls.model.AlreadyExistsException;
import org.springframework.security.acls.model.MutableAcl;
import org.springframework.security.acls.model.NotFoundException;
import org.springframework.security.acls.model.ObjectIdentity;
import org.springframework.security.acls.model.Permission;
import org.springframework.security.acls.model.Sid;
import org.springframework.stereotype.Service;

/**
 *
 * @author Daniel
 */
@Service
public class AclManager {

    @Autowired
    private JdbcMutableAclService aclService;

    //aclManager.set(username, Consortium.class, 2l, List.of(BasePermission.ADMINISTRATION));
    @Transactional
    public Acl set(final String principal,
            final Class domainClass,
            final long id,
            List<Permission> permissions) throws AlreadyExistsException, NotFoundException {

        ObjectIdentity oi = new ObjectIdentityImpl(domainClass, id);
        Sid sid = new PrincipalSid(principal);
        // Create or update the relevant ACL
        MutableAcl acl;
        try {
            acl = (MutableAcl) aclService.readAclById(oi);
        } catch (NotFoundException nfe) {
            acl = aclService.createAcl(oi);
        }

        // Now grant some permissions via an access control entry (ACE)
        for (Permission permission : permissions) {

            acl.insertAce(acl.getEntries().size(), permission, sid, true);
//            acl.getEntries().add(new SimpleAccessControlEntryImpl(acl, sid, permission, true));
        }

        aclService.updateAcl(acl);
        return acl;
    }

}
