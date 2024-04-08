package com.xiliulou.electricity.domain.car;


import lombok.Data;

/**
 * <p>
 *    Description: This class is UserDepositPayTypeDo!
 * </p>
 * <p>Project: saas-electricity</p>
 * <p>Copyright: Copyright (c) 2024</p>
 * <p>Company: xiliulou</p>
 * @author <a href="mailto:wxblifeng@163.com">PeakLee</a>
 * <a herf="https://benyun.feishu.cn/wiki/GrNjwBNZkipB5wkiws2cmsEDnVU#Nffzd1GUWoZOWAxqnV9cXzk2nQh">15.1  实名用户列表（16条优化项）</a>
 * @since V1.0 2024/3/13
**/
@Data
public class UserDepositPayTypeDO {
    /**
     * <p>
     *    Description: 订单编号
     * </p>
    */
    private String orderNo;
    
    /**
     * <p>
     *    Description: 支付类型 1-线上、2-线下、3-免押
     * </p>
     */
    private Integer payType;
    
}
