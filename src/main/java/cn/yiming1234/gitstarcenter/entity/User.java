package cn.yiming1234.gitstarcenter.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import net.minidev.json.annotate.JsonIgnore;

import java.sql.Timestamp;

@Data
@TableName("users")
public class User {
    @TableId(type = IdType.AUTO)
    private Integer id;
    @TableField(value = "github_id")
    private Long githubId;
    private String username;
    private String nickname;
    @JsonIgnore
    private String password;
    private String email;
    @TableField(value = "avatar_url")
    private String avatarUrl;
    @TableField(value = "follower_count")
    private int followerCount;
    @TableField(value = "following_count")
    private int followingCount;
    @TableField(value = "created_at")
    private Timestamp createdAt;
    @TableField(value = "updated_at")
    private Timestamp updatedAt;
}