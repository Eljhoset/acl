package com.example.testAcl.repository;

import com.example.testAcl.model.AclObjectIdentity;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.CrudRepository;

/**
 *
 * @author Daniel
 */
public interface AclObjectIdentityRepository extends CrudRepository<AclObjectIdentity, Long>, QuerydslPredicateExecutor<AclObjectIdentity> {

}
