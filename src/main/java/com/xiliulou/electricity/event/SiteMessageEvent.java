package com.xiliulou.electricity.event;


import com.xiliulou.electricity.dto.message.MessageDTO;
import com.xiliulou.electricity.enums.message.SiteMessageType;
import lombok.Getter;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.context.ApplicationEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

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
public class SiteMessageEvent extends ApplicationEvent {
    
    private static final long serialVersionUID = 5960854795942420018L;
    
    private SiteMessageType code;
    
    private Long tenantId;
    
    private Long notifyTime;
    
    private final Map<String, Object> context;
    
    public SiteMessageEvent(Object source, Map<String, Object> context) {
        super(source);
        this.context = context;
    }
    
    public MessageDTO toDTO() {
        MessageDTO messageDTO = new MessageDTO();
        messageDTO.setCode(Optional.ofNullable(this.code).map(SiteMessageType::getCode).orElse(null));
        messageDTO.setContext(this.context);
        messageDTO.setNotifyTime(this.notifyTime);
        messageDTO.setTenantId(this.tenantId);
        return messageDTO;
    }
    
    public static Builder builder(Object source) {
        return new Builder(source);
    }
    
    public static class Builder {
        
        private final SiteMessageEvent event;
        
        public Builder(Object source) {
            this.event = new SiteMessageEvent(source, new HashMap<>());
        }
        
        public Builder code(SiteMessageType code) {
            this.event.code = code;
            return this;
        }
        
        public Builder notifyTime(Long notifyTime) {
            this.event.notifyTime = notifyTime;
            return this;
        }
        
        public Builder tenantId(Long tenantId) {
            this.event.tenantId = tenantId;
            return this;
        }
        
        public Builder addContext(String key, Object value) {
            this.event.context.put(key, ObjectUtils.defaultIfNull(value, ""));
            return this;
        }
        
        public SiteMessageEvent build() {
            this.event.notifyTime = System.currentTimeMillis();
            return this.event;
        }
    }
}
