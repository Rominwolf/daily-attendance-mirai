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
}
