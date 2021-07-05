package com.xiliulou.electricity.entity;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.baomidou.mybatisplus.annotation.TableName;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 实名认证资料项(TEleAuthEntry)实体类
 *
 * @author makejava
 * @since 2021-02-20 13:37:11
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("t_ele_auth_entry")
public class EleAuthEntry {

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    /**
    * 资料项名称
    */
    private String name;
    //是否使用（0--使用,1--不使用）
    private Integer isUse;
    /**
    * 资料项类型 select---下拉选择框 input--输入框 radio--单选 file--文件上传
    */
    private String type;
    /**
    * 创建时间
    */
    private Long createTime;
    /**
    * 更新时间
    */
    private Long updateTime;
    /**
    * 删除标记
    */
    private Object delFlag;
    /**
    * 备注
    */
    private String remark;

    //是否可以修改(1--姓名，2--身份证，3--身份证正面 ，4--身份证反面)
    private Integer identify;

    //租户id
    private Integer tenantId;

    public static final Integer DEL_NORMAL = 0;
    public static final Integer DEL_DEL = 1;

    public static final String TYPE_SELECT = "select";
    public static final String TYPE_RADIO = "radio";
    public static final String TYPE_INPUT = "input";
    public static final String TYPE_FILE = "file";
    //姓名
    public static final Integer ID_NAME_ID = 1;
    //身份证
    public static final Integer ID_ID_CARD = 2;
    //身份证正面
    public static final  Integer ID_CARD_FRONT_PHOTO = 3;
    //身份证反面
    public static final  Integer ID_CARD_BACK_PHOTO = 4;


    //使用
    public static final Integer IS_USE = 0;
    //默认不使用
    public static final Integer NO_USE = 1;

}
