package ink.wsm.mirai.daily_attendance_v2.cores.data;

import ink.wsm.mirai.daily_attendance_v2.cores.S;
import ink.wsm.mirai.daily_attendance_v2.utils.Smart;
import ink.wsm.mirai.daily_attendance_v2.utils.Yamler;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

public class User {
    public static class Field {
        public static String score = "score";

        public static String candle = "candle";
        public static String rocker = "rocker";
        public static String dice = "dice";
        public static String paper = "paper";
        public static String lucky = "lucky";

        public static String isTransferred = "is_transferred";
        public static String lastSeenDate = ".last_seen_date";
        public static String lastSeenTime = ".last_seen_time";
        public static String total = ".total";
        public static String history = ".history";
        public static String totalTalk = ".total_talk";
        public static String remindStatus = ".remind.status";
        public static String isJoined = ".is_joined";

        public static String use = "use.";

        public static String stepMax = ".step.max";
        public static String stepLast = ".step.last";
    }

    String userPath;
    Yamler user;

    public User(long fromId) {
        this.userPath = S.Data.userFolder + fromId + ".yml";
        this.user = new Yamler(this.userPath);
    }

    public User(String filePath) {
        this.userPath = filePath;
        this.user = new Yamler(this.userPath);
    }

    /**
     * 获取用户的 Yamler 类
     *
     * @return 返回 Yamler 类
     */
    public Yamler get() {
        return user;
    }

    /**
     * 获取用户凭证票的数量
     *
     * @return 返回积分数量（默认：0）
     */
    public long getScore() {
        return user.getLong(Field.score);
    }

    /**
     * 获取用户所持指定道具的个数
     *
     * @param name 道具ID（User.Field）
     * @return 返回数量（默认：0）
     */
    public long getItemAmount(String name) {
        return user.getLong(name);
    }

    /**
     * 用户是否已将旧版用户数据转移到新版数据
     *
     * @return 返回真假（默认：假）
     */
    public boolean isTransferred() {
        return user.getBoolean(Field.isTransferred);
    }

    /**
     * 用户是否允许接收指定打卡的每日提醒
     *
     * @return 返回真假（默认：假）
     */
    public boolean isRemindOn(String type) {
        return user.getBoolean(type + Field.remindStatus);
    }

    /**
     * 用户是否已参与今日打卡（通过是否标记了 is_joined 字段）
     *
     * @param type 打卡类型（如：wake, nap, sleep）
     * @return 返回真假（默认：假）
     */
    public boolean isJoined(String type) {
        return user.getBoolean(type + Field.isJoined);
    }

    /**
     * 用户是否已参与今日晚安打卡（通过检查今日ID和最后打卡ID是否一致，并检查当前时间是否处于结算时间内）
     *
     * @return 返回真假（默认：假）
     */
    public boolean isJoinedSleep() {
        String type = "sleep";

        int hour = Smart.getHour();
        long start = General.getLong(type + General.Field.start);
        long end = General.getLong(type + General.Field.end);
        long settle = General.getLong(type + General.Field.settle);

        //如果当前时间不处于晚安打卡计时期内（当前小时＜打卡开始时间且≥结算时间），则返回假
        if (hour < start && hour >= settle) return false;

        long todayId = Smart.getTodayId();
        long lastSeenDate = getLastSeenDate(type);
        long subtract = todayId - lastSeenDate;// todayId - lastSeenDate

        //如果相差日期为1（进入了明天），且当前时间在晚安打卡时间内，则返回假
        if (subtract == 1 && hour >= start && hour < end)
            return false;

        //如果相差日期为0或1（当日或明日），则返回真
        return subtract == 0 || subtract == 1;
    }

    /**
     * 用户是否已使用指定道具
     *
     * @param id 目标道具ID
     * @return 返回真假（默认：假）
     */
    public boolean isUseItem(String id) {
        String field = Field.use + id;
        return user.getBoolean(field);
    }

    /**
     * 用户最后打卡时间ID
     *
     * @param type 打卡类型（如：wake, nap, sleep）
     * @return 返回 yyyyMMdd
     */
    public long getLastSeenDate(String type) {
        String field = type + Field.lastSeenDate;
        return user.getLong(field);
    }

    /**
     * 用户最后打卡时间
     *
     * @param type 打卡类型（如：wake, nap, sleep）
     * @return 返回 yyyy-MM-dd HH:mm:ss
     */
    public String getLastSeenTime(String type) {
        String field = type + Field.lastSeenTime;
        return user.getString(field);
    }

    /**
     * 用户累计打卡次数
     *
     * @param type 打卡类型（如：wake, nap, sleep）
     * @return 返回次数（默认：0）
     */
    public long getTotal(String type) {
        String field = type + Field.total;
        return user.getLong(field);
    }

    /**
     * 用户参与晚安打卡后的发言次数
     *
     * @param type 打卡类型（如：wake, nap, sleep）
     * @return 返回次数（默认：0）
     */
    public long getTotalTalk(String type) {
        String field = type + Field.totalTalk;
        return user.getLong(field);
    }

