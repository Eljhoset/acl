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
public interface PosRepository extends JpaRepository<Pos, Long>, QuerydslPredicateExecutor<Pos> {

    AclPredicate<Pos> PREDICATE_POS = new AclPredicate<Pos>() {
        @Override
        protected NumberPath<Long> getInnerObjectId() {
            return QPos.pos.id;
        }
    };

    @Override
    @PreAuthorize("hasAnyRole('ROLE_SUPER_ADMIN','ROLE_ADMIN')")
    Page<Pos> findAll(Predicate predicate, Pageable pageable);

    @Override
    @PreAuthorize("hasAnyRole('ROLE_SUPER_ADMIN','ROLE_ADMIN')")
    public default Page<Pos> findAll(Pageable pageable) {
        final QPos qPos = QPos.pos;
        BooleanExpression viewableFor = qPos.bettingshop.consortium.in(ConsortiumRepository.PREDICATE_CONSORTIUM.selectWhereSomeObjectHasAnAclEntry())
                .or(qPos.bettingshop.in(BettingShopRepository.PREDICATE_BETTINGSHOP.selectWhereSomeObjectHasAnAclEntry()))
                .or(qPos.in(PREDICATE_POS.selectWhereSomeObjectHasAnAclEntry()))
                .and(QBettingshop.bettingshop.status.ne(EntityStatus.DELETED));
        return findAll(viewableFor, pageable);
    }

    @Override
    @PostAuthorize("hasRole('ROLE_SUPER_ADMIN') or hasRole('ROLE_ADMIN') and hasPermission(returnObject, 'ADMINISTRATION') or hasPermission(returnObject, 'READ') or hasPermission(returnObject.consortium, 'READ')")
    public default Pos getOne(Long id) {
        final QPos qPos = QPos.pos;
        return findOne(qPos.status.ne(EntityStatus.DELETED).and(qPos.id.eq(id))).orElseThrow(() -> {
            return null;
        });
    }

    @Override
    @PreAuthorize("hasRole('ROLE_SUPER_ADMIN') or  hasRole('ROLE_ADMIN') and "
            + "(#bettingshop.id eq null ? hasPermission(#bettingshop.consortium, 'WRITE') : "
            + "hasPermission(#bettingshop, 'ADMINISTRATION') or hasPermission(#bettingshop, 'WRITE')) or hasPermission(#bettingshop.consortium, 'WRITE')")
    public Pos save(Pos pos);

    @Override
    @PreAuthorize("hasRole('ROLE_SUPER_ADMIN') or ( hasRole('ROLE_ADMIN') and hasPermission(#bettingshop.consortium, 'WRITE')) and"
            + " (hasPermission(#bettingshop, 'DELETE') or hasPermission(#bettingshop, 'ADMINISTRATION'))")
    public default void delete(Pos pos) {
        pos.setStatus(EntityStatus.DELETED);
        save(pos);
    }

    @Override
    public default void deleteById(Long id) {
        delete(getOne(id));
    }

    @PreAuthorize("hasRole('ROLE_SUPER_ADMIN') or  hasRole('ROLE_ADMIN') and hasPermission(#consortium, 'READ')")
    public default Page<Pos> findByBettingshop(Bettingshop bettingshop, Pageable pageable) {
        final QPos qPos = QPos.pos;
        BooleanExpression predicate = qPos.bettingshop.eq(bettingshop).and(qPos.status.ne(EntityStatus.DELETED));
        return findAll(predicate, pageable);
    }
}
