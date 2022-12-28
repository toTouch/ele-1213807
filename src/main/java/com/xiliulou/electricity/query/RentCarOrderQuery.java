package com.xiliulou.electricity.query;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2022-12-27-17:44
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RentCarOrderQuery {
//    @NotBlank(message = "车辆编码不能为空!")
    private String sn;

    @NotNull(message = "门店id不能为空!")
    private Long storeId;

    @NotNull(message = "车辆型号不能为空!")
    private Long carModelId;

    @NotNull(message = "车辆租赁时间不能为空!")
    private Integer rentTime;

    @NotBlank(message = "车辆租赁方式不能为空!")
    private String rentType;

    @NotBlank(message = "用户名称不能为空!")
    private String username;

    @NotBlank(message = "用户手机号不能为空!")
    private String phone;

    private Long size;
    private Long offset;
    private String name;
    private Long beginTime;
    private Long endTime;
    private Integer status;
    private String orderId;

    private Integer tenantId;

    private List<Long> franchiseeIds;

    private Long franchiseeId;

    private String franchiseeName;

    private Long uid;
    /**
     * 0--正常 1--删除
     */
    private Integer delFlag;

    private Integer depositType;
    private Integer payType;

    private Integer refundOrderType;

    private List<Long> storeIds;

    private String carModel;

    private String storeName;

    private Long id;
    /**
     * 车辆编号
     */
    private String carSn;
    /**
     * 租车押金
     */
    private Double carDeposit;
    /**
     * 订单类型(1--租车,2--还车)
     */
    private Integer type;
    /**
     * 交易方式
     */
    private Integer transactionType;
    /**
     * 备注
     */
    private String remark;
    /**
     * 创建时间
     */
    private Long createTime;
    /**
     * 创建时间
     */
    private Long updateTime;

}
