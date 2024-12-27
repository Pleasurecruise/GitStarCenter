package cn.yiming1234.gitstarcenter.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.sql.Timestamp;

@Data
@TableName("repositories")
public class Repository {
    @TableId
    private Integer id;
    @TableField("user_id")
    private Integer userId;
    @TableField("repo_auth")
    private String repoAuth;
    @TableField("repo_name")
    private String repoName;
    @TableField("repo_bio")
    private String repoBio;
    @TableField("star_count")
    private int starCount;
    @TableField("fork_count")
    private int forkCount;
    @TableField("watch_count")
    private int watchCount;
    private String language;
    @TableField(fill = FieldFill.INSERT)
    private Timestamp createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Timestamp updatedAt;
}