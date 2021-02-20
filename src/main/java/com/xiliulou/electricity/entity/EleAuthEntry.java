package com.xiliulou.electricity.entity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.baomidou.mybatisplus.annotation.TableName;
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
    
    private Long id;
    /**
    * 资料项名称
    */
    private String name;
    /**
    * 资料项标识Id
    */
    private Object identity;
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
    
    private Object needModify;

    public static final Integer DEL_NORMAL = 0;
    public static final Integer DEL_DEL = 1;

}