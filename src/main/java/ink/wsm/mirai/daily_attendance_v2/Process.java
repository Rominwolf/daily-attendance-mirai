package ink.wsm.mirai.daily_attendance_v2;

import ink.wsm.mirai.daily_attendance_v2.cores.Remind;
import ink.wsm.mirai.daily_attendance_v2.cores.S;
import ink.wsm.mirai.daily_attendance_v2.cores.Transfer;
import ink.wsm.mirai.daily_attendance_v2.cores.attendances.Attendance;
import ink.wsm.mirai.daily_attendance_v2.cores.attendances.Nap;
import ink.wsm.mirai.daily_attendance_v2.cores.attendances.Run;
import ink.wsm.mirai.daily_attendance_v2.cores.attendances.Sleep;
import ink.wsm.mirai.daily_attendance_v2.cores.cmds.*;
import ink.wsm.mirai.daily_attendance_v2.cores.data.General;
import ink.wsm.mirai.daily_attendance_v2.utils.Mirai;
import ink.wsm.mirai.daily_attendance_v2.utils.Smart;
import net.mamoe.mirai.Bot;
import net.mamoe.mirai.event.Event;
import net.mamoe.mirai.message.data.MessageChain;
import org.apache.commons.lang3.StringUtils;

public class Process {
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

    public Process(Mirai mirai, Event event) {
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

        // 删除尖括号和、大括号和首尾空格
        String[] deleteList = new String[]{"<", ">", "[", "]"};
        String[] deletedList = new String[]{"", "", "", ""};
        message = StringUtils.replaceEach(message, deleteList, deletedList).trim();
    }

    /**
     * 处理命令
     */
    public Object commandProcess() {
        //testing
        if (message.equals(S.Command.testing))
            return testing();

        // ---------- 命令相关 ----------

        //发送应用菜单
        if (message.equals(S.Command.main))
            return sendMenu();

        //查询用户信息
        if (message.startsWith(S.Command.info))
            return sendInfo();

        //用户使用道具
        if (message.startsWith(S.Command.use))
            return sendUse();

        //凭证兑换第三方货币
        if (message.startsWith(S.Command.exch))
            return sendExch();

        //排行榜
        if (message.startsWith(S.Command.rank))
            return sendRank();

        //用户设定菜单
        if (message.startsWith(S.Command.set))
            return sendSet();

        //群聊设定菜单
        if (message.startsWith(S.Command.put))
            return sendPut();

        //关于本应用
        if (message.startsWith(S.Command.about))
            return sendAbout();

        // ---------- 打卡相关 ----------

        //参与快速打卡
        if (message.equals(S.Command.kaCN) || message.equals(S.Command.ka))
            return sendAttendanceAuto();

        //参与早起打卡
        if (message.equals(S.Command.wake))
            return sendAttendance("wake");

        //参与午睡打卡
        if (message.equals(S.Command.nap))
            return sendAttendance("nap");

        //参与晚安打卡
        if (message.equals(S.Command.sleep))
            return sendAttendance("sleep");

        //参与运动打卡
        if (message.equals(S.Command.run))
            return sendAttendance("run");

        //晚安打卡状态分析
        analyzingSleepStatus("talk");

        return null;
    }

    private Object testing() {
        //Run.timerProcessCheck();
        //Run.output(mirai, event);
        //Attendance.processAttendanceTimer("sleep");
        Remind.processTimer("nap");
//        Nap nap = new Nap(mirai, event);
//        nap.rollTheAward().forEach(Mirai::newLog);
        return null;
    }

    /**
     * 发送应用菜单
     */
    private Object sendMenu() {
        transferTheData();

        String result = S.get("menu.main");

        //群聊模式则增加 put 命令
        if (isGroup) result += S.get("menu.put");

        return mirai.sendMessage(result);
    }

    /**
     * 发送关于本应用
     */
    private Object sendAbout() {
        mirai.sendMessage(S.get("about"));
        return true;
    }

    /**
     * 发送用户信息菜单
     */
    private Object sendInfo() {
        transferTheData();

        Info info = new Info(mirai, event);
        info.process();

        return true;
    }

    /**
     * 发送用户使用道具菜单
     */
    private Object sendUse() {
        transferTheData();

        Use use = new Use(mirai, event);
        return mirai.sendMessage(use.process());
    }

    /**
     * 发送用户兑换菜单
     */
    private Object sendExch() {
        transferTheData();

        Exchange exchange = new Exchange(mirai, event);
        exchange.process();
        return true;
    }

    /**
     * 发送排行榜菜单
     */
    private Object sendRank() {
        transferTheData();

        Rank rank = new Rank(mirai, event);
        return mirai.sendMessage(rank.process());
    }

    /**
     * 发送用户设定菜单
     */
    private Object sendSet() {
        transferTheData();

        UserSet userSet = new UserSet(mirai, event);
        return mirai.sendMessage(userSet.process());
    }

    /**
     * 发送群设定菜单
     */
    private Object sendPut() {
        transferTheData();

        GroupPut groupPut = new GroupPut(mirai, event);
        return mirai.sendMessage(groupPut.process());
    }

    /**
     * 发送打卡参与结果
     *
     * @param type 打卡类型
     */
    private Object sendAttendance(String type) {
        transferTheData();

        //如果打卡类型为午睡打卡则单独处理
        if (type.equals("nap")) {
            Nap nap = new Nap(mirai, event);
            mirai.sendMessage(nap.process());
            return true;
        }

        //如果打卡类型为运动打卡则单独处理
        if (type.equals("run")) {
            Run run = new Run(mirai, event);
            run.process();
            return true;
        }

        Attendance attendance = new Attendance(mirai, event);
        mirai.sendMessage(attendance.process(type));

        return true;
    }

    /**
     * 发送打卡参与结果（自动判定时间）
     */
    private Object sendAttendanceAuto() {
        transferTheData();

        int hour = Smart.getHour();

        String[] types = new String[]{"wake", "nap", "sleep"};

        //循环当前时间，查看是否允许打卡类型
        for (String type : types) {
            long startHour = General.getLong(type + ".start");
            long endHour = General.getLong(type + ".end");

            //如果当前时间未到 startHour 或已过 endHour 则 continue
            if (hour < startHour || hour >= endHour) continue;

            sendAttendance(type);
            return true;
        }

        //如果循环未匹配则返回当前时间段没有打卡任务的消息
        return mirai.sendMessage(S.get("doNotHaveTasks"));
    }

    /**
     * 分析晚安打卡的状态
     *
     * @param activeType 活跃类型（talk, recall, nudge）
     */
    public void analyzingSleepStatus(String activeType) {
        Sleep sleep = new Sleep(mirai, event);
        mirai.sendMessage(sleep.checkSleepTalking(activeType));
    }

    /**
     * 进行用户数据的转移
     */
    private void transferTheData() {
        Transfer transfer = new Transfer(mirai, event);
        transfer.process();
    }
}
