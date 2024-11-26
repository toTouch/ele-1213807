package com.xiliulou.electricity.service.process;

import cn.hutool.core.util.StrUtil;
import com.xiliulou.electricity.dto.ExchangeAssertProcessDTO;
import com.xiliulou.electricity.entity.ElectricityCabinetBox;
import com.xiliulou.electricity.service.ElectricityCabinetBoxService;
import com.xiliulou.electricity.service.pipeline.ProcessContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Objects;

/**
 * @ClassName: ExchangeCabinetCellAssertProcess
 * @description: 柜机格挡校验处理器
 * @author: renhang
 * @create: 2024-11-19 11:55
 */
@Service("exchangeCabinetCellAssertProcess")
@Slf4j
public class ExchangeCabinetCellAssertProcess extends AbstractExchangeCommonHandler implements ExchangeAssertProcess<ExchangeAssertProcessDTO> {
    
    @Resource
    private ElectricityCabinetBoxService electricityCabinetBoxService;
    
    @Override
    public void process(ProcessContext<ExchangeAssertProcessDTO> context) {
        
        ExchangeAssertProcessDTO processModel = context.getProcessModel();
        ElectricityCabinetBox cabinetBox = electricityCabinetBoxService.queryByCellNo(processModel.getEid(), String.valueOf(processModel.getCellNo()));
        if (Objects.isNull(cabinetBox)) {
            log.warn("CabinetCellAssert Warn! boxCell is null, eid is {} , cell is {}", processModel.getEid(), processModel.getCellNo());
            breakChain(context, "100553", "未找到此仓门");
            return;
        }
        
        if (StrUtil.isBlank(cabinetBox.getSn())) {
            log.warn("CabinetCellAssert Warn! batterySn is null, eid is {} , cell is {}", processModel.getEid(), processModel.getCellNo());
            breakChain(context, "100569", "格挡电池不存在");
            return;
        }
        
        if (Objects.equals(cabinetBox.getIsLock(), ElectricityCabinetBox.OPEN_DOOR)) {
            log.warn("CabinetCellAssert Warn! boxCell is not close, eid is {} , cell is {}", processModel.getEid(), processModel.getCellNo());
            breakChain(context, "100568", processModel.getCellNo() + "号仓门未关闭");
            return;
        }
        
        context.getProcessModel().getChainObject().setBatteryName(cabinetBox.getSn());
    }
    
}
