package com.berrycloud.acl.security;

import java.io.Serializable;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.transaction.annotation.Transactional;

import com.berrycloud.acl.AclLogic;
import com.berrycloud.acl.domain.AclRole;
import com.berrycloud.acl.domain.AclUser;

public class SimpleAclUserDetailsService implements UserDetailsService {
    // TODO extend it to a UserDetailsManager ???
    
    @PersistenceContext
    private EntityManager em;

    private Class<AclUser<Serializable, AclRole<Serializable>>> aclUserType;

    @Autowired
    private void setAclUserType(AclLogic aclLogic) {
	aclUserType = aclLogic.getAclUserType();
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

	CriteriaBuilder cb = em.getCriteriaBuilder();
	CriteriaQuery<AclUser<Serializable, AclRole<Serializable>>> query = cb
		.createQuery(aclUserType);
	Root<AclUser<Serializable, AclRole<Serializable>>> root = query.from(aclUserType);
	query.select(root).where(cb.equal(root.get("username"), username));

	AclUser<Serializable, AclRole<Serializable>> person = em.createQuery(query)
		.getSingleResult();
	if (person == null) {
	    throw (new UsernameNotFoundException("User with username'" + username + "' cannot be found."));
	}

	return new SimpleAclUserDetails(person);
    }

}
