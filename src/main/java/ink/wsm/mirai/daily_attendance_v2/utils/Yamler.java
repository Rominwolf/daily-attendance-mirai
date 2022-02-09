package ink.wsm.mirai.daily_attendance_v2.utils;

import org.apache.commons.lang3.StringUtils;
import org.simpleyaml.configuration.ConfigurationSection;
import org.simpleyaml.configuration.file.YamlFile;

import java.util.*;
import java.util.stream.Collectors;

public class Yamler {
    private Exception error;
    private String filePath;
    public YamlFile yamlFile;

    /**
     * 初始化
     */
    public Yamler(String filePath) {
        this.filePath = filePath;
    }

    /**
     * 检测指定路径是否存在
     *
     * @param path 路径地址
     * @return 返回T/F
     */
    public boolean exist(String path) {
        return get(path) != null;
    }

    /**
     * 获取指定 Key 的 Value，不存在或获取失败返回 null
     */
    public Object get(String key) {
        open();
        Object result = yamlFile.get(key);
        close();
        return result;
    }

    /**
     * 获取字符串类型的 Value，不存在或获取失败则返回 ""
     */
    public String getString(String key) {
        String result = "";
        try {
            result = (String) get(key);
            if (result == null) result = "";
        } catch (Exception exception) {
            error = exception;
        }

        return result;
    }

    /**
     * 获取逻辑类型的 Value，不存在或获取失败则返回 false
     */
    public boolean getBoolean(String key) {
        boolean result = false;
        try {
            result = (boolean) get(key);
        } catch (Exception exception) {
            error = exception;
        }

        return result;
    }

    /**
     * 获取整数类型的 Value，不存在或获取失败则返回 0
     */
    public int getInt(String key) {
        int result = 0;
        try {
            result = (int) get(key);
        } catch (Exception exception) {
            error = exception;
        }

        return result;
    }

    /**
     * 获取长整数类型的 Value，不存在或获取失败则返回 0
     */
    public long getLong(String key) {
        long result = 0;
        try {
            result = Long.parseLong(get(key) + "");
        } catch (Exception exception) {
            error = exception;
        }

        return result;
    }

    /**
     * 获取浮点型类型的 Value，不存在或获取失败则返回 0
     */
    public double getDouble(String key) {
        double result = 0.0;
        try {
            result = (double) get(key);
        } catch (Exception exception) {
            error = exception;
        }

        return result;
    }

    /**
     * 获取 List as ? 类型的 Value，不存在或获取失败返回空的初始化后的 ArrayList
     */
    public List<?> getList(String key) {
        Object object = get(key);
        List<Object> result = new ArrayList<>();

        //如果 key 不存在则返回初始化的 List
        if (!exist(key)) return result;

        if (object instanceof ArrayList<?>)
            result.addAll((List<?>) object);

        return result;
    }

    /**
     * 获取 Set as ? 类型的 Keys，不存在或获取失败返回空的初始化后的 Set
     */
    public Set<?> getKey(String key) {
        Set<?> keys = new HashSet<>();

        //如果 key 不存在则返回初始化的 Set
        if (!exist(key)) return keys;

        open();
        ConfigurationSection section = yamlFile.getConfigurationSection(key);
        keys = section.getKeys(false);
        close();

        return keys;
    }

    /**
     * 设置指定 Key 的 Value，返回设定的结果
     */
    public Object set(String key, Object value) {
        open();
        yamlFile.set(key, value);
        save();
        close();
        return get(key);
    }

    /**
     * 设置指定字符串 Key 的 Value，返回设定后的结果。
     * mode 可以表示为 0(直接更新)，1(结尾附加"...")，-1(删除包含"value"的元素)。
     * Delimiter 表示为分隔符
     */
    public String setString(String key, Object value, int mode, String delimiter) {
        String temp = value + "";
        if (mode == 1)
            temp = getString(key) + value + delimiter;
        if (mode == -1)
            temp = StringUtils.replace(getString(key), value + delimiter, "");

        return set(key, temp) + "";
    }

    /**
     * 设置指定长整型 Key 的 Value(允许+/-)，返回设置后的结果。
     *
     * @param directUpdate 是否直接更新（表示不进行增加/删除）
     */
    public long setLong(String key, long value, boolean directUpdate) {
        long temp = value;

        //不进行直接更新
        if (!directUpdate)
            temp = getLong(key) + value;

        return Long.parseLong(set(key, temp) + "");
    }

    /**
     * 获取错误报告
     */
    public Exception getError() {
        return error;
    }

    /**
     * 与 Yaml 文件创建连接，返回成功与否
     */
    public void open() {
        yamlFile = new YamlFile(filePath);

        try {
            if (!yamlFile.exists())
                yamlFile.createNewFile(true);

            yamlFile.load();
        } catch (Exception exception) {
            error = exception;
        }

    }

    /**
     * 关闭与 Yaml 文件的连接，返回成功与否
     */
    public void close() {
        try {
            yamlFile = null;
        } catch (Exception exception) {
            error = exception;
        }

    }

    /**
     * 保存 Yaml 的更新，返回成功与否
     */
    public void save() {
        try {
            yamlFile.save();
        } catch (Exception e) {
            error = e;
        }

    }

    /**
     * 获取 List as Map 类型的排序结果，按要求进行排序整个目录下的所有文件的数据
     *
     * @param folderPath 欲检索的目录
     * @param path       路径
     * @param type       排序类型（asc/desc）
     * @param limit      保留多少条（为 -1 则全部保留）
     * @param offset     偏移量（仅 limit 不为 -1 时有效）
     * @return 返回 Map as String & Object 的排序结果，默认为初始化后的 List，
     * Map keys: key(无扩展文件名), value(内容), extension(扩展文件名)
     */
    public static List<Map<String, Object>>
    sortWithFolder(String folderPath, String path, String type,
                   int limit, int offset) {
        String orderType = type.toLowerCase();
        ArrayList<String> filePaths = Smart.getFiles(folderPath);
        List<Map<String, Object>> result = new ArrayList<>();

        //循环，将该目录下的所有文件的指定路径的值保存到返回变量中
        for (String filePath : filePaths) {
            String fileName = StringUtils.substringAfterLast(filePath, "\\");//文件名
            String key = StringUtils.substringBefore(fileName, ".");//无扩展文件名
            String extension = StringUtils.substringAfter(fileName, ".");//文件扩展名

            Yamler yamler = new Yamler(filePath);
            Object value = yamler.get(path);

            Map<String, Object> map = new HashMap<>();
            map.put("key", key);
            map.put("value", value);
            map.put("extension", extension);

            result.add(map);
        }

        //按要求进行排序
        result.sort((x, y) -> {
            Double one = Double.MIN_VALUE;
            Double two = Double.MIN_VALUE;

            Object xValue = x.get("value");
            Object yValue = y.get("value");

            //如果 x,y 不为 null 则写入真实数据
            if (xValue != null)
                one = Double.valueOf(xValue.toString());

            if (yValue != null)
                two = Double.valueOf(yValue.toString());

            //升序排序
            if (orderType.equals("asc")) return one.compareTo(two);

            return two.compareTo(one);
        });

        //如果 limit 不为 -1 则进行 offset/limit 限制
        if (limit != -1)
            result = result.stream().skip(offset).limit(limit)
                    .collect(Collectors.toList());

        return result;
    }

    /**
     * 根据提供的数字转换为增加/减少的数字形式，正数表示为1，负数表示为-1，0表示为1
     *
     * @param content 欲分析的数字
     * @return 返回1或-1
     */
    public static int numberSwitch(long content) {
        return content >= 0 ? 1 : -1;
    }
}
