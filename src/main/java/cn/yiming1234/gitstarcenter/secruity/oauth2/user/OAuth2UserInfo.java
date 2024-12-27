package cn.yiming1234.gitstarcenter.secruity.oauth2.user;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * 统一用户信息
 */
@Getter
@Slf4j
public abstract class OAuth2UserInfo {
    protected Map<String, Object> attributes;

    public OAuth2UserInfo(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    public abstract Long getId();

    public abstract String getName();

    public abstract String getEmail();

    public abstract String getImageUrl();
}
