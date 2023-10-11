package com.xiliulou.electricity.vo;

import lombok.Data;

import java.util.List;

/**
 * @author zgw
 * @date 2022/8/17 17:04
 * @mood
 */
@Data
public class OtaRequestVo {

    private Integer upgradeType;

    private Long completeTime;

    private List<Integer> successCells;

    private List<Integer> failCells;

    private Boolean coreUpgradeResult;

    private Boolean operateResult;

    private String msg;
}
