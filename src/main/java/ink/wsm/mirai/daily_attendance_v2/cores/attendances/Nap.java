package ink.wsm.mirai.daily_attendance_v2.cores.attendances;

import ink.wsm.mirai.daily_attendance_v2.cores.S;
import ink.wsm.mirai.daily_attendance_v2.cores.cmds.Use;
import ink.wsm.mirai.daily_attendance_v2.cores.data.General;
import ink.wsm.mirai.daily_attendance_v2.cores.data.User;
import ink.wsm.mirai.daily_attendance_v2.utils.Mirai;
import ink.wsm.mirai.daily_attendance_v2.utils.Smart;
import ink.wsm.mirai.daily_attendance_v2.utils.Yamler;
import net.mamoe.mirai.Bot;
import net.mamoe.mirai.event.Event;
import net.mamoe.mirai.message.data.MessageChain;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 午睡打卡相关
 */
public class Nap {
    String type = "nap";

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

    public Nap(Mirai mirai, Event event) {
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
     * 返回进行午睡打卡的结果字符串
     */
    public String process() {
        //进行有效性请求
        Map<String, Object> isValidMap = Attendance.checkIsJoined(fromId, type);

        //如果 valid 为 false 则表示请求无效，返回失败 reason
        if (!(boolean) isValidMap.get("valid")) return isValidMap.get("reason") + "";

        long endHour = General.getLong(type + General.Field.end);

        //数据表
        Map<String, Object> data = new HashMap<>();
        data.put("end", endHour);

        //抽取奖励
        Map<String, Object> awardMap = rollTheAward();
        String awardId = awardMap.get("id") + "";//奖励ID
        String awardName = awardMap.get("name") + "";//奖励名字
        String awardUnit = awardMap.get("unit") + "";//奖励单位
        int awardAmount = Integer.parseInt(awardMap.get("amount") + "");//奖励数量
        int awardProb = Integer.parseInt(awardMap.get("prob") + "");//奖励概率
        int awardRarity = Integer.parseInt(awardMap.get("rarity") + "");//奖励稀有度

        //奖励翻倍
        awardAmount = (int) Use.useDiceAndResponse(fromId, awardAmount);

        data.put("unit", awardUnit);
        data.put("resource", awardName);
        data.put("amount", awardAmount);
        data.put("prob", awardProb);

        //获取指定凭证数量绑定的 lucky 文本
        List<?> luckyList = S.getList("lucky");
        String lucky = String.valueOf(luckyList.get(awardRarity));
        data.put("lucky", lucky);

        //更新用户的打卡信息
        Attendance.updateUserData(fromId, type);

        //如果奖励id为凭证则给予用户凭证
        if (awardId.equals("score"))
            user.updateScore(awardAmount);

        //如果奖励id为蜡烛则给予用户夜光蜡烛
        if (awardId.equals("candle"))
            user.updateItem(User.Field.candle, awardAmount);

        String result = S.get(type + ".success");
        result = Smart.replaceAllTheFields(result, data);
        return result;
    }

    /**
     * 抽取奖励
     *
     * @return 返回 Map as String, Object
     * (id: 奖励编号；amount: 数量；name: 奖励名字；unit: 奖励单位；
     * prob: 抽取概率；rarity: 稀有度(越大越稀有, 0开始))
     */
    public Map<String, Object> rollTheAward() {
        String fieldAwards = type + General.Field.awards;

        Map<String, Object> result;
        int random = RandomUtils.nextInt(0, 100);

        Set<?> awardsKey = General.getKeysList(fieldAwards);
        for (Object awardKey : awardsKey) {
            String startText = StringUtils.substringBefore(awardKey + "", "-");
            String endText = StringUtils.substringAfter(awardKey + "", "-");
            long start = Smart.objectToLong(startText, -1);
            long end = Smart.objectToLong(endText, -1);

            //如果 start 或 end 为 -1 则表示无法识别目标奖励数据，跳到下一个
            if (start == -1 || end == -1) continue;

            //如果随机值小于 start 或大于等于 end 则表示不在次范围内，跳到下一个
            if (random < start || random >= end) continue;

            String fieldAwardData = fieldAwards + "." + awardKey; // nap.awards.0-100

            result = inputTheRollData(fieldAwardData, start, end);

            return result;
        }

        //如果未找到目标奖励则使用 default 的奖励数据
        String fieldAwardData = fieldAwards + ".default"; // nap.awards.default

        result = inputTheRollData(fieldAwardData, 0, 100);

        return result;
    }

    /**
     * 插入奖励结果数据到 Map as String, Object
     *
     * @param fieldAwardData 奖励数据的路径（nap.awards.0-100）
     * @param start          起始值
     * @param end            结束值
     * @return 返回 Map
     */
    private Map<String, Object> inputTheRollData(String fieldAwardData, long start, long end) {
        String id = General.getString(fieldAwardData + ".id");//奖励ID
        String name = S.get("system.resource." + id);//奖励名字
        String unitName = S.get("system.unit." + id);//奖励单位
        long amount = General.getLong(fieldAwardData + ".amount");//奖励数量
        long rarity = General.getLong(fieldAwardData + ".rarity");//奖励稀有度
        long prob = end - start;//奖励获取概率

        Map<String, Object> result = new HashMap<>();
        result.put("id", id);
        result.put("amount", amount);
        result.put("rarity", rarity);
        result.put("prob", prob);
        result.put("name", name);
        result.put("unit", unitName);

        return result;
    }
}
