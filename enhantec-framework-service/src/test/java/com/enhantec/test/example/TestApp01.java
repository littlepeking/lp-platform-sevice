package com.enhantec.test.example;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.time.LocalDateTime;
import java.time.ZoneId;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest
public class TestApp01 {

    @Autowired
    MockMvc mockMvc;

    @Test
    public void testAuthentication() throws Exception {
        String result = mockMvc.perform(post("/testAuth").header("Bearer","wcdwdwdwdwd").
                contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk()).
              andReturn().getResponse().getContentAsString();
        System.out.println(result);
    }



    @Test
    public void addUser() throws Exception {
        LocalDateTime date = LocalDateTime.now();
        String content = "{\"userName\":\"john\",\"testDate\":" + date.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli() + "}";
        String result = mockMvc.perform(post("/user").contentType(MediaType.APPLICATION_JSON).content(content)).andExpect(status().isOk()).andExpect(jsonPath("$.userName").value("john")).andReturn().getResponse().getContentAsString();
        System.out.println(result);
    }

    @Test
    public void uploadFile() throws Exception {
        String result = mockMvc.perform( MockMvcRequestBuilders.multipart("/file/upload")
            .file(new MockMultipartFile("file","originalFile.txt","multipart/form-data","hello enhantec".getBytes())).param("type","receipt"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        System.out.println(result);
    }

}