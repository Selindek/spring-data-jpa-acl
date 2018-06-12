package com.berrycloud.acl;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasProperty;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import com.berrycloud.acl.data.AclMetaData;
import com.berrycloud.acl.domain.SimpleAclRole;
import com.berrycloud.acl.domain.SimpleAclUser;
import com.berrycloud.acl.sample.minimal.AclAppMinimal;
import com.berrycloud.acl.security.AclUserDetailsService;
import com.berrycloud.acl.security.access.AclPermissionEvaluator;

@SpringBootTest(classes = AclAppMinimal.class)
@RunWith(SpringRunner.class)
public class AclMinimalIntegrationTest {

    @Autowired(required = false)
    private AclLogicImpl aclLogic;

    @Autowired(required = false)
    private AclUserDetailsService<?> aclUserDetailsService;

    @Autowired(required = false)
    private AclUtils aclUtils;

    @Autowired(required = false)
    private AclPermissionEvaluator aclPermissionEvaluator;

    @Autowired(required = false)
    private AclMetaData aclMetaData;

    @Autowired(required = false)
    private AclSpecification aclSpecification;

    @Test
    public void testDefaultBeans() {
        assertNotNull(aclLogic);
        assertNotNull(aclUserDetailsService);
        assertNotNull(aclMetaData);
        assertNotNull(aclPermissionEvaluator);
        assertNotNull(aclSpecification);
        assertNotNull(aclUtils);
    }

    @SuppressWarnings("unchecked")
    @Test
    @Transactional
    public void testDefaultSettings() {
        assertTrue(aclLogic.isManagedType(SimpleAclUser.class));
        assertTrue(aclLogic.isManagedType(SimpleAclRole.class));

        SimpleAclUser admin = (SimpleAclUser) aclLogic.loadUserByUsername("admin");
        assertNotNull(admin);
        // assertEquals(aclLogic.getUserId(admin), 1);

        assertThat(admin.getAclRoles(), containsInAnyOrder(hasProperty("roleName", is("ROLE_ADMIN")),
                hasProperty("roleName", is("ROLE_USER"))));

        SimpleAclUser user = (SimpleAclUser) aclLogic.loadUserByUsername("user");
        assertNotNull(user);
        // assertEquals(aclLogic.getUserId(user), 2);

        assertThat(user.getAclRoles(), contains(hasProperty("roleName", is("ROLE_USER"))));
    }
}
