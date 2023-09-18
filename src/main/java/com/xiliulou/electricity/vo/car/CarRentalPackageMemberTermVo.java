package com.xiliulou.electricity.vo.car;

import com.xiliulou.electricity.enums.MemberTermStatusEnum;
import com.xiliulou.electricity.enums.RentalPackageTypeEnum;
import lombok.Data;

import java.io.Serializable;

/**
 * 租车套餐会员期限展现层数据模型
 *
 * @author xiaohui.song
 **/
@Data
public class CarRentalPackageMemberTermVo implements Serializable {

    private static final long serialVersionUID = 9170850108947570840L;

    /**
     * 套餐类型
     * <pre>
     *     1-单车
     *     2-车电一体
     * </pre>
     * @see RentalPackageTypeEnum
     */
    private Integer rentalPackageType;

    /**
     * 到期时间
     */
    private Long dueTime;

    /**
     * 总计到期时间
     */
    private Long dueTimeTotal;

    /**
     * 状态
     * <pre>
     *     -1-已过期
     *     0-待生效
     *     1-正常
     *     2-申请冻结
     *     3-冻结
     *     4-申请退押
     *     5-申请退租
     * </pre>
     * @see MemberTermStatusEnum
     */
    private Integer status;

}
