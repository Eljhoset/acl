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
public class BettingShopRepositoryTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private BettingShopRepository bettingShopRepository;

    @Test(expected = AccessDeniedException.class)
    @WithMockUser(username = "username")
    public void givenEmptyRole_whenFindAllBettingShops_thenCredentialFail() {
        bettingShopRepository.findAll(PageRequest.of(0, 10));
    }

    @Transactional
    @Test(expected = AccessDeniedException.class)
    @WithMockUser(username = "username")
    public void givenEmptyRole_whenGetOneBettingShops_thenCredentialFail() {
        bettingShopRepository.getOne(1l);
    }

    @Transactional
    @Test(expected = AccessDeniedException.class)
    @WithMockUser(username = "username", roles = {"ADMIN"})
    public void givenAdminRole_whenGetOneBettingShopsForbbiden_thenCredentialFail() {
        bettingShopRepository.getOne(2l);
    }

    @Test
    @Transactional
    @WithMockUser(username = "username", roles = {"SUPER_ADMIN"})
    public void givenSuperAdminRole_whenGetOneBettingShops_thenGetBettingshop() {
        Bettingshop bettingshop = bettingShopRepository.getOne(1l);
        assertNotNull("get bettingshop 1 should not be empty", bettingshop);
        bettingshop = bettingShopRepository.getOne(2l);
        assertNotNull("get bettingshop 2 should not be empty", bettingshop);
    }

    @Test
    @Transactional
    @WithMockUser(username = "username", roles = {"ADMIN"})
    public void givenAdminRole_whenGetOneBettingShopsAllowed_thenGetBettingshop() {
        Bettingshop bettingshop = bettingShopRepository.getOne(1l);
        assertNotNull("get bettingshop 1 should not be empty", bettingshop);
    }

    @Test
    @Transactional
    @WithMockUser(username = "username", roles = {"ADMIN"})
    public void givenAdminRole_whenGetOneBettingShopsAllowedByConsortium_thenGetBettingshop() {
        Bettingshop bettingshop = bettingShopRepository.getOne(3l);
        assertNotNull("get bettingshop 3 should not be empty", bettingshop);
    }

    @Test
    @WithMockUser(username = "username", roles = {"ADMIN"})
    public void givenAdminRole_whenFindAll_thenEntities() {

        assertThat("I should get only the bettingshop if have access to read", bettingShopRepository.findAll(BettingShopRepository.PREDICATE_BETTINGSHOP.viewable(), PageRequest.of(0, 10)).getContent(), hasSize(1));

        final List<Bettingshop> inResult = bettingShopRepository.findAll(PageRequest.of(0, 10)).getContent();
        assertThat("I should get only the bettingshop if have access to read and also the one that have the consortium i can get", inResult, hasSize(2));

        assertThat("I should get only the bettingshop(1 and 3) if have access to read and also the one that have the consortium i can get", inResult, contains(
                hasProperty("id", is(1l)),
                hasProperty("id", is(3l))
        ));

    }

    @Test
    @WithMockUser(username = "username", roles = {"SUPER_ADMIN"})
    public void givenSuperAdminRole_whenFindAll_thenEntities() {

        assertThat("I should get all the bettingshop by id", bettingShopRepository.findAll(BettingShopRepository.PREDICATE_BETTINGSHOP.viewable(), PageRequest.of(0, 10)).getContent(), hasSize(4));

        final List<Bettingshop> inResult = bettingShopRepository.findAll(PageRequest.of(0, 10)).getContent();
        assertThat("I should get all the bettingshop", inResult, hasSize(3));

        assertThat("I should get only the bettingshop(1,2 and  3)", inResult, contains(
                hasProperty("id", is(1l)),
                hasProperty("id", is(2l)),
                hasProperty("id", is(3l))
        ));

    }

    @Test(expected = AccessDeniedException.class)
    @WithMockUser(username = "username")
    public void givenEmptyRole_whenCreateBettingShop_thenCredentialFail() {
        Bettingshop build = Bettingshop.builder().name("betting shop from test")
                .consortium(Consortium.builder().id(1l).build()).build();
        bettingShopRepository.save(build);
    }

    @Test
    @WithMockUser(username = "username", roles = {"SUPER_ADMIN"})
    public void givenSuperAdminRole_whenCreateBettingShop_thenCreateBettingShop() {
        Bettingshop build = Bettingshop.builder().name("betting shop from test")
                .consortium(Consortium.builder().id(1l).build()).build();
        Bettingshop bettingshop = bettingShopRepository.save(build);
        assertNotNull("created bettingshop should not be null", bettingshop);
        jdbcTemplate.execute(String.format("delete from bettingshop where id=%d;", bettingshop.getId()));
    }

    @Test(expected = AccessDeniedException.class)
    @WithMockUser(username = "username", roles = {"ADMIN"})
    public void givenAdminRole_whenCreateBettingShopOfForbiddenConsortium_thenCredentialFail() {
        Bettingshop build = Bettingshop.builder().name("betting shop from test")
                .consortium(Consortium.builder().id(1l).build()).build();
        bettingShopRepository.save(build);
    }

    @Test
    @Transactional
    @WithMockUser(username = "username", roles = {"ADMIN"})
    public void givenAdminRole_whenCreateBettingShopOfAllowedConsortium_thenCreateBettingShop() {
        Bettingshop build = Bettingshop.builder().name("betting shop from test")
                .consortium(Consortium.builder().id(2l).build()).build();
        Bettingshop bettingshop = bettingShopRepository.save(build);
        assertNotNull("created bettingshop should not be null", bettingshop);
        jdbcTemplate.execute(String.format("delete from bettingshop where id=%d;", bettingshop.getId()));
    }

    @Test(expected = AccessDeniedException.class)
    @Transactional
    @WithMockUser(username = "username", roles = {"ADMIN"})
    public void givenAdminRole_whenUpdateBettingShopForbidden_thenCredentialFail() {
        Bettingshop bettingshop = bettingShopRepository.findById(2l).get();
        bettingshop.setName("updated");
        bettingShopRepository.save(bettingshop);
    }

    @Test
    @Transactional
    @WithMockUser(username = "username", roles = {"ADMIN"})
    public void givenAdminRole_whenUpdateBettingShopAllowed_thenUpdateeBettingShop() {
        Bettingshop bettingshop = bettingShopRepository.findById(1l).get();
        String originalName = bettingshop.getName();
        bettingshop.setName("updated");
        Bettingshop updated = bettingShopRepository.save(bettingshop);
        assertNotNull("updated bettingshop should not be null", updated);
        assertThat("Name should be updated", updated.getName(), is("updated"));
        bettingshop.setName(originalName);
        updated = bettingShopRepository.save(bettingshop);
        assertThat("Name should be updated to original", updated.getName(), is(originalName));
    }

    @Test
    @Transactional
    @WithMockUser(username = "username", roles = {"ADMIN"})
    public void givenAdminRole_whenUpdateBettingShopOfAllowedConsortium_thenUpdateeBettingShop() {
        Bettingshop bettingshop = bettingShopRepository.findById(3l).get();
        String originalName = bettingshop.getName();
        bettingshop.setName("updated");
        Bettingshop updated = bettingShopRepository.save(bettingshop);
        assertNotNull("created bettingshop should not be null", updated);
        assertThat("Name should be updated", updated.getName(), is("updated"));
        bettingshop.setName(originalName);
        updated = bettingShopRepository.save(bettingshop);
        assertThat("Name should be updated to original", updated.getName(), is(originalName));
    }

    @Test(expected = AccessDeniedException.class)
    @Transactional
    @WithMockUser(username = "username", roles = {"ADMIN"})
    public void givenAdminRole_whenDeleteBettingShopForbidden_thenCredentialFail() {
        Bettingshop bettingshop = bettingShopRepository.findById(2l).get();
        bettingShopRepository.delete(bettingshop);
    }

    @Test
    @Transactional
    @WithMockUser(username = "username", roles = {"ADMIN"})
    public void givenAdminRole_whenDeleteBettingShopAllowed_thenDeleteBettingShop() {
        bettingShopRepository.deleteById(1l);
        Bettingshop bettingshop = bettingShopRepository.findById(1l).get();

        assertNotNull("deleted bettingshop should not be null", bettingshop);
        assertThat("deleted bettingshop should be deleted", bettingshop.getStatus(), is(EntityStatus.DELETED));
        bettingshop.setStatus(EntityStatus.ACTIVE);
        bettingshop = bettingShopRepository.save(bettingshop);
        assertThat("Status should be updated to original", bettingshop.getStatus(), is(EntityStatus.ACTIVE));
    }

    @Test
    @Transactional
    @WithMockUser(username = "username", roles = {"ADMIN"})
    public void givenAdminRole_whenDeleteBettingShopOfAllowedConsortium_thenDeleteBettingShop() {
        bettingShopRepository.deleteById(3l);
        Bettingshop bettingshop = bettingShopRepository.findById(3l).get();

        assertNotNull("deleted bettingshop should not be null", bettingshop);
        assertThat("deleted bettingshop should be deleted", bettingshop.getStatus(), is(EntityStatus.DELETED));
        bettingshop.setStatus(EntityStatus.ACTIVE);
        bettingshop = bettingShopRepository.save(bettingshop);
        assertThat("Status should be updated to original", bettingshop.getStatus(), is(EntityStatus.ACTIVE));
    }

    @Test
    @WithMockUser(username = "username", roles = {"SUPER_ADMIN"})
    public void givenSuperAdminRole_whenGetBettingShopByConsortium_thenGetBettingShops() {
        List<Bettingshop> content = bettingShopRepository.findByConsortium(Consortium.builder().id(1l).build(), PageRequest.of(0, 10)).getContent();
        assertThat("I should get 1 bettingshop", content, hasSize(2));
        assertThat("I should get only the bettingshop(1 and 2)", content, contains(
                hasProperty("id", is(1l)),
                hasProperty("id", is(2l))));

        content = bettingShopRepository.findByConsortium(Consortium.builder().id(2l).build(), PageRequest.of(0, 10)).getContent();
        assertThat("I should get 1 bettingshop", content, hasSize(1));
        assertThat("I should get only the bettingshop(3)", content, contains(
                hasProperty("id", is(3l))));
    }

    @Test(expected = AccessDeniedException.class)
    @WithMockUser(username = "username", roles = {"ADMIN"})
    public void givenAdminRole_whenGetBettingShopByConsortiumForbidden_thenCredentialFail() {
        bettingShopRepository.findByConsortium(Consortium.builder().id(1l).build(), PageRequest.of(0, 10));
    }

    @Test
    @WithMockUser(username = "username", roles = {"ADMIN"})
    public void givenAdminRole_whenGetBettingShopByConsortiumAllowed_thenCredentialFail() {
        List<Bettingshop> content = bettingShopRepository.findByConsortium(Consortium.builder().id(2l).build(), PageRequest.of(0, 10)).getContent();
        assertThat("I should get 1 bettingshop", content, hasSize(1));
        assertThat("I should get only the bettingshop(3)", content, contains(
                hasProperty("id", is(3l))));
    }
}
