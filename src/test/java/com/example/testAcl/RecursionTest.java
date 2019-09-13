package com.example.testAcl;

import com.example.testAcl.deep.AclRecursionService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 *
 * @author Daniel
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class RecursionTest {

    @Autowired
    private AclRecursionService aclRecursionService;

    @Test
    public void test() {
        aclRecursionService.findAll();
    }
}
