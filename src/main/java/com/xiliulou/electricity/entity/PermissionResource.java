package com.xiliulou.electricity.entity;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.baomidou.mybatisplus.annotation.TableName;
/**
 * (PermissionResource)实体类
 *
 * @author Eclair
 * @since 2020-12-09 15:38:22
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("t_permission_resource")
public class PermissionResource {

    @TableId(value = "id", type = IdType.AUTO)
	private Long id;
    /**
    * 权限名称
    */
    private String name;
    /**
    * 权限类型,1--页面 2--url
    */
    private Integer type;
    /**
    * 权限
    */
    private String uri;
    /**
    * 权限方法
    */
    private String method;
    /**
    * 权限顺序
    */
    private Double sort;
    /**
    * 父菜单id
    */
    private Long parent;
    /**
    * 描述
    */
    private String desc;

    private Long createTime;

    private Long updateTime;

    private Integer delFlag;

    public static final Integer DEL_NORMAL = 0;
    public static final Integer DEL_DEL = 1;

    public static final long MENU_ROOT = 0;

    public static final Integer TYPE_PAGE = 1;
    public static final Integer TYPE_URL = 2;
}
