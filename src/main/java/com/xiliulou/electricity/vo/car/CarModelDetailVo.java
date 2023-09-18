package com.xiliulou.electricity.vo.car;

import com.xiliulou.electricity.vo.StoreVO;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 车辆型号 VO
 *
 * @author xiaohui.song
 **/
@Data
public class CarModelDetailVo implements Serializable {

    private static final long serialVersionUID = -3397808849014441627L;

    /**
     * 车辆型号Id
     */
    private Integer id;

    /**
     * 型号名称
     */
    private String name;

    /**
     * 车辆型号图片
     */
    private List<String> pictureUrls;

    /**
     * 其它参数
     */
    private String otherProperties;

    /**
     * 门店信息
     */
    private StoreVO store;


}
