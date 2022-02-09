package ink.wsm.mirai.daily_attendance_v2.cores.cmds;

import ink.wsm.mirai.daily_attendance_v2.cores.S;
import ink.wsm.mirai.daily_attendance_v2.cores.data.User;
import ink.wsm.mirai.daily_attendance_v2.utils.Mirai;
import net.mamoe.mirai.Bot;
import net.mamoe.mirai.event.Event;
import net.mamoe.mirai.message.data.MessageChain;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * 道具相关类
 */
public class Use {
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

    User user;

    public Use(Mirai mirai, Event event) {
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
     * 发送使用道具的结果字符串
     *
     * @return 返回结果字符串
     */
    public String process() {
        String index = StringUtils.substringAfterLast(message, " ");

        //如果没有序号则发送菜单
        if (message.equals(S.Command.use) || index == null)
            return S.get("use.menu");

        //使用夜光蜡烛
        if (index.equals("1"))
            return useItem(User.Field.candle);

        //使用翻倍骰子
        if (index.equals("2"))
            return useItem(User.Field.dice);

        //没有目标序号
        return S.get("use.notExist");
    }

    /**
     * 使用指定道具
     *
     * @return 返回结果字符串
     */
    private String useItem(String id) {
        boolean isUsed = user.isUseItem(id);

        //今夜已使用翻倍骰子
        if (isUsed)
            return S.get("use." + id + ".alreadyUsed");

        long amount = user.getItemAmount(id);

        //数量不够
        if (amount < 1)
            return S.get("use." + id + ".notEnough");

        //更新用户数据：道具数量-1、设置已使用本道具
        user.updateItem(id, -1);
        user.setUseItem(id, true);

        return S.get("use." + id + ".success");
    }

    /**
     * 获取翻倍骰子的倍数
     *
     * @return 返回倍数（1, 2, 3）
     */
    private static int rollTheDice() {
        int rand = RandomUtils.nextInt(0, 100);
        int result = 1;

        if (rand >= 60 && rand < 95) result = 2;
        if (rand >= 95) result = 3;

        return result;
    }

    /**
     * 检测是否使用了翻倍骰子且返回使用后的数量并更新道具状态
     *
     * @param fromId QQ号
     * @param amount 原数量
     * @return 返回翻倍后的数量（如果使用了道具）
     */
    public static long useDiceAndResponse(long fromId, long amount) {
        User user = new User(fromId);

        //如果未使用翻倍骰子则返回原数量
        if (!user.isUseItem(User.Field.dice)) return amount;

        //进行翻倍
        int roll = rollTheDice();
        amount *= roll;

        //设置使用骰子状态
        user.setUseItem(User.Field.dice, false);

        return amount;
    }
}
