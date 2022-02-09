package ink.wsm.mirai.daily_attendance_v2.utils;

import ink.wsm.mirai.daily_attendance_v2.cores.S;
import net.mamoe.mirai.Bot;
import net.mamoe.mirai.console.command.CommandExecuteResult;
import net.mamoe.mirai.console.command.CommandManager;
import net.mamoe.mirai.console.command.ConsoleCommandSender;
import net.mamoe.mirai.contact.*;
import net.mamoe.mirai.message.MessageReceipt;
import net.mamoe.mirai.message.code.MiraiCode;
import net.mamoe.mirai.message.data.*;
import net.mamoe.mirai.utils.ExternalResource;
import net.mamoe.mirai.utils.MiraiLogger;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

/**
 * Mirai 通用工具类
 */
public class Mirai {
    public static MiraiLogger logger;

    public Bot bot;//机器人
    public long fromId;//QQ号
    public long fromGroup;//群号
    public MessageChain source;//消息源
    public Object transMain = new Object();//主要消息，往往是接近原生消息的消息
    public Object transMinor = new Object();//次要消息，往往是转换成 Mirai 码的消息
    public MessageChain transMessage;//原生消息

    public Mirai(Bot bot, long fromId, long fromGroup, MessageChain source) {
        this.bot = bot;
        this.fromId = fromId;
        this.fromGroup = fromGroup;
        this.source = source;
    }

    /**
     * 混合版发送消息方法<br>
     * main：接收单条消息，如 String、int 等等……（会自动引用回复，如果 source 为 null 则自动添加艾特）<br>
     * minor：接收消息串 MessageChain<br>
     * <b>优先选择自定义消息串并发送</b>
     */
    public MessageReceipt sendMessage(Object main, MessageChain minor) {
        Bot bot = this.bot;
        long fromId = this.fromId;
        long fromGroup = this.fromGroup;
        MessageChain source = this.source;
        String content = (String) main;

        if (content == null || content.equals("")) return null;

        //删除结尾的空行
        while (StringUtils.endsWith(content, "\\n"))
            content = StringUtils.removeEnd(content, "\\n");
        while (StringUtils.endsWith(content, "\n"))
            content = StringUtils.removeEnd(content, "\n");

        // Block Code -> Mirai Code
        content = this.blockCodeToMiraiCode(content, fromGroup == 0);

        if (fromGroup != 0) {
            Group group = bot.getGroup(fromGroup);
            if (group == null) return null;

            //如果存在自定义消息串则优先发发送自定义消息串
            if (minor != null) return group.sendMessage(minor);

            MessageChain chain;
            if (source == null) {
                if (fromId == 0)
                    chain = new MessageChainBuilder()
                            .append(MiraiCode.deserializeMiraiCode(content)).build();
                else chain = new MessageChainBuilder()
                        .append(new At(fromId).plus(" "))
                        .append(MiraiCode.deserializeMiraiCode(content))
                        .build();
            } else {
                chain = new MessageChainBuilder()
                        .append(new QuoteReply(source))
                        .append(MiraiCode.deserializeMiraiCode(content))
                        .build();
            }
            return group.sendMessage(chain);
        }

        //先尝试是否为好友，如果非好友则尝试陌生人，如果均返回 null 则返回
        Contact friend = bot.getFriend(fromId);
        if (friend == null) friend = bot.getStranger(fromId);
        if (friend == null) return null;

        //如果存在自定义消息串则优先发发送自定义消息串
        if (minor != null) return friend.sendMessage(minor);

        MessageChain chain;
        if (source == null) {
            chain = new MessageChainBuilder()
                    .append(MiraiCode.deserializeMiraiCode(content))
                    .build();
        } else {
            chain = new MessageChainBuilder()
                    .append(new QuoteReply(source))
                    .append(MiraiCode.deserializeMiraiCode(content))
                    .build();
        }
        return friend.sendMessage(chain);

    }

    /**
     * Object · 混合版发送消息方法
     */
    public MessageReceipt sendMessage(Object main) {
        return sendMessage(main, null);
    }

    /**
     * MessageChain · 混合版发送消息方法
     */
    public MessageReceipt sendMessage(MessageChain minor) {
        return sendMessage(null, minor);
    }

