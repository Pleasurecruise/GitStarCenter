package cn.yiming1234.gitstarcenter.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.sql.Timestamp;

@Data
public class RepositoryVO {
    private Integer id;
    private Integer userId;
    private String username;
    private String repoAuth;
    private String repoName;
    private String repoBio;
    private int starCount;
    private int forkCount;
    private int watchCount;
    private int followerCount;
    private String language;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone="GMT+8")
    private Timestamp updatedAt;
}