    /**
     * 用户跑步步数相关信息
     *
     * @return 返回 Map as String(max, last), Long
     */
    public Map<String, Long> getStep() {
        String type = "run";
        Map<String, Long> result = new HashMap<>();

        String field = type + Field.stepMax;
        result.put("max", user.getLong(field));

        field = type + Field.stepLast;
        result.put("last", user.getLong(field));

        return result;
    }

    /**
     * 更改用户数据文件中的指定键的值
     *
     * @param key   键路径
     * @param value 值
     * @return 返回更新后的结果
     */
    public Object setValue(String key, Object value) {
        return user.set(key, value);
    }

    /**
     * 更新用户积分
     *
     * @param content 要增加或减少的数量
     * @return 返回更新后的结果
     */
    public long updateScore(long content) {
        String field = Field.score;
        return user.setLong(field, content, false);
    }

    /**
     * 更新用户所持指定道具的个数
     *
     * @param name    道具ID（User.Field）
     * @param content 要增加或减少的数量
     * @return 返回更新后的结果
     */
    public long updateItem(String name, long content) {
        return user.setLong(name, content, false);
    }

    /**
     * 更新用户最后打卡时间ID
     *
     * @param type   打卡类型（如：wake, nap, sleep）
     * @param dateId 新的日期ID（yyyyMMdd）
     * @return 返回更新后的结果
     */
    public long updateLastSeenDate(String type, long dateId) {
        String field = type + Field.lastSeenDate;
        return user.setLong(field, dateId, true);
    }

    /**
     * 更新用户最后打卡时间
     *
     * @param type 打卡类型（如：wake, nap, sleep）
     * @param time 新的打卡时间（yyyy-MM-dd HH:mm:ss）
     * @return 返回更新后的结果
     */
    public String setLastSeenTime(String type, String time) {
        String field = type + Field.lastSeenTime;
        return user.setString(field, time, 0, "");
    }

    /**
     * 更新用户是否已使用指定道具的状态
     *
     * @param id 道具ID
     * @return 返回更新后的结果
     */
    public boolean setUseItem(String id, boolean status) {
        String field = Field.use + id;
        return (boolean) user.set(field, status);
    }

    /**
     * 更新用户累计打卡次数
     *
     * @param type    打卡类型（如：wake, nap, sleep）
     * @param content 要增加或减少的数量
     * @return 返回更新后的结果
     */
    public long updateTotal(String type, long content) {
        String field = type + Field.total;
        return user.setLong(field, content, false);
    }

    /**
     * 更新用户参与打卡后的发言次数
     *
     * @param type    打卡类型（如：wake, nap, sleep）
     * @param content 要增加或减少的数量
     * @return 返回更新后的结果
     */
    public long updateTotalTalk(String type, long content) {
        String field = type + Field.totalTalk;
        return user.setLong(field, content, false);
    }

    /**
     * 设定用户已参与今日打卡在 history 上为真
     *
     * @param type 打卡类型
     * @return 返回更新后的结果
     */
    public boolean setTodayJoinedInHistory(String type) {
        String year = Smart.getDate("yyyy");
        int month = Integer.parseInt(Smart.getDate("MM"));
        int day = Integer.parseInt(Smart.getDate("dd"));

        String field = type + Field.history + "." + year + "." + month + "." + day;
        return (boolean) user.set(field, true);
    }

    /**
     * 获取用户历史上累计参与打卡的次数
     *
     * @param type 打卡类型
     * @return 返回累计参与打卡次数
     */
    public long getHistoryTotal(String type) {
        String field = type + Field.history;
        Set<?> yearKeys = user.getKey(field);
        AtomicLong amount = new AtomicLong();

        //当年打卡的次数键
        yearKeys.forEach((Object yearKey) -> {
            String yearField = field + "." + yearKey; //wake.history.2022
            Set<?> monthKeys = user.getKey(yearField); //当月打卡的次数键

            monthKeys.forEach((Object dayKey) -> amount.getAndIncrement());
        });

        return amount.get();
    }

    /**
     * 获取用户历史上目标年份/月份的参与打卡的次数
     *
     * @param type  打卡类型
     * @param year  年份
     * @param month 月份
     * @return 返回参与打卡的次数
     */
    public long getHistoryTotalAtMonth(String type, int year, int month) {
        String field = type + Field.history + "." + year + "." + month;
        Set<?> keys = user.getKey(field);
        return keys.size();
    }

    /**
     * 获取用户历史上本月连续的参与打卡的次数
     *
     * @param type 打卡类型
     * @return 返回连续参与打卡的次数
     */
    public long getHistoryContinuousTotalInThisMonth(String type) {
        int year = Integer.parseInt(Smart.getDate("yyyy"));
        int month = Integer.parseInt(Smart.getDate("MM"));
        int day = Integer.parseInt(Smart.getDate("dd"));
        int yesterDay = day - 1;

        int amount = 0;
        String field = type + Field.history + "." + year + "." + month + ".";

        for (int i = yesterDay; i > 0; i--) {
            String dayField = field + i;

            //如果当前日期未打卡则直接跳出，因为再循环是无意义的（2022.1.1 -> !true）
            if (!user.exist(dayField)) break;

            amount++;
        }

        String todayField = field + day;

        //如果今日已打卡则连续签到次数+1
        if (user.exist(todayField)) amount++;

        return amount;
    }
}
