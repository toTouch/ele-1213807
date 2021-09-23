package com.xiliulou.electricity.constant;

import java.util.LinkedHashMap;
import java.util.Set;

public class BatteryConstant {

	private static final LinkedHashMap<String, Float> BATTERY_V = new LinkedHashMap<>();
	private static final LinkedHashMap<String, String> BATTERY_V_SHORT_V = new LinkedHashMap<>();
	private static final LinkedHashMap<String, Integer> BATTERY = new LinkedHashMap<>();

	/**
	 * 三元锂
	 */

	public static final String TERNARY_LITHIUM = "TERNARY_LITHIUM";
	public static final String TERNARY_SHORT_LITHIUM = "T";

	/**
	 * 铁锂
	 */

	public static final String IRON_LITHIUM = "IRON_LITHIUM";
	public static final String IRON_SHORT_LITHIUM = "I";

	/**
	 * 12V的三元锂的3串
	 */

	public static final String B_12V_TERNARY_LITHIUM_3 = "B_12V_" + TERNARY_LITHIUM + "_03";
	public static final String B_12V_TERNARY_SHORT_LITHIUM_3 = "12V/" + TERNARY_SHORT_LITHIUM + "/3";

	/**
	 * 12V的铁锂的3串
	 */

	public static final String B_12V_IRON_LITHIUM_3 = "B_12V_" + IRON_LITHIUM + "_03";
	public static final String B_12V_IRON_SHORT_LITHIUM_3 = "12V/" + IRON_SHORT_LITHIUM + "/3";

	/**
	 * 24V的三元锂的7串
	 */

	public static final String B_24V_TERNARY_LITHIUM_7 = "B_24V_" + TERNARY_LITHIUM + "_07";
	public static final String B_24V_TERNARY_SHORT_LITHIUM_7 = "24V/" + TERNARY_SHORT_LITHIUM + "/7";

	/**
	 * 24V的铁锂的8串
	 */

	public static final String B_24V_IRON_LITHIUM_8 = "B_24V_" + IRON_LITHIUM + "_08";
	public static final String B_24V_IRON_SHORT_LITHIUM_8 = "24V/" + IRON_SHORT_LITHIUM + "/8";

	/**
	 * 36V的三元锂的10串
	 */

	public static final String B_36V_TERNARY_LITHIUM_10 = "B_36V_" + TERNARY_LITHIUM + "_10";
	public static final String B_36V_TERNARY_SHORT_LITHIUM_10 = "36V/" + TERNARY_SHORT_LITHIUM + "/10";

	/**
	 * 36V的铁锂的10串
	 */

	public static final String B_36V_IRON_LITHIUM_10 = "B_36V_" + IRON_LITHIUM + "_10";
	public static final String B_36V_IRON_SHORT_LITHIUM_10 = "36V/" + IRON_SHORT_LITHIUM + "/10";

	/**
	 * 36V的铁锂11串
	 */

	public static final String B_36V_IRON_LITHIUM_11 = "B_36V_" + IRON_LITHIUM + "_11";
	public static final String B_36V_IRON_SHORT_LITHIUM_11 = "36V/" + IRON_SHORT_LITHIUM + "/11";

	/**
	 * 36V的铁锂12串
	 */

	public static final String B_36V_IRON_LITHIUM_12 = "B_36V_" + IRON_LITHIUM + "_12";
	public static final String B_36V_IRON_SHORT_LITHIUM_12 = "36V/" + IRON_SHORT_LITHIUM + "/12";

	/**
	 * 48V的三元锂的13串
	 */

	public static final String B_48V_TERNARY_LITHIUM_13 = "B_48V_" + TERNARY_LITHIUM + "_13";
	public static final String B_48V_TERNARY_SHORT_LITHIUM_13 = "48V/" + TERNARY_SHORT_LITHIUM + "/13";

	/**
	 * 48V的三元锂的14串
	 */

