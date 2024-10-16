package com.xiliulou.electricity.callback.impl.pxz;


import com.xiliulou.electricity.callback.CallbackHandler;
import com.xiliulou.electricity.dto.callback.CallbackContext;
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
public interface PxzSupport<T> extends CallbackHandler<T> {
    default boolean support(Integer channel) {
        return FreeDepositServiceWayEnums.PXZ.getChannel().equals(channel);
    }
    
    default CallbackContext<?> success(){
        return CallbackContext.builder()
                .params(PXZ_SUCCESS)
                .next(Boolean.FALSE)
                .build();
    }
    
    default CallbackContext<?> failed(){
        return CallbackContext.builder()
                .params(PXZ_FAILED)
                .next(Boolean.FALSE)
                .build();
    }
}
