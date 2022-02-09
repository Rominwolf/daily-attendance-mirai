package ink.wsm.mirai.daily_attendance_v2.cores.cmds;

import ink.wsm.mirai.daily_attendance_v2.cores.S;
import ink.wsm.mirai.daily_attendance_v2.cores.Terminal;
import ink.wsm.mirai.daily_attendance_v2.cores.data.General;
import ink.wsm.mirai.daily_attendance_v2.cores.data.User;
import ink.wsm.mirai.daily_attendance_v2.utils.Mirai;
import ink.wsm.mirai.daily_attendance_v2.utils.Smart;
import ink.wsm.mirai.daily_attendance_v2.utils.Yamler;
import net.mamoe.mirai.Bot;
import net.mamoe.mirai.event.Event;
import net.mamoe.mirai.message.data.MessageChain;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * 凭证兑换成第三方资金相关类
 */
public class Exchange {
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

    public Exchange(Mirai mirai, Event event) {
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
     * 处理兑换相关命令并发送消息
     */
    public void process() {
        //发送菜单
        if (message.equals(S.Command.exch)) {
            mirai.sendMessage(getMenuString());
            return;
        }

        String result = processExchange();
        mirai.sendMessage(result);
    }

    /**
     * 获取菜单
     *
     * @return 返回字符串
     */
    private String getMenuString() {
        Map<String, String> rateMap = getRate();
        String rate = rateMap.get("rate");
        int rateScore = Integer.parseInt(rateMap.get("score"));
        int rateMoney = Integer.parseInt(rateMap.get("money"));

        int score = 10;
        int money = (score / rateScore) * rateMoney;

        return S.get("exchange.menu")
                .replace("{name}", S.get("system.resource.exchange"))
                .replace("{unit}", S.get("system.unit.exchange"))
                .replace("{rate}", rate)
                .replace("{money}", money + "");
    }

    /**
     * 获取比率信息
     *
     * @return 返回 Map as String(rate, score, money), String
     */
    private Map<String, String> getRate() {
        String rate = General.getString("exchange.rate");
        String rateScore = StringUtils.substringBefore(rate, ":");
        String rateMoney = StringUtils.substringAfter(rate, ":");

        Map<String, String> result = new HashMap<>();
        result.put("rate", rate);
        result.put("score", rateScore);
        result.put("money", rateMoney);

        return result;
    }

    /**
     * 处理兑换信息
     *
     * @return 返回兑换结果字符串
     */
    private String processExchange() {
        String amountText = StringUtils.substringAfterLast(message, " ");
        int amount = 1;

        //如果 amountText 为数字则更新 amount
        if (Smart.isNumeric(amountText))
            amount = Integer.parseInt(amountText);

        //欲兑换的数量过少
        if (amount < 1)
            return S.get("exchange.tooSmall");

        Map<String, String> rateMap = getRate();
        int rateScore = Integer.parseInt(rateMap.get("score"));
        int rateMoney = Integer.parseInt(rateMap.get("money"));

        long score = user.getScore();
        int money = (amount / rateScore) * rateMoney;

        //凭证不够则返回
        if (amount > score)
            return S.get("snow.tooLarge")
                    .replace("{score}", amount + "")
                    .replace("{money}", money + "")
                    .replace("{name}", S.get("system.resource.exchange"))
                    .replace("{unit}", S.get("system.unit.exchange"));

        String command = General.getString("exchange.command")
                .replace("{targetId}", fromId + "")
                .replace("{amount}", money + "");
        boolean isSucceed = Terminal.requestCommand(command);

        //如果提交命令行失败则返回
        if (!isSucceed)
            return S.get("exchange.failed");

        //减少用户的凭证数量
        user.updateScore(-amount);

        return S.get("exchange.success")
                .replace("{score}", amount + "")
                .replace("{money}", money + "")
                .replace("{name}", S.get("system.resource.exchange"))
                .replace("{unit}", S.get("system.unit.exchange"));
    }
}
