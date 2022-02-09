package ink.wsm.mirai.daily_attendance_v2.cores.cmds;

import ink.wsm.mirai.daily_attendance_v2.cores.S;
import ink.wsm.mirai.daily_attendance_v2.cores.data.User;
import ink.wsm.mirai.daily_attendance_v2.utils.Mirai;
import ink.wsm.mirai.daily_attendance_v2.utils.Smart;
import ink.wsm.mirai.daily_attendance_v2.utils.Yamler;
import net.mamoe.mirai.Bot;
import net.mamoe.mirai.event.Event;
import net.mamoe.mirai.message.data.At;
import net.mamoe.mirai.message.data.MessageChain;

import java.util.HashMap;
import java.util.Map;

/**
 * 用户信息命令类
 */
public class Info {
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

    public Info(Mirai mirai, Event event) {
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
     * 查询目标用户的信息并发送消息
     */
    public void process() {
        long targetId = fromId;

        //如果艾特了指定用户则使用查询目标用户的信息
        At at = (At) messageRaw.stream().filter(At.class::isInstance).findFirst().orElse(null);
        if (at != null) targetId = at.getTarget();

        //如果 targetId 和 fromId 不同则获取目标用户的名字
        String name = S.get("system.you");
        if (targetId != fromId)
            name = mirai.getUserNicknameFromGroup(targetId, fromGroup);

        // /da info 1 @
        String[] commands = message.split(" ");

        //填充菜单
        String result = S.get("info.menu").replace("{name}", name);
        String index = "";

        //如果为 群聊 且 目标用户和该用户ID相同 则添加结尾提醒艾特字符串
        if (isGroup && targetId == fromId) result += S.get("info.tipAt");

        //如果命令存在序号则填充序号
        if (commands.length >= 3) index = commands[2];

        //发送资源信息
        if (index.equals("1"))
            result = getUserInfo(targetId, S.get("info.resource"));

        //发送打卡信息
        if (index.equals("2"))
            result = getUserInfo(targetId, S.get("info.attendance"));

        //发送消息
        mirai.sendMessage(result);
    }

    /**
     * 获取用户信息字符串数据
     *
     * @param targetId 目标QQ号
     * @param content  欲填充的字符串
     * @return 返回结果字符串
     */
    public String getUserInfo(long targetId, String content) {
        int year = Integer.parseInt(Smart.getDate("yyyy"));
        int month = Integer.parseInt(Smart.getDate("MM"));

        User userTarget = new User(targetId);

        //键值数据表
        Map<String, Object> data = new HashMap<>();

        String name = S.get("system.you");

        //如果 targetId 和 fromId 不同则获取目标用户的名字
        if (targetId != fromId)
            name = mirai.getUserNicknameFromGroup(targetId, fromGroup);

        data.put("name", name);
        data.put("score", userTarget.getScore());

        //添加道具数量
        data.put("candle", userTarget.getItemAmount(User.Field.candle));
        data.put("rocker", userTarget.getItemAmount(User.Field.rocker));
        data.put("dice", userTarget.getItemAmount(User.Field.dice));
        data.put("paper", userTarget.getItemAmount(User.Field.paper));
        data.put("lucky", userTarget.getItemAmount(User.Field.lucky));

        //将打卡数据添加至数据表
        String[] types = new String[]{"wake", "nap", "sleep", "run"};
        for (String type : types) {
            String shortTime = Smart.getShortTime(userTarget.getLastSeenTime(type));
            boolean isRemindOn = userTarget.isRemindOn(type);
            long total = userTarget.getTotal(type);
            long totalMonth = userTarget.getHistoryTotalAtMonth(type, year, month);
            long totalContinuous = userTarget.getHistoryContinuousTotalInThisMonth(type);

            String remind = S.get("system.turn" + (isRemindOn ? "On" : "Off"));

            data.put(type + "_remind", remind);
            data.put(type + "_short", shortTime);
            data.put(type + "_total", total);
            data.put(type + "_total_month", totalMonth);
            data.put(type + "_total_continuous", totalContinuous);
        }

        //添加跑步步数相关数据
        Map<String, Long> stepMap = userTarget.getStep();
        long stepMax = stepMap.get("max");
        long stepLast = stepMap.get("last");
        data.put("run_step_max", stepMax);
        data.put("run_step_last", stepLast);

        String result = content;
        result = Smart.replaceAllTheFields(result, data);

        return result;
    }
}
