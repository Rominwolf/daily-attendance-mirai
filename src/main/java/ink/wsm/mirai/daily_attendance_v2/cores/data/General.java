package ink.wsm.mirai.daily_attendance_v2.cores.data;

import ink.wsm.mirai.daily_attendance_v2.cores.S;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 全局配置文件相关
 */
public class General {
    public static class Field {
        public static String start = ".start";
        public static String end = ".end";
        public static String settle = ".settle";

        public static String status = ".status";
        public static String check = ".check";

        public static String awards = ".awards";

        public static String continuous = ".continuous";
        public static String id = ".id";
        public static String amount = ".amount";
    }

    /**
     * 获取指定路径的长整数型数据
     *
     * @param key 键路径
     * @return 返回结果（默认：0）
     */
    public static long getLong(String key) {
        return S.Data.generalYaml.getLong(key);
    }

    /**
     * 获取指定路径的字符串型数据
     *
     * @param key 键路径
     * @return 返回结果（默认：""）
     */
    public static String getString(String key) {
        return S.Data.generalYaml.getString(key);
    }

    /**
     * 获取指定路径的布尔型数据
     *
     * @param key 键路径
     * @return 返回结果（默认：false）
     */
    public static boolean getBoolean(String key) {
        return S.Data.generalYaml.getBoolean(key);
    }

    /**
     * 获取指定路径的键列表数据
     *
     * @param key 键路径
     * @return 返回结果（默认：已初始化的 Set）
     */
    public static Set<?> getKeysList(String key) {
        return S.Data.generalYaml.getKey(key);
    }

    /**
     * 获取指定打卡的连续打卡数据
     *
     * @param key  打卡类型
     * @param type 连续打卡类型（week, month）
     * @return 返回 Map as String(id, amount), Object
     */
    public static Map<String, Object> getContinuous(String key, String type) {
        String path = key + Field.continuous + "." + type;
        String id = S.Data.generalYaml.getString(path + Field.id);
        int amount = S.Data.generalYaml.getInt(path + Field.amount);

        Map<String, Object> result = new HashMap<>();
        result.put("id", id);
        result.put("amount", amount);

        return result;
    }
}
