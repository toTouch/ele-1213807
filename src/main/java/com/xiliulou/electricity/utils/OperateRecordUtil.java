package com.xiliulou.electricity.utils;


import cn.hutool.core.bean.BeanUtil;
import com.xiliulou.electricity.dto.operate.OperateLogDTO;
import com.xiliulou.electricity.mq.producer.OperationRecordProducer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Objects;

/**
 * <p>
 * Description: This class is OperateRecordUtil!
 * </p>
 * <p>Project: saas-electricity</p>
 * <p>Copyright: Copyright (c) 2024</p>
 * <p>Company: xiliulou</p>
 *
 * @author <a href="mailto:wxblifeng@163.com">PeakLee</a>
 * @since V1.0 2024/3/26
 **/
@Slf4j
@Component
public class OperateRecordUtil {
    
    private final OperationRecordProducer operationRecordProducer;
    
    public OperateRecordUtil(OperationRecordProducer operationRecordProducer) {
        this.operationRecordProducer = operationRecordProducer;
    }
    
    public void record(Object oldValue, Object newValue) {
        OperateLogDTO operateLogDTO = OperateLogDTO.ofWebRequest();
        record(oldValue,newValue,operateLogDTO);
    }
    
    @SuppressWarnings("unchecked")
    public void record(Object oldValue, Object newValue,OperateLogDTO operateLogDTO) {
        if (!Objects.isNull(oldValue)) {
            if (oldValue instanceof Map){
                operateLogDTO.setOldValue((Map<String, Object>) oldValue);
            }else {
                operateLogDTO.setOldValue(BeanUtil.beanToMap(oldValue, false, true));
            }
        }
        if (!Objects.isNull(newValue)) {
            if (newValue instanceof Map){
                operateLogDTO.setNewValue((Map<String, Object>) newValue);
            }else {
                operateLogDTO.setNewValue(BeanUtil.beanToMap(newValue, false, true));
            }
        }
        operationRecordProducer.sendMessage(operateLogDTO);
    }
}
