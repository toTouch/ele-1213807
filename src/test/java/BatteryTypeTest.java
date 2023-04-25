import com.xiliulou.electricity.entity.BatteryMaterial;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2023-04-23-17:37
 */
@Slf4j
public class BatteryTypeTest {
    private static final String SEPARATOR = "_";

    private static final String SEPARATE = "/";

    @Test
    public void parseBatteryType(){
        String name = "BT207205824CN22080980529";

        System.out.println(analysisBatteryTypeByBatteryName(name));
    }

    public static String analysisBatteryTypeByBatteryName(String batteryName) {
        String type = "";

        try {
            //获取系统定义的电池材质
            List<BatteryMaterial> batteryMaterials = new ArrayList<>();
            BatteryMaterial batteryMaterial1 = new BatteryMaterial();
            batteryMaterial1.setKind(1);
            batteryMaterial1.setType("IRON_LITHIUM");
            batteryMaterial1.setShortType("I");
            batteryMaterials.add(batteryMaterial1);

            BatteryMaterial batteryMaterial2 = new BatteryMaterial();
            batteryMaterial2.setKind(2);
            batteryMaterial2.setType("TERNARY_LITHIUM");
            batteryMaterial2.setShortType("T");
            batteryMaterials.add(batteryMaterial2);

            if (CollectionUtils.isEmpty(batteryMaterials)) {
                log.error("ELE ERROR!battery type analysis fail,batteryMaterials is null,batteryName={}", batteryName);
                return type;
            }

            if (StringUtils.isBlank(batteryName) || batteryName.length() < 11) {
                log.error("ELE ERROR!battery type analysis fail,batteryName is illegal,batteryName={}", batteryName);
                return type;
            }

            StringBuilder modelTypeName = new StringBuilder("B_");
            char[] batteryChars = batteryName.toCharArray();

            //获取电压
            String chargeV = split(batteryChars, 4, 6);
            modelTypeName.append(chargeV).append("V").append(SEPARATOR);

            //获取材料体系
            char material = batteryChars[2];
            Map<String, String> materialMap = batteryMaterials.stream().collect(Collectors
                    .toMap(item -> String.valueOf(item.getKind()), BatteryMaterial::getType, (item1, item2) -> item2));

            //如果电池编码对应的材质不存在，返回空
            String materialName = materialMap.get(String.valueOf(material));
            if (StringUtils.isBlank(materialName)) {
                log.error("ELE ERROR!battery type analysis fail,materialName is blank,batteryName={}", batteryName);
                return type;
            }

            modelTypeName.append(materialName).append(SEPARATOR);
            modelTypeName.append(split(batteryChars, 9, 11));
            return modelTypeName.toString();
        } catch (Exception e) {
            log.error("ELE ERROR!battery type analysis fail,batteryName={}", batteryName, e);
        }

        return type;
    }


    private static String split(char[] strArray, int beginIndex, int endIndex) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = beginIndex; i < endIndex; i++) {
            stringBuilder.append(strArray[i]);
        }
        return stringBuilder.toString();
    }

}