	public static final String B_48V_TERNARY_LITHIUM_14 = "B_48V_" + TERNARY_LITHIUM + "_14";
	public static final String B_48V_TERNARY_SHORT_LITHIUM_14 = "48V/" + TERNARY_SHORT_LITHIUM + "/14";

	/**
	 * 48V的铁锂的15串
	 */

	public static final String B_48V_IRON_LITHIUM_15 = "B_48V_" + IRON_LITHIUM + "_15";
	public static final String B_48V_IRON_SHORT_LITHIUM_15 = "48/" + IRON_SHORT_LITHIUM + "/15";

	/**
	 * 48V的铁锂的16串
	 */

	public static final String B_48V_IRON_LITHIUM_16 = "B_48V_" + IRON_LITHIUM + "_16";
	public static final String B_48V_IRON_SHORT_LITHIUM_16 = "48V/" + IRON_SHORT_LITHIUM + "/16";

	/**
	 * 60V的铁锂的17串
	 */

	public static final String B_60V_TERNARY_LITHIUM_17 = "B_60V_" + TERNARY_LITHIUM + "_17";
	public static final String B_60V_TERNARY_SHORT_LITHIUM_17 = "60V/" + TERNARY_SHORT_LITHIUM + "/17";

	/**
	 * 60V的铁锂的20串
	 */

	public static final String B_60V_IRON_LITHIUM_20 = "B_60V_" + IRON_LITHIUM + "_20";
	public static final String B_60V_IRON_SHORT_LITHIUM_20 = "60V/" + IRON_SHORT_LITHIUM + "/20";

	/**
	 * 72V的三元锂的20串
	 */

	public static final String B_72V_TERNARY_LITHIUM_20 = "B_72V_" + TERNARY_LITHIUM + "_20";
	public static final String B_72V_TERNARY_SHORT_LITHIUM_20 = "72V/" + TERNARY_SHORT_LITHIUM + "/20";

	/**
	 * 72V的铁锂的24串
	 */

	public static final String B_72V_IRON_LITHIUM_24 = "B_72V_" + IRON_LITHIUM + "_24";
	public static final String B_72V_IRON_SHORT_LITHIUM_24 = "72V/" + IRON_SHORT_LITHIUM + "/24";

