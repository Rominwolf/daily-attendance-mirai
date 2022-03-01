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
- [x] 旧版数据转移系统

## 安装

为了确保能够正常使用，请**仔细**阅读安装须知。

1. 将 Release 渠道下的 `ink.wsm.mirai.daily_attendance_v2.plugin.zip` 压缩包文件解压至 `<你的 Mirai 根目录>\config\` 目录下。
2. （可选）将 `general.yml` 配置文件的配置数据进行自由修改。
3. （可选）如果开启了**运动打卡**请确保已成功在动态数据文件 `<你的 Mirai 根目录>\data\ink.wsm.mirai.daily_attendance_v2.plugin\global.yml` 中设定了机器人的**
   QQ空间**的 `bkn` & `cookies`。路径为 `bot.bkn` & `bot.cookies`。
4. 将应用本体文件放入 `<你的 Mirai 根目录>\plugins\` 目录下并重启 Mirai 即可使用。

## 命令

使用 `/da` 命令即可查看本应用支持的所有二级命令。

## 特别鸣谢

感谢所有支持**雪球**的同志们！:P

## 支持开发者

请为我与我的博客续命，谢谢;w;  
为我发电 >> https://afdian.net/@rominwolf
