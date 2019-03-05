package com.berrycloud.acl;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.support.AclDeletePermissionException;
import org.springframework.data.jpa.repository.support.AclReadPermissionException;
import org.springframework.data.jpa.repository.support.AclUpdatePermissionException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import com.berrycloud.acl.domain.SimpleAclRole;
import com.berrycloud.acl.domain.SimpleAclUser;
import com.berrycloud.acl.sample.all.AclAppAll;
import com.berrycloud.acl.sample.all.entity.Attachment;
import com.berrycloud.acl.sample.all.entity.Document;
import com.berrycloud.acl.sample.all.entity.Person;
import com.berrycloud.acl.sample.all.entity.PersonHasPersonPermission;
import com.berrycloud.acl.sample.all.entity.Project;
import com.berrycloud.acl.sample.all.entity.TestGroup;
import com.berrycloud.acl.sample.all.entity.Theme;
import com.berrycloud.acl.sample.all.repository.AttachmentRepository;
import com.berrycloud.acl.sample.all.repository.DocumentRepository;
import com.berrycloud.acl.sample.all.repository.GroupRepository;
import com.berrycloud.acl.sample.all.repository.PersonHasPersonRepository;
import com.berrycloud.acl.sample.all.repository.PersonRepository;
import com.berrycloud.acl.sample.all.repository.ProjectRepository;
import com.berrycloud.acl.sample.all.repository.RoleRepository;
import com.berrycloud.acl.sample.all.repository.ThemeRepository;
import com.berrycloud.acl.sample.all.service.PersonService;
import com.berrycloud.acl.security.AclUserDetails;
import com.berrycloud.acl.security.AclUserDetailsService;

@SpringBootTest(classes = AclAppAll.class)
@RunWith(SpringRunner.class)
@Transactional
public class AclAllRepositoryIntegrationTest {

  @Autowired
  private EntityManager em;

  @Autowired
  private AclLogic aclLogic;

  @Autowired
  private AclUserDetailsService<?> aclUserDetailsService;

  @Autowired
  private PersonRepository personRepository;

  @Autowired
  PersonHasPersonRepository personHasPersonRepository;

  @Autowired
  private DocumentRepository documentRepository;

  @Autowired
  private GroupRepository groupRepository;

  @Autowired
  private ProjectRepository projectRepository;

  @Autowired
  private RoleRepository roleRepository;

  @Autowired
  private AttachmentRepository attachmentRepository;

  @Autowired
  private ThemeRepository themeRepository;

  @Autowired
  private PersonService personService;

  private SimpleAclRole adminRole;
  private SimpleAclRole editorRole;
  private SimpleAclRole manipulatorRole;

  private Person admin, user, user2, user3;

  @Before
  public void initTests() {
    adminRole = new SimpleAclRole(AclConstants.ROLE_ADMIN);
    SimpleAclRole userRole = new SimpleAclRole(AclConstants.ROLE_USER);
    editorRole = new SimpleAclRole("ROLE_EDITOR");
    manipulatorRole = new SimpleAclRole("ROLE_MANIPULATOR");
    roleRepository.saveWithoutPermissionCheck(adminRole);
    roleRepository.saveWithoutPermissionCheck(userRole);
    roleRepository.saveWithoutPermissionCheck(editorRole);
    roleRepository.saveWithoutPermissionCheck(manipulatorRole);

    admin = new Person("admin", "a", "a");
    admin.getAclRoles().add(adminRole);
    personRepository.saveWithoutPermissionCheck(admin);

    user = new Person("user", "u", "u");
    user.getAclRoles().add(userRole);
    personRepository.saveWithoutPermissionCheck(user);

    user2 = new Person("user2", "u2", "u2");
    user2.getAclRoles().add(userRole);
    user2.setCreatedBy(user);
    personRepository.saveWithoutPermissionCheck(user2);

    user3 = new Person("user3", "u3", "u3");
    user3.getAclRoles().add(userRole);
    personRepository.saveWithoutPermissionCheck(user3);

  }

