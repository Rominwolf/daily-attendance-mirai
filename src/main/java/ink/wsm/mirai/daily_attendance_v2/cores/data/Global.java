package ink.wsm.mirai.daily_attendance_v2.cores.data;

import ink.wsm.mirai.daily_attendance_v2.cores.S;
import ink.wsm.mirai.daily_attendance_v2.utils.Yamler;

/**
 * 全局动态数据表
 */
public class Global {
    public static class Field {
        public static String pool = ".pool";
        public static String list = ".list";
        public static String botBkn = "bot.bkn";
        public static String botCookies = "bot.cookies";
    }

    public Yamler yamler;

    public Global() {
        this.yamler = S.Data.globalYaml;
    }

    /**
     * 获取指定打卡类型的奖池奖金数量
     *
     * @param type 打卡类型
     * @return 奖池数量（默认0）
     */
    public long getPool(String type) {
        return yamler.getLong(type + Field.pool);
    }

    /**
     * 获取指定打卡类型的参与打卡的用户列表（字符串集合式）
     *
     * @param type 打卡类型
     * @return 打卡列表（使用“,”分割，默认""）
     */
    public String getList(String type) {
        return yamler.getString(type + Field.list);
    }

    /**
     * 获取用户配置的密钥 bkn
     *
     * @return bkn 数据
     */
    public long getBotBkn() {
        return yamler.getLong(Field.botBkn);
    }

    /**
     * 获取用户配置的密钥 cookies
     *
     * @return cookies 数据
     */
    public String getBotCookies() {
        return yamler.getString(Field.botCookies);
    }
}
