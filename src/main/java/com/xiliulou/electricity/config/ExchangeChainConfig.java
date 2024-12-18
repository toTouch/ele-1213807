package com.xiliulou.electricity.config;

import com.xiliulou.electricity.enums.ExchangeAssertChainTypeEnum;
import com.xiliulou.electricity.service.pipeline.ExchangeProcessChain;
import com.xiliulou.electricity.service.pipeline.ProcessController;
import com.xiliulou.electricity.service.process.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * @ClassName: ExchangeChainConfig
 * @description: 换电校验责任链配置
 * @author: renhang
 * @create: 2024-11-15 13:52
 */
@Configuration
public class ExchangeChainConfig {

    @Resource
    private ExchangeCabinetCellAssertProcess exchangeCabinetCellAssertProcess;

    @Resource
    private ExchangeUserInfoAssertProcess exchangeUserInfoAssertProcess;

    @Resource
    private ExchangeCabinetAssertProcess exchangeCabinetAssertProcess;

    @Resource
    private ExchangeEndOrderAssertProcess exchangeEndOrderAssertProcess;

    @Resource
    private ExchangeMemberAssertProcess exchangeMemberAssertProcess;

    @Resource
    private SelfOpenCellAssertProcess selfOpenCellAssertProcess;

    @Resource
    private ExchangeUnFinishedOrderAssertProcess exchangeUnFinishedOrderAssertProcess;

    @Bean("exchangeProcessChain")
    public ExchangeProcessChain exchangeProcessChain() {
        ExchangeProcessChain processChain = new ExchangeProcessChain();
        // 如要扩展，添加枚举值以及自定义责任链顺序
        processChain.setProcessList(Arrays.asList(exchangeCabinetAssertProcess, exchangeCabinetCellAssertProcess, exchangeUserInfoAssertProcess, exchangeMemberAssertProcess,
                exchangeEndOrderAssertProcess));
        return processChain;
    }

    /**
     * 租电自主仓
     *
     * @return ExchangeProcessChain
     */
    @Bean("rentBatteryLessSelfOpenProcessChain")
    public ExchangeProcessChain rentBatteryLessSelfOpenProcessChain() {
        ExchangeProcessChain processChain = new ExchangeProcessChain();
        processChain.setProcessList(
                Arrays.asList(exchangeCabinetAssertProcess, exchangeUserInfoAssertProcess, selfOpenCellAssertProcess, exchangeUnFinishedOrderAssertProcess));
        return processChain;
    }

    @Bean("processController")
    public ProcessController apiProcessController() {
        ProcessController processController = new ProcessController();
        Map<Integer, ExchangeProcessChain> templateConfig = new HashMap<>(4);
        templateConfig.put(ExchangeAssertChainTypeEnum.QUICK_EXCHANGE_ASSERT.getCode(), exchangeProcessChain());
        templateConfig.put(ExchangeAssertChainTypeEnum.RENT_BATTERY_LESS_OPEN_FULL_ASSERT.getCode(), rentBatteryLessSelfOpenProcessChain());
        processController.setProcessMap(templateConfig);
        return processController;
    }


}
