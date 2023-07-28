package com.xiliulou.electricity.vo.insurance;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 用户保险信息
 *
 * @author xiaohui.song
 **/
@Data
public class UserInsuranceVO implements Serializable {

    private static final long serialVersionUID = -4443513794335742581L;

    /**
     * 保险名称
     */
    private String insuranceName;

    /**
     * 过期时间
     */
    private Long insuranceExpireTime;

    /**
     * 保费
     */
    private BigDecimal premium;


}
