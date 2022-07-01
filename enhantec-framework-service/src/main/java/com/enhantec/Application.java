package com.enhantec;

import com.enhantec.common.exception.EHApplicationException;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@SpringBootApplication
@EnableSwagger2
@RestController
@EnableCaching
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class,args);
    }

    @GetMapping("/echo")
    public String echo(){
        return "echo";
    }

    @GetMapping("/testRuntimeError")
    public void testRuntimeError(){
        throw new RuntimeException("run time exception test msg.");
    }

    @GetMapping("/testEHApplicationError")
    public void testEHApplicationError(){
        throw new EHApplicationException("run time exception test msg.");
    }

    @GetMapping("/testEHApplicationError2")
    public void testEHApplicationError2(){
        throw new EHApplicationException("c-args","arg1","arg2","arg3");
    }

}
