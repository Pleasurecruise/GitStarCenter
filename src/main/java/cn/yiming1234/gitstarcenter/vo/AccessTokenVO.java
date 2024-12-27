package cn.yiming1234.gitstarcenter.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AccessTokenVO {
    private String username;
    private String token;
}
