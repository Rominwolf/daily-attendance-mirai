package ink.wsm.mirai.daily_attendance_v2.cores;

import ink.wsm.mirai.daily_attendance_v2.cores.attendances.Attendance;
import ink.wsm.mirai.daily_attendance_v2.cores.attendances.Run;
import ink.wsm.mirai.daily_attendance_v2.cores.data.General;
import ink.wsm.mirai.daily_attendance_v2.utils.Mirai;
import ink.wsm.mirai.daily_attendance_v2.utils.Smart;
import ink.wsm.mirai.daily_attendance_v2.utils.Yamler;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 计时器相关
 */
public class Timer {
    /**
     * 创建计时器任务
     */
    public static void create() {
        ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
        executorService.scheduleAtFixedRate(() -> {
            try {
                int minute = Smart.getMinute();
                if (minute != 0) return;

                int hour = Smart.getHour();

                settle(hour);
                remind(hour);
                runCheck(hour);
                globalListToNone(hour);

            } catch (Exception e) {
                Mirai.sendMessageToOwner(S.Owner.TimerError,
                        e.getMessage(), e.getCause().toString());
            }
        }, 0, 1, TimeUnit.MINUTES);//每分钟执行一次
    }

    /**
     * 常规打卡结算
     */
    public static void settle(int hour) {
        //允许的打卡类型
        String[] types = new String[]{"wake", "sleep"};

        //如果当前小时等于目标打卡开启时间则：
        for (String type : types) {
            long settle = General.getLong(type + General.Field.settle);

            //进行当前打卡类型的结算
            if (hour == settle)
                Attendance.processAttendanceTimer(type);
        }
    }

    /**
     * 常规打卡提醒
     */
    public static void remind(int hour) {
        //允许的提醒打卡类型
        String[] types = new String[]{"wake", "nap", "sleep"};

        //如果当前小时等于目标打卡开启时间则：
        for (String type : types) {
            long start = General.getLong(type + General.Field.start);
            long end = General.getLong(type + General.Field.end);

            //发送需要提醒的打卡列表（当前小时在开始和结束打卡的时间内）
            if (hour >= start && hour < end)
                Remind.processTimer(type);
        }
    }

    /**
     * 运动打卡步数检测
     */
    public static void runCheck(int hour) {
        long check = General.getLong("run" + General.Field.check);

        //如果当前时间等于运动打卡检测时间则进行步数检测
        if (hour == check)
            Run.timerProcessCheck();
    }

    /**
     * 常规打卡全局打卡用户列表清空
     */
    public static void globalListToNone(int hour) {
        //仅限每天0点重置
        if (hour != 0) return;

        //允许的打卡类型
        String[] types = new String[]{"nap", "run"};

        for (String type : types) {
            Yamler global = S.Data.globalYaml;

            //增加 global 字段
            String fieldPool = type + S.Global.Field.pool;
            String fieldList = type + S.Global.Field.list;

            global.set(fieldPool, 0);
            global.set(fieldList, "");
        }
    }

}
