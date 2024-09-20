package com.xiliulou.electricity.event;

import com.xiliulou.electricity.dto.message.ThirdPartyMallMessageDTO;
import com.xiliulou.electricity.enums.thirdParthMall.ThirdPartyMallEnum;
import com.xiliulou.electricity.enums.thirdParthMall.ThirdPartyMallMessageType;
import lombok.Getter;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.context.ApplicationEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * @author HeYafeng
 * @description
 * @date 2024/9/19 13:56:42
 */
@Getter
public class ThirdPartyMallEvent extends ApplicationEvent {
    
    private static final long serialVersionUID = 3959186933636172577L;
    
    private Integer tenantId;
    
    private ThirdPartyMallEnum mall;
    
    private ThirdPartyMallMessageType code;
    
    private final Map<String, Object> context;
    
    public ThirdPartyMallEvent(Object source, Map<String, Object> context) {
        super(source);
        this.context = context;
    }
    
    public ThirdPartyMallMessageDTO toDTO() {
        ThirdPartyMallMessageDTO messageDTO = new ThirdPartyMallMessageDTO();
        messageDTO.setCode(Optional.ofNullable(this.code).map(ThirdPartyMallMessageType::getCode).orElse(null));
        messageDTO.setContext(this.context);
        messageDTO.setMall(Optional.ofNullable(this.mall).map(ThirdPartyMallEnum::getCode).orElse(null));
        messageDTO.setTenantId(this.tenantId);
        return messageDTO;
    }
    
    public static ThirdPartyMallEvent.Builder builder(Object source) {
        return new ThirdPartyMallEvent.Builder(source);
    }
    
    public static class Builder {
        
        private final ThirdPartyMallEvent event;
        
        public Builder(Object source) {
            this.event = new ThirdPartyMallEvent(source, new HashMap<>());
        }
        
        public ThirdPartyMallEvent.Builder code(ThirdPartyMallMessageType code) {
            this.event.code = code;
            return this;
        }
        
        public ThirdPartyMallEvent.Builder mall(ThirdPartyMallEnum mall) {
            this.event.mall = mall;
            return this;
        }
        
        public ThirdPartyMallEvent.Builder tenantId(Integer tenantId) {
            this.event.tenantId = tenantId;
            return this;
        }
        
        public ThirdPartyMallEvent.Builder addContext(String key, Object value) {
            this.event.context.put(key, ObjectUtils.defaultIfNull(value, ""));
            return this;
        }
        
        public ThirdPartyMallEvent build() {
            return this.event;
        }
    }
    
}
