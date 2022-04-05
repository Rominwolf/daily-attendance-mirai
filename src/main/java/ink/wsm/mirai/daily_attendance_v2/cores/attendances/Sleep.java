package ink.wsm.mirai.daily_attendance_v2.cores.attendances;

import ink.wsm.mirai.daily_attendance_v2.cores.S;
import ink.wsm.mirai.daily_attendance_v2.cores.data.General;
import ink.wsm.mirai.daily_attendance_v2.cores.data.User;
import ink.wsm.mirai.daily_attendance_v2.utils.Mirai;
import ink.wsm.mirai.daily_attendance_v2.utils.Yamler;
import net.mamoe.mirai.Bot;
import net.mamoe.mirai.event.Event;
import net.mamoe.mirai.message.data.MessageChain;

import java.util.List;

/**
 * 晚安打卡相关类
 */
public class Sleep {
    String type = "sleep";

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

    public Sleep(Mirai mirai, Event event) {
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
     * 检查用户参与晚安打卡的状态，并返回相应结果
     * 如果参与了打卡则检测是否使用了蜡烛或发言次数
     *
     * @param activeType 活跃类型（talk, nudge, recall...）
     */
    public String checkSleepTalking(String activeType) {
        boolean isJoined = user.isJoinedSleep();

        //如果没有参与晚安打卡则返回
        if (!isJoined) return "";

        boolean isUseCandle = user.isUseItem(User.Field.candle);

        //如果已使用夜光蜡烛则返回
        if (isUseCandle) return "";

        //检测消息是否处于白名单消息内
        List<?> whitelistActives = General.getList(
                type + General.Field.whitelist + General.Field.active);
        for (Object whitelistActive : whitelistActives) {
            String content = whitelistActive + "";

            //如果该消息为白名单消息，则返回
            if (content.equals(message)) return "";
        }

        long talkTimes = user.updateTotalTalk(type, 1);

        //如果该用户之前已发言超过 3 次则直接返回
        if (talkTimes > 4) return "";

        //如果发言次数小于 3 次则返回警告字符串
        if (talkTimes < 4)
            return S.get(type + ".talkingWarn")
                    .replace("{active}", S.get("system.active." + activeType))
                    .replace("{times}", (4 - talkTimes) + "");

        //否则打卡失败：已加入标记为false、删除全局打卡列表中目标用户
        user.setValue(type + User.Field.isJoined, false);
        global.setString(type + ".list", fromId, -1, ",");

        return S.get(type + ".failed");
    }
}
