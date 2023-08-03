package com.xiliulou.electricity.vo;

import com.xiliulou.electricity.entity.ElectricityCarModel;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class DivisionAccountOperationRecordVO {
    private Long id;
    /**
     * 分帐配置名称
     */
    private String name;
    /**
     * 分帐层级
     */
    private Integer hierarchy;
    /**
     * 加盟商id
     */
    private Long franchiseeId;
    private String  franchiseeName;
    /**
     * 门店id
     */
    private Long storeId;
    private String storeName;

    private BigDecimal operatorRate;
    private BigDecimal operatorRateOther;
    private BigDecimal franchiseeRate;
    private BigDecimal franchiseeRateOther;
    private BigDecimal storeRate;
    /**
     * 状态（0-启用，1-禁用）
     */
    private Integer status;
    /**
     * 业务类型
     */
    private Integer type;

    private List<String> membercardNames;

    private List<BatteryMemberCardVO> memberCardList;

    private List<ElectricityCarModel> carModelList;

    private List<BatteryMemberCardVO> batteryPackages;

    private List<BatteryMemberCardVO> carRentalPackages;

    private List<BatteryMemberCardVO> carWithBatteryPackages;

    private Long createTime;

    private Long updateTime;




    /**
     * 用户绑定的用户名
     */
    private String userName;

    /**
     * 分账配置id
     */
    private Integer divisionAccountId;

    /**
     * 修改人id
     */
    private Long uid;

}
