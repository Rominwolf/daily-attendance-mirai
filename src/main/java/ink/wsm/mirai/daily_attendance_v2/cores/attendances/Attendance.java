package ink.wsm.mirai.daily_attendance_v2.cores.attendances;

import ink.wsm.mirai.daily_attendance_v2.cores.S;
import ink.wsm.mirai.daily_attendance_v2.cores.cmds.Use;
import ink.wsm.mirai.daily_attendance_v2.cores.data.General;
import ink.wsm.mirai.daily_attendance_v2.cores.data.Global;
import ink.wsm.mirai.daily_attendance_v2.cores.data.User;
import ink.wsm.mirai.daily_attendance_v2.utils.Mirai;
import ink.wsm.mirai.daily_attendance_v2.utils.RedPacket;
import ink.wsm.mirai.daily_attendance_v2.utils.Smart;
import ink.wsm.mirai.daily_attendance_v2.utils.Yamler;
import net.mamoe.mirai.Bot;
import net.mamoe.mirai.event.Event;
import net.mamoe.mirai.message.data.MessageChain;
import org.apache.commons.lang3.RandomUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 全局打卡相关方法类
 */
public class Attendance {
    User user;
    Yamler global;

    Mirai mirai;
    Event event;
    Bot bot;
    long fromId;
    long fromGroup;
    boolean isGroup;
    MessageChain source;
    MessageChain messageRaw;
    String message;
    String messageAsMiraiCode;
    String eventClass;

    public Attendance(Mirai mirai, Event event) {
        this.mirai = mirai;
        this.event = event;
        this.bot = mirai.bot;
        this.fromId = mirai.fromId;
        this.fromGroup = mirai.fromGroup;
        this.isGroup = fromGroup != 0;
        this.source = mirai.source;
        this.message = mirai.transMain.toString();
        this.messageAsMiraiCode = mirai.transMinor.toString();
        this.messageRaw = mirai.transMessage;
        this.eventClass = event.getClass().toString();

        this.user = new User(fromId);
        this.global = S.Data.globalYaml;
    }

    /**
     * 全局打卡事件处理
     *
     * @param type 打卡类型
     * @return 返回结果的字符串
     */
    public String process(String type) {
        //进行有效性请求
        Map<String, Object> isValidMap = Attendance.checkIsJoined(fromId, type);

        //如果 valid 为 false 则表示请求无效，返回失败 reason
        if (!(boolean) isValidMap.get("valid")) return isValidMap.get("reason") + "";

        long endHour = General.getLong(type + General.Field.end);

        //数据表
        Map<String, Object> data = new HashMap<>();
        data.put("end", endHour);

        //抽取凭证数量
        int score = Attendance.rollTheScore();
        data.put("score", score);

        //获取指定凭证数量绑定的 lucky 文本
        List<?> luckyList = S.getList("lucky");
        String lucky = String.valueOf(luckyList.get(score - 1));
        data.put("lucky", lucky);

        //更新用户的打卡信息
        updateUserData(fromId, type);

        //更新全局的信息
        Map<String, Object> globalMap = updateGlobalData(fromId, type, score);
        int pool = Integer.parseInt(globalMap.get("pool") + "");
        data.put("pool", pool);

        String result = S.get(type + ".joined");
        result = Smart.replaceAllTheFields(result, data);

        //连续打卡7天处理
        String continuous = updateContinuousWeekData(fromId, type);
        mirai.sendMessage(continuous);

        //连续打卡一月处理
        continuous = updateContinuousMonthData(fromId, type);
        mirai.sendMessage(continuous);

        return result;
    }

    /**
     * 检查打卡状态（是否已参与打卡、打卡过早/过晚）
     *
     * @param fromId QQ号
     * @param type   打卡类型
     * @return 返回 Map as String, Object (valid: boolean (是否有效); reason: String (如果无效的原因))
     */
    public static Map<String, Object> checkIsJoined(long fromId, String type) {
        User user = new User(fromId);
        Map<String, Object> result = new HashMap<>();

        boolean isValid = true;

        long todayId = Smart.getTodayId();
        long lastSeenDate = user.getLastSeenDate(type);

        //今日已打卡
        if (lastSeenDate >= todayId) {
            result.put("reason", S.get(type + ".youAlreadyJoined"));
            result.put("valid", false);
            return result;
        }

        int hour = Smart.getHour();
        long startHour = General.getLong(type + General.Field.start);
        long endHour = General.getLong(type + General.Field.end);
        long checkHour = General.getLong(type + General.Field.check);

        String reason = "";

        //打卡时间过早
        if (hour < startHour) {
            isValid = false;
            reason = S.get(type + ".tooEarly");
        }

        //打卡时间过晚
        if (hour >= endHour) {
            isValid = false;
            reason = S.get(type + ".tooLate");
        }

        reason = reason
                .replace("{start}", startHour + "")
                .replace("{end}", endHour + "")
                .replace("{check}", checkHour + "");

        result.put("reason", reason);
        result.put("valid", isValid);

        return result;
    }

    /**
     * 进行凭证的抽取
     *
     * @return 返回凭证数量
     */
    public static int rollTheScore() {
        int rand = RandomUtils.nextInt(0, 100);

        //抽取到的凭证数量（42%/48%/8%/2%）
        int score = 1; // 42%
        if (rand >= 42 && rand < 90) score = 2; // 48%
        if (rand >= 90 && rand < 98) score = 3; // 8%
        if (rand >= 98 && rand < 100) score = 4; // 2%

        return score;
    }

