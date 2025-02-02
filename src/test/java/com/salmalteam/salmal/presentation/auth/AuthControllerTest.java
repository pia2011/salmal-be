package com.salmalteam.salmal.presentation.auth;

import com.salmalteam.salmal.dto.request.auth.LoginRequest;
import com.salmalteam.salmal.dto.request.auth.LogoutRequest;
import com.salmalteam.salmal.dto.request.auth.ReissueTokenRequest;
import com.salmalteam.salmal.dto.request.auth.SignUpRequest;
import com.salmalteam.salmal.dto.response.auth.LoginResponse;
import com.salmalteam.salmal.dto.response.auth.TokenResponse;
import com.salmalteam.salmal.support.PresentationTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;


import java.nio.charset.StandardCharsets;

import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.ArgumentMatchers.*;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthControllerTest extends PresentationTest {

    private static final String BASE_URL = "/api/auth";
    @Test
    void 로그인() throws Exception {

        // given
        final String providerId = "providerId";
        final String accessToken = "accessToken";
        final String refreshToken = "refreshToken";
        final LoginRequest loginRequest = new LoginRequest(providerId);
        final LoginResponse loginResponse = LoginResponse.of(accessToken, refreshToken);
        given(authService.login(any())).willReturn(loginResponse);

        // when
        final ResultActions resultActions = mockMvc.perform(RestDocumentationRequestBuilders.post(BASE_URL+"/login")
                        .content(createJson(loginRequest))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk());

        // then
        resultActions.andDo(restDocs.document(
                requestFields(
                        fieldWithPath("providerId").type(JsonFieldType.STRING).description("소셜 로그인 식별자")
                ),
                responseFields(
                        fieldWithPath("accessToken").type(JsonFieldType.STRING).description("접근 토큰"),
                        fieldWithPath("refreshToken").type(JsonFieldType.STRING).description("재발급 토큰")
                )
        ));

    }

    @Test
    void 회원가입() throws Exception{

        // given
        final String providerId = "providerId";
        final String provider = "kakao";
        final String nickName = "nickName";
        final String accessToken = "accessToken";
        final String refreshToken = "refreshToken";
        final Boolean marketingInformationConsent = true;
        final SignUpRequest signUpRequest = new SignUpRequest(providerId, nickName, marketingInformationConsent);
        final LoginResponse loginResponse = LoginResponse.of(accessToken, refreshToken);
        given(authService.signUp(eq(provider),any())).willReturn(loginResponse);

        // when
        final ResultActions resultActions = mockMvc.perform(RestDocumentationRequestBuilders.post(BASE_URL+"/signup/{provider}", provider)
                        .content(createJson(signUpRequest))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk());

        // then
        resultActions.andDo(restDocs.document(
                pathParameters(
                        parameterWithName("provider").description("소셜 로그인 제공자 ( kakao, apple )")
                ),
                requestFields(
                        fieldWithPath("providerId").type(JsonFieldType.STRING).description("소셜 로그인 식별자"),
                        fieldWithPath("nickName").type(JsonFieldType.STRING).description("닉네임"),
                        fieldWithPath("marketingInformationConsent").type(JsonFieldType.BOOLEAN).description("마케팅 정보 수신 동의 여부")
                ),
                responseFields(
                        fieldWithPath("accessToken").type(JsonFieldType.STRING).description("접근 토큰"),
                        fieldWithPath("refreshToken").type(JsonFieldType.STRING).description("재발급 토큰")
                )
        ));

    }

    @Test
    void 로그아웃() throws Exception{

        // given
        final String refreshToken = "refreshToken";
        final LogoutRequest logoutRequest = new LogoutRequest(refreshToken);
        final ResultActions resultActions = mockMvc.perform(RestDocumentationRequestBuilders.post(BASE_URL + "/logout")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer accessToken")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createJson(logoutRequest))
                        .characterEncoding(StandardCharsets.UTF_8))
                .andExpect(status().isOk());

        // when & then
        resultActions.andDo(restDocs.document(
                requestHeaders(
                        headerWithName(HttpHeaders.AUTHORIZATION).description("Bearer 타입 AccessToken")
                ),
                requestFields(
                        fieldWithPath("refreshToken").type(JsonFieldType.STRING).description("삭제할 재발급 토큰")
                )
        ));

    }

    @Test
    void 접근_토큰_재발급() throws Exception{

        // given
        final String accessToken = "accessToken";
        final String refreshToken = "refreshToken";
        final ReissueTokenRequest reissueTokenRequest = new ReissueTokenRequest(refreshToken);
        final TokenResponse tokenResponse = TokenResponse.from(accessToken);
        given(authService.reissueAccessToken(any())).willReturn(tokenResponse);

        final ResultActions resultActions = mockMvc.perform(RestDocumentationRequestBuilders.post(BASE_URL + "/reissue")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createJson(reissueTokenRequest))
                        .characterEncoding(StandardCharsets.UTF_8))
                .andExpect(status().isOk());


        // when & then
        resultActions.andDo(restDocs.document(
                requestFields(
                        fieldWithPath("refreshToken").type(JsonFieldType.STRING).description("접근 토큰의 재발급에 사용될 재토큰")
                ),
                responseFields(
                        fieldWithPath("accessToken").type(JsonFieldType.STRING).description("재발급한 접근 토큰")
                )
        ));
    }

}