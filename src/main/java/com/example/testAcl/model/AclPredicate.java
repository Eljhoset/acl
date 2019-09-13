package com.example.testAcl.model;

import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.types.PathMetadataFactory;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.EntityPathBase;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.JPQLQuery;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.persistence.Id;
import org.springframework.core.GenericTypeResolver;
import org.springframework.security.core.context.SecurityContextHolder;

public class AclPredicate<T> {

    private static final String SUPER_ADMIN_ROLE = "ROLE_SUPER_ADMIN";
//    private final String FIND_OBJECTS_WITH_ACCESS = ""
//            + "SELECT "
//            + "    obj.object_id_identity AS obj_id, "
//            + "    class.class AS class "
//            + "FROM "
//            + "    acl_object_identity obj, "
//            + "    acl_class class, "
//            + "    acl_entry entry "
//            + "WHERE "
//            + "    obj.object_id_class = class.id "
//            + "    and entry.granting = true "
//            + "    and entry.acl_object_identity = obj.id "
//            + "    and entry.sid = (SELECT id FROM acl_sid WHERE sid = ?) "
//            + "    and obj.object_id_class = (SELECT id FROM acl_class WHERE acl_class.class = ?) "
//            + "GROUP BY "
//            + "    obj.object_id_identity, "
//            + "    class.class ";

//    public List<ObjectIdentity> getObjectsWithAccess(Class clazz, String sid) {
//        Object[] args = {sid, clazz.getName()};
//        List<ObjectIdentity> objects = _jdbcTemplate.query(FIND_OBJECTS_WITH_ACCESS, args, getRowMapper());
//        return objects.size() == 0 ? null : objects;
//    }
//
//    private RowMapper<ObjectIdentity> getRowMapper() {
//        return (rs, rowNum) -> {
//            String javaType = rs.getString("class");
//            Long identifier = rs.getLong("obj_id");
//            return new ObjectIdentityImpl(javaType, identifier);
//        };
//    }
    protected Class<T> getType() {
        return (Class<T>) GenericTypeResolver.resolveTypeArguments(getClass(), AclPredicate.class)[0];
    }

    protected NumberPath<Long> getInnerObjectId() {
        String idField = findIdField(getType());
        PathMetadata forProperty = PathMetadataFactory.forProperty(getInnerObject(), idField);
        return Expressions.numberPath(Long.class, forProperty);
    }

    protected EntityPathBase<T> getInnerObject() {
        return new PathBuilder<>(getType(), getType().getSimpleName());
    }

    private String findIdField(Class cls) {
        for (Field field : cls.getDeclaredFields()) {
            String name = field.getName();
            Annotation[] annotations = field.getDeclaredAnnotations();
            for (Annotation annotation : annotations) {
                if (annotation.annotationType().equals(Id.class)) {
                    return name;
                }
            }
        }
        return null;
    }

    private String getCurrentUser() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    private Set<String> getRoles() {
        return SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                .map(r -> r.getAuthority()).collect(Collectors.toSet());
    }

    private List<String> getSids() {
        final List<String> sids = new ArrayList<>(getRoles());
        sids.add(getCurrentUser());
        return sids;
    }

    private JPQLQuery<AclSid> selectAclSidSubQuery(List<String> sids) {
        QAclSid aclSid = QAclSid.aclSid;
        return JPAExpressions.selectFrom(aclSid).where(aclSid.sid.in(sids))
                .select(aclSid);
    }

    private JPQLQuery<AclClass> selectAclClassSubQuery() {

        QAclClass aclClass = QAclClass.aclClass;
        return JPAExpressions.selectFrom(aclClass).where(
                aclClass.className.eq(getType().getName()));
    }

    private JPQLQuery<AclObjectIdentity> selectAclObjectIdentity(
            NumberPath<Long> id) {
        QAclObjectIdentity objectIdentity = QAclObjectIdentity.aclObjectIdentity;

        return JPAExpressions
                .selectFrom(objectIdentity)
                .where(objectIdentity.objectIdIdentity.eq(id).and(
                        objectIdentity.objectIdClass
                                .eq(selectAclClassSubQuery()))).select(objectIdentity);
    }

    private JPQLQuery<Long> selectCountOfAclEntry(NumberPath<Long> id,
            List<String> sids) {
        QAclEntry aclEntry = QAclEntry.aclEntry;

        return JPAExpressions
                .selectFrom(aclEntry)
                .where(aclEntry.aclObjectIdentity.eq(
                        selectAclObjectIdentity(id)).and(
                        aclEntry.granting.eq(true).and(
                                aclEntry.sid.in(selectAclSidSubQuery(sids))))).select(aclEntry.id.count());
    }

    private JPQLQuery<T> selectWhereSomeObjectHasAnAclEntry(
            NumberPath<Long> id, List<String> sids) {

        return JPAExpressions.selectFrom(getInnerObject())
                .where(Expressions.ONE.eq(sids.contains(SUPER_ADMIN_ROLE) ? Expressions.ONE : Expressions.ZERO).or(selectCountOfAclEntry(id, sids).gt(0L)));
    }

    private JPQLQuery<Long> selectWhereSomeObjectHasAnAclEntryId(
            NumberPath<Long> id, List<String> sids) {

        return selectWhereSomeObjectHasAnAclEntry(id, sids)
                .select(getInnerObjectId());
    }

    private JPQLQuery<T> selectWhereSomeObjectHasAnAclEntryObject(List<String> sids) {
        return selectWhereSomeObjectHasAnAclEntry(getInnerObjectId(), sids)
                .select(getInnerObject());
    }

    private JPQLQuery<T> selectWhereSomeObjectHasAnAclEntry(List<String> sids) {

        JPQLQuery<T> qLQuery = JPAExpressions
                .selectFrom(getInnerObject())
                .where(getInnerObject().in(selectWhereSomeObjectHasAnAclEntryObject(sids)
                        .select(getInnerObject())));
        return qLQuery;
    }

    public JPQLQuery<T> selectWhereSomeObjectHasAnAclEntry() {
        return selectWhereSomeObjectHasAnAclEntry(getSids());
    }

    /**
     * Returns a predicate defining a query from {@code SomeObject} which
     * performs the necessary check to ensure that the user has privileges to
     * view the someObject in at least some form or another.This predicate is
     * used for non global roles to determine if someObject should be displayed
     * on the dashboard. Runs a sub-query to determine all the id's that the
     * user has permission to view and then does a simple check to figure out
     * whether the current id is present in the set of viewable ids.
     *
     * @return - the predicate
     * @see AuthUtil#getAllSids()
     */
    public BooleanExpression viewable() {
        /**
         * Equivalent to the following JPQL: select someObject from SomeObject
         * someObject where someObject.someProperty = 'true' or someObject.id in
         * (select innerSomeObject.id from SomeObject innerSomeObject where
         * (select count(aclEntry) from AclEntry aclEntry where
         * aclEntry.aclObjectIdentity = (select aclObjectIdentity from
         * AclObjectIdentity aclObjectIdentity where
         * aclObjectIdentity.objectIdIdentity = someObject.id and
         * aclObjectIdentity.objectIdClass = (select aclClass from AclClass
         * aclClass where aclClass.objectClass = 'com.acme.SomeObject')) and
         * aclEntry.granting = 'true' and aclEntry.sid in (select aclSid from
         * AclSid aclSid where aclSid.sid in (:sids))) > 0)
         */
        return getInnerObjectId().in(
                selectWhereSomeObjectHasAnAclEntryId(getInnerObjectId(), getSids()));
    }

}
