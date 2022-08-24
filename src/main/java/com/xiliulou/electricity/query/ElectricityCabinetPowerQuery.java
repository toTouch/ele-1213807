package com.xiliulou.electricity.query;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;

/**
 * @author: lxc
 * @Date: 2021/04/13 10:02
 * @Description:
 */
@Data
@Builder
public class ElectricityCabinetPowerQuery {

  private Long size;
  private Long offset;
  private Integer electricityCabinetId;
  /**
   * 日期
   */
  private LocalDate date;

  private String electricityCabinetName;

  private Integer tenantId;

  private Long beginTime;
  private Long endTime;
}
