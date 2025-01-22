package com.xiliulou.electricity.constant;

/**
 * @author HeYafeng
 * @description 资产管理相关常量
 * @date 2023/11/28 14:56:15
 */
public class AssetConstant {
    
    public static final Integer DEL_NORMAL = 0;
    
    public static final Integer DEL_DEL = 1;
    
    /**
     * 库房状态：0-启用
     */
    public static final Integer ASSET_WAREHOUSE_STATUS_ENABLE = 0;
    
    /**
     * 默认库房名称
     */
    public static final String ASSET_WAREHOUSE_DEFAULT_NAME = "默认库房";
    
    /**
     * 库房状态：1-禁用
     */
    public static final Integer ASSET_WAREHOUSE_STATUS_DISABLE = 1;
    
    /**
     * 资产盘点（t_asset_inventory）状态：进行中
     */
    public static final Integer ASSET_INVENTORY_STATUS_TAKING = 0;
    
    /**
     * 资产盘点（t_asset_inventory）状态：完成
     */
    public static final Integer ASSET_INVENTORY_STATUS_FINISHED = 1;
    
    /**
     * 盘点详情（t_asset_inventory_detail）是否已盘点：0-未盘点
     */
    public static final Integer ASSET_INVENTORY_DETAIL_STATUS_NO = 0;
    
    /**
     * 盘点详情（t_asset_inventory_detail）是否已盘点：1-已盘点
     */
    public static final Integer ASSET_INVENTORY_DETAIL_STATUS_YES = 1;
    
    /**
     * 资产调拨数量限制
     */
    public static final Integer ASSET_ALLOCATE_LIMIT_NUMBER = 50;
    
    /**
     * 资产退库数量限制
     */
    public static final Integer ASSET_EXIT_WAREHOUSE_LIMIT_NUMBER = 3000;
    
    /***
     * 车辆批量入库 车牌号长度限制
     */
    public static final Integer ASSET_CAR_BATCH_SAVE_LICENSE_PLATE_NUMBER_SIZE = 10;
    
    /***
     * 车辆批量入库 车架号长度限制
     */
    public static final Integer ASSET_CAR_BATCH_SAVE_VIN_SIZE = 17;
    
    /***
     * 车辆批量入库 电机号长度限制
     */
    public static final Integer ASSET_CAR_BATCH_SAVE_MOTOR_NUMBER_SIZE = 17;
    
    /**
     * <p>
     *    Description: 类型为id的提交
     * </p>
    */
    public static final Integer ASSET_EXIT_WAREHOUSE_SUBMIT_TYPE_BY_ID = 0;
    
    /**
     * <p>
     *    Description: 类型为sn的提交
     * </p>
     */
    public static final Integer ASSET_EXIT_WAREHOUSE_SUBMIT_TYPE_BY_SN = 1;
    
    /**
     * 根据ID退库
     */
    public static final Integer ASSET_EXIT_WAREHOUSE_MODE_ID = 1;
    
    /**
     * 根据SN退库
     */
    public static final Integer ASSET_EXIT_WAREHOUSE_MODE_SN = 2;
    
}
