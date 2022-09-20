package com.xiliulou.electricity.entity;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.baomidou.mybatisplus.annotation.TableName;


/**
 * (PermissionTemplate)表实体类
 *
 * @author zzlong
 * @since 2022-09-19 16:34:06
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("t_permission_template")
public class PermissionTemplate {

    private Long id;
    /**
     * 权限id
     */
    private Long pid;
    /**
     * 权限类型，0：不显示的权限，1：运营商，2：加盟商，3：门店，
     */
    private Integer type;

    public static final Integer DEL_NORMAL = 0;
    public static final Integer DEL_DEL = 1;

    //不显示的权限
    public static final Integer TYPE_UNSHOW = 0;
    //运营商
    public static final Integer TYPE_OPERATE = 1;
    //加盟商
    public static final Integer TYPE_FRANCHISEE = 2;
    //门店
    public static final Integer TYPE_STORE = 3;



}
