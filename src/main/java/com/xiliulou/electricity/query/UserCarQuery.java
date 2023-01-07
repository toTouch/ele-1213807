package com.xiliulou.electricity.query;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2023-01-07-13:53
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserCarQuery {

    private Long id;
    /**
     * uid
     */
    private Long uid;
    /**
     * 车辆id
     */
    private Long cid;
    /**
     * 车辆sn
     */
    private String sn;
    /**
     * 车辆型号
     */
    private Long carModel;
    /**
     * 0--正常 1--删除
     */
    private Integer delFlag;

    private Integer tenantId;

    private Long createTime;

    private Long updateTime;

}
