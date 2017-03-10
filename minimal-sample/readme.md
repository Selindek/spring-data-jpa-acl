# Minimal Sample Application for ACL

This is the simplest demo application possible for ACL package. It demonstrates how easy to start working with the ACL package.
We need to add the following dependencies to the pom.xml:
	
		<!-- Spring Boot -->
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter</artifactId>
		</dependency>

		<!-- ACL package -->
		<dependency>
			<groupId>com.berrycloud.acl</groupId>
			<artifactId>spring-data-jpa-acl</artifactId>
		</dependency>

		<!-- Spring Data Rest Webmvc (REST API) -->
		<dependency>
			<groupId>org.springframework.data</groupId>
			<artifactId>spring-data-rest-webmvc</artifactId>
		</dependency>

		<!-- Need for properly visualising LAZY-LOADED properties -->
		<dependency>
			<groupId>com.fasterxml.jackson.datatype</groupId>
			<artifactId>jackson-datatype-hibernate5</artifactId>
		</dependency>	
		
		<!-- H2 In-memory DB for testing purpose -->
		<dependency>
			<groupId>com.h2database</groupId>
			<artifactId>h2</artifactId>
		</dependency>
		
		
We have only two classes. Our Application:

	@SpringBootApplication
	@EnableAclJpaRepositories
	public class AclMinimalSampleApplication {
	
	    public static void main(String[] args) {
	        SpringApplication.run(AclMinimalSampleApplication.class, args);
	    }
	}		
	
And we also create a repository for the default AclUser domain class (what is SimpleAclUser), so we can immediately browse our User table via Spring Data Rest:

	public interface SimpleAclUserRepository extends AclJpaRepository<SimpleAclUser, Integer> {	
	}
	
Start the application and enter the following URL:
	
	/http:localhost:8080/simpleAclUsers

If the application doesn't define AclUser and AclRole domain classes, the ACL package automatically creates them and also creates the ROLE_USER and ROLE_ADMIN roles, and creates a "user" and "admin" users for testing purpose. Both users' password are "password". It also activates Spring Security with default settings, so you can use the default login-screen for logging in either as "user" or "admin". As "user" you can see only the SimpleAclUser entity of the "user", as "admin" you can see both object in the list. (You can log in as a different Principal using the default logout URI http://localhost:8080/login?logout , or you could start an other browser window in incognito mode)

