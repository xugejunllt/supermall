package com.lanf.rocketmq.model.enums;

import lombok.Getter;

@Getter
public enum DelayLevelEnum {

    LEVEL_1("1s", 1),
    LEVEL_2("5s", 2),
    LEVEL_3("10s", 3),
    LEVEL_4("30s", 4),
    LEVEL_5("1m", 5),
    LEVEL_6("2m", 6),
    LEVEL_7("3m", 7),
    LEVEL_8("4m", 8),
    LEVEL_9("5m", 9),
    LEVEL_10("6m", 10),
    LEVEL_11("7m", 11),
    LEVEL_12("8m", 12),
    LEVEL_13("9m", 13),
    LEVEL_14("10m", 14),
    LEVEL_15("20m", 15),
    LEVEL_16("30m", 16),
    LEVEL_17("1h", 17),
    LEVEL_18("2h", 18);

    private final String description;
    private final int level;

    DelayLevelEnum(String description, int level) {
        this.description = description;
        this.level = level;
    }

    public static DelayLevelEnum fromLevel(int level) {
        for (DelayLevelEnum delayLevel : values()) {
            if (delayLevel.getLevel() == level) {
                return delayLevel;
            }
        }
        throw new IllegalArgumentException("无效的延迟级别: " + level);
    }

}
