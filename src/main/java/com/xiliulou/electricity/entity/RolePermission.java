package com.xiliulou.electricity.entity;















            import com.baomidou.mybatisplus.annotation.IdType;
            import com.baomidou.mybatisplus.annotation.TableId;
            import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.baomidou.mybatisplus.annotation.TableName;
            
            import java.util.Objects;

/**
 * (RolePermission)实体类
 *
 * @author Eclair
 * @since 2020-12-09 14:36:22
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("t_role_permission")
public class RolePermission implements Comparable<RolePermission> {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long roleId;

    private Long pId;

    public static final Integer DEL_NORMAL = 0;
    public static final Integer DEL_DEL = 1;
    
    @Override
    public int compareTo(RolePermission o) {
        if (Objects.isNull(o) || Objects.isNull(o.getRoleId()) || Objects.isNull(o.getPId())){
            return -1;
        }
        if (Objects.equals(o.getRoleId(), this.getRoleId()) && Objects.equals(o.getPId(), this.getPId())){
            return 0;
        }
        return 1;
    }
}