    /**
     * 更新用户的总计打卡次数、最后打卡日期ID、最后打卡时间、历史上参与打卡、
     * 参与打卡状态、打卡后发言次数归零、使用夜光蜡烛为假
     *
     * @param fromId QQ号
     * @param type   打卡类型
     */
    public static void updateUserData(long fromId, String type) {
        long todayId = Smart.getTodayId();
        User user = new User(fromId);

        user.updateTotal(type, 1);
        user.updateLastSeenDate(type, todayId);
        user.setLastSeenTime(type, Smart.getDate());
        user.setTodayJoinedInHistory(type);

        user.setValue(type + User.Field.isJoined, true);
        user.setValue(type + User.Field.totalTalk, 0);
        user.setUseItem(User.Field.candle, false);
    }

    /**
     * 更新全局的全局打卡列表：添加用户、增加奖池积分、提交控制台命令
     *
     * @param fromId QQ号
     * @param type   打卡类型
     * @param score  增加的凭证数量
     * @return 返回 Map：pool(当前奖池的凭证数量)
     */
    public static Map<String, Object> updateGlobalData(long fromId, String type, int score) {
        long todayId = Smart.getTodayId();
        Global global = new Global();
        Map<String, Object> result = new HashMap<>();

        long pool = global.yamler.setLong(
                type + S.Global.Field.pool, score, false);
        String list = global.yamler.setString(
                type + S.Global.Field.list, fromId, 1, ",");
        result.put("pool", pool);

        //提交控制台命令
        String command = "/sb total " + todayId + " " + type + "_list " + list;
        Mirai.sendConsoleMessage(command);

        return result;
    }

    /**
     * 分配一个红包奖金（最小1张凭证，不保留小数）
     *
     * @param pool   奖池
     * @param amount 红包数量
     * @return 返回一个红包分配结果的 List
     */
    public static List<Long> allocateRedPacket(long pool, int amount) {
        RedPacket redPacket = new RedPacket(pool, amount);
        redPacket.setMinMoney(1);
        redPacket.setScale(0);
        return redPacket.result();
    }

    /**
     * 更新用户连续打卡的奖励（每连续打卡7天）
     *
     * @param fromId QQ号
     * @param type   打卡类型
     * @return 返回连续打卡的奖励消息
     */
    public String updateContinuousWeekData(long fromId, String type) {
        User user = new User(fromId);

        long amount = user.getHistoryContinuousTotalInThisMonth(type);

        //如果不是7的倍数则返回
        if (amount % 7 != 0) return "";

        return updateContinuousData(fromId, type, "week");
    }

    /**
     * 更新用户连续打卡的奖励（连续打卡一月）
     *
     * @param fromId QQ号
     * @param type   打卡类型
     * @return 返回连续打卡的奖励消息
     */
    public String updateContinuousMonthData(long fromId, String type) {
        User user = new User(fromId);

        int monthDays = Smart.getDaysOfMonth(new Date());
        long amount = user.getHistoryContinuousTotalInThisMonth(type);

        //如果不是连续打卡同本月天数一样的次数则返回
        if (amount != monthDays) return "";

        return updateContinuousData(fromId, type, "month");
    }

    /**
     * 更新用户连续打卡的奖励（每连续打卡7天）
     *
     * @param fromId         QQ号
     * @param type           打卡类型
     * @param continuousType 连续打卡类型（week, month）
     * @return 返回连续打卡的奖励消息
     */
    private String updateContinuousData(long fromId, String type, String continuousType) {
        User user = new User(fromId);

        long amount = user.getHistoryContinuousTotalInThisMonth(type);

        Map<String, Object> continuous = General.getContinuous(type, continuousType);
        String continuousId = continuous.get("id") + "";
        long continuousAmount = Smart.objectToLong(continuous.get("amount"));

        //增加道具数量
        user.updateItem(continuousId, +continuousAmount);

        String attendanceName = S.get(type + ".name");
        String continuousName = S.get("system.resource." + continuousId);

        return S.get("continuous.result")
                .replace("{type}", attendanceName)
                .replace("{day}", amount + "")
                .replace("{name}", continuousName)
                .replace("{amount}", continuousAmount + "");
    }

    /**
     * 处理打卡的奖池凭证奖励
     *
     * @param type 打卡类型
     */
    public static void processAttendanceTimer(String type) {
        Bot bot = Mirai.getBot();

        //如果当前没有 bot 则返回
        if (bot == null) return;

        //增加 global 字段
        String fieldPool = type + S.Global.Field.pool;
        String fieldList = type + S.Global.Field.list;

        Yamler global = S.Data.globalYaml;
        int pool = global.getInt(fieldPool);
        String list = global.getString(fieldList);

        String[] wakeList = list.split(",");
        int amount = wakeList.length;

        //将今日全局成功打卡列表和奖池清零
        global.set(fieldPool, 0);
        global.set(fieldList, "");

        //分配奖池奖金
        List<Long> redPackets = Attendance.allocateRedPacket(pool, amount);

        //循环每个参与打卡的用户列表
        for (int i = 0; i < amount; i++) {
            String userId = wakeList[i];

            //如果 userId 不是数字则 continue
            if (!Smart.isNumeric(userId)) continue;

            long targetId = Long.parseLong(userId);
            User user = new User(targetId);

            //获取红包凭证数量并进行翻倍
            long score = redPackets.get(i);
            score = Use.useDiceAndResponse(targetId, score);

            //更新用户凭证数量、设置夜光蜡烛使用状态、已加入打卡标记为假、累计发言次数归零
            user.updateScore(score);
            user.setUseItem(User.Field.candle, false);
            user.setValue(type + User.Field.isJoined, false);
            user.setValue(type + User.Field.totalTalk, 0);

            String result = S.get(type + ".pool")
                    .replace("{score}", score + "")
                    .replace("{pool}", pool + "");

            Mirai mirai = new Mirai(bot, targetId, 0, null);
            mirai.sendMessage(result);
        }
    }
}
