import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.electricity.constant.BatteryConstant;
import lombok.Data;
import org.junit.Test;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2023-04-10-18:49
 */
public class BatteryModelTest {

    /**
     * 12V的三元锂的3串
     */

    public static final String B_12V_TERNARY_LITHIUM_3 = "B_12V_TERNARY_LITHIUM_03";
    public static final String B_12V_TERNARY_SHORT_LITHIUM_3 = "12V/T/3";

    /**
     * 12V的铁锂的3串
     */

    public static final String B_12V_IRON_LITHIUM_3 = "B_12V_IRON_LITHIUM_03";
    public static final String B_12V_IRON_SHORT_LITHIUM_3 = "12V/I/3";

    /**
     * 24V的三元锂的7串
     */

    public static final String B_24V_TERNARY_LITHIUM_7 = "B_24V_TERNARY_LITHIUM_07";
    public static final String B_24V_TERNARY_SHORT_LITHIUM_7 = "24V/T/7";

    /**
     * 24V的铁锂的8串
     */

    public static final String B_24V_IRON_LITHIUM_8 = "B_24V_IRON_LITHIUM_08";
    public static final String B_24V_IRON_SHORT_LITHIUM_8 = "24V/I/8";

    /**
     * 36V的三元锂的10串
     */

    public static final String B_36V_TERNARY_LITHIUM_10 = "B_36V_TERNARY_LITHIUM_10";
    public static final String B_36V_TERNARY_SHORT_LITHIUM_10 = "36V/T/10";

    /**
     * 36V的铁锂的10串
     */

    public static final String B_36V_IRON_LITHIUM_10 = "B_36V_IRON_LITHIUM_10";
    public static final String B_36V_IRON_SHORT_LITHIUM_10 = "36V/I/10";

    /**
     * 36V的铁锂11串
     */

    public static final String B_36V_IRON_LITHIUM_11 = "B_36V_IRON_LITHIUM_11";
    public static final String B_36V_IRON_SHORT_LITHIUM_11 = "36V/I/11";

    /**
     * 36V的铁锂12串
     */

    public static final String B_36V_IRON_LITHIUM_12 = "B_36V_IRON_LITHIUM_12";
    public static final String B_36V_IRON_SHORT_LITHIUM_12 = "36V/I/12";

    /**
     * 48V的三元锂的13串
     */

    public static final String B_48V_TERNARY_LITHIUM_13 = "B_48V_TERNARY_LITHIUM_13";
    public static final String B_48V_TERNARY_SHORT_LITHIUM_13 = "48V/T/13";

    /**
     * 48V的三元锂的14串
     */

    public static final String B_48V_TERNARY_LITHIUM_14 = "B_48V_TERNARY_LITHIUM_14";
    public static final String B_48V_TERNARY_SHORT_LITHIUM_14 = "48V/T/14";

    /**
     * 48V的铁锂的15串
     */

    public static final String B_48V_IRON_LITHIUM_15 = "B_48V_IRON_LITHIUM_15";
    public static final String B_48V_IRON_SHORT_LITHIUM_15 = "48/I/15";

    /**
     * 48V的铁锂的16串
     */

    public static final String B_48V_IRON_LITHIUM_16 = "B_48V_IRON_LITHIUM_16";
    public static final String B_48V_IRON_SHORT_LITHIUM_16 = "48V/I/16";

    /**
     * 60V的铁锂的17串
     */

    public static final String B_60V_TERNARY_LITHIUM_17 = "B_60V_TERNARY_LITHIUM_17";
    public static final String B_60V_TERNARY_SHORT_LITHIUM_17 = "60V/T/17";

    /**
     * 60V的铁锂的20串
     */

    public static final String B_60V_IRON_LITHIUM_20 = "B_60V_IRON_LITHIUM_20";
    public static final String B_60V_IRON_SHORT_LITHIUM_20 = "60V/I/20";

    /**
     * 72V的三元锂的20串
     */

    public static final String B_72V_TERNARY_LITHIUM_20 = "B_72V_TERNARY_LITHIUM_20";
    public static final String B_72V_TERNARY_SHORT_LITHIUM_20 = "72V/T/20";

    /**
     * 72V的铁锂的24串
     */

    public static final String B_72V_IRON_LITHIUM_24 = "B_72V_IRON_LITHIUM_24";
    public static final String B_72V_IRON_SHORT_LITHIUM_24 = "72V/I/24";


    private static final LinkedHashMap<String, Float> BATTERY_V = new LinkedHashMap<>();
    private static final LinkedHashMap<String, String> BATTERY_V_SHORT_V = new LinkedHashMap<>();

    private static final LinkedHashMap<String, Integer> BATTERY = new LinkedHashMap<>();
    private static final LinkedHashMap<Integer, String> BATTERY_SHORT = new LinkedHashMap<>();

