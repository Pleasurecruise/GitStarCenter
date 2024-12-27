package cn.yiming1234.gitstarcenter.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.sql.Timestamp;

@Data
@TableName("interactions")
public class Interaction {
    @TableId(type = IdType.AUTO)
    private Integer id;
    @TableField(value = "source_user_id")
    private Integer sourceUserId;
    @TableField(value = "target_user_id")
    private Integer targetUserId;
    @TableField(value = "is_star")
    private Boolean isStar;
    @TableField(value = "is_fork")
    private Boolean isFork;
    @TableField(value = "is_follow")
    private Boolean isFollow;
    @TableField(value = "is_watch")
    private Boolean isWatch;
    @TableField(fill = FieldFill.INSERT)
    private Timestamp createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Timestamp updatedAt;
}