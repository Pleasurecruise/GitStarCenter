package cn.yiming1234.gitstarcenter.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserVO {
    private int id;
    private String username;
    private String nickname;
    private String avatar;
    private String email;
    private String repoAuth;
    private String repoName;
    private int starCount;
    private int forkCount;
    private int watchCount;
    private int followerCount;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone="GMT+8")
    private Timestamp repoUpdateTime;
}
