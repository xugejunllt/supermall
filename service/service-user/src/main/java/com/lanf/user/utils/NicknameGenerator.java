package com.lanf.user.utils;


import java.util.Random;

/**
 * 中文昵称生成工具类
 * 随机生成常见中文昵称
 */
public class NicknameGenerator {

    private static final String[] PREFIXES = {
        "快乐的", "幸福的", "自由的", "勇敢的", "聪明的", "温柔的", "阳光的", "可爱的",
        "优雅的", "潇洒的", "神秘的", "浪漫的", "单纯的", "善良的", "坚强的", "自信的",
        "安静的", "活泼的", "热情的", "冷静的", "孤独的", "追梦的", "努力的", "奋斗的",
        "微笑的", "沉默的", "随性的", "淡然的", "执着的", "洒脱的", "低调的", "张扬的"
    };

    private static final String[] NOUNS = {
        "小猫", "小狗", "小鸟", "小鱼", "小熊", "小兔", "小鹿", "小狼",
        "风", "云", "雨", "雪", "星", "月", "日", "海",
        "山", "水", "花", "草", "树", "叶", "竹", "梅",
        "咖啡", "奶茶", "可乐", "果汁", "布丁", "蛋糕", "糖果", "巧克力",
        "旅行者", "追梦人", "读书人", "音乐家", "画家", "作家", "摄影师", "设计师",
        "少年", "青年", "骑士", "王子", "公主", "精灵", "天使", "恶魔"
    };

    private static final String[] SUFFIXES = {
        "", "", "", "", // 空后缀占比较高，让昵称更简洁
        "酱", "君", "大人", "小可爱", "小朋友", "小能手", "达人", "专家",
        "一号", "先生", "小姐", "同学", "老师", "队长", "先锋", "使者"
    };

    private static final String[] ADJECTIVES = {
        "红色", "蓝色", "绿色", "黄色", "紫色", "黑色", "白色", "灰色",
        "金色", "银色", "水晶", "琉璃", "翡翠", "琥珀", "珊瑚", "珍珠"
    };

    private static final Random RANDOM = new Random();

    /**
     * 生成随机中文昵称
     * @return 随机昵称
     */
    public static String generateNickname() {
        int pattern = RANDOM.nextInt(4);
        StringBuilder nickname = new StringBuilder();

        switch (pattern) {
            case 0:
                // 模式1: 形容词 + 名词 (如: 快乐的小猫)
                nickname.append(getRandomElement(PREFIXES))
                        .append(getRandomElement(NOUNS));
                break;
            case 1:
                // 模式2: 颜色 + 名词 (如: 红色玫瑰)
                nickname.append(getRandomElement(ADJECTIVES))
                        .append(getRandomElement(NOUNS));
                break;
            case 2:
                // 模式3: 名词 + 后缀 (如: 旅行者一号)
                nickname.append(getRandomElement(NOUNS))
                        .append(getRandomElement(SUFFIXES));
                break;
            case 3:
                // 模式4: 前缀 + 名词 + 后缀 (如: 勇敢的小猫达人)
                nickname.append(getRandomElement(PREFIXES))
                        .append(getRandomElement(NOUNS))
                        .append(getRandomElement(SUFFIXES));
                break;
        }

        return nickname.toString();
    }

    /**
     * 从数组中随机获取一个元素
     */
    private static String getRandomElement(String[] array) {
        return array[RANDOM.nextInt(array.length)];
    }

    /**
     * 生成带数字后缀的昵称（确保唯一性）
     * @param baseNickname 基础昵称
     * @return 带数字的昵称
     */
    public static String generateNicknameWithNumber(String baseNickname) {
        int number = RANDOM.nextInt(9999) + 1;
        return baseNickname + number;
    }

    /**
     * 生成完全随机的昵称（带数字）
     * @return 随机昵称
     */
    public static String generateUniqueNickname() {
        String baseNickname = generateNickname();
        return generateNicknameWithNumber(baseNickname);
    }
}
