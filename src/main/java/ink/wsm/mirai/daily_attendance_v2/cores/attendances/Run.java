package ink.wsm.mirai.daily_attendance_v2.cores.attendances;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import ink.wsm.mirai.daily_attendance_v2.cores.S;
import ink.wsm.mirai.daily_attendance_v2.cores.cmds.Use;
import ink.wsm.mirai.daily_attendance_v2.cores.data.General;
import ink.wsm.mirai.daily_attendance_v2.cores.data.Global;
import ink.wsm.mirai.daily_attendance_v2.cores.data.User;
import ink.wsm.mirai.daily_attendance_v2.utils.Mirai;
import ink.wsm.mirai.daily_attendance_v2.utils.Smart;
import ink.wsm.mirai.daily_attendance_v2.utils.Yamler;
import net.mamoe.mirai.Bot;
import net.mamoe.mirai.event.Event;
import net.mamoe.mirai.message.data.MessageChain;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 运动打卡相关
 */
public class Run {
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

    public Run(Mirai mirai, Event event) {
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
     * 处理发送运动打卡命令后的消息
     */
    public void process() {
        String result = S.get("run.intro");
        mirai.sendMessage(result);
    }

    /**
     * 检测运动打卡步数信息
     */
    public static void timerProcessCheck() {
        Bot bot = Mirai.getBot();

        //如果为能加载机器人数据则返回
        if (bot == null) return;

        Mirai mirai = new Mirai(bot, 0, 0, null);

        int minStep = 5000;//最低步数
        int segStep = 5000;//分割步数
        int maxScore = 4;//最大凭证数量

        String type = "run";
        boolean status = General.getBoolean(type + General.Field.status);

        //如果未开启运动打卡检测则返回
        if (!status) return;

        List<Map<String, Object>> sportsList = Run.getSportList(minStep);

        for (Map<String, Object> sportData : sportsList) {
            long targetId = Smart.objectToLong(sportData.get("uin"));
            User user = new User(targetId);

            long todayId = Smart.getTodayId();
            long lastSeenDate = user.getLastSeenDate(type);

            //如果该用户今日已打卡则跳过
            if (lastSeenDate == todayId) continue;

            long points = Smart.objectToLong(sportData.get("points"));
            long score = points / segStep;

            //如果获得的凭证数量超过了最大凭证数量则强制为最大凭证数量
            if (score > maxScore) score = maxScore;

            //凭证进行翻倍
            score = Use.useDiceAndResponse(targetId, score);

            //更新用户凭证数量
            user.updateScore(score);

            //更新用户打卡数据
            Attendance.updateUserData(targetId, type);

            String newTop = "";
            long stepMax = user.getStep().get("max");

            //用户步数创新高则更新最高步数
            if (points > stepMax) {
                user.setValue(type + User.Field.stepMax, points);
                newTop = S.get("run.newTop");
            }

            //更新用户上次跑步步数
            user.setValue(type + User.Field.stepLast, points);

            String result = S.get("run.success")
                    .replace("{new_top}", newTop)
                    .replace("{step}", points + "")
                    .replace("{score}", score + "");

            //设置用户ID并发送消息
            mirai.fromId = targetId;
            mirai.sendMessage(result);
        }
    }

    public static void output(Mirai mirai, Event event) {
        List<Map<String, Object>> sportList = Run.getSportList(5000);
        sportList.forEach((sport) -> {
            String name = sport.get("name") + "";
            String points = sport.get("points") + "";
            String rank = sport.get("rank") + "";
            String uin = sport.get("uin") + "";
            Mirai.newLog(name, points, rank, uin);
        });
    }

    /**
     * 获取目标机器人的QQ运动排行榜数据
     *
     * @param minStep 最小步数（小于该步数的将不添加到返回数组）
     * @return 返回 List as Map as String(uin, points, rank), Object
     */
    private static List<Map<String, Object>> getSportList(int minStep) {
        List<Map<String, Object>> result = new ArrayList<>();

        Global global = new Global();
        String bkn = global.getBotBkn() + "";
        String cookies = global.getBotCookies();

        String uri = "https://quic.yundong.qq.com/pushsport/cgi/rank/friends?g_tk=" + bkn;
        String post = "dcapiKey=user_rank&l5apiKey=rank_friends&params=" +
                "{\"cmd\":1,\"pno\":{page},\"dtype\":1,\"pnum\":50}";

        JSONArray headers = new JSONArray();
        headers.add(Smart.newHeader("Content-Type", "application/x-www-form-urlencoded"));
        headers.add(Smart.newHeader("Origin", "https://quic.yundong.qq.com"));
        headers.add(Smart.newHeader("Cookie", cookies));
        headers.add(Smart.newHeader("User-Agent", "Mozilla/5.0 (Linux; Android 10; " +
                "TNY-AL00 Build/HUAWEITNY-AL00; wv) AppleWebKit/537.36 (KHTML, like " +
                "Gecko) Version/4.0 Chrome/66.0.3359.126 MQQBrowser/6.2 TBS/045114 " +
                "Mobile Safari/537.36 V1_AND_SQ_8.2.7_1334_YYB_D PA QQ/8.2.7.4410 " +
                "NetType/WIFI WebP/0.3.0 Pixel/1080 StatusBarHeight/72 SimpleUISwitch/0"));

        loopOutside:
        for (int i = 1; i <= 10; i++) {
            String tempPost = post.replace("{page}", i + "");
            JSONObject response = Smart.httpRequest(uri, "POST", tempPost, headers);

            //如果返回的代码不为0则表示获取失败，直接跳出
            int code = response.getIntValue("code");
            if (code != 0) break;

            JSONArray dataList = response.getJSONObject("data").getJSONArray("list");

            for (Object dataObj : dataList) {
                JSONObject data = (JSONObject) JSONObject.toJSON(dataObj);
                long points = data.getLong("points");

                //如果当前用户的步数已经小于限制步数则跳出整套循环
                if (points < minStep) break loopOutside;

                Map<String, Object> tempMap = JSONObject.toJavaObject(data, Map.class);
                result.add(tempMap);
            }
        }

        return result;
    }
}
