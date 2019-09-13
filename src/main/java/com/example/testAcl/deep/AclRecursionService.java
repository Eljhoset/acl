package com.example.testAcl.deep;

import com.example.testAcl.model.QAclObjectIdentity;
import com.querydsl.core.FetchableQuery;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.sql.JPASQLQuery;
import com.querydsl.sql.SQLExpressions;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author Daniel
 */
@Service
public class AclRecursionService {

    @PersistenceContext
    private EntityManager em;

    @Autowired
    private SQLTemplatesService sqlTemplatesService;

    public void findAll() {

        final long id = 1l;
        QAclObjectIdentity objectIdentity = QAclObjectIdentity.aclObjectIdentity;
        QAclObjectIdentity p = QAclObjectIdentity.aclObjectIdentity;
        JPASQLQuery query = new JPASQLQuery(em, sqlTemplatesService.getTemplates());

        FetchableQuery select = query.withRecursive(objectIdentity, SQLExpressions
                .unionAll(
                        JPAExpressions.selectFrom(objectIdentity).where(objectIdentity.id.eq(id)).select(objectIdentity.id, objectIdentity.parentObject.id),
                        JPAExpressions.selectFrom(p).innerJoin(p).on(p.parentObject.id.eq(p.id)).select(p.id, p.parentObject.id)))
                .from(objectIdentity)
                .select(objectIdentity.id, objectIdentity.parentObject.id);

        List fetch = select.fetch();

    }
}
