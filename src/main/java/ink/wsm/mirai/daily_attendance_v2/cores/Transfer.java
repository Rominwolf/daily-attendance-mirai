package ink.wsm.mirai.daily_attendance_v2.cores;

import ink.wsm.mirai.daily_attendance_v2.cores.data.User;
import ink.wsm.mirai.daily_attendance_v2.utils.JsonProper;
import ink.wsm.mirai.daily_attendance_v2.utils.Mirai;
import net.mamoe.mirai.Bot;
import net.mamoe.mirai.event.Event;
import net.mamoe.mirai.message.data.MessageChain;

/**
 * 旧版用户数据转移类
 */
public class Transfer {
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

    public Transfer(Mirai mirai, Event event) {
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
     * 进行数据的转移
     */
    public void process() {
        boolean isTransferred = user.isTransferred();

        //已转移数据，返回
        if (isTransferred) return;

        //data\ink.wsm.mirai.daily_attendance.DailyAttendancePlugin
        //data\ink.wsm.mirai.daily_attendance_v2.plugin

        String oldDataFolder = S.Data.dataFolder
                .replace("_v2.plugin", ".DailyAttendancePlugin");

        String oldUserDataFile = oldDataFolder + "user_" + fromId + ".json";

        JsonProper jsonProper = new JsonProper(oldUserDataFile);

        user.updateScore(jsonProper.getIntValue("score"));

        String[] types = new String[]{"wake", "nap", "sleep"};
        for (String type : types) {
            long lastSeenDate = jsonProper.getLongValue(type + "_last_seen_date");
            user.updateLastSeenDate(type, lastSeenDate);

            int total = jsonProper.getIntValue(type + "_total");
            user.setValue(type + User.Field.total, total);

            String lastSeenTime = jsonProper.getStringValue(type + "_last_seen_time");
            user.setLastSeenTime(type, lastSeenTime);
        }

        user.setValue(User.Field.isTransferred, true);
    }
}
