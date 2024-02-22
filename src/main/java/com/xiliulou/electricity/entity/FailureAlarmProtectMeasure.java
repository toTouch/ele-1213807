package com.xiliulou.electricity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 *故障告警设置-保护措施
 * @author maxiaodong
 * @date 2023-12-15 13:58:23
 */

@Data
@TableName("t_failure_alarm_protect_measure")
public class FailureAlarmProtectMeasure {
	@TableId(value = "id",type = IdType.AUTO)
	private Long id;
	
	/**
	 * 故障告警设置id
	 */
	private Long failureAlarmId;
	
	/**
	 * 保护措施(1- 上报， 2- 发出告警，3- 喇叭长鸣，4 -蜂鸣器响，5 -启动灭火装置，6-关闭电源、切断交流，7-切断交流，8-切断电源输出，9-切断输出，10，-启动加热，11-启动排风，12-启动充电，13-停止充电，14-停止放电，15-锁闭电池仓，
	 * 16-通信诊断后，可恢复解除原告警信息，17-仓门关闭且通信诊断后，可恢复解除原告警信息，18-禁止充电)
	 */
	private Integer protectMeasure;
}


