package ink.wsm.mirai.daily_attendance_v2.utils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import ink.wsm.mirai.daily_attendance_v2.cores.S;
import okhttp3.*;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class Smart {
    /**
     * 获取当前指定格式的指定时间
     */
    public static String getDate(String format, Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.ENGLISH);
        return sdf.format(date);
    }

    /**
     * 将指定日期格式的字符串日期转换为 Date，转换失败则返回null
     */
    public static Date formatToDate(String datetime, String format) {
        SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.ENGLISH);
        try {
            return sdf.parse(datetime);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 获取当前指定格式的当前时间
     */
    public static String getDate(String format) {
        SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.ENGLISH);
        return sdf.format(new Date());
    }

    /**
     * 获取当前时间（yyyy-MM-dd HH:mm:ss）
     */
    public static String getDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
        return sdf.format(new Date());
    }

    /**
     * 获取今日的数字ID（如20220107）
     */
    public static long getTodayId() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd", Locale.ENGLISH);
        return Long.parseLong(sdf.format(new Date()));
    }

    /**
     * 获取当前小时（24小时制）
     *
     * @return 返回小时
     */
    public static int getHour() {
        return Integer.parseInt(Smart.getDate("HH"));
    }

    /**
     * 获取当前分钟
     *
     * @return 返回分钟
     */
    public static int getMinute() {
        return Integer.parseInt(Smart.getDate("mm"));
    }

    /**
     * 将字符串时间转为时间日期
     */
    public static Date getDateByString(String time) {
        Date date = null;
        if (time == null) return date;

        String pattern = "yyyy-MM-dd HH:mm:ss";
        SimpleDateFormat format = new SimpleDateFormat(pattern, Locale.ENGLISH);
        try {
            date = format.parse(time);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }

    /**
     * 获取今天是星期几
     */
    public static int getWhichDay() {
        return Integer.parseInt(getDate("u"));
    }

    /**
     * 获取今天是否为周末
     */
    public static boolean isWeekend() {
        int week = getWhichDay();
        return week == 6 || week == 7;
    }

    /**
     * object -> Long
     *
     * @return 返回转换后的结果，默认为0
     */
    public static Long objectToLong(Object o) {
        return objectToLong(o, 0);

    }

    /**
     * Rfc 1123 (Mon, 15 Jun 2009 20:45:30 GMT) -> Date
     */
    public static Date rfc1123ToDate(String rfc1123, String zoneId) {
        ZonedDateTime zdt = ZonedDateTime.parse(rfc1123, DateTimeFormatter.RFC_1123_DATE_TIME);
        ZoneId z = ZoneId.of(zoneId);
        ZonedDateTime zdtMontreal = zdt.withZoneSameInstant(z);
        return Date.from(zdtMontreal.toInstant());
    }

    /**
     * 格式化时间为短时间
     */
    public static String getShortTime(Date date) {
        String shortString;
        long now = Calendar.getInstance().getTimeInMillis();
        if (date == null) return S.get("system.unknown");

        long delTime = (now - date.getTime()) / 1000;
        if (delTime > 365 * 24 * 60 * 60) {
            shortString = (int) (delTime / (365 * 24 * 60 * 60)) + " " + S.get("system.ago.year");
        } else if (delTime > 24 * 60 * 60) {
            shortString = (int) (delTime / (24 * 60 * 60)) + " " + S.get("system.ago.day");
        } else if (delTime > 60 * 60) {
            shortString = (int) (delTime / (60 * 60)) + " " + S.get("system.ago.hour");
        } else if (delTime > 60) {
            shortString = (int) (delTime / (60)) + " " + S.get("system.ago.minute");
        } else if (delTime > 1) {
            shortString = delTime + " " + S.get("system.ago.second");
        } else {
            shortString = S.get("system.ago.moment");
        }
        return shortString;
    }

    /**
     * 格式化时间为短时间（yyyy-MM-dd HH:mm:ss）
     */
    public static String getShortTime(String time) {
        if (time.equals("")) return S.get("system.unknown");
        Date date = getDateByString(time);
        return Smart.getShortTime(date);
    }

    /**
     * 获取指定月有多少天
     */
    public static int getDaysOfMonth(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
    }

    /**
     * 判断是否为是数字
     */
    public static boolean isNumeric(String str) {
        try {
            String bigStr = new BigDecimal(str).toString();
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    /**
     * Convert the ResultSet to a List of Maps, where each Map represents a row with columnNames and columValues
     */
    public static List<Map<String, Object>> resultSetToList(ResultSet rs) throws SQLException {
        ResultSetMetaData md = rs.getMetaData();
        int columns = md.getColumnCount();
        List<Map<String, Object>> rows = new ArrayList<>();
        while (rs.next()) {
            Map<String, Object> row = new HashMap<>(columns);
            for (int i = 1; i <= columns; ++i) {
                row.put(md.getColumnName(i), rs.getObject(i));
            }
            rows.add(row);
        }
        return rows;
    }

    /**
     * 获取目录下的所有文件
     */
    public static ArrayList<String> getFiles(String path) {
        ArrayList<String> files = new ArrayList<>();
        File file = new File(path);
        File[] tempList = file.listFiles();
        if (tempList == null) return new ArrayList<>();

        for (File value : tempList) {
            if (value.isFile()) {
                files.add(value.toString());
            }
        }
        return files;
    }

    /**
     * 读取网络文件到串流
     */
    public static InputStream getFileInputStream(String path) {
        try {
            URL url = new URL(path);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(3 * 1000);
            conn.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)");
            return conn.getInputStream();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 读取本地文件到内存
     */
    public static File getFileFromLocal(String path) {
        return new File(path);
    }

    /**
     * 将网络图片转换为 Base64
     */
    public static String urlImageToBase64(String imgURL) {
        ByteArrayOutputStream data = new ByteArrayOutputStream();
        try {
            URL url = new URL(imgURL);
            byte[] by = new byte[1024];

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000);
            InputStream is = conn.getInputStream();

            int len = -1;
            while ((len = is.read(by)) != -1) {
                data.write(by, 0, len);
            }

            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return Base64.getMimeEncoder().encodeToString(data.toByteArray());
    }

    /**
     * 获取一串字符串的长度，失败则返回-1
     *
     * @author She_lock
     */
    public static long getLineNumber(String target) {
        long length = -1;
        try {
            LineNumberReader lnr = new LineNumberReader(new CharArrayReader(target.toCharArray()));
            lnr.skip(Long.MAX_VALUE);
            lnr.close();
            length = lnr.getLineNumber() + 1;
        } catch (Exception ignored) {
        }
        return length;
    }

    /**
     * 睡觉
     */
    public static void sleep(int second) {
        try {
            TimeUnit.SECONDS.sleep(second);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * 重写 toString 方法
     *
     * @author 靳兆鲁
     */
    public static <T> String overrideToString(T t) {
        StringBuilder result = new StringBuilder("[");

        for (Field declaredField : t.getClass().getDeclaredFields()) {
            try {
                result
                        .append(declaredField.getName())
                        .append("=")
                        .append(declaredField.get(t))
                        .append(",");
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        return result.substring(0, result.length() - 1) + "]";
    }

    /**
     * 命令行解析器，获取指定key（包括-号）的value，如不存在则返回 defaultReturn 的内容
     */
    public static String commandParsing(String command, String key, String defaultReturn) {
        String value = "";
        String[] commands = command.split(" ");

        for (String s : commands) {
            if (!StringUtils.startsWith(s, key)) continue;
            value = StringUtils.substringAfter(s, key + "=");
        }

        if (value.isEmpty()) value = defaultReturn;
        return value;
    }

    /**
     * 命令行组合器，使用 {?} 为变量替换，如：/sb user {?} favor {?}
     */
    public static String commandCombiner(String content, Object... args) {
        for (Object arg : args) {
            String arger = arg + "";
            content = StringUtils.replaceOnce(content, "{?}", arger);
        }

        return content;
    }

    /**
     * 创建新header
     */
    public static JSONObject newHeader(String key, String value) {
        JSONObject result = new JSONObject();
        result.put(key, value);
        return result;
    }

    /**
     * HTTP 网络请求
     */
    public static JSONObject httpRequest(String uri, String type, String postData, JSONArray headers) {
        if (headers == null) headers = new JSONArray();

        type = type.toLowerCase();
        String contentType = "application/json";
        Request request = new Request.Builder().url(uri).build();

        for (int i = 0; i < headers.size(); i++) {
            JSONObject header = headers.getJSONObject(i);
            for (Map.Entry<String, Object> entry : header.entrySet()) {
                String key = entry.getKey().toLowerCase();
                String value = (String) entry.getValue();
                request = request.newBuilder().addHeader(key, value).build();
                if (!key.equals("content-type")) break;
                contentType = value;
            }
        }

        OkHttpClient client = new OkHttpClient().newBuilder()
                .connectTimeout(S.Default.httpTimeout, TimeUnit.SECONDS)
                .readTimeout(S.Default.httpTimeout, TimeUnit.SECONDS).build();
        RequestBody requestBody = RequestBody.create(postData, MediaType.parse(contentType));

        if (type.equals("get")) request = request.newBuilder().get().build();

        if (type.equals("post") || type.equals("put") || type.equals("patch"))
            request = request.newBuilder().post(requestBody).build();

        Response response;
        String jsonRaw;
        JSONObject result;

        try {
            response = client.newCall(request).execute();
            jsonRaw = response.body().string();
            result = JSONObject.parseObject(jsonRaw);
        } catch (Exception e) {
            result = new JSONObject();
            result.put("code", "error");
            result.put("msg", e.getMessage());
        }

        return result;


    }

    /**
     * HTTP 网络请求
     */
    public static JSONObject httpRequest(String uri, String type, JSONObject postData, JSONArray headers) {
        if (postData == null) postData = new JSONObject();

        String postDataRaw = postData.toJSONString();
        return httpRequest(uri, type, postDataRaw, headers);
    }

    /**
     * 检查当前小时是否在规定的小时内
     *
     * @param min 起始小时（包含）
     * @param max 终止小时（不包含）
     * @return 返回是否存在
     */
    public static boolean checkHourValid(int min, int max) {
        int hour = Integer.parseInt(Smart.getDate("HH"));
        return hour >= min && hour < max;
    }

    /**
     * 字符串 -> Unicode
     *
     * @param content 待转换的字符串内容
     * @return 转换成 Unicode 后的字符串内容
     */
    public static String stringToUnicode(String content) {
        StringBuilder unicode = new StringBuilder();
        for (int i = 0; i < content.length(); i++) {
            // 取出每一个字符
            char c = content.charAt(i);
            // 转换为unicode
            unicode.append("\\u").append(Integer.toHexString(c));
        }

        return unicode.toString();
    }

    /**
     * Base64 编码
     */
    public static String base64Encode(String content, String charset, boolean isUrlSafe) {
        String asB64 = "";
        try {
            asB64 = Base64.getEncoder().encodeToString(content.getBytes(charset));
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 是否是 UrlSafe 模式
        if (isUrlSafe) {
            asB64 = asB64.replace('+', '-')
                    .replace('/', '_')
                    .replaceAll("=", "");
        }

        return asB64;
    }

    /**
     * Base64 解码
     */
    public static String base64Decode(String content, String charset, boolean isUrlSafe) {
        // 是否为 UrlSafe 模式
        if (isUrlSafe) {
            content = content.replace('-', '+')
                    .replace('_', '/');
            int mod4 = content.length() % 4;
            if (mod4 > 0) {
                content = content + "====".substring(mod4);
            }
        }

        byte[] asBytes = Base64.getDecoder().decode(content);
        String result = null;
        try {
            result = new String(asBytes, charset);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 获取字符串内所有以“{}”包围的字段列表
     *
     * @param content        待分析的字符串
     * @param keepConstraint 是否保留“{}”
     * @return 返回结果数组
     */
    public static String[] getAllFields(String content, boolean keepConstraint) {
        String[] result = StringUtils.substringsBetween(content, "{", "}");

        //如果保存约束则添加大括号
        if (keepConstraint)
            for (int i = 0; i < result.length; i++)
                result[i] = "{" + result[i] + "}";

        return result;
    }

    /**
     * 替换字符串内的所有字段
     *
     * @param content 字符串
     * @param data    待替换的数据表
     * @return 替换后的结果
     */
    public static String replaceAllTheFields(String content, Map<String, ?> data) {
        StringBuilder result = new StringBuilder();
        result.append(content);

        data.forEach((String key, Object value) -> {
            String keyWithBrackets = "{" + key + "}";

            int start = result.indexOf(keyWithBrackets);
            int end = keyWithBrackets.length() + start;

            //如果 content 中存在目标 key 则更新 content 字符串的数据
            if (result.toString().contains(keyWithBrackets))
                result.replace(start, end, value + "");
        });

        return result.toString();
    }

    /**
     * Object -> 长整数，万能判断版：自动分析内容，如果无法进行转换则返回 defaultValue 的值
     *
     * @param object       内容
     * @param defaultValue 默认数值
     * @return 返回转换后的结果
     */
    public static long objectToLong(Object object, long defaultValue) {
        String content = object + "";
        long result = defaultValue;

        //如果为数字
        if (isNumeric(content)) result = Long.parseLong(content);

        return result;
    }
}
