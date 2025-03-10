package com.xiliulou.electricity.event;

import com.xiliulou.electricity.dto.message.ThirdPartyDataDTO;
import com.xiliulou.electricity.enums.thirdParth.ThirdPartyDataType;
import lombok.Getter;
import org.apache.commons.lang3.ObjectUtils;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * @author HeYafeng
 * @description
 * @date 2024/9/19 13:56:42
 */
@Getter
public class ThirdPartyEvent implements Serializable {
    
    private static final long serialVersionUID = 3959186933636172577L;
    
    private String traceId;
    
    private Integer tenantId;
    
    private ThirdPartyDataType type;
    
    private final Map<String, Object> context;
    
    private Integer delayLevel;
    
    public ThirdPartyEvent(Map<String, Object> context) {
        this.context = context;
    }
    
    public ThirdPartyDataDTO toDTO() {
        ThirdPartyDataDTO messageDTO = new ThirdPartyDataDTO();
        messageDTO.setTraceId(this.traceId);
        messageDTO.setTenantId(this.tenantId);
        messageDTO.setType(Optional.ofNullable(this.type).map(ThirdPartyDataType::getCode).orElse(null));
        messageDTO.setContext(this.context);
        return messageDTO;
    }
    
    public static ThirdPartyEvent.Builder builder() {
        return new ThirdPartyEvent.Builder();
    }
    
    public static class Builder {
        
        private final ThirdPartyEvent event;
        
        public Builder() {
            this.event = new ThirdPartyEvent(new HashMap<>());
        }
        
        public ThirdPartyEvent.Builder traceId(String traceId) {
            this.event.traceId = traceId;
            return this;
        }
        
        public ThirdPartyEvent.Builder type(ThirdPartyDataType type) {
            this.event.type = type;
            return this;
        }
        
        public ThirdPartyEvent.Builder tenantId(Integer tenantId) {
            this.event.tenantId = tenantId;
            return this;
        }
        
        
        public ThirdPartyEvent.Builder addContext(String key, Object value) {
            this.event.context.put(key, ObjectUtils.defaultIfNull(value, ""));
            return this;
        }
        
        public ThirdPartyEvent.Builder delayLevel(Integer delayLevel) {
            this.event.delayLevel = delayLevel;
            return this;
        }
        
        public ThirdPartyEvent build() {
            return this.event;
        }
    }
    
}
