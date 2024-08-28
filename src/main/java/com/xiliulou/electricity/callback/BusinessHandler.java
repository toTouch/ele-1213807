package com.xiliulou.electricity.callback;


import com.xiliulou.electricity.entity.FreeDepositOrder;

/**
 * <p>
 * Description: This interface is BusinessHandler!
 * </p>
 * <p>Project: saas-electricity</p>
 * <p>Copyright: Copyright (c) 2024</p>
 * <p>Company: xiliulou</p>
 *
 * @author <a href="mailto:wxblifeng@163.com">PeakLee</a>
 * @since V1.0 2024/8/27
 **/
public interface BusinessHandler {
    
    boolean support(Integer type);
    
    boolean freeDeposit(FreeDepositOrder order);
    
    boolean unfree(FreeDepositOrder order);
    
    boolean authPay(FreeDepositOrder order);
    
    default void timeout(FreeDepositOrder order){};
}
