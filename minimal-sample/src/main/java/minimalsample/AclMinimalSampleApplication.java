package minimalsample;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.berrycloud.acl.configuration.EnableAclJpaRepositories;

@SpringBootApplication
@EnableAclJpaRepositories
public class AclMinimalSampleApplication {

    public static void main(String[] args) {
        SpringApplication.run(AclMinimalSampleApplication.class, args);
    }
}
