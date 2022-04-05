package ink.wsm.mirai.daily_attendance_v2;

import ink.wsm.mirai.daily_attendance_v2.cores.S;
import ink.wsm.mirai.daily_attendance_v2.cores.Terminal;
import ink.wsm.mirai.daily_attendance_v2.cores.Timer;
import ink.wsm.mirai.daily_attendance_v2.utils.Mirai;
import ink.wsm.mirai.daily_attendance_v2.utils.Yamler;
import net.mamoe.mirai.console.plugin.jvm.JavaPlugin;
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescriptionBuilder;
import net.mamoe.mirai.event.GlobalEventChannel;

public final class Plugin extends JavaPlugin {
    public static final Plugin INSTANCE = new Plugin();

    private Plugin() {
        super(new JvmPluginDescriptionBuilder("ink.wsm.mirai.daily_attendance_v2.plugin", "2.22")
                .name("Daily Attendance 2")
                .info("The plugin of the Daily Attendance version 2.")
                .author("Rominwolf")
                .build());
    }

    @Override
    public void onEnable() {
        Mirai.logger = getLogger();
        Mirai.createLog("info", "插件已加载！");

        S.Data.dataFolder = INSTANCE.getDataFolder().getPath() + "/";
        S.Data.configFolder = INSTANCE.getConfigFolder().getPath() + "/";
        S.Data.userFolder = S.Data.dataFolder + "users/";
        S.Data.groupFolder = S.Data.dataFolder + "groups/";

        S.Data.generalYaml = new Yamler(S.Data.configFolder + "general.yml");
        S.Data.globalYaml = new Yamler(S.Data.dataFolder + "global.yml");

        //创建全局事件
        GlobalEventChannel.INSTANCE.registerListenerHost(new Listener());

        //创建控制台任务
        Terminal.create(INSTANCE);

        //时钟任务
        Timer.create();
    }
}