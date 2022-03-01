package ink.wsm.mirai.daily_attendance_v2;

import ink.wsm.mirai.daily_attendance_v2.utils.Mirai;
import net.mamoe.mirai.Bot;
import net.mamoe.mirai.event.EventHandler;
import net.mamoe.mirai.event.SimpleListenerHost;
import net.mamoe.mirai.event.events.MessageEvent;
import net.mamoe.mirai.event.events.MessageRecallEvent;
import net.mamoe.mirai.event.events.NudgeEvent;
import net.mamoe.mirai.message.data.MessageChain;

public class Listener extends SimpleListenerHost {
    /**
     * 收到全局消息事件
     */
    @EventHandler
    public void onMessageEvent(MessageEvent event) {
        String className = event.getSubject().getClass().toString();
        boolean isGroup = className.contains("GroupImpl");

        Bot bot = event.getBot();
        long fromId = event.getSender().getId();
        long fromGroup = isGroup ? event.getSubject().getId() : 0;
        MessageChain source = event.getMessage();
        Mirai mirai = new Mirai(bot, fromId, fromGroup, source);
        mirai.transMain = event.getMessage().contentToString();
        mirai.transMinor = event.getMessage().serializeToMiraiCode();
        mirai.transMessage = event.getMessage();

        Process process = new Process(mirai, event);
        process.commandProcess();
    }

    //消息撤回事件
    @EventHandler
    public void onMessageRecallEvent(MessageRecallEvent event) {
        Bot bot = event.getBot();
        long fromId = event.getAuthorId();
        Mirai mirai = new Mirai(bot, fromId, 0, null);
        Process process = new Process(mirai, event);

        //分析晚安打卡是否产生活跃的状态
        process.analyzingSleepStatus("recall");
    }

    /**
     * 收到戳一戳事件
     */
    @EventHandler
    public void onNudgeEvent(NudgeEvent event) {
        Bot bot = event.getBot();
        long fromId = event.getFrom().getId();
        Mirai mirai = new Mirai(bot, fromId, 0, null);
        Process process = new Process(mirai, event);

        //分析晚安打卡是否产生活跃的状态
        process.analyzingSleepStatus("nudge");
    }
}
