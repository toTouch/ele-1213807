package com.xiliulou.electricity.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * (CupboardOtherConfig)实体类
 *
 * @author Eclair
 * @since 2021-07-21 15:22:55
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("t_ele_other_config")
public class EleOtherConfig {

    private Long id;
    /**
     * 机柜id
     */
    private Integer eid;
    /**
     * 4G卡号
     */
    private String cardNumber;


    private Integer tenantId;

    private Long createTime;

    private Long updateTime;

    private Integer delFlag;

    public static final Integer DEL_NORMAL = 0;
    public static final Integer DEL_DEL = 1;

}
