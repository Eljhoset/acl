package com.example.testAcl;

import java.util.List;
import javax.transaction.Transactional;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;

/**
 *
 * @author Daniel
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class ConsortiumRepositoryTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private ConsortiumRepository consortiumRepository;

    @Test(expected = AccessDeniedException.class)
    @WithMockUser(username = "username")
    public void givenEmptyRole_whenFindAllConsortiums_thenCredentialFail() {
        consortiumRepository.findAll(PageRequest.of(0, 10));
    }

    @Transactional
    @Test(expected = AccessDeniedException.class)
    @WithMockUser(username = "username")
    public void givenEmptyRole_whenGetOneConsortiums_thenCredentialFail() {
        consortiumRepository.getOne(1l);
    }

    @Transactional
    @Test(expected = AccessDeniedException.class)
    @WithMockUser(username = "username", roles = {"ADMIN"})
    public void givenAdminRole_whenGetOneConsortiumsForbbiden_thenCredentialFail() {
        consortiumRepository.getOne(1l);
    }

    @Test
    @Transactional
    @WithMockUser(username = "username", roles = {"SUPER_ADMIN"})
    public void givenSuperAdminRole_whenGetOneConsortiums_thenGetConsortium() {
        Consortium consortium = consortiumRepository.getOne(1l);
        assertNotNull("get consortium 1 should not be empty", consortium);
        consortium = consortiumRepository.getOne(2l);
        assertNotNull("get consortium 2 should not be empty", consortium);
    }

    @Test
    @Transactional
    @WithMockUser(username = "username", roles = {"ADMIN"})
    public void givendminRole_whenGetOneConsortiumsAllowed_thenGetConsortium() {
        Consortium consortium = consortiumRepository.getOne(2l);
        assertNotNull("get consortium 2 should not be empty", consortium);
    }

    @Test
    @WithMockUser(username = "username", roles = {"SUPER_ADMIN"})
    public void givenSuperAdminRole_whenFindAll_thenEntities() {

        assertThat("I should get all the consortium", consortiumRepository.findAll(ConsortiumRepository.PREDICATE_CONSORTIUM.viewable(), PageRequest.of(0, 10)).getContent(), hasSize(3));

        final List<Consortium> inResult = consortiumRepository.findAll(PageRequest.of(0, 10)).getContent();
        assertThat("I should get all the consortium actives", inResult, hasSize(2));

        assertThat("I should get only the consortium(1 and 2)", inResult, contains(
                hasProperty("id", is(1l)),
                hasProperty("id", is(2l))
        ));

    }

    @Test
    @WithMockUser(username = "username", roles = {"ADMIN"})
    public void givenAdminRole_whenFindAll_thenEntities() {

        assertThat("I should get only the consortium if have access to read", consortiumRepository.findAll(ConsortiumRepository.PREDICATE_CONSORTIUM.viewable(), PageRequest.of(0, 10)).getContent(), hasSize(1));

        final List<Consortium> inResult = consortiumRepository.findAll(PageRequest.of(0, 10)).getContent();
        assertThat("I should get only the consortium if have access to read and also the one that have the consortium i can get", inResult, hasSize(1));

        assertThat("I should get only the consortium 2 if have access to read", inResult, contains(
                hasProperty("id", is(2l))
        ));

    }

    @Test(expected = AccessDeniedException.class)
    @WithMockUser(username = "username")
    public void givenEmptyRole_whenCreateConsortium_thenCredentialFail() {
        Consortium build = Consortium.builder().name("betting shop from test").build();
        consortiumRepository.save(build);
    }

    @Test
    @WithMockUser(username = "username", roles = {"SUPER_ADMIN"})
    public void givenSuperAdminRole_whenCreateConsortium_thenCreateConsortium() {
        Consortium build = Consortium.builder().name("betting shop from test")
                .build();
        Consortium consortium = consortiumRepository.save(build);
        assertNotNull("created consortium should not be null", consortium);
        jdbcTemplate.execute(String.format("delete from consortium where id=%d;", consortium.getId()));
    }

    @Test(expected = AccessDeniedException.class)
    @WithMockUser(username = "username", roles = {"ADMIN"})
    public void givenAdminRole_whenCreateConsortium_thenCredentialFail() {
        Consortium build = Consortium.builder().name("betting shop from test")
                .build();
        consortiumRepository.save(build);
    }

    @Test(expected = AccessDeniedException.class)
    @Transactional
    @WithMockUser(username = "username", roles = {"ADMIN"})
    public void givenAdminRole_whenUpdateConsortiumForbidden_thenCredentialFail() {
        Consortium consortium = consortiumRepository.findById(1l).get();
        consortium.setName("updated");
        consortiumRepository.save(consortium);
    }

    @Test
    @Transactional
    @WithMockUser(username = "username", roles = {"ADMIN"})
    public void givenAdminRole_whenUpdateConsortiumAllowed_thenUpdateeConsortium() {
        Consortium consortium = consortiumRepository.findById(2l).get();
        String originalName = consortium.getName();
        consortium.setName("updated");
        Consortium updated = consortiumRepository.save(consortium);
        assertNotNull("updated consortium should not be null", updated);
        assertThat("Name should be updated", updated.getName(), is("updated"));
        consortium.setName(originalName);
        updated = consortiumRepository.save(consortium);
        assertThat("Name should be updated to original", updated.getName(), is(originalName));
    }

    @Test(expected = AccessDeniedException.class)
    @Transactional
    @WithMockUser(username = "username", roles = {"ADMIN"})
    public void givenAdminRole_whenDeleteConsortium_thenCredentialFail() {
        Consortium consortium = consortiumRepository.findById(2l).get();
        consortiumRepository.delete(consortium);
    }

    @Test
    @Transactional
    @WithMockUser(username = "username", roles = {"SUPER_ADMIN"})
    public void givenSuperAdminRole_whenDeleteConsortiumAllowed_thenCredentialFail() {
        consortiumRepository.deleteById(1l);
        Consortium consortium = consortiumRepository.findById(1l).get();

        assertNotNull("deleted consortium should not be null", consortium);
        assertThat("deleted consortium should be deleted", consortium.getStatus(), is(EntityStatus.DELETED));
        consortium.setStatus(EntityStatus.ACTIVE);
        consortium = consortiumRepository.save(consortium);
        assertThat("Status should be updated to original", consortium.getStatus(), is(EntityStatus.ACTIVE));
    }

}
