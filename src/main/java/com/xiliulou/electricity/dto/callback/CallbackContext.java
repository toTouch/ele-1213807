package com.xiliulou.electricity.dto.callback;


import lombok.Builder;
import lombok.Getter;

/**
 * <p>
 * Description: This class is CallbackDTO!
 * </p>
 * <p>Project: saas-electricity</p>
 * <p>Copyright: Copyright (c) 2024</p>
 * <p>Company: xiliulou</p>
 *
 * @author <a href="mailto:wxblifeng@163.com">PeakLee</a>
 * @since V1.0 2024/8/27
 **/
@Getter
@Builder
public class CallbackContext<T> {
    
    private Integer channel;
    
    private Integer tenantId;
    
    private Integer business;
    
    private Integer type;
    
    private boolean next;
    
    private boolean success;
    
    private T params;
    
}
