package com.example.testAcl;

import com.example.testAcl.model.AclPredicate;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.EntityPathBase;
import com.querydsl.core.types.dsl.NumberPath;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;

/**
 *
 * @author Daniel
 */
public interface ConsortiumRepository extends JpaRepository<Consortium, Long>,
        QuerydslPredicateExecutor<Consortium> {

    AclPredicate<Consortium> PREDICATE_CONSORTIUM = new AclPredicate<Consortium>() {
        @Override
        protected NumberPath<Long> getInnerObjectId() {
            return QConsortium.consortium.id;
        }

        @Override
        protected EntityPathBase<Consortium> getInnerObject() {
            return QConsortium.consortium;
        }

    };

    @Override
    @PreAuthorize("hasAnyRole('ROLE_SUPER_ADMIN','ROLE_ADMIN')")
    Page<Consortium> findAll(Predicate predicate, Pageable pageable);

    @Override
    @PreAuthorize("hasAnyRole('ROLE_SUPER_ADMIN','ROLE_ADMIN')")
    public default Page<Consortium> findAll(Pageable pageable) {
        BooleanExpression viewableFor = QConsortium.consortium.in(ConsortiumRepository.PREDICATE_CONSORTIUM.selectWhereSomeObjectHasAnAclEntry())
                .and(QConsortium.consortium.status.ne(EntityStatus.DELETED));
        return findAll(viewableFor, pageable);
    }

    @Override
    @PostAuthorize("hasRole('ROLE_SUPER_ADMIN') or (hasRole('ROLE_ADMIN') and hasPermission(returnObject, 'READ'))")
    public default Consortium getOne(Long id) {
        final QConsortium qConsortium = QConsortium.consortium;
        return findOne(qConsortium.status.ne(EntityStatus.DELETED).and(qConsortium.id.eq(id))).orElseThrow(() -> {
            return null;
        });
    }

    @Override
    @PreAuthorize("hasRole('ROLE_SUPER_ADMIN') or  hasRole('ROLE_ADMIN') and "
            + "(#consortium.id eq null ? hasRole('ROLE_SUPER_ADMIN') : "
            + "hasPermission(#consortium, 'ADMINISTRATION') or hasPermission(#consortium, 'WRITE'))")
    public Consortium save(Consortium consortium);

    @Override
    @PreAuthorize("hasRole('ROLE_SUPER_ADMIN')")
    public default void delete(Consortium consortium) {
        consortium.setStatus(EntityStatus.DELETED);
        save(consortium);
    }

    @Override
    public default void deleteById(Long id) {
        delete(getOne(id));
    }

}
