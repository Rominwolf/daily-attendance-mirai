package ink.wsm.mirai.daily_attendance_v2.cores.data;

import ink.wsm.mirai.daily_attendance_v2.cores.S;
import ink.wsm.mirai.daily_attendance_v2.utils.Yamler;

/**
 * 群数据相关类
 */
public class Group {
    public static class Field {
        public static String remindStatus = ".remind.status";
    }

    String groupPath;
    Yamler group;

    public Group(long fromId) {
        this.groupPath = S.Data.groupFolder + fromId + ".yml";
        this.group = new Yamler(this.groupPath);
    }

    public Group(String filePath) {
        this.groupPath = filePath;
        this.group = new Yamler(this.groupPath);
    }

    /**
     * 获取群的 Yamler 类
     *
     * @return 返回 Yamler 类
     */
    public Yamler get() {
        return group;
    }

    /**
     * 群是否允许接收指定打卡的每日提醒
     *
     * @return 返回真假（默认：假）
     */
    public boolean isRemindOn(String type) {
        return group.getBoolean(type + Field.remindStatus);
    }

    /**
     * 更改群数据文件中的指定键的值
     *
     * @param key   键路径
     * @param value 值
     * @return 返回更新后的结果
     */
    public Object setValue(String key, Object value) {
        return group.set(key, value);
    }

}
