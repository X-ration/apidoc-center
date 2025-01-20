package com.adam.rest_demo_project;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"com.adam.rest_demo_project"})
public class RestDemoProjectApplication {

    public static void main(String[] args) {
        SpringApplication.run(RestDemoProjectApplication.class, args);
    }

}
