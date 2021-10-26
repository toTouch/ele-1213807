
import com.alibaba.excel.util.StringUtils;
import org.junit.Test;


/**
 * @author: Miss.Li
 * @Date: 2021/9/17 15:14
 * @Description:
 */
public class BatteryTest {

	public static final String TERNARY_LITHIUM = "TERNARY_LITHIUM";
	public static final String IRON_LITHIUM = "IRON_LITHIUM";

	@Test
	public void test1() {
		String batteryName="BT2048026130000210911001";
		String result=parseBatteryNameAcquireBatteryModel(batteryName);
		System.out.println(result);
	}

	public static String parseBatteryNameAcquireBatteryModel(String batteryName) {
		if (StringUtils.isEmpty(batteryName) || batteryName.length() < 11) {
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
			modelName.append(IRON_LITHIUM).append("_");
		} else {
			modelName.append(TERNARY_LITHIUM).append("_");
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
}
