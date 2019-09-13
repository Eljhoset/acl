package com.example.testAcl;

import javax.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;

/**
 *
 * @author Daniel
 */
@Configuration
public class AclConfiguration {

    @PostConstruct
    private void init() {
//        ObjectIdentity oi = new ObjectIdentityImpl(Foo.class, new Long(44));
    }
}
