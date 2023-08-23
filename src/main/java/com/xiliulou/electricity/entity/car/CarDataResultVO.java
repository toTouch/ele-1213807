package com.xiliulou.electricity.entity.car;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.xiliulou.electricity.vo.Jt808DeviceInfoVo;
import lombok.Data;

/**
 * 车辆数据模型
 */
@Data
public class CarDataResultVO {

    private CarDataVO carDataVO;

    private Jt808DeviceInfoVo jt808DeviceInfoVo;

}
