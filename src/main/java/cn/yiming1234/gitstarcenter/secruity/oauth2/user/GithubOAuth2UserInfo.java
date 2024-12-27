package cn.yiming1234.gitstarcenter.secruity.oauth2.user;

import java.util.Map;

public class GithubOAuth2UserInfo extends OAuth2UserInfo {
    public GithubOAuth2UserInfo(Map<String, Object> attributes) {
        super(attributes);
    }

    @Override
    public Long getId() {
        return ((Integer) attributes.get("id")).longValue();
    }

    @Override
    public String getName() {
        return (String) attributes.get("login");
    }

    @Override
    public String getEmail() {
        return (String) attributes.get("notification_email");
    }

    @Override
    public String getImageUrl() {
        return (String) attributes.get("avatar_url");
    }

}
