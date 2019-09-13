package com.example.testAcl;

import com.example.testAcl.model.AclPredicate;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
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
public interface BettingShopRepository extends JpaRepository<Bettingshop, Long>,
        QuerydslPredicateExecutor<Bettingshop> {

    AclPredicate<Bettingshop> PREDICATE_BETTINGSHOP = new AclPredicate<Bettingshop>() {
        @Override
        protected NumberPath<Long> getInnerObjectId() {
            return QBettingshop.bettingshop.id;
        }
    };

    @Override
    @PreAuthorize("hasAnyRole('ROLE_SUPER_ADMIN','ROLE_ADMIN')")
    Page<Bettingshop> findAll(Predicate predicate, Pageable pageable);

    @Override
    @PreAuthorize("hasAnyRole('ROLE_SUPER_ADMIN','ROLE_ADMIN')")
    public default Page<Bettingshop> findAll(Pageable pageable) {
        BooleanExpression viewableFor = QBettingshop.bettingshop.consortium.in(ConsortiumRepository.PREDICATE_CONSORTIUM.selectWhereSomeObjectHasAnAclEntry())
                .or(QBettingshop.bettingshop.in(PREDICATE_BETTINGSHOP.selectWhereSomeObjectHasAnAclEntry())).and(QBettingshop.bettingshop.status.ne(EntityStatus.DELETED));
        return findAll(viewableFor, pageable);
    }

    @Override
    @PostAuthorize("hasRole('ROLE_SUPER_ADMIN') or hasRole('ROLE_ADMIN') and hasPermission(returnObject, 'ADMINISTRATION') or hasPermission(returnObject, 'READ') or hasPermission(returnObject.consortium, 'READ')")
    public default Bettingshop getOne(Long id) {
        final QBettingshop qBettingshop = QBettingshop.bettingshop;
        return findOne(qBettingshop.status.ne(EntityStatus.DELETED).and(qBettingshop.id.eq(id))).orElseThrow(() -> {
            return null;
        });
    }

    @Override
    @PreAuthorize("hasRole('ROLE_SUPER_ADMIN') or  hasRole('ROLE_ADMIN') and "
            + "(#bettingshop.id eq null ? hasPermission(#bettingshop.consortium, 'WRITE') : "
            + "hasPermission(#bettingshop, 'ADMINISTRATION') or hasPermission(#bettingshop, 'WRITE')) or hasPermission(#bettingshop.consortium, 'WRITE')")
    public Bettingshop save(Bettingshop bettingshop);

    @Override
    @PreAuthorize("hasRole('ROLE_SUPER_ADMIN') or ( hasRole('ROLE_ADMIN') and hasPermission(#bettingshop.consortium, 'WRITE')) and"
            + " (hasPermission(#bettingshop, 'DELETE') or hasPermission(#bettingshop, 'ADMINISTRATION'))")
    public default void delete(Bettingshop bettingshop) {
        bettingshop.setStatus(EntityStatus.DELETED);
        save(bettingshop);
    }

    @Override
    public default void deleteById(Long id) {
        delete(getOne(id));
    }

    @PreAuthorize("hasRole('ROLE_SUPER_ADMIN') or  hasRole('ROLE_ADMIN') and hasPermission(#consortium, 'READ')")
    public default Page<Bettingshop> findByConsortium(Consortium consortium, Pageable pageable) {
        final QBettingshop qBettingshop = QBettingshop.bettingshop;
        BooleanExpression predicate = qBettingshop.consortium.eq(consortium).and(qBettingshop.status.ne(EntityStatus.DELETED));
        return findAll(predicate, pageable);
    }

}
