package com.xiliulou.electricity.entity.car.basic;

import com.xiliulou.electricity.enums.DelFlagEnum;
import lombok.Data;

import java.io.Serializable;

/**
 * 租车基础持久类
 *
 * @author xiaohui.song
 **/
@Data
public class BasicCarPO implements Serializable {

    private static final long serialVersionUID = 8272918001150547084L;

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 租户ID
     */
    private Integer tenantId;

    /**
     * 加盟商ID
     */
    private Integer franchiseeId;

    /**
     * 门店ID
     */
    private Integer storeId;

    /**
     * 创建人ID
     */
    private Long createUid;

    /**
     * 修改人ID
     */
    private Long updateUid;

    /**
     * 创建时间，时间戳
     */
    private Long createTime;

    /**
     * 修改时间，时间戳
     */
    private Long updateTime;

    /**
     * 删除标识
     * <pre>
     *     0-正常
     *     1-删除
     * </pre>
     * @see DelFlagEnum
     */
    private Integer delFlag = DelFlagEnum.OK.getCode();
}
