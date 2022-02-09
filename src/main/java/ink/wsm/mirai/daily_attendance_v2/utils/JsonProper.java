package ink.wsm.mirai.daily_attendance_v2.utils;

import base.props4j.util.FileUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;

/**
 * 文件配置读取操作（自动保存）
 */
public class JsonProper {
    private final String fileName;

    public JsonProper(String fileName) {
        this.fileName = fileName;
        createFile(fileName);
    }

    /**
     * 获取数据<br>
     * key：完全按照 Json 标准（如：first.list[3].value），在结尾添加新元素表示子项
     */
    public Object getValue(String... keys) {
        Object rawJson = readFile(this.fileName);

        try {
            for (String key : keys) {
                //如果存在“[”，则按照数组类型处理
                if (key.contains("[")) {
                    int id = Integer.parseInt(StringUtils.substringBetween(key, "[", "]"));
                    key = StringUtils.substringBefore(key, "[");
                    JSONArray jsonTemp = JSONObject.parseObject(rawJson.toString()).getJSONArray(key);
                    rawJson = jsonTemp.get(id);
                    continue;
                }

                rawJson = JSONObject.parseObject(rawJson.toString()).get(key);
            }
        } catch (Exception e) {
            //Mirai.createLog("error", e.fillInStackTrace());
            return null;
        }

        return rawJson;
    }

    /**
     * 获取数据，如果不存在则返回 0
     */
    public int getIntValue(String... keys) {
        Object value = getValue(keys);
        if (value == null) return 0;
        return ((Number) value).intValue();
    }

    /**
     * 获取数据，如果不存在则返回 0
     */
    public long getLongValue(String... keys) {
        Object value = getValue(keys);
        if (value == null) return 0;
        return ((Number) value).longValue();
    }

    /**
     * 获取数据，如果不存在则返回 ""
     */
    public String getStringValue(String... keys) {
        Object value = getValue(keys);
        if (value == null) return "";
        return value.toString();
    }

    /**
     * 获取数据，如果不存在则返回 false
     */
    public boolean getBooleanValue(String... keys) {
        Object value = getValue(keys);
        if (value == null) return false;
        return (Boolean) value;
    }

    /**
     * 读取文件
     */
    public Object readFile(String fileName) {
        String content = null;
        File file = new File(fileName);
        try {
            content = FileUtil.readFileUTF8(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return content;
    }

    /**
     * 写入文件
     */
    public void writeFile(String fileName, Object content) {
        File file = new File(fileName);
        try {
            FileUtil.writeFileUTF8(file, content.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 如果文件不存在则创建
     */
    public boolean createFile(String fileName) {
        File file = new File(fileName);
        if (file.exists()) return true;
        try {
            file.createNewFile();
        } catch (IOException e) {
            return false;
        }
        return true;
    }
}