  public void setAuthentication(String username) {
    if (username == null) {
      SecurityContextHolder.getContext().setAuthentication(null);
      return;
    }
    AclUserDetails userDetails = aclUserDetailsService.loadUserByUsername(username);

    Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, "",
        userDetails.getAuthorities());
    SecurityContextHolder.getContext().setAuthentication(authentication);
  }

  @Test
  public void testGivenContextWhenStartAppThenEntitiesAreManaged() {
    assertFalse(aclLogic.isManagedType(SimpleAclUser.class));
    assertTrue(aclLogic.isManagedType(SimpleAclRole.class));
    assertTrue(aclLogic.isManagedType(Person.class));
    assertTrue(aclLogic.isManagedType(Document.class));
    assertTrue(aclLogic.isManagedType(TestGroup.class));
  }

  @Test
  public void testGivenNullAuthenticationWhenCallIsAdminTheReturnFalse() {
    setAuthentication(null);
    assertFalse(AclUserDetailsService.isAdmin());
  }

  @Test
  public void testGivenUserAuthenticationWhenCallIsAdminTheReturnFalse() {
    setAuthentication("user");
    assertFalse(AclUserDetailsService.isAdmin());
  }

  @Test
  public void testGivenAdminAuthenticationWhenCallIsAdminTheReturnTrue() {
    setAuthentication("admin");
    assertTrue(AclUserDetailsService.isAdmin());
  }

  @Test
  public void testGivenNullAuthenticationWhenCallGetUsernameTheReturnNull() {
    setAuthentication(null);
    assertNull(AclUserDetailsService.getUsername());
  }

  @Test
  public void testGivenAuthenticationWhenCallGetUsernameTheReturnProperUsername() {
    setAuthentication("user");
    assertThat(AclUserDetailsService.getUsername(), is("user"));
  }

  @Test
  public void testGivenNullValueWhenCallIsManagedTypeThenReturnFalse() {
    assertFalse(aclLogic.isManagedType(null));
  }

  @Test
  public void testGivenNullAuthenticationWhenCallExistOnSimpleAclRoleThenReturnTrue() {
    setAuthentication(null);
    assertTrue(roleRepository.existsById(adminRole.getId()));
  }

  @Test(expected = AclUpdatePermissionException.class)
  public void testGivenNullAuthenticationWhenCallUpdateOnSimpleAclRoleThenThrowException() {
    setAuthentication(null);
    roleRepository.save(adminRole);
  }

  @Test(expected = AclUpdatePermissionException.class)
  public void testGivenUserRoleWhenCallUpdateOnSimpleAclRoleThenThrowException() {
    setAuthentication("user");
    roleRepository.save(adminRole);
  }

  @Test
  public void testGivenAdminRoleWhenCallUpdateOnSimpleAclRoleThenNoException() {
    setAuthentication("admin");
    roleRepository.save(adminRole);
  }

  @Test
  public void testGivenNoAuthenticationWhenCallCountThenReturnZero() {
    setAuthentication(null);
    assertThat(personRepository.count(), is(0L));
  }

  @Test
  public void testGivenAdminAuthenticationWhenCallCountThenReturnNonZero() {
    setAuthentication("admin");
    assertThat(personRepository.count(), greaterThan(0L));
  }

  @Test(expected = AclDeletePermissionException.class)
  public void testGivenNoAuthenticationWhenCallDeleteThenThrowException() {
    setAuthentication(null);
    personRepository.delete(admin);
  }

  @Test
  public void testGivenAdminAuthenticationWhenCallDeleteUserThenUserIsDeleted() {
    setAuthentication("admin");
    Person deleteUser = new Person("delme", "d", "d");
    personRepository.save(deleteUser);
    personRepository.delete(deleteUser);
    assertFalse(personRepository.existsById(deleteUser.getId()));
  }

  @Test(expected = AclDeletePermissionException.class)
  public void testGivenNoAuthenticationWhenCallDeleteUserThenThrowException() {
    setAuthentication(null);
    personRepository.deleteById(admin.getId());
  }

  @Test
  public void testGivenAdminAuthenticationWhenCallDeleteUserByIdThenUserIsDeleted() {
    setAuthentication("admin");
    Person deleteUser = new Person("delme", "d", "d");
    personRepository.save(deleteUser);
    personRepository.deleteById(deleteUser.getId());
    assertFalse(personRepository.existsById(deleteUser.getId()));
  }

  @Test
  public void testGivenAdminAuthenticationWhenCallDeleteDetachedUserThenUserIsDeleted() {
    setAuthentication("admin");
    Person deleteUser = new Person("delme", "d", "d");
    personRepository.save(deleteUser);
    em.flush();
    em.detach(deleteUser);
    personRepository.deleteById(deleteUser.getId());
    assertFalse(personRepository.existsById(deleteUser.getId()));
  }

  @Test
  public void testGivenAdminAuthenticationWhenCallDeleteEmptyCollectionThenNothingIsDeleted() {
    setAuthentication("admin");
    long count = personRepository.count();
    personRepository.deleteInBatch(new HashSet<>());
    assertThat(personRepository.count(), is(count));
  }

  @Test
  public void testGivenUserAuthenticationWhenCallDeleteMultipleUsersThenOnlyProperUsersAreDeleted() {
    setAuthentication("user");
    Person deleteUser = new Person("delme", "d", "d");
    deleteUser.setCreatedBy(user);
    personRepository.saveWithoutPermissionCheck(deleteUser);
    personRepository.deleteInBatch(Arrays.asList(admin, deleteUser));
    personRepository.flush();
    personRepository.clear();
    assertFalse(personRepository.findByIdWithoutPermissionCheck(deleteUser.getId()).isPresent());
    assertTrue(personRepository.findByIdWithoutPermissionCheck(admin.getId()).isPresent());
  }

  @Test
  public void testGivenUserAuthenticationWhenCallDeleteAllThenOnlyPermittedUsersAreDeleted() {
    setAuthentication("user");
    Person deleteUser = new Person("delme", "d", "d");
    deleteUser.setCreatedBy(user);
    personRepository.saveWithoutPermissionCheck(deleteUser);
    personRepository.deleteAll();
    personRepository.flush();
    personRepository.clear();
    assertFalse(personRepository.findByIdWithoutPermissionCheck(deleteUser.getId()).isPresent());
    assertTrue(personRepository.findByIdWithoutPermissionCheck(admin.getId()).isPresent());

  }

  @Test
  public void testGivenUserAuthenticationWhenCallDeleteAllInBatchThenOnlyPermittedUsersAreDeleted() {
    setAuthentication("user");
    Person deleteUser = new Person("delme", "d", "d");
    deleteUser.setCreatedBy(user);
    personRepository.saveWithoutPermissionCheck(deleteUser);
    personRepository.deleteAllInBatch();
    personRepository.flush();
    personRepository.clear();
    assertFalse(personRepository.findByIdWithoutPermissionCheck(deleteUser.getId()).isPresent());
    assertTrue(personRepository.findByIdWithoutPermissionCheck(admin.getId()).isPresent());
  }

  @Test
  public void testGivenUserAuthenticationWhenCallfindByIdPermittedUserThenReturnUser() {
    setAuthentication("user");
    assertTrue(personRepository.findById(user.getId()).isPresent());
  }

  @Test
  public void testGivenUserAuthenticationWhenCallfindByIdNotPermittedUserThenReturnNull() {
    setAuthentication("user");
    assertFalse(personRepository.findById(admin.getId()).isPresent());
  }

  @Test
  public void testGivenUserAuthenticationWhenCallfindByIdWithAllowedPermissionThenReturnUser() {
    setAuthentication("user");
    assertTrue(personRepository.findById(user.getId(), AclConstants.READ_PERMISSION).isPresent());
  }

  @Test
  public void testGivenUserAuthenticationWhenCallfindByIdWithNotAllowedPermissionThenReturnNull() {
    setAuthentication("user");
    assertFalse(personRepository.findById(user.getId(), AclConstants.DELETE_PERMISSION).isPresent());
  }

  @Test
  public void testGivenUserAuthenticationWhenCallGetOnePermittedUserThenReturnUser() {
    setAuthentication("user");
    assertTrue(personRepository.findById(user.getId()).isPresent());
  }

  @Test(expected = AclReadPermissionException.class)
  public void testGivenUserAuthenticationWhenCallGetOneNotPermittedUserThenThrowException() {
    setAuthentication("user");
    personRepository.getOne(admin.getId());
  }

  @Test(expected = AclReadPermissionException.class)
  public void testGivenUserAuthenticationWhenCallGetOneWithNotAllowedPermissionThenThrowException() {
    setAuthentication("user");
    personRepository.getOne(user.getId(), AclConstants.DELETE_PERMISSION);
  }

  @Test
  public void testGivenUserAuthenticationWhenCallGetOnePermittedUserWithAllowedPermissionThenReturnUser() {
    setAuthentication("user");
    personRepository.getOne(user.getId(), AclConstants.READ_PERMISSION);
  }

  @Test
  public void testGivenUserAuthenticationWhenCallExistWithNotPermittedUserIdThenReturnFalse() {
    setAuthentication("user");
    assertFalse(personRepository.existsById(admin.getId()));
  }

  @Test
  public void testGivenUserAuthenticationWhenCallFindAllTheReturnNumberOfPermittedUsers() {
    setAuthentication("user");
    assertThat(personRepository.findAll().size(), is(2));
  }

  @Test
  public void testGivenUserAuthenticationWhenCallFindAllNoAclTheReturnNumberOfAllUsers() {
    setAuthentication("user");
    assertThat(personRepository.findNoAclAllByIdNotNull().size(), is(4));
  }

  @Test
  public void testGivenAdminAuthenticationWhenCallFindAllTheReturnNumberOfAllUsers() {
    setAuthentication("admin");
    assertThat(personRepository.findAll().size(), is(4));
  }

  @Test
  public void testGivenAdminAuthenticationWhenCallfindByIdWithSpecificationTheReturnObject() {
    Specification<Person> spec = new Specification<Person>() {

      private static final long serialVersionUID = 1L;

      @Override
      public Predicate toPredicate(Root<Person> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
        return cb.equal(root.get("username"), "admin");
      }
    };
    setAuthentication("admin");
    assertTrue(personRepository.findOne(spec).isPresent());
  }

  @Test
  public void testGivenUserAuthenticationWhenCallfindByIdWithSpecificationWithoutPermissionTheReturnNull() {
    Specification<Person> spec = new Specification<Person>() {

      private static final long serialVersionUID = 1L;

      @Override
      public Predicate toPredicate(Root<Person> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
        return cb.equal(root.get("username"), "admin");
      }
    };
    setAuthentication("user");
    assertFalse(personRepository.findOne(spec).isPresent());
  }

  @Test
  public void testGivenAdmiAuthenticationWhenCallUpdateThenObjectIsUpdated() {
    setAuthentication("admin");
    admin.setFirstName("changed");
    personRepository.save(admin);
    assertThat(personRepository.getOne(admin.getId()).getFirstName(), is("changed"));
  }

  @Test(expected = AclUpdatePermissionException.class)
  public void testGivenUserAuthenticationWhenCallUpdateWithoutPermissionThenThrowException() {
    setAuthentication("user");
    admin.setFirstName("changed");
    personRepository.save(admin);
  }

  @Test(expected = AclUpdatePermissionException.class)
  public void testGivenNoPermissionWhenCallUpdateWithModifiedPermissionPropertyThenThrowException() {
    setAuthentication("user");
    user3.setCreatedBy(user);
    personRepository.save(user3);
  }

  @Test
  public void testGivenUserAuthenticationWhenCallfindAllSortedThenReturnPermittedObjectsInProperOrder() {
    setAuthentication("user");
    List<Person> list = personRepository.findAll(Sort.by(new Sort.Order(Sort.Direction.ASC, "username")));
    assertThat(list.size(), is(2));
    assertThat(list.get(0).getId(), is(user.getId()));
    assertThat(list.get(1).getId(), is(user2.getId()));
  }

  @Test
  public void testGivenAdminAuthenticationWhenCallfindAllSortedThenReturnAllObjectsInProperOrder() {
    setAuthentication("admin");
    List<Person> list = personRepository.findAll(Sort.by(new Sort.Order(Sort.Direction.DESC, "username")));
    assertThat(list.size(), is(4));
    assertThat(list.get(0).getId(), is(user3.getId()));
    assertThat(list.get(1).getId(), is(user2.getId()));
    assertThat(list.get(2).getId(), is(user.getId()));
    assertThat(list.get(3).getId(), is(admin.getId()));
  }

  @Test
  public void testGivenAdminAuthenticationWhenCallfindByIdByExampleThenReturnObject() {
    setAuthentication("admin");
    assertTrue(personRepository.findOne(Example.of(admin)).isPresent());
  }

  @Test
  public void testGivenUserAuthenticationWhenCallfindByIdByExampleWithoutPermissionThenReturnNull() {
    setAuthentication("user");
    assertFalse(personRepository.findOne(Example.of(admin)).isPresent());
  }

  @Test
  public void testGivenUserAuthenticationWhenCallAuthorizedMethodByIdWithPermissionThenMethodIsCalled() {
    setAuthentication("user");
    assertTrue(personService.loadPerson(user.getId(), AclConstants.READ_PERMISSION));
  }

  @Test(expected = AccessDeniedException.class)
  public void testGivenUserAuthenticationWhenCallAuthorizedMethodByIdWithoutPermissionThenThrowException() {
    setAuthentication("user");
    assertTrue(personService.loadPerson(user.getId(), AclConstants.UPDATE_PERMISSION));
  }

  @Test(expected = AccessDeniedException.class)
  public void testGivenUserAuthenticationWhenCallAuthorizedMethodWithInvalidTypeThenThrowException() {
    setAuthentication("user");
    assertTrue(personService.loadPersonInvalidType(user.getId(), AclConstants.READ_PERMISSION));
  }

  @Test
  public void testGivenUserAuthenticationWhenCallAuthorizedMethodByObjectWithPermissionThenMethodIsCalled() {
    setAuthentication("user");
    assertTrue(personService.loadPerson(user, AclConstants.READ_PERMISSION));
  }

  @Test(expected = AccessDeniedException.class)
  public void testGivenUserAuthenticationWhenCallAuthorizedMethodByObjectWithoutPermissionThenThrowException() {
    setAuthentication("user");
    assertTrue(personService.loadPerson(user, AclConstants.UPDATE_PERMISSION));
  }

  @Test(expected = AccessDeniedException.class)
  public void testGivenUserAuthenticationWhenCallAuthorizedMethodByObjectWithInvalidObjectThenThrowException() {
    setAuthentication("user");
    assertTrue(personService.loadInvalidObject("user", AclConstants.READ_PERMISSION));
  }

  @Test(expected = AccessDeniedException.class)
  public void testGivenUserAuthenticationWhenCallAuthorizedMethodByObjectWithNullThenThrowException() {
    setAuthentication("user");
    assertTrue(personService.loadInvalidObject(null, AclConstants.READ_PERMISSION));
  }

  @Test
  public void testGivenUserAuthenticationWhenCallHasAuthorityThenReturnProperValues() {
    setAuthentication("user");
    assertTrue(AclUserDetailsService.hasAuthority(AclConstants.ROLE_USER));
    assertFalse(AclUserDetailsService.hasAuthority(AclConstants.ROLE_ADMIN));
  }

  @Test
  public void testGivenGroupRoleWhenCallHasAuthorityThenReturnTrueForMembers() {
    TestGroup editorGroup = new TestGroup("Editor's Group", admin);
    editorGroup.getMembers().add(user);
    editorGroup.setRole(editorRole);
    groupRepository.saveWithoutPermissionCheck(editorGroup);
    user.getGroups().add(editorGroup);
    personRepository.saveWithoutPermissionCheck(user);
    setAuthentication("user");
    assertTrue(AclUserDetailsService.hasAuthority("ROLE_EDITOR"));
  }

  @Test
  public void testGivenGroupRoleWhenCallHasAuthorityThenReturnFalseForCreator() {
    TestGroup editorGroup = new TestGroup("Editor's Group", admin);
    editorGroup.getMembers().add(user);
    editorGroup.setRole(editorRole);
    groupRepository.saveWithoutPermissionCheck(editorGroup);
    user.getGroups().add(editorGroup);
    personRepository.saveWithoutPermissionCheck(user);
    setAuthentication("admin");
    assertFalse(AclUserDetailsService.hasAuthority("ROLE_EDITOR"));
  }

  @Test
  public void testGivenGroupMembersWhenCallRepositoryMethodsThenReturnObjectAccordingToOwnerPermission() {
    setAuthentication("user");
    Person member = new Person("member", "m", "m");
    personRepository.saveWithoutPermissionCheck(member);
    assertFalse(personRepository.findById(member.getId()).isPresent());

    TestGroup editorGroup = new TestGroup("Editor's Group", user);
    editorGroup.getMembers().add(member);
    groupRepository.saveWithoutPermissionCheck(editorGroup);
    member.getGroups().add(editorGroup);
    personRepository.saveWithoutPermissionCheck(member);

    assertTrue(personRepository.findById(member.getId()).isPresent());
  }

  @Test
  public void testGivenGroupMembersWhenCallProjectRepositoryMethodsThenReturnObjectAccordingToParentOwnerPermission() {
    setAuthentication("user");
    Person member = new Person("member", "m", "m");
    personRepository.saveWithoutPermissionCheck(member);

    TestGroup projectGroup = new TestGroup("Project Group", user);
    projectGroup.getMembers().add(member);
    groupRepository.saveWithoutPermissionCheck(projectGroup);
    member.getGroups().add(projectGroup);
    personRepository.saveWithoutPermissionCheck(member);

    Project project = new Project();
    project.getGroups().add(projectGroup);
    projectRepository.saveWithoutPermissionCheck(project);
    projectGroup.getProjects().add(project);
    groupRepository.saveWithoutPermissionCheck(projectGroup);

    assertTrue(projectRepository.findById(project.getId()).isPresent());
  }

  @Test
  public void testGivenCurrentUserHasPermissionInMultipleWaysWhenCallProjectRepositoryMethodsThenReturnListIsDistinct() {
    setAuthentication("user");
    Person member = new Person("member", "m", "m");
    personRepository.saveWithoutPermissionCheck(member);
    Person member2 = new Person("member2", "m2", "m2");
    personRepository.saveWithoutPermissionCheck(member2);

    TestGroup projectGroup = new TestGroup("Project Group", user);
    projectGroup.getMembers().add(member);
    projectGroup.getMembers().add(member2);
    groupRepository.saveWithoutPermissionCheck(projectGroup);
    member.getGroups().add(projectGroup);
    member2.getGroups().add(projectGroup);
    personRepository.saveWithoutPermissionCheck(member);
    personRepository.saveWithoutPermissionCheck(member2);

    Project project = new Project();
    project.getGroups().add(projectGroup);
    projectRepository.saveWithoutPermissionCheck(project);
    projectGroup.getProjects().add(project);
    groupRepository.saveWithoutPermissionCheck(projectGroup);

    assertTrue(projectRepository.findById(project.getId()).isPresent());

    assertThat(projectRepository.findAll().size(), is(1));

  }

  @Test
  public void testGivenNoAclEntityWhenCallRepositoryMethodsThenReturnObject() {
    Theme theme = new Theme("theme", "content");
    themeRepository.save(theme);
    setAuthentication("user");
    assertTrue(themeRepository.findById(theme.getId()).isPresent());
  }

  @Test
  public void testGivenFullOwnerPermissionsWhenCallDeleteThenObjectIsDeleted() {
    setAuthentication("user2");
    Document doc = new Document("doc", "content", user2);
    documentRepository.saveWithoutPermissionCheck(doc);
    assertTrue(documentRepository.existsById(doc.getId()));
    documentRepository.delete(doc);
    assertFalse(documentRepository.existsById(doc.getId()));
  }

  @Test
  public void testGivenRolePermissionsAndNoRoleWhenCallfindByIdOnAttachmentThenReturnNull() {
    Attachment attachment = new Attachment("name", "content", null, null);
    attachmentRepository.saveWithoutPermissionCheck(attachment);
    setAuthentication("user");
    assertFalse(attachmentRepository.findById(attachment.getId()).isPresent());
  }

  @Test
  public void testGivenRolePermissionsAndProperRoleWhenCallfindByIdOnAttachmentThenReturnObject() {
    Attachment attachment = new Attachment("name", "content", null, null);
    attachmentRepository.saveWithoutPermissionCheck(attachment);
    user.getAclRoles().add(manipulatorRole);
    personRepository.saveWithoutPermissionCheck(user);
    setAuthentication("user");
    assertTrue(attachmentRepository.findById(attachment.getId()).isPresent());
  }

  @Test(expected = AclDeletePermissionException.class)
  public void testGivenRolePermissionsAndProperRoleWhenCallDeleteOnAttachmentThenThrowException() {
    Attachment attachment = new Attachment("name", "content", null, null);
    attachmentRepository.saveWithoutPermissionCheck(attachment);
    user.getAclRoles().add(manipulatorRole);
    personRepository.saveWithoutPermissionCheck(user);
    setAuthentication("user");
    attachmentRepository.delete(attachment);
  }

  @Test
  public void testGivenRolePermissionsAndProperRoleWhenCallSaveOnAttachmentThenObjectIsSaved() {
    Attachment attachment = new Attachment("name", "content", null, null);
    attachmentRepository.saveWithoutPermissionCheck(attachment);
    user.getAclRoles().add(manipulatorRole);
    personRepository.saveWithoutPermissionCheck(user);
    setAuthentication("user");
    attachment.setContent("new content");
    attachmentRepository.save(attachment);

    final Optional<Attachment> optionalAttachment = attachmentRepository.findById(attachment.getId());
    assertTrue(optionalAttachment.isPresent());
    assertThat(optionalAttachment.get().getContent(), is("new content"));
  }

  @Test
  public void testGivenRolePermissionsAndNoProperRoleAsAdminWhenCallfindByIdOnAttachmentThenReturnNull() {
    Attachment attachment = new Attachment("name", "content", null, null);
    attachmentRepository.saveWithoutPermissionCheck(attachment);
    setAuthentication("admin");
    assertFalse(attachmentRepository.findById(attachment.getId()).isPresent());
  }

  @Test
  public void testGivenDirectPermissionLinkToPersonWhenCallExistsThenReturnTrue() {
    setAuthentication("user2");
    assertFalse(personRepository.existsById(user.getId()));
    PersonHasPersonPermission ppp = new PersonHasPersonPermission(user2, user, "read");
    personHasPersonRepository.save(ppp);

    assertTrue(personRepository.existsById(user.getId()));
  }
}
