package com.berrycloud.acl;

import javax.persistence.criteria.CommonAbstractCriteria;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.data.jpa.domain.Specification;

public interface AclSpecification extends Specification<Object> {

    Predicate toPredicate(Root<?> root, CommonAbstractCriteria query, CriteriaBuilder cb, String permission);
}
