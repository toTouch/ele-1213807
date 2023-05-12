package com.xiliulou.electricity.dto;

import lombok.Data;

/**
 * 柜机其他设置
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2022-08-11-18:27
 */
@Data
public class ElectricityCabinetOtherSetting {
    /**
     * 系统导航栏
     */
    private Integer systemBarStatus;
    /**
     * 反锁
     */
    private Integer revertLockStatus;
    /**
     * iot心跳检测
     */
    private Integer iotCheckStatus;
    /**
     * 自动温控
     */
    private Integer autoTempControlStatus;

    /**
     * 新硬件状态
     */
    private Integer newHardwareStatus;

    /**
     * 底层日志开启
     */
    private Integer logStatus;

    /**
     * 应用模式
     */
    private String applicationMode;
    /**
     * 加热的最低阀值
     */
    private String openHeatCondition;
    /**
     * 散热的起始条件
     */
    private String openFanCondition;
    /**
     * 普通模式下的充电电压
     */
    private String normalChargeV;
    /**
     * 普通模式下的充电电流
     */
    private String normalChargeA;
    /**
     * 换电标准
     */
    private String exchangeCondition;
    /**
     * 同时充电的个数
     */
    private Integer chargingNum;

    /**
     * 轮训上报的时间
     */
    private Integer loopTimeReport;
    /**
     * 充电停止上限
     */
    private String chargeMaxCondition;

    /**
     * 充电停止上限(电压)
     */
    private String chargeMaxConditionV;

    /**
     * 充电停止下限
     */
    private String chargeMinCondition;
    /**
     * 充电停止下限(电压)
     */
    private String chargeMinConditionV;
    /**
     * api地址
     */
    private String apiAddress;
    /**
     * 二维码地址
     */
    private String qrAddress;
    /**
     * 三元组下载地址
     */
    private String downloadAddress;
    /**
     * 耗电量倍率
     * */
    private Integer powerConsumptionMultiply;

    /**
     * 显示真实电量
     *
     * multi_v模式
     * */
    private Integer realQuantity;
    /**
     * 满电判断标准
     * multi——v模式
     * */
    private Integer maxBatteryStandard;

    private Integer enableBatteryExceptionCheck;

    private String bms;

    private Integer serverHeartBeat;

    /**
     * 开启电池BMS异常检测
     */
    private Integer enableBatteryBMSExceptionCheck;

    /**
     * 充电策略最大电流
     */
    private Integer defaultChargeStorageMaxA;
    private Integer aOpenChargeStrategy;
}
