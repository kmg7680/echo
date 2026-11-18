package com.echo.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/**
 * GET /timeline 통합 테스트. post → 앱 계층 → timeline 연결을 검증한다.
 */
@SpringBootTest
@AutoConfigureMockMvc
class TimelineIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private void createPost(String content) throws Exception {
        mockMvc.perform(post("/posts")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"content\":\"" + content + "\"}"))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("타임라인은 최신 글이 먼저 오고 limit을 반영한다")
    void timelineLatestFirstWithLimit() throws Exception {
        createPost("타임라인 첫번째");
        createPost("타임라인 두번째");

        mockMvc.perform(get("/timeline").param("limit", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].content").value("타임라인 두번째"))
                .andExpect(jsonPath("$.data[1].content").value("타임라인 첫번째"));
    }
}
