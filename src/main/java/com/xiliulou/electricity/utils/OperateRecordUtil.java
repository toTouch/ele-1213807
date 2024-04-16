package com.xiliulou.electricity.utils;


import cn.hutool.core.bean.BeanUtil;
import com.xiliulou.core.thread.XllThreadPoolExecutorService;
import com.xiliulou.core.thread.XllThreadPoolExecutors;
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
    
    private final XllThreadPoolExecutorService executors;
    
    public OperateRecordUtil(OperationRecordProducer operationRecordProducer) {
        this.operationRecordProducer = operationRecordProducer;
        executors = XllThreadPoolExecutors.newFixedThreadPool("OPERATE_RECORD_UTIL_POOL",1,"operate-record-util-pool");
    }
    
    public void record(Object oldValue, Object newValue) {
        OperateLogDTO operateLogDTO = OperateLogDTO.ofWebRequest();
        record(oldValue,newValue,operateLogDTO);
    }
    
    public void record(Object oldValue, Object newValue,OperateLogDTO operateLogDTO) {
        final OperateLogDTO builder = builder(oldValue, newValue, operateLogDTO);
        operationRecordProducer.sendMessage(builder);
    }
    
    /**
     * <p>
     *    Description: 异步处理及发送操作记录
     * </p>
     * @param oldValue oldValue 旧值
     * @param newValue newValue 新值
     * @param operateArgs operateArgs 操作参数1
     * @param operateArgs2 operateArgs2 操作参数2
     * @param operateFunction operateFunction 操作函数
     * <p>Project: OperateRecordUtil</p>
     * <p>Copyright: Copyright (c) 2024</p>
     * <p>Company: www.xiliulou.com</p>
     * @author <a href="mailto:wxblifeng@163.com">PeakLee</a>
     * @since V1.0 2024/4/16
    */
    public <F,S> void asyncRecord(Object oldValue, Object newValue,final F operateArgs,final S operateArgs2, final OperateFunction<F,S> operateFunction) {
        final OperateLogDTO operateLogDTO = OperateLogDTO.ofWebRequest();
        final OperateLogDTO builder = builder(oldValue, newValue, operateLogDTO);
        executors.execute(() -> {
            OperateLogDTO operate = operateFunction.operate(operateArgs, operateArgs2, builder);
            operationRecordProducer.sendMessage(operate);
        });
    }
    
    @SuppressWarnings("all")
    private OperateLogDTO builder(Object oldValue, Object newValue,OperateLogDTO operateLogDTO) {
        if (Objects.isNull(operateLogDTO)){
            operateLogDTO = OperateLogDTO.ofWebRequest();
        }
        
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
        return operateLogDTO;
    }
    
    @FunctionalInterface
    public interface OperateFunction<F,S>{
        OperateLogDTO operate(F f,S s,OperateLogDTO o);
    }
}
