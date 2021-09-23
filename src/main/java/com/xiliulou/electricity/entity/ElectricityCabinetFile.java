package com.xiliulou.electricity.entity;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.baomidou.mybatisplus.annotation.TableName;
/**
 * 换电柜文件表(TElectricityCabinetFile)实体类
 *
 * @author makejava
 * @since 2020-11-27 10:17:18
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("t_electricity_cabinet_file")
public class ElectricityCabinetFile {

    @TableId(value = "id",type = IdType.AUTO)
    private Long id;
    /**
    * 文件名称
    */
    private String name;
    /**
    * 换电柜id
    */
    private Integer otherId;
    /**
    * 类型
    */
    private Integer type;
    /**
    * 文件次序
    */
    private Integer sequence;
    /**
    * 文件的url
    */
    private String url;
    /**
    * 保存路径
    */
    private String bucketName;
    /**
    * 是否oss 0--是 1--否
    */
    private Integer isOss;
    /**
    * 创建时间
    */
    private Long createTime;
    /**
    * 更新时间
    */
    private Long updateTime;

    //租户id
    private Integer tenantId;

    //换电柜柜机图片
    public static final Integer TYPE_ELECTRICITY_CABINET = 1;

    //邀请活动图片
    public static final Integer TYPE_SHARE_ACTIVITY = 2;

    //门店商品图片
    public static final Integer TYPE_STORE_GOODS = 3;

}
