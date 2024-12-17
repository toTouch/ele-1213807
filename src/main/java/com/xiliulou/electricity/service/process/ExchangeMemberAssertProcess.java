package com.xiliulou.electricity.service.process;

import com.xiliulou.electricity.dto.ExchangeAssertProcessDTO;
import com.xiliulou.electricity.dto.ExchangeMemberResultDTO;
import com.xiliulou.electricity.entity.UserInfo;
import com.xiliulou.electricity.enums.PackageTypeEnum;
import com.xiliulou.electricity.enums.YesNoEnum;
import com.xiliulou.electricity.service.pipeline.ProcessContext;
import com.xiliulou.electricity.service.process.handler.ExchangeBasicHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Objects;

/**
 * @ClassName: ExchangeMemberAssertProcess
 * @description: 套餐校验处理器
 * @author: renhang
 * @create: 2024-11-12 14:55
 */
@Service("exchangeMemberAssertProcess")
@Slf4j
public class ExchangeMemberAssertProcess extends AbstractExchangeCommonHandler implements ExchangeAssertProcess<ExchangeAssertProcessDTO> {
    
    @Resource
    private ApplicationContext applicationContext;
    
    
    @Override
    public void process(ProcessContext<ExchangeAssertProcessDTO> context) {
        
        UserInfo userInfo = context.getProcessModel().getUserInfo();
        
        Triple<Boolean, String, Object> triple = null;
        if (Objects.equals(userInfo.getBatteryDepositStatus(), UserInfo.BATTERY_DEPOSIT_STATUS_YES)) {
            triple = applicationContext.getBean(PackageTypeEnum.PACKAGE_TYPE_BATTERY.getServiceImpl(), ExchangeBasicHandler.class).handler(context.getProcessModel().getUserInfo());
        } else if (Objects.equals(userInfo.getCarBatteryDepositStatus(), YesNoEnum.YES.getCode())) {
            triple = applicationContext.getBean(PackageTypeEnum.PACKAGE_TYPE_CAR_BATTERY.getServiceImpl(), ExchangeBasicHandler.class)
                    .handler(context.getProcessModel().getUserInfo());
        } else {
            log.warn("ExchangeMemberAssertProcess WARN! not pay deposit,uid={}", userInfo.getUid());
            breakChain(context, "ELECTRICITY.0042", "未缴纳押金");
            return;
        }
        if (Objects.isNull(triple)) {
            breakChain(context, "000001", "系统获取套餐异常");
            return;
        }
        
        // 校验失败
        if (!triple.getLeft()) {
            breakChain(context, triple.getMiddle(), String.valueOf(triple.getRight()));
            return;
        }
        
        ExchangeMemberResultDTO dto = (ExchangeMemberResultDTO) triple.getRight();
        context.getProcessModel().getChainObject().setElectricityBattery(dto.getElectricityBattery()).setElectricityConfig(dto.getElectricityConfig());
    }
    
    
}