    /**
     * 新版 | 发送消息，type={code(表示为 Block Code)|chain(表示为Message Chain)|!group(表示不允许群聊发送)}
     */
    private MessageReceipt send(String type, Object message) {
        if (message == null) return null;

        Bot bot = this.bot;
        long fromId = this.fromId;
        long fromGroup = this.fromGroup;
        MessageChain source = this.source;

        //如果不存在 !group 则表示允许群聊发送
        boolean allowGroupSend = !type.contains("!group");

        if (type.contains("code")) {
            String content = (String) message;

            //删除结尾的空行
            while (StringUtils.endsWith(content, "\\n"))
                content = StringUtils.removeEnd(content, "\\n");
            while (StringUtils.endsWith(content, "\n"))
                content = StringUtils.removeEnd(content, "\n");

            // Block Code -> Mirai Code
            content = this.blockCodeToMiraiCode(content, fromGroup == 0);
        }

        // 如果 fromGroup 存在且不存在 !group 则优先选择群聊发送
        // 否则或如果发送失败则尝试以私聊方式发送
        // 如果再次发送失败则尝试使用陌生人方式发送
        // 如果依然发送失败则抛出错误

        // fromGroup 存在 且 允许群聊发送
        if (fromGroup != 0 && allowGroupSend) {
            Group group = bot.getGroup(fromGroup);

        }

        return null;
    }

    /**
     * 内部 | 群聊发送消息，返回是否发送成功
     */
    private ResultSendMessage sendByGroup(String type, Object message) {
        Bot bot = this.bot;
        long fromId = this.fromId;
        long fromGroup = this.fromGroup;
        MessageChain source = this.source;

        ResultSendMessage result = new ResultSendMessage();
        Group group = bot.getGroup(fromGroup);

        // group 为 null
        if (group == null) {
            result.status = false;
            return result;
        }

        // 以 MessageChain 的方式发送消息
        if (type.contains("chain")) {
            MessageReceipt<Group> receipt = group.sendMessage((MessageChain) message);
            result.status = true;
            result.receipt = receipt;
            return result;
        }

        MessageChain chain = new MessageChainBuilder().build();

        // 如果存在引用则进行引用回复，否则进行艾特
        if (source != null) chain.add(new QuoteReply(source));
        else chain.add(new At(fromId));

        //chain.add(MiraiCode.deserializeMiraiCode(message + ""));

        return result;
    }

    /**
     * 创建新日志<br>多个 content 之间用全角空格分割<br>
     * type 支持 info, error, debug, verbose, warning
     */
    public static void createLog(String type, Object... content) {
        StringBuilder texts = new StringBuilder();
        for (Object o : content) texts.append(o).append(" ");
        String text = texts.toString();

        if (type.equals("error")) Mirai.logger.error(text);
        if (type.equals("debug")) Mirai.logger.debug(text);
        if (type.equals("info")) Mirai.logger.info(text);
        if (type.equals("verbose")) Mirai.logger.verbose(text);
        if (type.equals("warning")) Mirai.logger.warning(text);
    }

    /**
     * 快速创建 Info 类型的日志
     */
    public static void newLog(Object... content) {
        createLog("info", content);
    }

    /**
     * 向所有者发送消息，多条消息使用换行分割
     */
    public static void sendMessageToOwner(Object... content) {
        StringBuilder message = new StringBuilder();
        for (Object o : content) message.append(o).append("\n");

        Bot bot = null;
        Mirai.createLog("error", content);

        //循环3次，尝试获取 bot 的状态
        for (int i = 0; i < 3; i++) {
            bot = Mirai.getBot();
            if (bot != null) break;
            Smart.sleep(10);
        }

        Mirai mirai = new Mirai(bot, S.Owner.Id, 0, null);
        mirai.sendMessage(message.toString());
    }

    /**
     * 向控制台发送消息
     */
    public static CommandExecuteResult sendConsoleMessage(Object command) {
        MessageChain message = new MessageChainBuilder()
                .append(command.toString())
                .build();
        return CommandManager.INSTANCE.executeCommand(ConsoleCommandSender.INSTANCE, message, false);
    }

    /**
     * 获取指定用户的用户名，在获取失败时返回QQ号
     */
    public String getUserNickname(long id) {
        return getUserNicknameFromGroup(id, 0);
    }

    /**
     * 获取指定群的指定用户的群名片，在获取失败或为空时返回用户名，如果再获取失败则返回QQ号
     */
    public String getUserNicknameFromGroup(long id, long fromGroup) {
        String nickname = null;
        Group group = bot.getGroup(fromGroup);

        //优先群获取昵称
        if (group != null) {
            NormalMember member = group.get(id);
            if (member != null) {
                nickname = member.getNameCard();
                if (nickname.equals("")) {
                    nickname = member.getNick();
                }
            }
        }

        //获取失败则尝试好友获取昵称
        if (nickname == null) {
            Friend friend = bot.getFriend(id);
            if (friend != null) {
                nickname = friend.getNick();
                if (nickname.equals("")) nickname = friend.queryProfile().getNickname();
            }
        }

        //再不济则尝试陌生人获取昵称
        if (nickname == null) {
            Stranger stranger = bot.getStranger(id);
            if (stranger != null) nickname = stranger.queryProfile().getNickname();
        }

        //还是失败则只能返回QQ号
        if (nickname == null || nickname.equals(""))
            nickname = id + "";

        return nickname;
    }

