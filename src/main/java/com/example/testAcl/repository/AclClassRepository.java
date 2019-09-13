package com.example.testAcl.repository;

import com.example.testAcl.model.AclClass;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.CrudRepository;

/**
 *
 * @author Daniel
 */
public interface AclClassRepository extends CrudRepository<AclClass, Long>, QuerydslPredicateExecutor<AclClass> {

}
