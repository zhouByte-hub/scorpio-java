package com.zhoubyte.scorpioflowable;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan(basePackages = "com.zhoubyte.scorpioflowable.mapper")
public class ScorpioFlowableApplication {

    public static void main(String[] args) {
        SpringApplication.run(ScorpioFlowableApplication.class, args);
    }

}
