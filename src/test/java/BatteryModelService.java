import android.content.Context;

import com.blankj.utilcode.util.StringUtils;
import com.xiliulou.electricity.constant.BatteryConstant;
import com.xiliulou.electricity.utils.SPUtil;

public class BatteryModelService {
    public static String parseBatteryNameAcquireBatteryModel(String batteryName) {
        if (StringUtils.isTrimEmpty(batteryName) || batteryName.length() < 11) {
            return "";
        }

        StringBuilder modelName = new StringBuilder("B_");
        char[] batteryChars = batteryName.toCharArray();

        //获取电压
        String chargeV = split(batteryChars, 4, 7);
        modelName.append(chargeV).append("V").append("_");

        //获取材料体系
        char material = batteryChars[2];
        if (material == '1') {
            modelName.append(BatteryConstant.IRON_LITHIUM).append("_");
        } else {
            modelName.append(BatteryConstant.TERNARY_LITHIUM).append("_");
        }

        modelName.append(split(batteryChars, 9, 11));
        return modelName.toString();
    }

    private static String split(char[] strArray, int beginIndex, int endIndex) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = beginIndex; i < endIndex; i++) {
            stringBuilder.append(strArray[i]);
        }
        return stringBuilder.toString();
    }

    public static Float parseBatteryAcquireChargeV(String batteryName) {
        return BatteryConstant.queryChargeVByBatteryModel(parseBatteryNameAcquireBatteryModel(batteryName));
    }

    public static Integer parseBatteryAcquireChargeA(Context context, String batteryName) {
        return acquireBatteryModelChargeA(context, parseBatteryNameAcquireBatteryModel(batteryName));
    }

    public static Integer acquireBatteryModelChargeA(Context context, String longKey) {
        return (Integer) SPUtil.get(context, longKey, 0);
    }
}
