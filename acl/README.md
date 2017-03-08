# Spring Data JPA ACL

This package is an extension for Spring Data JPA. It provides a fast an easily customizable access-control system for projects using Spring Repositories.

ACL usually means 'Access Control List', but in this case it stands for 'Access Control Logic'. Although it could still use lists for permission control most of the access decisions are made by predefined rules.

Let's see the main differences between traditional AC-List implementations and this package:

1. This package uses only standard JPA methods and queries, so in theory it's compatible with any Relational DB that has JPA support.
2. All the access control logics are defined in the domain objects via annotations, so you can see the data-structure and the access rules in the same place
3. The permission decisions are happened in the DB side, so there is much less traffic between your application and the DB, and you can even use paginated queries on permission-filtered entities.
4. It's compatible with Spring Data Jpa Repositories so the ACL is automatically applied for all standard repository methods.


## Basics

The basic idea behind this ACL package is that most of the time when you want to grant access a user to a given object in the DB, there is already some kind of relation between them.

Let's see some example:

- A user has full control to his documents (probably there is an author field in the document table). 
- Users in the same work-group can see (but cannot modify) each-others. (There must be a workgroup table with a ManyToMany relationship with users.)
- Administrators have full access to all objects. (They must have a special role attached to them somehow. Either directly or they are members of an admin group)
- If a user is a moderator in a topic he can edit all messages sent to that topic (Must be a relation between messages and topics) and also the replies to these messages (There must be a field in the message table what stores the original message if the current one is a reply.)

So instead of creating a new permission entry (or a bunch of entries) every time we are creating a new object, we simply use the existing relationships for access control.
Sometimes it means that you have to add a few extra relations to your tables when you design your DB structure, but hey, that's why it's called 'relational DB'!

## Usage

Add the following dependency to your pom.xml:

	<dependency>
      	<groupId>com.berrycloud.acl</groupId>
      	<artifactId>spring-data-jpa-acl</artifactId>
		<version>LATEST</version>
	</dependency>
	
*** Repository ???

Thanks to Spring's auto-configuration all necessary beans are initialized automatically. All you have to do is add the following annotation to one of your configuration class:
	
	@EnableAclJpaRepositories
	
The ACL extension is basically a security feature, so it automatically turns on Spring Security too. For ACL security we need at least the following basic components:
AclUserDetailsService (With an AclUserDetails implementation)
AclUser entity
AclRole entity

If any of these beans or domain classes are missing the ACl automatically creates them:
SimpleAclUserDetailsService (using SimpleAclUserDetails)
SimpleAclUser
SimpleAclRole

You can use all Spring Data Repository features exactly the same way as it's described in Spring Data JPA documentation, but you have to extend the AclJpaRepository interface instead of JpaRepository:

	public interface UserRepository extends AclJpaRepository<SimpleAclUser, Integer>{
	    
	    List<SimpleAclUser> findByLastName(@Param("name") String name);
	
	    SimpleAclUser findByUsername(@Param("username") String username);
	    
	    List<SimpleAclUser> findAllByOrderByLastNameDesc();
	    List<SimpleAclUser> findAllByOrderByIdAsc();
	    
	    Long countByIdGreaterThan(@Param("id") Integer id);
	}

The results of all query methods will be filtered by the ACL rules and all save and delete methods will be executed only on entities what the current user has permission to based on the AC rules.

Unfortunately the query methods where you manually define the native or JPA query cannot be processed by the ACL, so if any of your repositories contains such methods you will get a warning during startup:

	    @Query("select p from SimpleAclUser p")
	    List<SimpleAclUser> mySelect();

You can annotate such methods with the @NoAcl annotation to prevent the warnings.

@NoAcl annotation is also useful if you want to create methods (or repositories) without permission checks. (E.g. you want a repository with full ACL support for your API and an other one without permission checks for inner use for your scheduled tasks.)

If you also want to use Spring Data Rest module, you can include it the same way:

    <dependency>
      <groupId>org.springframework.data</groupId>
      <artifactId>spring-data-rest-webmvc</artifactId>
    </dependency>
    
If this package is in the classpath the ACL module automatically creates the necessary beans and modification for the rest API and it will use the ACL secured repository methods. There is also a slight improvement in the rest API that allows using paginated queries on properties.

You should also consider adding the following dependency if you use Hibernate, otherwise lazily loaded properties could appear in an inconsistent way:
    
	<dependency>
      <groupId>com.fasterxml.jackson.datatype</groupId>
      <artifactId>jackson-datatype-hibernate5</artifactId>
      <optional>true</optional>
    </dependency>
    
## ACL rules

You can build up your ACL rules using a few simple annotations. All of the following annotations have a String[] field where you can set a list of permissions. Permissions are represented by strings. The ACl uses the following default permissions:
read
update
delete
all

'all' permission provides all of the other permissions not only the ones listed here but any other permissions you use in your project.
'read' permission is automatically provided if any other permission is present. (It's quite logical: If you have any kind of permission to an object i.e. you can 'do' something with that object then we can assume you can 'see' that object.)

### @AclSelf

This annotation defines the permissions the users will gain on their own domain object. By default any user can read, update or delete her own domain-object but sometimes you want to limit their access. (Maybe you want to create special protocol for deletion, or you want to prevent modifying usernames or passwords ) 

A simple example for a User domain class:

	@Entity
	@AclSelf({ "read" })
	public class Person implements AclUser<SimpleAclRole> {
	
	    @Id
	    @GeneratedValue(strategy = GenerationType.AUTO)
	    private Integer id;
	
	    @Column(unique = true, nullable = false)
	    private String username;
	    @JsonIgnore
	    private String password = "password";
	
	    @ManyToMany(fetch = FetchType.LAZY)
	    private Set<SimpleAclRole> aclRoles;
	
	    private String firstName;
	    private String lastName;
	    
	    // ... other properties, getters/setters
	}
   
It implements the AclUser interface, so when a user logs in, the AclUserDetailsService automatically tries to locate the user by username in this table. The @AclSelf annotation grants "read" permission for users to their own user-object, so every user can see but cannot modify or delete their user-object. 

If you create the following repository

	@RepositoryRestResource(collectionResourceRel = "people", path = "people")
	public interface PersonRepository extends AclJpaRepository<Person, Integer>{  
	}

... and have spring-data-rest-webmvc dependency in your pom.xml then if a user tries to access the /people endpoint she could only her own user object there.


### @AclOwner

With this annotation we can provide some permission to a user or group of users via a property. Let's see the following simple document entity: 

	@Entity
	public class Document {
	
	    @Id
	    @GeneratedValue(strategy = GenerationType.AUTO)
	    private Integer id;
	
	    private String title;
	    private String content;

	    @AclOwner
	    @ManyToOne(fetch = FetchType.LAZY)
	    private Person creator;
	}

Most of the time you create properties like 'creator' or 'createdBy' or 'owner' anyway so adding this association to your domain class is not a real plus. But by adding one extra annotation here you can easily set a permission for all of the users in your DB to their documents. (The default permission is 'all' for all of the ACL annotations)

If the users of this system commonly share documents with each others (and can modify each-others documents) then you can add the following association:

	@AclOwner("update")
	@ManyToMany
	private List<Person> editors;
	
All the users who are in relation with a document via this association will gain the 'update' permission to that document.
 