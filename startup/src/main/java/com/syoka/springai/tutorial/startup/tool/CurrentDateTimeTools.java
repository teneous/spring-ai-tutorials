package com.syoka.springai.tutorial.startup.tool;


import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.context.i18n.LocaleContextHolder;

/**
 * @author syoka
 * @version CurrentDateTimeTools.java, v 0.1 2025-03-31 14:39 syoka
 */
public class CurrentDateTimeTools {

    @Tool(description = "获取当前地区的时间")
    String getCurrentDateTime() {
        return LocalDateTime.now().atZone(LocaleContextHolder.getTimeZone().toZoneId()).toString();
    }

    @Tool(description = "基于给定的时间设置闹钟，使用ISO-8601格式")
    void setAlarm(String time) {
        LocalDateTime alarmTime = LocalDateTime.parse(time, DateTimeFormatter.ISO_DATE_TIME);
        System.out.println("Alarm set for " + alarmTime);
    }

}
