package ink.wsm.mirai.daily_attendance_v2.cores.cmds;

import ink.wsm.mirai.daily_attendance_v2.cores.S;
import ink.wsm.mirai.daily_attendance_v2.cores.data.General;
import ink.wsm.mirai.daily_attendance_v2.cores.data.User;
import ink.wsm.mirai.daily_attendance_v2.utils.Mirai;
import net.mamoe.mirai.Bot;
import net.mamoe.mirai.event.Event;
import net.mamoe.mirai.message.data.MessageChain;

import java.util.Locale;

/**
 * 用户应用设定类
 */
public class UserSet {
    User user;

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

    public UserSet(Mirai mirai, Event event) {
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
    }

    /**
     * 返回用户设定的结果
     */
    public String process() {
        String[] commands = message.split(" ");

        //如果长度小于等于 2 则发送菜单
        if (commands.length <= 2)
            return S.get("set.menu");

        //设置类型
        String type = commands[2];

        //类型为提醒：
        if (type.equals("remind"))
            return remind();

        //类型为运动打卡检测：
        if (type.equals("run"))
            return runCheck();

        //没有进入到任意类型则返回菜单
        return S.get("set.menu");
    }

    /**
     * 处理打卡提醒的数据，返回结果字符串
     */
    private String remind() {
        String[] commands = message.split(" ");

        //如果命令长度不为 5 则返回菜单
        if (commands.length != 5)
            return S.get("set.menu");

        //转小写
        message = message.toLowerCase(Locale.ROOT);

        String type = commands[3];
        String statusText = commands[4];

        //如果打卡类型无效则返回
        if (!"wake,nap,sleep".contains(type))
            return S.get("set.failUnknownType");

        //如果状态无效则返回
        if (!"on,off".contains(statusText))
            return S.get("set.failUnknownStatus");

        boolean status = statusText.equals("on");
        long end = General.getLong(type + General.Field.end);

        //提交更改
        user.setValue(type + User.Field.remindStatus, status);

        return S.get("set.successRemind" + (status ? "On" : "Off"))
                .replace("{type}", S.get(type + ".name"))
                .replace("{hour}", (end - 1) + "");
    }

    /**
     * 处理运动打卡的数据，返回结果字符串
     */
    private String runCheck() {
        String[] commands = message.split(" ");

        //如果命令长度不为 4 则返回菜单
        if (commands.length != 4)
            return S.get("set.menu");

        //转小写
        message = message.toLowerCase(Locale.ROOT);

        String type = "run";
        String statusText = commands[3];

        //如果状态无效则返回
        if (!"on,off".contains(statusText))
            return S.get("set.failUnknownStatus");

        //status 为 true 则表示开启检测，否则关闭检测；isClosed 为 true 表示关闭检测，否则开启检测
        boolean status = statusText.equals("on");
        boolean isClosed = !status;

        //提交更改
        user.setValue(type + User.Field.isClosed, isClosed);

        return S.get("set.successRunCheck" + (status ? "On" : "Off"))
                .replace("{type}", S.get(type + ".name"));
    }
}
