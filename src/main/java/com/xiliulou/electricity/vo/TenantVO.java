package com.xiliulou.electricity.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2023-02-15-11:38
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TenantVO {

    private Integer id;
    /**
     * 租户名称
     */
    private String name;
    /**
     * 租户编号
     */
    private String code;
    /**
     * 0正常 1-冻结
     */
    private Integer status;


    private Long createTime;

    private Long updateTime;

    //过期时间
    private Long expireTime;

    /**
     * 人脸核身次数
     */
    private Integer faceRecognizeCapacity;

    private Integer freeDepositCapacity;

    private Integer esignCapacity;

}
