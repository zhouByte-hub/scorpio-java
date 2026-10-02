package com.zhoubyte.core.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "scorpio")
@Data
public class ScorpioConfig {

    private Integer memorySize = 32;

}