	static {
		BATTERY_V.put(B_12V_TERNARY_LITHIUM_3, 12.6F);
		BATTERY_V.put(B_12V_IRON_LITHIUM_3, 14.6F);
		BATTERY_V.put(B_24V_TERNARY_LITHIUM_7, 29.4F);
		BATTERY_V.put(B_24V_IRON_LITHIUM_8, 29.2F);
		BATTERY_V.put(B_36V_IRON_LITHIUM_10, 36.5F);
		BATTERY_V.put(B_36V_TERNARY_LITHIUM_10, 42F);
		BATTERY_V.put(B_36V_IRON_LITHIUM_11, 40.15F);
		BATTERY_V.put(B_36V_IRON_LITHIUM_12, 43.8F);
		BATTERY_V.put(B_48V_TERNARY_LITHIUM_13, 54.6F);
		BATTERY_V.put(B_48V_TERNARY_LITHIUM_14, 58.8F);
		BATTERY_V.put(B_48V_IRON_LITHIUM_15, 54.8F);
		BATTERY_V.put(B_48V_IRON_LITHIUM_16, 58.4F);
		BATTERY_V.put(B_60V_TERNARY_LITHIUM_17, 71.4F);
		BATTERY_V.put(B_60V_IRON_LITHIUM_20, 73F);
		BATTERY_V.put(B_72V_TERNARY_LITHIUM_20, 84F);
		BATTERY_V.put(B_72V_IRON_LITHIUM_24, 87.6F);

		BATTERY_V_SHORT_V.put(B_12V_TERNARY_LITHIUM_3, B_12V_TERNARY_SHORT_LITHIUM_3);
		BATTERY_V_SHORT_V.put(B_12V_IRON_LITHIUM_3, B_12V_IRON_SHORT_LITHIUM_3);
		BATTERY_V_SHORT_V.put(B_24V_TERNARY_LITHIUM_7, B_24V_TERNARY_SHORT_LITHIUM_7);
		BATTERY_V_SHORT_V.put(B_24V_IRON_LITHIUM_8, B_24V_IRON_SHORT_LITHIUM_8);
		BATTERY_V_SHORT_V.put(B_36V_IRON_LITHIUM_10, B_36V_IRON_SHORT_LITHIUM_10);
		BATTERY_V_SHORT_V.put(B_36V_TERNARY_LITHIUM_10, B_36V_TERNARY_SHORT_LITHIUM_10);
		BATTERY_V_SHORT_V.put(B_36V_IRON_LITHIUM_11, B_36V_IRON_SHORT_LITHIUM_11);
		BATTERY_V_SHORT_V.put(B_36V_IRON_LITHIUM_12, B_36V_IRON_SHORT_LITHIUM_12);
		BATTERY_V_SHORT_V.put(B_48V_TERNARY_LITHIUM_13, B_48V_TERNARY_SHORT_LITHIUM_13);
		BATTERY_V_SHORT_V.put(B_48V_TERNARY_LITHIUM_14, B_48V_TERNARY_SHORT_LITHIUM_14);
		BATTERY_V_SHORT_V.put(B_48V_IRON_LITHIUM_15, B_48V_IRON_SHORT_LITHIUM_15);
		BATTERY_V_SHORT_V.put(B_48V_IRON_LITHIUM_16, B_48V_IRON_SHORT_LITHIUM_16);
		BATTERY_V_SHORT_V.put(B_60V_TERNARY_LITHIUM_17, B_60V_TERNARY_SHORT_LITHIUM_17);
		BATTERY_V_SHORT_V.put(B_60V_IRON_LITHIUM_20, B_60V_IRON_SHORT_LITHIUM_20);
		BATTERY_V_SHORT_V.put(B_72V_TERNARY_LITHIUM_20, B_72V_TERNARY_SHORT_LITHIUM_20);
		BATTERY_V_SHORT_V.put(B_72V_IRON_LITHIUM_24, B_72V_IRON_SHORT_LITHIUM_24);


		BATTERY.put(B_12V_TERNARY_LITHIUM_3, 1);
		BATTERY.put(B_12V_IRON_LITHIUM_3, 2);
		BATTERY.put(B_24V_TERNARY_LITHIUM_7, 3);
		BATTERY.put(B_24V_IRON_LITHIUM_8, 4);
		BATTERY.put(B_36V_TERNARY_LITHIUM_10, 5);
		BATTERY.put(B_36V_IRON_LITHIUM_10, 6);
		BATTERY.put(B_36V_IRON_LITHIUM_11, 7);
		BATTERY.put(B_36V_IRON_LITHIUM_12, 8);
		BATTERY.put(B_48V_TERNARY_LITHIUM_13, 9);
		BATTERY.put(B_48V_TERNARY_LITHIUM_14, 10);
		BATTERY.put(B_48V_IRON_LITHIUM_15, 11);
		BATTERY.put(B_48V_IRON_LITHIUM_16, 12);
		BATTERY.put(B_60V_TERNARY_LITHIUM_17, 13);
		BATTERY.put(B_60V_IRON_LITHIUM_20, 14);
		BATTERY.put(B_72V_TERNARY_LITHIUM_20, 15);
		BATTERY.put(B_72V_IRON_LITHIUM_24, 16);
	}

	public static Float queryChargeVByBatteryModel(String model) {
		Float chargeV = BATTERY_V.get(model);
		return chargeV == null ? 0.0F : chargeV;
	}

	public static Set<String> acquireBatteryModels() {
		return BATTERY_V_SHORT_V.keySet();
	}

	public static String acquireBatteryShortModel(String longKey) {
		return BATTERY_V_SHORT_V.get(longKey);
	}

	public static Integer acquireBattery(String longKey) {
		return BATTERY.get(longKey);
	}
}