    /**
     * 获取用户性别<br>
     * 默认返回：那个人
     */
    public String getUserGender(long id) {
        String gender = S.get("system.thisGuy");
        Stranger user = this.bot.getStranger(id);
        if (user != null) gender = user.queryProfile().getSex().name();
        return gender;
    }

    /**
     * 获取在线的第一个机器人类<br>
     * 如果没有则返回null
     */
    public static Bot getBot() {
        List<Bot> bots = Bot.getInstances();
        if (bots.size() == 0) return null;
        return bots.get(0);
    }

    /**
     * 将 ExternalResource 上传到服务器。
     * 如果 uploadToFriend 为 true 则表示以 Friend 模式上传，否则上传到 Group（前提是Group存在）
     * 返回图片ID，如果上传失败则返回空
     */
    public String uploadImage(ExternalResource resource, boolean uploadToFriend) {
        Image image;

        try {
            if (!uploadToFriend && fromGroup != 0)
                image = bot.getGroup(fromGroup).uploadImage(resource);
            else
                image = bot.getFriend(fromId).uploadImage(resource);
        } catch (Exception exception) {
            return "";
        }

        return image.getImageId();
    }

    /**
     * 上传images中的图片并返回图片ID，接收并发送[value...]，上传失败则返回[""...]
     */
    public String[] uploadImageToCode(String[] images, boolean uploadToFriend) {
        int count = images.length;
        String[] result = new String[count];

        for (int i = 0; i < count; i++) {
            String value = images[i];
            String imageId;
            ExternalResource resource;
            try {
                if (StringUtils.startsWith(value, "http"))
                    resource = ExternalResource.create(Smart.getFileInputStream(value));
                else
                    resource = ExternalResource.create(Smart.getFileFromLocal(value));

                imageId = this.uploadImage(resource, uploadToFriend);
                resource.close();
            } catch (Exception exception) {
                result[i] = "";
                continue;
            }

            result[i] = imageId;
        }

        return result;
    }

    /**
     * 昵称块变为昵称，[block:nick:{id}] -> @昵称(QQ号)
     */
    public String[] nickCodeToNickname(String[] ids) {
        int count = ids.length;
        String[] result = new String[count];

        for (int i = 0; i < count; i++) {
            long id = Smart.isNumeric(ids[i]) ? Long.parseLong(ids[i]) : 0;
            String nickname = this.getUserNickname(id);
            String nick = "@" + nickname;
            if (!nickname.equals(id + "")) nick += "(" + id + ")";
            result[i] = nick;
        }

        return result;
    }

    /**
     * 将 [block:...:...] 变为 Mirai Code 的形式[mirai:...:...]
     */
    public String blockCodeToMiraiCode(String text, boolean uploadToFriend) {
        /* ----- 获取图片块、上传并变更 ----- */
        // 上传图片 [block:image:{file}] -> [mirai:image:{image.id}]
        String[] imagesPath = StringUtils.substringsBetween(text, "[block:image:", "]");
        if (imagesPath == null) imagesPath = new String[]{};

        String[] imagesId = uploadImageToCode(imagesPath, uploadToFriend);
        text = StringUtils.replaceEach(text, imagesPath, imagesId);

        /* ----- 获取昵称块并变更 ----- */
        // 昵称 [block:nick:{id}] -> @昵称(QQ号)
        String blockStart = "[block:nick:";
        String blockEnd = "]";
        String[] ids = StringUtils.substringsBetween(text, blockStart, blockEnd);
        if (ids == null) ids = new String[]{};

        String[] nicks = nickCodeToNickname(ids);
        for (int i = 0; i < ids.length; i++) ids[i] = blockStart + ids[i] + blockEnd;
        text = StringUtils.replaceEach(text, ids, nicks);

        text = StringUtils.replace(text, "[block", "[mirai");
        return text;
    }

    /**
     * 类 | 发送消息的结果
     */
    private static class ResultSendMessage {
        public boolean status = false;
        public MessageReceipt receipt = null;
    }

    /**
     * 检测目标用户在目标群是否为管理员或群主
     *
     * @return 真：为管理员或群主（私聊状态下也为真）
     */
    public boolean isAdminFromGroup() {
        if (fromGroup == 0) return true;

        Group group = bot.getGroup(fromGroup);

        //目标群聊已消失，返回假
        if (group == null) return false;

        NormalMember member = group.get(fromId);

        //目标用户已消失，返回假
        if (member == null) return false;

        //权限等级不为0则为管理员或群主
        return member.getPermission().getLevel() != 0;
    }
}
