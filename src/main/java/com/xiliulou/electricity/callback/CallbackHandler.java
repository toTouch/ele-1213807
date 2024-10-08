package com.xiliulou.electricity.callback;


import com.xiliulou.electricity.dto.callback.CallbackContext;

import java.util.Map;

/**
 * <p>
 * Description: This interface is CallbackHandler!
 * </p>
 * <p>Project: saas-electricity</p>
 * <p>Copyright: Copyright (c) 2024</p>
 * <p>Company: xiliulou</p>
 *
 * @author <a href="mailto:wxblifeng@163.com">PeakLee</a>
 * @since V1.0 2024/8/27
 **/
public interface CallbackHandler<T> {
    
    int order();
    
    boolean support(Integer channel);
    
    CallbackContext<?> handler(CallbackContext<T> callbackContext);
    
    CallbackContext<?> success();
    
    CallbackContext<?> failed();
    
    Map<String,Object> PXZ_SUCCESS= Map.of("respCode", "000000");
    
    Map<String,Object> PXZ_FAILED= Map.of("respCode", "000001");
    
    String FY_SUCCESS="000000";
}
