package ink.wsm.mirai.daily_attendance_v2.cores.cmds;

import ink.wsm.mirai.daily_attendance_v2.cores.S;
import ink.wsm.mirai.daily_attendance_v2.cores.data.User;
import ink.wsm.mirai.daily_attendance_v2.utils.Mirai;
import ink.wsm.mirai.daily_attendance_v2.utils.Smart;
import ink.wsm.mirai.daily_attendance_v2.utils.Yamler;
import net.mamoe.mirai.Bot;
import net.mamoe.mirai.event.Event;
import net.mamoe.mirai.message.data.MessageChain;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 排行榜相关
 */
public class Rank {
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

    public Rank(Mirai mirai, Event event) {
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
     * 返回结果字符串
     */
    public String process() {
        int index = -1;
        String indexText = StringUtils.substringAfterLast(message, " ");

        //检测是否为数字
        if (Smart.isNumeric(indexText))
            index = Integer.parseInt(indexText);

        String typeName = S.get("rank.menuMap." + index);
        String unitPath = "system.unit.";

        //发送凭证排名
        if (index == 1)
            return processResult(User.Field.score, typeName, "score");

        //发送早起打卡次数排名
        if (index == 11)
            return processResult("wake" + User.Field.total,
                    typeName, "times");

        //发送午安打卡次数排名
        if (index == 12)
            return processResult("nap" + User.Field.total,
                    typeName, "times");

        //发送晚安打卡次数排名
        if (index == 13)
            return processResult("sleep" + User.Field.total,
                    typeName, "times");

        //发送运动打卡次数排名
        if (index == 14)
            return processResult("run" + User.Field.total,
                    typeName, "times");

        //发送运动打卡历史步数排名
        if (index == 21)
            return processResult("run" + User.Field.stepMax,
                    typeName, "step");

        //发送运动打卡上次步数排名
        if (index == 22)
            return processResult("run" + User.Field.stepLast,
                    typeName, "step");

        //不存在任何类型，发送菜单
        return menu();
    }

    /**
     * 发送菜单字符串
     */
    private String menu() {
        StringBuilder list = new StringBuilder();

        Map<Object, Object> menuMap = S.getMap("rank.menuMap");
        menuMap.forEach((key, value) -> list.append(key).append(". ").append(value).append("\n"));

        return S.get("rank.menu")
                .replace("{list}", list);
    }

    /**
     * 获取前 8 名用户指定资源数量的排行字符串
     *
     * @param path     欲排序的路径
     * @param typeName 排名类型（如“用户所持凭证排名”）
     * @param unitId   资源单位ID
     */
    private String processResult(String path, String typeName, String unitId) {
        List<Map<String, Long>> lists = getRankList(path, 8);

        String unitPath = "system.unit." + unitId;
        String unit = S.get(unitPath);

        String listText = getListResult(lists, unit, fromId);

        return S.get("rank.result")
                .replace("{name}", typeName)
                .replace("{list}", listText);
    }

    /**
     * 获取指定排名字符串列表
     *
     * @param lists    List as Map 的排名列表数据
     * @param unit     资源单位
     * @param targetId 目标QQ号（添加“你在此”表情）
     * @return 返回结果字符串（不包括消息头）
     */
    private String getListResult(List<Map<String, Long>> lists, String unit, long targetId) {
        StringBuilder listText = new StringBuilder();

        int index = 1; //用户排名
        long beforeLastPoint = 0; //前一名用户的点数数量（用以比较）

        for (Map<String, Long> list : lists) {
            long id = list.get("id");
            long amount = list.get("amount");
            String name = mirai.getUserNicknameFromGroup(id, fromGroup);

            //添加皇冠 Emoji（第一名）
            String crown = index == 1 ? S.get("system.emoji.crown") + " | " : "";

            //添加举手 Emoji（你在这）
            String youAreHere = id == targetId ? S.get("system.emoji.handsUp") + " | " : "";

            String temp = S.get("rank.list")
                    .replace("{index}", index + "")
                    .replace("{amount}", amount + "")
                    .replace("{name}", name)
                    .replace("{unit}", unit)
                    .replace("{you}", youAreHere)
                    .replace("{crown}", crown);
            listText.append(temp);

            //如果前一名用户的点数不等于当前用户的点数，则 index+1
            if (beforeLastPoint != amount) index++;

            //更新前一名用户的点数为当前用户的点数
            beforeLastPoint = amount;
        }

        return listText.toString();
    }

    /**
     * 获取指定路径的所有用户的排名列表
     *
     * @param path 路径
     * @return 返回 List as Map => id(QQ号), amount(数量)
     */
    private List<Map<String, Long>> getRankList(String path, int limit) {
        List<Map<String, Long>> result = new ArrayList<>();
        List<Map<String, Object>> lists = Yamler.sortWithFolder(
                S.Data.userFolder, path, "desc", limit, 0);

        for (Map<String, Object> list : lists) {
            Map<String, Long> temp = new HashMap<>();
            String key = list.get("key") + "";
            String value = list.get("value") + "";

            //如果 k, v 为数字则填入到 id, amount 否则填入 0
            long id = Smart.isNumeric(key) ? Long.parseLong(key) : 0;
            long amount = Smart.isNumeric(value) ? Long.parseLong(value) : 0;

            //填入到 temp => id, amount
            temp.put("id", id);
            temp.put("amount", amount);

            //填入到 result => temp
            result.add(temp);
        }

        return result;
    }
}
