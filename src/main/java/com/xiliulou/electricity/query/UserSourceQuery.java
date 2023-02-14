package com.xiliulou.electricity.query;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2023-02-12-14:11
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSourceQuery {
    /**
     * 设备名称
     */
    private String deviceName;
    /**
     * 设备产品
     */
    private String productKey;
    /**
     * 用户来源 1：扫码，2：邀请，3：其它
     */
    @NotNull(message = "用户来源不能为空")
    private Integer source;

    private Long refId;

    private Long size;
    private Long offset;
    private Integer tenantId;
    private Long startTime;
    private Long endTime;
    /**
     * 用户名字
     */
    private String name;
    /**
     * 手机
     */
    private String phone;

    /**
     * 柜机
     */
    private Integer electricityCabinetId;
    private List<Integer> electricityCabinetIds;

    /**
     * 所属加盟商
     */
    private Long franchiseeId;


    private Long storeId;

    private Long uid;

}
