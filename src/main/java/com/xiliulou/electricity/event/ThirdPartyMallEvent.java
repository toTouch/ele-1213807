package com.xiliulou.electricity.event;

import com.xiliulou.electricity.dto.message.ThirdPartyMallDataDTO;
import com.xiliulou.electricity.enums.thirdParthMall.ThirdPartyMallDataType;
import lombok.Getter;
import org.apache.commons.lang3.ObjectUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * @author HeYafeng
 * @description
 * @date 2024/9/19 13:56:42
 */
@Getter
public class ThirdPartyMallEvent {
    
    private static final long serialVersionUID = 3959186933636172577L;
    
    private String traceId;
    
    private Integer tenantId;
    
    private Integer mall;
    
    private ThirdPartyMallDataType type;
    
    private final Map<String, Object> context;
    
    private Integer delayLevel;
    
    public ThirdPartyMallEvent(Map<String, Object> context) {
        this.context = context;
    }
    
    public ThirdPartyMallDataDTO toDTO() {
        ThirdPartyMallDataDTO messageDTO = new ThirdPartyMallDataDTO();
        messageDTO.setTraceId(this.traceId);
        messageDTO.setTenantId(this.tenantId);
        messageDTO.setMall(this.mall);
        messageDTO.setType(Optional.ofNullable(this.type).map(ThirdPartyMallDataType::getCode).orElse(null));
        messageDTO.setContext(this.context);
        return messageDTO;
    }
    
    public static ThirdPartyMallEvent.Builder builder() {
        return new ThirdPartyMallEvent.Builder();
    }
    
    public static class Builder {
        
        private final ThirdPartyMallEvent event;
        
        public Builder() {
            this.event = new ThirdPartyMallEvent(new HashMap<>());
        }
        
        public ThirdPartyMallEvent.Builder traceId(String traceId) {
            this.event.traceId = traceId;
            return this;
        }
        
        public ThirdPartyMallEvent.Builder type(ThirdPartyMallDataType type) {
            this.event.type = type;
            return this;
        }
        
        public ThirdPartyMallEvent.Builder mall(Integer mall) {
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
        
        public ThirdPartyMallEvent.Builder delayLevel(Integer delayLevel) {
            this.event.delayLevel = delayLevel;
            return this;
        }
        
        public ThirdPartyMallEvent build() {
            return this.event;
        }
    }
    
}
