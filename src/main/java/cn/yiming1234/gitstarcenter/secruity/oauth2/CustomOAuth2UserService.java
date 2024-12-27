package cn.yiming1234.gitstarcenter.secruity.oauth2;

import cn.yiming1234.gitstarcenter.entity.User;
import cn.yiming1234.gitstarcenter.mapper.UserMapper;
import cn.yiming1234.gitstarcenter.secruity.UserPrincipal;
import cn.yiming1234.gitstarcenter.secruity.oauth2.user.OAuth2UserInfo;
import cn.yiming1234.gitstarcenter.secruity.oauth2.user.OAuth2UserInfoFactory;
import cn.yiming1234.gitstarcenter.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Slf4j
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserMapper userMapper;
    private final UserService userService;
    public CustomOAuth2UserService(UserMapper userMapper, UserService userService) {
        this.userMapper = userMapper;
        this.userService = userService;
    }

    /**
     * 处理OAuth2User
     */
    @Override
    public OAuth2User loadUser(OAuth2UserRequest oAuth2UserRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(oAuth2UserRequest);
        try {
            return processOAuth2User(oAuth2UserRequest, oAuth2User);
        } catch (AuthenticationException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new InternalAuthenticationServiceException(ex.getMessage(), ex.getCause());
        }
    }

    private OAuth2User processOAuth2User(OAuth2UserRequest oAuth2UserRequest, OAuth2User oAuth2User) {
        OAuth2UserInfo oAuth2UserInfo = OAuth2UserInfoFactory.getOAuth2UserInfo(oAuth2UserRequest.getClientRegistration().getRegistrationId(), oAuth2User.getAttributes());
        Optional<User> userOptional = Optional.ofNullable(userMapper.selectByUsername(oAuth2UserInfo.getName()));
        User user = userOptional.orElse(new User());
        if(userOptional.isEmpty()) {
            user.setGithubId(oAuth2UserInfo.getId());
            user.setUsername(oAuth2UserInfo.getName());
            user.setNickname(oAuth2User.getAttribute("name"));
            user.setEmail(oAuth2UserInfo.getEmail());
            user.setAvatarUrl(oAuth2UserInfo.getImageUrl());
            user.setFollowerCount(oAuth2User.getAttribute("followers"));
            user.setFollowingCount(oAuth2User.getAttribute("following"));
            userService.saveUserInfo(user);
        }
        user.setGithubId(oAuth2UserInfo.getId());
        user.setUsername(oAuth2UserInfo.getName());
        user.setNickname(oAuth2User.getAttribute("name"));
        user.setEmail(oAuth2UserInfo.getEmail());
        user.setAvatarUrl(oAuth2UserInfo.getImageUrl());
        user.setFollowerCount(oAuth2User.getAttribute("followers"));
        user.setFollowingCount(oAuth2User.getAttribute("following"));
        userService.updateUser(user);

        return UserPrincipal.create(oAuth2UserInfo.getName(), oAuth2User.getAttributes());
    }
}
