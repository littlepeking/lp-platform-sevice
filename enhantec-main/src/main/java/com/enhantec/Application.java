package com.enhantec;

import com.enhantec.common.exception.EHApplicationException;
import com.enhantec.example.test.dto.User;
import com.fasterxml.jackson.annotation.JsonView;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import javax.validation.Valid;

@SpringBootApplication
@EnableSwagger2
@RestController
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class,args);
    }


//    @Bean
//    @Primary
//    public ObjectMapper objectMapper() {
//        return    new ObjectMapper()
//                .registerModule(new ParameterNamesModule())
//                .registerModule(new Jdk8Module())
//                .registerModule(new JavaTimeModule());
//    }

    @GetMapping("/echo")
    public String echo(){
        return "echo";
    }

    @JsonView(User.UserSubView1.class)
    @PostMapping("/user")
    public User addUser(@Valid @RequestBody User user
          //  , BindingResult result
    ){

//        if(result.hasErrors()){
//            System.out.println(result);
//         //   throw new Exception(result.toString());
//        }
        user.setUserId(1);
        return user;
    }

    @GetMapping("/testRuntimeError")
    public void testRuntimeError(){
        throw new RuntimeException("run time exception test msg.");
    }

    @GetMapping("/testEHApplicationError")
    public void testEHApplicationError(){
        throw new EHApplicationException("run time exception test msg.");
    }


}
