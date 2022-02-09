package ink.wsm.mirai.daily_attendance_v2.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

public class RedPacket {
    private double minMoney = 0.01;//最小获得金额
    private int scale = 2;//小数位数
    private int remainSize;//剩余包数量
    private BigDecimal remainMoney;//剩余金额

    public RedPacket(long money, int size) {
        remainMoney = BigDecimal.valueOf(money);
        remainSize = size;
    }

    public void setScale(int scale) {
        this.scale = scale;
    }

    public void setMinMoney(double minMoney) {
        this.minMoney = minMoney;
    }

    /**
     * 获取红包分配结果
     */
    public List<Long> result() {
        int size = remainSize;

        List<Long> result = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            long money = ((Number) getRandomMoney()).longValue();
            result.add(money);
        }

        return result;
    }

    private BigDecimal getRandomMoney() {
        if (remainSize == 1) {
            remainSize--;
            return remainMoney.setScale(scale, RoundingMode.DOWN);
        }

        BigDecimal random = BigDecimal.valueOf(Math.random());
        BigDecimal min = BigDecimal.valueOf(minMoney);

        BigDecimal halfRemainSize = BigDecimal.valueOf(remainSize).divide(new BigDecimal(2), RoundingMode.UP);
        BigDecimal max1 = remainMoney.divide(halfRemainSize, RoundingMode.DOWN);
        BigDecimal minRemainAmount = min.multiply(BigDecimal.valueOf(remainSize - 1)).setScale(scale, RoundingMode.DOWN);
        BigDecimal max2 = remainMoney.subtract(minRemainAmount);
        BigDecimal max = (max1.compareTo(max2) < 0) ? max1 : max2;

        BigDecimal money = random.multiply(max).setScale(scale, RoundingMode.DOWN);
        money = money.compareTo(min) < 0 ? min : money;

        remainSize--;
        remainMoney = remainMoney.subtract(money).setScale(scale, RoundingMode.DOWN);

        return money;
    }
}
