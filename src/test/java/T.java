import com.xiliulou.electricity.vo.merchant.MerchantCabinetPowerMonthDetailVO;

import java.util.ArrayList;
import java.util.List;

/**
 * @author HeYafeng
 * @description TODO
 * @date 2024/3/11 18:48:13
 */
public class T {
    
    public static void main(String[] args) {
        MerchantCabinetPowerMonthDetailVO detailVO1 = MerchantCabinetPowerMonthDetailVO.builder().placeId(4L).eid(5347L).date("2024-02-01").startPower(539913.00)
                .endPower(573427.00).sumPower(33514.00).sumCharge(126682.92).beginTime(1705248000000L).endTime(1705680000000L).build();
        MerchantCabinetPowerMonthDetailVO detailVO2 = MerchantCabinetPowerMonthDetailVO.builder().placeId(4L).eid(5347L).date("2024-02-01").startPower(663642.00)
                .endPower(691950.00).sumPower(28308.00).sumCharge(107004.24).beginTime(1706763600000L).endTime(1707098400000L).build();
        MerchantCabinetPowerMonthDetailVO detailVO3 = MerchantCabinetPowerMonthDetailVO.builder().placeId(4L).eid(5347L).date("2024-02-01").startPower(700388.00)
                .endPower(729069.00).sumPower(28681.00).sumCharge(108414.18).beginTime(1707195600000L).endTime(1707563196000L).build();
        MerchantCabinetPowerMonthDetailVO detailVO4 = MerchantCabinetPowerMonthDetailVO.builder().placeId(5L).eid(5347L).date("2024-02-01").startPower(577951.00)
                .endPower(692589.00).sumPower(114638.00).sumCharge(433331.64).beginTime(1705730400000L).endTime(1707109200000L).build();
        MerchantCabinetPowerMonthDetailVO detailVO5 = MerchantCabinetPowerMonthDetailVO.builder().placeId(5L).eid(5349L).date("2024-02-01").startPower(641933.00)
                .endPower(701667.00).sumPower(59734.00).sumCharge(201900.92).beginTime(1706418000000L).endTime(1707112395000L).build();
        MerchantCabinetPowerMonthDetailVO detailVO6 = MerchantCabinetPowerMonthDetailVO.builder().placeId(7L).eid(5347L).date("2024-02-01").startPower(728074.00)
                .endPower(866497.00).sumPower(138423.00).sumCharge(523238.94).beginTime(1707544810000L).endTime(1709222399000L).build();
        MerchantCabinetPowerMonthDetailVO detailVO7 = MerchantCabinetPowerMonthDetailVO.builder().placeId(7L).eid(5349L).date("2024-02-01").startPower(511947.00)
                .endPower(878839.00).sumPower(366892.00).sumCharge(1240094.96).beginTime(1704866432000L).endTime(1709222399000L).build();
        MerchantCabinetPowerMonthDetailVO detailVO8 = MerchantCabinetPowerMonthDetailVO.builder().placeId(7L).eid(5350L).date("2024-02-01").startPower(34046.00).endPower(874119.00)
                .sumPower(840073.00).sumCharge(3570310.25).beginTime(1699164000000L).endTime(1709222399000L).build();
        MerchantCabinetPowerMonthDetailVO detailVO9 = MerchantCabinetPowerMonthDetailVO.builder().placeId(8L).eid(0L).date("2024-02-01").startPower(0.00).endPower(0.00)
                .sumPower(0.00).sumCharge(0.00).beginTime(1706716800000L).endTime(1709222399000L).build();
        MerchantCabinetPowerMonthDetailVO detailVO10 = MerchantCabinetPowerMonthDetailVO.builder().placeId(1L).eid(0L).date("2024-02-01").startPower(0.00).endPower(0.00)
                .sumPower(0.00).sumCharge(0.00).beginTime(1706716800000L).endTime(1709222399000L).build();
        MerchantCabinetPowerMonthDetailVO detailVO11 = MerchantCabinetPowerMonthDetailVO.builder().placeId(2L).eid(0L).date("2024-02-01").startPower(0.00).endPower(0.00)
                .sumPower(0.00).sumCharge(0.00).beginTime(1706716800000L).endTime(1709222399000L).build();
        MerchantCabinetPowerMonthDetailVO detailVO12 = MerchantCabinetPowerMonthDetailVO.builder().placeId(3L).eid(0L).date("2024-02-01").startPower(0.00).endPower(0.00)
                .sumPower(0.00).sumCharge(0.00).beginTime(1706716800000L).endTime(1709222399000L).build();
        MerchantCabinetPowerMonthDetailVO detailVO13 = MerchantCabinetPowerMonthDetailVO.builder().placeId(9L).eid(0L).date("2024-02-01").startPower(0.00).endPower(0.00)
                .sumPower(0.00).sumCharge(0.00).beginTime(1706716800000L).endTime(1709222399000L).build();
        MerchantCabinetPowerMonthDetailVO detailVO14 = MerchantCabinetPowerMonthDetailVO.builder().placeId(10L).eid(0L).date("2024-02-01").startPower(0.00).endPower(0.00)
                .sumPower(0.00).sumCharge(0.00).beginTime(1706716800000L).endTime(1709222399000L).build();
        
        List<MerchantCabinetPowerMonthDetailVO> detailList = List.of(detailVO1, detailVO2, detailVO3, detailVO4, detailVO5, detailVO6, detailVO7, detailVO8, detailVO9, detailVO10,
                detailVO11, detailVO12, detailVO13, detailVO14);
        
        //排序：1.按sumPower是否等于0进行分组，将等于0的放在集合最后 2.将不等于0的按照placeId进行分组，每个分组内部按照beginTime进行倒叙排序。3.将2中各分组按照第一条数据的beginTime进行倒叙
        
    }
}
