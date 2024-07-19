package com.xiliulou.electricity.handler.exchange.impl;

import com.xiliulou.electricity.constant.ExchangeFailReaonConstants;
import com.xiliulou.electricity.dto.ExchangeReasonCellDTO;
import com.xiliulou.electricity.enums.ExchangeReasonTypeEnum;
import com.xiliulou.electricity.handler.exchange.BasicHandler;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @ClassName: OpenCellFailHandler
 * @description: 开门失败
 * @author: renhang
 * @create: 2024-07-19 14:06
 */
@Service
public class OpenCellFailHandler extends BasicHandler {
    
    public OpenCellFailHandler() {
        reasonCode = ExchangeReasonTypeEnum.OPEN_CELL_FAIL.getReasonCode();
    }
    
    @Override
    public Integer handler(ExchangeReasonCellDTO dto) {
        // 定义匹配数字的正则表达式
        Pattern pattern = Pattern.compile("\\d+\\.?\\d*");
        Matcher matcher = pattern.matcher(dto.getMsg());
        
        Integer cell = null;
        // 提取并打印匹配到的数字
        if (matcher.find()) {
            cell = Integer.valueOf(matcher.group());
        }
        if (Objects.isNull(cell)) {
            return null;
        }
        
        return Objects.equals(cell, dto.getNewCell()) ? ExchangeFailReaonConstants.NEW_CELL : ExchangeFailReaonConstants.OLD_CELL;
    }
}
