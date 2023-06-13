/*******************************************************************************
 *                                     NOTICE
 *
 *             THIS SOFTWARE IS THE PROPERTY OF AND CONTAINS CONFIDENTIAL
 *             INFORMATION. AND SHALL NOT BE DISCLOSED WITHOUT PRIOR
 *             WRITTEN PERMISSION OF AUTHOR. LICENSED CUSTOMERS MAY COPY AND
 *             ADAPT THIS SOFTWARE FOR THEIR OWN USE IN ACCORDANCE WITH
 *             THE TERMS OF THEIR SOFTWARE LICENSE AGREEMENT.
 *             ALL OTHER RIGHTS RESERVED BY AUTHOR.
 *
 *             (c) COPYRIGHT 2022. ALL RIGHTS RESERVED.
 *
 *             Author: John Wang
 *             Email: john.wang_ca@hotmail.com
 * 
 *******************************************************************************/



package com.enhantec;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
public class TestApp01 {

    @Autowired
    private WebApplicationContext context;


    private MockMvc mockMvc;

    @BeforeEach
    public void setup(){
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void userInfo() throws Exception {
        String result = mockMvc.perform(get("/api/security/user/userInfo").
                contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk()).
                andReturn().getResponse().getContentAsString();
        System.out.println(result);
    }


//    @Test
//    public void testAuthentication() throws Exception {
//        String result = mockMvc.perform(post("/testAuth").header("Bearer","wcdwdwdwdwd").
//                contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk()).
//              andReturn().getResponse().getContentAsString();
//        System.out.println(result);
//    }
//

//
//    @Test
//    public void addUser() throws Exception {
//        LocalDateTime date = LocalDateTime.now();
//        String content = "{\"userName\":\"john\",\"testDate\":" + date.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli() + "}";
//        String result = mockMvc.perform(post("/user").contentType(MediaType.APPLICATION_JSON).content(content)).andExpect(status().isOk()).andExpect(jsonPath("$.userName").value("john")).andReturn().getResponse().getContentAsString();
//        System.out.println(result);
//    }

//    @Test
//    public void uploadFile() throws Exception {
//        String result = mockMvc.perform( MockMvcRequestBuilders.multipart("/file/upload")
//            .file(new MockMultipartFile("file","originalFile.txt","multipart/form-data","hello enhantec".getBytes())).param("type","receipt"))
//                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
//        System.out.println(result);
//    }

}
