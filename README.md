# 每日打卡 Daily Attendance
> 一款基于 Mirai 框架的 QQ 机器人插件，其灵感来源于市面上常见的“早起打卡”，但该插件支持更多功能。

该插件基于 Mirai-console 开发，请确保您机器人使用的是 [Mirai]([https://github.com/mamoe/mirai]) 框架。

## 许可证声明与须知
本插件以 **GNU AGPLv3** 协议开源，也请您在使用或二次创作时遵守此协议。  
*开发者承诺本应用永久免费，请勿用于出售、转卖等商业行为。*

## 服务调用表

- 基于 Mirai 开发：https://github.com/mamoe/mirai
- 依赖 Carleslc 开发的 Simple-Yaml：https://github.com/Carleslc/Simple-YAML
- 依赖 alibaba 开发的 fastjson：https://github.com/alibaba/fastjson
- 依赖 edolganov 开发的 props4j：https://github.com/edolganov/props4j
- 依赖 apache 开发的 commons-text：https://commons.apache.org/proper/commons-text
- 依赖 apache 开发的 commons-io：http://commons.apache.org/proper/commons-io
- 依赖 apache 开发的 commons-lang3：https://mvnrepository.com/artifact/org.apache.commons/commons-lang3

## 预计支持的功能

- [x] 快速打卡
- [x] 早起打卡
- [x] 午睡打卡
- [x] 晚安打卡
- [x] 运动打卡
- [ ] 番茄打卡
- [ ] 背单词打卡
- [x] 查询用户打卡信息
- [x] 用户属性设定
- [x] 群聊属性设定
- [x] 每日打卡提醒
- [x] 获取用户打卡排行榜
- [x] 道具系统
- [x] 控制台命令

## 安装

为了确保能够正常使用，请**仔细**阅读安装须知。

1. 将 Release 渠道下的应用本体文件放入 `<你的 Mirai 根目录>/plugins/` 目录下。
2. 将文件 `config/ink.wsm.mirai.daily_attendance_v2.plugin/general.yml` 和目录 `config/ink.wsm.mirai.daily_attendance_v2.plugin/languages` 放入你的 `<你的 Mirai 根目录>/config/ink.wsm.mirai.daily_attendance_v2.plugin` 目录下。
3. （可选）自由编辑和修改 `general.yml` 配置文件的配置数据。
4. （可选）如果开启了**运动打卡**请确保已成功在动态数据文件 `<你的 Mirai 根目录>/data/ink.wsm.mirai.daily_attendance_v2.plugin/global.yml` 中设定了机器人**
   QQ空间**的 `bkn` & `cookies`。路径为 `bot.bkn` & `bot.cookies`。

## 命令

使用 `/da` 命令即可查看本应用支持的所有**前台**二级命令。

### 控制台命令

**更新用户数据：**

`da user <QQ号> <目标键路径> <值>`

其中：
- <QQ号>：欲更新的用户的QQ。必填。
- <目标键路径>：欲更新的键的路径，如 wake.remind.status 则该用户的早起打卡提醒状态。具体键值请查阅 fromId.yml 文件。必填。
- <值>：欲更新的值，如布尔型(true/false)，整数型(-20/0/114)，字符串型("info...")。需要注意的是：整数型如果需要在原先的基础上增加/减少，可以在数字前增加 +/- 标记符，如 +10 或 -20。必填。

**更新群聊数据：**

同上，但二级命令需要变更为 group，即 user -> group。

具体键值请查阅 fromGroup.yml 文件。

## 疑难解答
### Q. 第三方资金是否有数额查询入口？

A. 本应用**查询不了**第三方资金的数额，需要使用**相对应的应用**来查询（可以使用任意金融类插件）。

本应用的货币“凭证”是保存到了应用内，然后如果需要购买道具的话，还是得调用第三方资金（如商店插件）应用……

可以通过**控制台命令**「/da user <目标QQ> <实体ID> <+/-的数量>」来修改用户的实体（如凭证、道具等）数量。

### Q. 道具只能随机获取吗？

A. **是的**，普通用户目前应用只能通过打卡来获取。

### Q. 有什么方法能只启用早起打卡，其它的打卡功能不开启吗？

A. 对于“午睡打卡”和“晚安打卡”，目前没有一个特定的选项可以关闭，但是您可以在配置文件中调整其打卡的**“开始时间”和“结束时间”到同一个小时**来实现该功能（如开始时间和结束时间均为0点）。

然后对于“运动打卡”，在配置文件中关闭即可（将 **run.status 置为 false**）。

## 特别鸣谢

感谢所有支持**雪球**的同志们！:P

## 支持开发者

请为我与我的博客续命，谢谢;w;  
为我发电 >> https://afdian.net/@rominwolf
