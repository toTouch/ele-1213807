package com.xiliulou.electricity.query.car;

import com.xiliulou.electricity.enums.PayStateEnum;
import com.xiliulou.electricity.enums.SlippageTypeEnum;
import lombok.Data;

import java.io.Serializable;

/**
 * 租车套餐订单逾期表，查询模型
 *
 * @author xiaohui.song
 **/
@Data
public class CarRentalPackageOrderSlippageQryReq implements Serializable {
    
    private static final long serialVersionUID = 2273061223218238582L;
    
    /**
     * 偏移量
     */
    private Integer offset = 0;
    
    /**
     * 取值数量
     */
    private Integer size = 10;
    
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
     * 订单编号
     */
    private String orderNo;
    
    /**
     * 用户ID
     */
    private Long uid;
    
    /**
     * 支付状态
     * <pre>
     *     1-未支付
     *     2-支付成功
     *     3-支付失败
     * </pre>
     *
     * @see PayStateEnum
     */
    private Integer payState;
    
    /**
     * 创建时间开始
     */
    private Long createTimeBegin;
    
    /**
     * 创建时间截止
     */
    private Long createTimeEnd;
    
    /**
     * 支付时间开始
     */
    private Long payTimeBegin;
    
    /**
     * 支付时间截止
     */
    private Long payTimeEnd;
    
    /**
     * 根据计费开始时间排序，1-倒叙(默认)，2-正序
     */
    private Integer orderByServiceFeeGenerateTime;
    
    /**
     * 根据支付时间排序，1-倒叙，2-正序
     */
    private Integer orderByPayTime;
    
    /**
     * <p>
     * Description: 套餐过期类型
     * <pre>
     *    1-过期
     *   2-冻结
     * </pre>
     * <a href="https://benyun.feishu.cn/wiki/GrNjwBNZkipB5wkiws2cmsEDnVU#OWZUdqItNo8IyKxAGhXcF303nmb">14.10 滞纳金记录（3条优化项）</a>
     *
     * @see SlippageTypeEnum
     * </p>
     */
    private Integer type;
    
    /**
     * @see com.xiliulou.core.base.enums.ChannelEnum
     */
    private String paymentChannel;
}
