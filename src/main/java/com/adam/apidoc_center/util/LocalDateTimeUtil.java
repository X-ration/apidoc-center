package com.adam.apidoc_center.util;

import com.adam.apidoc_center.config.WebConfig;
import org.springframework.util.Assert;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class LocalDateTimeUtil {

    public static String timeDiffFriendlyDesc(LocalDateTime localDateTime) {
        LocalDateTime now = LocalDateTime.now();
        Assert.isTrue(!localDateTime.isAfter(now), "timeDiffFriendlyDesc time is after now");
        long secondsDiff = ChronoUnit.SECONDS.between(localDateTime, now);
        if(secondsDiff < 60) {
            return secondsDiff + "秒前";
        } else if(secondsDiff < 60 * 60) {
            return (secondsDiff / 60) + "分钟前";
        } else if(secondsDiff < 24 * 60 * 60) {
            return (secondsDiff / (60 * 60)) + "小时前";
        } else {
            return (secondsDiff / (24 * 60 * 60)) + "天前";
        }
    }

    public static String friendlyNowDateTime() {
        return WebConfig.DATE_TIME_FORMATTER.format(LocalDateTime.now());
    }

    public static void main(String[] args) {
        LocalDateTime localDateTime = LocalDateTime.of(2024,12,23,10,6,0);
        System.out.println(timeDiffFriendlyDesc(localDateTime));
    }

}