    static {
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

        BATTERY_SHORT.put(1,B_12V_TERNARY_LITHIUM_3);
        BATTERY_SHORT.put(2,B_12V_IRON_LITHIUM_3);
        BATTERY_SHORT.put(3,B_24V_TERNARY_LITHIUM_7);
        BATTERY_SHORT.put(4,B_24V_IRON_LITHIUM_8);
        BATTERY_SHORT.put(5,B_36V_TERNARY_LITHIUM_10);
        BATTERY_SHORT.put(6,B_36V_IRON_LITHIUM_10);
        BATTERY_SHORT.put(7,B_36V_IRON_LITHIUM_11);
        BATTERY_SHORT.put(8,B_36V_IRON_LITHIUM_12);
        BATTERY_SHORT.put(9,B_48V_TERNARY_LITHIUM_13);
        BATTERY_SHORT.put(10,B_48V_TERNARY_LITHIUM_14);
        BATTERY_SHORT.put(11,B_48V_IRON_LITHIUM_15);
        BATTERY_SHORT.put(12,B_48V_IRON_LITHIUM_16);
        BATTERY_SHORT.put(13,B_60V_TERNARY_LITHIUM_17);
        BATTERY_SHORT.put(14,B_60V_IRON_LITHIUM_20);
        BATTERY_SHORT.put(15,B_72V_TERNARY_LITHIUM_20);
        BATTERY_SHORT.put(16,B_72V_IRON_LITHIUM_24);


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
    }


    @Test
    public void test(){
        List<Battery> list=new ArrayList<>();
        Battery b1=new Battery();
        b1.setBatteryModel(1);
        b1.setBatteryType("B_12V_TERNARY_LITHIUM_03");
        b1.setBatteryV(12.6F);
        b1.setBatteryVShort("12V/T/3");

        Battery b2=new Battery();
        b2.setBatteryModel(2);
        b2.setBatteryType("B_12V_IRON_LITHIUM_03");
        b2.setBatteryV(14.6F);
        b2.setBatteryVShort("12V/I/3");

        Battery b3=new Battery();
        b3.setBatteryModel(3);
        b3.setBatteryType("B_24V_TERNARY_LITHIUM_07");
        b3.setBatteryV(29.4F);
        b3.setBatteryVShort("24V/T/7");

        Battery b4=new Battery();
        b4.setBatteryModel(4);
        b4.setBatteryType("B_24V_IRON_LITHIUM_08");
        b4.setBatteryV(29.2F);
        b4.setBatteryVShort("24V/I/8");

        Battery b5=new Battery();
        b5.setBatteryModel(5);
        b5.setBatteryType("B_36V_TERNARY_LITHIUM_10");
        b5.setBatteryV(42F);
        b5.setBatteryVShort("36V/T/10");

        Battery b6=new Battery();
        b6.setBatteryModel(6);
        b6.setBatteryType("B_36V_IRON_LITHIUM_10");
        b6.setBatteryV(36.5F);
        b6.setBatteryVShort("36V/I/10");

        Battery b7=new Battery();
        b7.setBatteryModel(7);
        b7.setBatteryType("B_36V_IRON_LITHIUM_11");
        b7.setBatteryV(40.15F);
        b7.setBatteryVShort("36V/I/11");

        Battery b8=new Battery();
        b8.setBatteryModel(8);
        b8.setBatteryType("B_36V_IRON_LITHIUM_12");
        b8.setBatteryV(43.8F);
        b8.setBatteryVShort("36V/I/12");

        Battery b9=new Battery();
        b9.setBatteryModel(9);
        b9.setBatteryType("B_48V_TERNARY_LITHIUM_13");
        b9.setBatteryV(54.6F);
        b9.setBatteryVShort("48V/T/13");

        Battery b10=new Battery();
        b10.setBatteryModel(10);
        b10.setBatteryType("B_48V_TERNARY_LITHIUM_14");
        b10.setBatteryV(58.8F);
        b10.setBatteryVShort("48V/T/14");

        Battery b11=new Battery();
        b11.setBatteryModel(11);
        b11.setBatteryType("B_48V_IRON_LITHIUM_15");
        b11.setBatteryV(54.8F);
        b11.setBatteryVShort("48/I/15");

        Battery b12=new Battery();
        b12.setBatteryModel(12);
        b12.setBatteryType("B_48V_IRON_LITHIUM_16");
        b12.setBatteryV(58.4F);
        b12.setBatteryVShort("48V/I/16");

        Battery b13=new Battery();
        b13.setBatteryModel(13);
        b13.setBatteryType("B_60V_TERNARY_LITHIUM_17");
        b13.setBatteryV(71.4F);
        b13.setBatteryVShort("60V/T/17");

        Battery b14=new Battery();
        b14.setBatteryModel(14);
        b14.setBatteryType("B_60V_IRON_LITHIUM_20");
        b14.setBatteryV(73F);
        b14.setBatteryVShort("60V/I/20");

        Battery b15=new Battery();
        b15.setBatteryModel(15);
        b15.setBatteryType("B_72V_TERNARY_LITHIUM_20");
        b15.setBatteryV(84F);
        b15.setBatteryVShort("72V/T/20");

        Battery b16=new Battery();
        b16.setBatteryModel(16);
        b16.setBatteryType("B_72V_IRON_LITHIUM_24");
        b16.setBatteryV(87.6F);
        b16.setBatteryVShort("72V/I/24");

        list.add(b1);
        list.add(b2);
        list.add(b3);
        list.add(b4);
        list.add(b5);
        list.add(b6);
        list.add(b7);
        list.add(b8);
        list.add(b9);
        list.add(b10);
        list.add(b11);
        list.add(b12);
        list.add(b13);
        list.add(b14);
        list.add(b15);
        list.add(b16);

        System.out.println(JsonUtil.toJson(list));
    }



    @Data
     class Battery {
        private Integer batteryModel;
        private String batteryType;
        private Float batteryV;
        private String batteryVShort;
    }
}
