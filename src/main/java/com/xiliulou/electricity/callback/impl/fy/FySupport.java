package com.xiliulou.electricity.callback.impl.fy;


import com.xiliulou.electricity.callback.CallbackHandler;
import com.xiliulou.electricity.constant.FreeDepositConstant;
import com.xiliulou.electricity.dto.callback.CallbackContext;
import com.xiliulou.electricity.entity.FreeDepositOrder;
import com.xiliulou.electricity.enums.FreeDepositServiceWayEnums;

/**
 * <p>
 * Description: This class is AbstractPxz!
 * </p>
 * <p>Project: saas-electricity</p>
 * <p>Copyright: Copyright (c) 2024</p>
 * <p>Company: xiliulou</p>
 *
 * @author <a href="mailto:wxblifeng@163.com">PeakLee</a>
 * @since V1.0 2024/8/27
 **/
public interface FySupport<T> extends CallbackHandler<T> {
    default boolean support(Integer channel) {
        return FreeDepositServiceWayEnums.FY.getChannel().equals(channel);
    }
    default CallbackContext<?> success(){
        return CallbackContext.builder()
                .params(FreeDepositConstant.FY_SUCCESS)
                .next(Boolean.FALSE)
                .build();
    }
    
    default CallbackContext<?> failed(){
        return CallbackContext.builder()
                .params(FreeDepositConstant.FY_FAILED)
                .next(Boolean.FALSE)
                .build();
    }
}
