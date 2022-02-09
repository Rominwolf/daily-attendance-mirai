package ink.wsm.mirai.daily_attendance_v2.cores;

import ink.wsm.mirai.daily_attendance_v2.cores.data.Group;
import ink.wsm.mirai.daily_attendance_v2.cores.data.User;
import ink.wsm.mirai.daily_attendance_v2.utils.Mirai;
import ink.wsm.mirai.daily_attendance_v2.utils.Smart;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import net.mamoe.mirai.console.command.*;
import net.mamoe.mirai.message.data.MessageChain;
import net.mamoe.mirai.message.data.SingleMessage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * 控制台相关类
 */
public class Terminal {
    /**
     * 注册控制台命令事件
     */
    public static void create(CommandOwner owner) {
        String[] secondaryNames = new String[]{S.Command.terminal};
        String primaryName = S.Command.terminal;
        String desc = S.get("terminal.desc");

        Command command = new RawCommand(
                owner, primaryName, secondaryNames, desc, desc,
                owner.getParentPermission(), true) {
            @Nullable
            @Override
            public Object onCommand(@NotNull CommandSender commandSender,
                                    @NotNull MessageChain messages,
                                    @NotNull Continuation<? super Unit> continuation) {
                List<String> commands = new ArrayList<>();
                for (SingleMessage message : messages) commands.add(message.contentToString());

                String type = "";
                long id = 0;
                String key = "";
                Object value = "";

                // user 114514 wake.remind.status false
                if (commands.size() > 0) type = commands.get(0);
                if (commands.size() > 1) id = Long.parseLong(commands.get(1));
                if (commands.size() > 2) key = commands.get(2);
                if (commands.size() > 3) value = commands.get(3);

                if (type.equals("user"))
                    updateUserData(id, key, value);

                if (type.equals("group"))
                    updateGroupData(id, key, value);

                return null;
            }
        };

        CommandManager.INSTANCE.registerCommand(command, true);
    }

    /**
     * 更新用户数据
     *
     * @param id    目标QQ号
     * @param key   数据路径（如 score, candle, wake.total... 多级路径使用“.”分割）
     * @param value 数据（允许 true/false 或包括 +/- 的整数。
     *              如果为 T/F 则使用布尔型存储；
     *              如果使用 +/- 则使用整数型存储，直接填写数字则表示不进行增加/减少而直接更新）
     */
    public static void updateUserData(long id, String key, Object value) {
        User user = new User(id);
        String valueText = value.toString();
        Object oldValue = user.get().get(key);
        Object newValue = "";

        //布尔型数据的更新
        if (value.equals("true") || value.equals("false"))
            newValue = user.setValue(key, value.equals("true"));

        //如果为数字则以整数型进行数据的更新（以 +/- 开头则进行数据的增加/减少，否则直接更新）
        if (Smart.isNumeric(valueText)) {
            //表示数据不以 +/- 开头则直接更新
            boolean isDirectUpdate = !valueText.startsWith("+") && !valueText.startsWith("-");

            long data = Long.parseLong(valueText);
            newValue = user.get().setLong(key, data, isDirectUpdate);
        }

        String result = S.get("terminal.success")
                .replace("{id}", id + "")
                .replace("{path}", key)
                .replace("{new_value}", newValue + "")
                .replace("{old_value}", oldValue + "");

        Mirai.newLog(result);
    }

    /**
     * 更新群聊数据
     *
     * @param id    目标群号
     * @param key   数据路径（如 score, candle, wake.total... 多级路径使用“.”分割）
     * @param value 数据（允许 true/false 或包括 +/- 的整数。
     *              如果为 T/F 则使用布尔型存储；
     *              如果使用 +/- 则使用整数型存储，直接填写数字则表示不进行增加/减少而直接更新）
     */
    public static void updateGroupData(long id, String key, Object value) {
        Group group = new Group(id);
        String valueText = value.toString();
        Object oldValue = group.get().get(key);
        Object newValue = "";

        //布尔型数据的更新
        if (value.equals("true") || value.equals("false"))
            newValue = group.setValue(key, value.equals("true"));

        //如果为数字则以整数型进行数据的更新（以 +/- 开头则进行数据的增加/减少，否则直接更新）
        if (Smart.isNumeric(valueText)) {
            //表示数据不以 +/- 开头则直接更新
            boolean isDirectUpdate = !valueText.startsWith("+") && !valueText.startsWith("-");

            long data = Long.parseLong(valueText);
            newValue = group.get().setLong(key, data, isDirectUpdate);
        }

        String result = S.get("terminal.success")
                .replace("{id}", id + "")
                .replace("{path}", key)
                .replace("{new_value}", newValue + "")
                .replace("{old_value}", oldValue + "");

        Mirai.newLog(result);
    }

    /**
     * 提交一则向控制台的命令请求
     *
     * @param content 命令行
     * @return 返回是否执行成功
     */
    public static boolean requestCommand(String content) {
        CommandExecuteResult commandResult = Mirai.sendConsoleMessage(content);
        Throwable throwable = commandResult.getException();

        return throwable == null;
    }
}
