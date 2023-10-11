package com.xiliulou.electricity.vo;

import lombok.Data;

/**
 * @author: HRP
 * @Date: 2022/03/07 16:45
 * @Description:
 */
@Data
public class OperateMsgVo {

    /**
     * 操作记录
     */
    private String operateMsg;

    /**
     * 操作时间
     */
    private Long operateTime;

}
