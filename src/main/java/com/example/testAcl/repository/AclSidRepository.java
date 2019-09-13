package com.example.testAcl.repository;

import com.example.testAcl.model.AclSid;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.CrudRepository;

/**
 *
 * @author Daniel
 */
public interface AclSidRepository extends CrudRepository<AclSid, Long>, QuerydslPredicateExecutor<AclSid> {

}
