package com.zhoubyte.core.tools;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class TimeTools {

    @Tool(description = "根据ZoneID获取当前地区的时间")
    public String getCurrentTimezone(@ToolParam(description = "时区ID，比如：Asia/Shanghai") String zoneId) {
        ZoneId timeZoneId = ZoneId.of(zoneId);
        ZonedDateTime now = ZonedDateTime.now(timeZoneId);
        return now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }
}
