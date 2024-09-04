package com.xiliulou.electricity.callback;


import com.xiliulou.electricity.dto.callback.UnfreeFakeParams;

import java.util.Map;

/**
 * <p>
 * Description: This interface is FreeDepositNotifyService!
 * </p>
 * <p>Project: saas-electricity</p>
 * <p>Copyright: Copyright (c) 2024</p>
 * <p>Company: xiliulou</p>
 *
 * @author <a href="mailto:wxblifeng@163.com">PeakLee</a>
 * @since V1.0 2024/8/26
 **/
public interface FreeDepositNotifyService {
    
    Object notify(Integer channel,Integer business,Integer tenantId, Map<String, Object> params);
    
    
    Object unfreeFakeNotify(UnfreeFakeParams params) throws Exception;
}
