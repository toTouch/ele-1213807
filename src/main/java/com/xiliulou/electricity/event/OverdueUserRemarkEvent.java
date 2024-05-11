package com.xiliulou.electricity.event;


import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * <p>
 * Description: This class is OverdueUserRemarkEvent!
 * </p>
 * <p>Project: saas-electricity</p>
 * <p>Copyright: Copyright (c) 2024</p>
 * <p>Company: xiliulou</p>
 *
 * @author <a href="mailto:wxblifeng@163.com">PeakLee</a>
 * @since V1.0 2024/5/11
 **/
@Getter
public class OverdueUserRemarkEvent extends ApplicationEvent {
    
    private final Long uid;
    
    private final Integer type;
    
    private final Integer tenantId;
    
    public OverdueUserRemarkEvent(Object source, Long uid, Integer type, Integer tenantId) {
        super(source);
        this.uid = uid;
        this.type = type;
        this.tenantId = tenantId;
    }
}
