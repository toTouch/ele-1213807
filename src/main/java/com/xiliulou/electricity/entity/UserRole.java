package com.xiliulou.electricity.entity;















            import com.baomidou.mybatisplus.annotation.IdType;
            import com.baomidou.mybatisplus.annotation.TableId;
            import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.baomidou.mybatisplus.annotation.TableName;
/**
 * (UserRole)实体类
 *
 * @author Eclair
 * @since 2020-12-09 14:19:41
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("t_user_role")
public class UserRole {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long uid;

    private Long roleId;

    public static final Integer DEL_NORMAL = 0;
    public static final Integer DEL_DEL = 1;

}
