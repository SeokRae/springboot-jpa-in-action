package kr.seok.querydsl.domain;

import kr.seok.querydsl.controller.MemberController;
import kr.seok.querydsl.domain.repository.MemberQuerydslRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;

import javax.persistence.EntityManager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


/**
 * 강의는 API를 postman으로 조회
 */
@WebMvcTest(controllers = MemberController.class)
class Querydsl18조회컨트롤러Test {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    MemberQuerydslRepository memberQuerydslRepository;

    @Test
    public void testCase1() throws Exception {
        testMembers("teamB", "31", "35");
    }

    private void testMembers(String team, String ageGoe, String ageLoe) throws Exception {
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("teamName", team);
        map.add("ageGoe", ageGoe);
        map.add("ageLoe", ageLoe);

        MvcResult result = mockMvc.perform(
                MockMvcRequestBuilders
                        .get("/v1/members")
                        .accept(MediaType.APPLICATION_JSON)
                        .queryParams(map)
        )
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn();

        String resultDOW = result.getResponse().getContentAsString();
        System.out.println(resultDOW);

        assertNotNull(resultDOW);
    }
}
