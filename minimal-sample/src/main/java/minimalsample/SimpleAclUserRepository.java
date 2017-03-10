package minimalsample;

import com.berrycloud.acl.domain.SimpleAclUser;
import com.berrycloud.acl.repository.AclJpaRepository;

public interface SimpleAclUserRepository extends AclJpaRepository<SimpleAclUser, Integer> {

}
