package com.example.testAcl.repository;

import com.example.testAcl.model.AclEntry;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.CrudRepository;

/**
 *
 * @author Daniel
 */
public interface AclEntryRepository extends CrudRepository<AclEntry, Long>, QuerydslPredicateExecutor<AclEntry> {

}
