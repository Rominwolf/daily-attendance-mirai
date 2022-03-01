package ink.wsm.mirai.daily_attendance_v2.cores;

import ink.wsm.mirai.daily_attendance_v2.cores.data.Group;
import ink.wsm.mirai.daily_attendance_v2.cores.data.User;
import ink.wsm.mirai.daily_attendance_v2.utils.Mirai;
import ink.wsm.mirai.daily_attendance_v2.utils.Smart;
import net.mamoe.mirai.Bot;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.RandomUtils;

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
     * 处理指定打卡类型的开启打卡提醒的列表的消息的发送
     *
     * @param type    打卡类型
     * @param isGroup 是否为群聊模式
     */
    private static void processTimerInner(String type, boolean isGroup) {
        Bot bot = Mirai.getBot();
        List<Long> lists = getListAboutRemindOn(type, isGroup);
        Mirai mirai = new Mirai(bot, 0, 0, null);

        String name = S.get(type + ".name");
        String cmd = getAttendanceCommand(type);

        for (long targetId : lists) {
            String result = getRandomRemindString(isGroup)
                    .replace("{type}", name)
                    .replace("{cmd}", cmd);

            //进行变量的替换
            result = varsReplace(result, type, mirai, isGroup);

            //如果为群聊则更新 fromGroup 否则更新 fromId
            if (isGroup) {
                mirai.fromId = 0;
                mirai.fromGroup = targetId;
            } else {
                mirai.fromGroup = 0;
                mirai.fromId = targetId;
            }

            mirai.sendMessage(result);
        }
    }

    /**
     * 进行变量替换
     *
     * @param content 文案内容
     * @param type    打卡类型
     * @param mirai   Mirai
     * @param isGroup 是否为群聊
     * @return 返回替换后的文案
     */
    private static String varsReplace(String content, String type, Mirai mirai, boolean isGroup) {
        long targetId = mirai.fromId;
        if (isGroup) targetId = mirai.fromGroup;

        //替换为用户昵称，群聊下为空
        String nick = "";
        if (!isGroup) nick = mirai.getUserNicknameFromGroup(targetId, targetId);
        content = content.replace("{nick}", nick);

        //替换为用户累计打卡的次数，群聊下为0
        long total = 0;
        if (!isGroup) total = new User(targetId).getTotal(type);
        content = content.replace("{total}", total + "");

        //替换为用户连续打卡的次数，群聊下为0
        long cont = 0;
        if (!isGroup) cont = new User(targetId).getHistoryContinuousTotalInThisMonth(type);
        content = content.replace("{cont}", cont + "");

        return content;
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

    /**
     * 获得指定打卡类型的指定条件的一条随机提醒打卡的字符串
     *
     * @param isGroup 是否为群聊
     * @return 返回随机提醒消息（默认：""，需要自行替换{type}, {cmd}, {nick}）
     */
    private static String getRandomRemindString(boolean isGroup) {
        String from = isGroup ? "group" : "private";
        List<?> contents = S.getList("remind." + from + ".list");
        int index = RandomUtils.nextInt(0, contents.size());
        return contents.get(index) + "";
    }
}
