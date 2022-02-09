package ink.wsm.mirai.daily_attendance_v2.cores;

import ink.wsm.mirai.daily_attendance_v2.cores.data.Group;
import ink.wsm.mirai.daily_attendance_v2.cores.data.User;
import ink.wsm.mirai.daily_attendance_v2.utils.Mirai;
import ink.wsm.mirai.daily_attendance_v2.utils.Smart;
import net.mamoe.mirai.Bot;
import org.apache.commons.io.FilenameUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 提醒打卡相关类
 */
public class Remind {
    /**
     * 时钟 | 处理指定打卡类型的开启打卡提醒的列表的消息的发送
     *
     * @param type 打卡类型
     */
    public static void processTimer(String type) {
        Bot bot = Mirai.getBot();

        //如果当前没有 bot 则返回
        if (bot == null) return;

        processTimerInner(type, false);
        processTimerInner(type, true);
    }

    /**
     * 处理指定打卡类型的开启打卡提醒的列表的消息的发送（私聊版）
     *
     * @param type    打卡类型
     * @param isGroup 是否为群聊模式
     */
    private static void processTimerInner(String type, boolean isGroup) {
        Bot bot = Mirai.getBot();
        List<Long> lists = getListAboutRemindOn(type, isGroup);

        String name = S.get(type + ".name");
        String cmd = getAttendanceCommand(type);

        String fieldRemindType = isGroup ? "group" : "private";

        String result = S.get("remind." + fieldRemindType + ".msg")
                .replace("{type}", name)
                .replace("{cmd}", cmd);

        for (long targetId : lists) {
            Mirai mirai = new Mirai(bot, 0, 0, null);

            //如果为群聊则更新 fromGroup 否则更新 fromId
            if (isGroup) mirai.fromGroup = targetId;
            else mirai.fromId = targetId;

            mirai.sendMessage(result);
        }
    }

    /**
     * 获取指定打卡类型的打卡命令
     *
     * @param type 打卡类型
     * @return 返回命令
     */
    private static String getAttendanceCommand(String type) {
        String cmd = S.Command.main;

        if (type.equals("wake")) cmd = S.Command.kaCN;
        if (type.equals("nap")) cmd = S.Command.kaCN;
        if (type.equals("sleep")) cmd = S.Command.kaCN;

        return cmd;
    }

    /**
     * 获取指定目录下所有开启指定打卡类型且当日没有参与打卡的打卡列表（用户）
     *
     * @param attendanceType 打卡类型
     * @param isGroup        是否为群模式
     * @return 返回可以发送提醒消息的列表
     */
    private static List<Long> getListAboutRemindOn(String attendanceType, boolean isGroup) {
        String path = S.Data.userFolder;
        if (isGroup) path = S.Data.groupFolder;

        ArrayList<String> files = Smart.getFiles(path);
        List<Long> result = new ArrayList<>();

        for (String filePath : files) {
            boolean isRemindOn;

            //检测不同聊天类型下目标群/用户是否开启了目标打卡类型的提醒功能
            if (isGroup) {
                isRemindOn = new Group(filePath).isRemindOn(attendanceType);
            } else {
                isRemindOn = new User(filePath).isRemindOn(attendanceType);
            }

            //如果该用户未开启每日打卡提醒则 continue
            if (!isRemindOn) continue;

            //是否参与了打卡
            boolean isJoined = false;

            //仅用户有效：检测目标用户是否参与了当天的目标打卡
            if (!isGroup)
                isJoined = new User(filePath).isJoined(attendanceType);

            //如果该用户今日已参与此打卡则 continue
            if (isJoined) continue;

            //文件名（不包括扩展名）
            String fileName = FilenameUtils.getBaseName(filePath);

            //如果目标文件名是数字则添加到待提醒列表
            if (Smart.isNumeric(fileName))
                result.add(Long.valueOf(fileName));
        }

        return result;
    }
}
