package ink.wsm.mirai.daily_attendance_v2.cores;

import ink.wsm.mirai.daily_attendance_v2.utils.Yamler;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class S {
    /**
     * 所有者相关
     */
    public static class Owner {
        public static long Id = 657442693L;
        public static String TimerError = "每日打卡计时器出现错误：\n";
    }

    /**
     * 命令相关
     */
    public static class Command {
        public static String main = "/da";

        //快速打卡命令
        public static String ka = main + " ka";
        public static String kaCN = "打卡";

        public static String wake = main + " wake";
        public static String sleep = main + " sleep";
        public static String nap = main + " nap";
        public static String run = main + " run";

        public static String info = main + " info";
        public static String rank = main + " rank";
        public static String use = main + " use";
        public static String exch = main + " exch";
        public static String set = main + " set";
        public static String put = main + " put";

        public static String testing = main + " testing";

        public static String terminal = "da";
    }

    /**
     * 数据相关
     */
    public static class Data {
        public static String configFolder = null; //配置目录
        public static String dataFolder = null; //数据目录
        public static String userFolder = null; //用户数据目录
        public static String groupFolder = null; //群数据目录
        public static Yamler generalYaml = null; //全局配置文件
        public static Yamler globalYaml = null; //全局动态数据文件
    }

    /**
     * 默认数据相关
     */
    public static class Default {
        public static String language = "zh_CN";
        public static int httpTimeout = 30;
    }

    /**
     * 全局变量文件相关
     */
    public static class Global {
        public static class Field {
            public static String pool = ".pool";
            public static String list = ".list";
        }
    }

    /**
     * 获取语言配置文件的指定键的值，语言包使用 S.Default.language 的数据
     *
     * @param key 欲获取的键（多级路径使用“.”分割）
     * @return 返回获取到的结果，如不存在返回“”
     */
    public static String get(String key) {
        String filePath = Data.configFolder + "languages/" + Default.language + ".yml";
        Yamler yamler = new Yamler(filePath);
        return yamler.getString(key);
    }

    /**
     * 获取语言配置文件的指定键的值，并以列表的形式返回，语言包使用 S.Default.language 的数据
     *
     * @param key 欲获取的值（多级目录使用“.”分割）
     * @return 返回获取到的结果，如果不存在则返回已初始化的 List
     */
    public static List<?> getList(String key) {
        String filePath = Data.configFolder + "languages/" + Default.language + ".yml";
        Yamler yamler = new Yamler(filePath);
        return yamler.getList(key);
    }

    /**
     * 获取语言配置文件的指定键的值，并以 Map 的形式返回，语言包使用 S.Default.language 的数据
     *
     * @param path 欲获取的值（多级目录使用“.”分割）
     * @return 返回获取到的结果，如果不存在则返回已初始化的 Map
     */
    public static Map<Object, Object> getMap(String path) {
        String filePath = Data.configFolder + "languages/" + Default.language + ".yml";
        Yamler yamler = new Yamler(filePath);

        Map<Object, Object> result = new TreeMap<>();
        Set<?> keys = yamler.getKey(path);

        keys.forEach((key) -> {
            Object value = yamler.get(path + "." + key);
            result.put(key, value);
        });

        return result;
    }
}
