package cn.yiming1234.gitstarcenter.secruity.oauth2.user;

import cn.yiming1234.gitstarcenter.exception.OAuth2AuthenticationException;
import cn.yiming1234.gitstarcenter.enumeration.AuthProvider;

import java.util.Map;

public class OAuth2UserInfoFactory {
    public static OAuth2UserInfo getOAuth2UserInfo(String registrationId, Map<String, Object> attributes) {
        if (registrationId.equalsIgnoreCase(AuthProvider.GITHUB.toString())) {
            return new GithubOAuth2UserInfo(attributes);
        } else {
            throw new OAuth2AuthenticationException("Sorry! Login with " + registrationId + " is not supported yet.");
        }
    }
}
