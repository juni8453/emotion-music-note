package com.emotionmusicnote.user.service;

import com.emotionmusicnote.user.controller.response.SignInUserInfo;
import com.emotionmusicnote.user.domain.User;
import com.emotionmusicnote.user.domain.UserRepository;
import com.emotionmusicnote.user.oauth.KakaoTokens;
import com.emotionmusicnote.user.oauth.KakaoUserInfo;
import com.emotionmusicnote.user.oauth.OAuthProvider;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@RequiredArgsConstructor
@Service
public class UserLoginService {

  private final UserRepository userRepository;
  private final RestTemplate restTemplate;

  @Value("${oauth.kakao.grantType}")
  private String grantType;

  @Value("${oauth.kakao.clientId}")
  private String clientId;

  @Value("${oauth.kakao.redirectUri}")
  private String redirectUri;

  @Value("${oauth.kakao.tokenUri}")
  private String tokenUri;

  @Value("${oauth.kakao.userInfoUri}")
  private String userInfoUri;

  public SignInUserInfo login(String code, HttpSession session) {
    KakaoTokens kakaoTokens = getToken(code);
    KakaoUserInfo kakaoUserInfo = getUserInfo(kakaoTokens);
    User user = findOrCreateUser(kakaoUserInfo);

    // 사용자의 고유 정보를 포함해 세션 저장
    session.setAttribute("user", user);

    return SignInUserInfo.builder()
        .nickname(user.getNickname())
        .profileImageUrl(user.getProfileImageUrl())
        .sessionId(session.getId())
        .build();
  }

  /**
   * GET/POST https://kapi.kakao.com/v2/user/me 으로 설정한 Headers 정보를 담아 요청 사용자 정보를 가져오기 위한 Required
   * Parameter Header Authorization : Bearer ${ACCESS_TOKEN} Content-type :
   * application/x-www-form-urlencoded;charset=utf-8
   */
  private KakaoTokens getToken(String code) {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

    MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
    body.add("grant_type", grantType);
    body.add("client_id", clientId);
    body.add("redirect_uri", redirectUri);
    body.add("code", code);

    HttpEntity<MultiValueMap<String, String>> requestToken = new HttpEntity<>(body, headers);

    return restTemplate.postForEntity(
            tokenUri,
            requestToken,
            KakaoTokens.class)
        .getBody();
  }

  private User findOrCreateUser(KakaoUserInfo kakaoUserInfo) {
    // 네이버 고유 식별 ID 는 문자열 타입이라 String 으로 통일을 위해 String.valueOf() 사용
    String providerId = String.valueOf(kakaoUserInfo.getId());

    // providerId 를 통해 User 를 찾고 있다면 해당 유저를 반환, 없다면 생성 후 반환
    return userRepository.findByProviderId(providerId)
        .orElseGet(() -> saveUser(kakaoUserInfo));
  }

  private User saveUser(KakaoUserInfo kakaoUserInfo) {
    User user = User.builder()
        .nickname(kakaoUserInfo.getNickname())
        .providerId(String.valueOf(kakaoUserInfo.getId()))
        .profileImageUrl(kakaoUserInfo.getKakaoAccount().getKakaoProfile().getProfileImageUrl())
        .oAuthProvider(OAuthProvider.KAKAO)
        .build();

    return userRepository.save(user);
  }

  /**
   * GET/POST https://kapi.kakao.com/v2/user/me 으로 설정한 Headers 정보를 담아 요청 사용자 정보를 가져오기 위한 Required
   * Parameter Header Authorization : Bearer ${ACCESS_TOKEN} Content-type :
   * application/x-www-form-urlencoded;charset=utf-8
   */
  private KakaoUserInfo getUserInfo(KakaoTokens kakaoTokens) {
    HttpHeaders userInfoHeaders = new HttpHeaders();
    userInfoHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
    userInfoHeaders.add("Authorization", "Bearer " + kakaoTokens.getAccessToken());

    HttpEntity<MultiValueMap<String, String>> requestUserProfile = new HttpEntity<>(
        userInfoHeaders);

    return restTemplate.exchange(
            userInfoUri,
            HttpMethod.GET,
            requestUserProfile,
            KakaoUserInfo.class)
        .getBody();
  }
}
