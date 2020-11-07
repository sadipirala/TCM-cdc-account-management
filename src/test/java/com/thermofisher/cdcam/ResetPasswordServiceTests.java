package com.thermofisher.cdcam;

import com.thermofisher.CdcamApplication;
import com.thermofisher.cdcam.model.AccountInfo;
import com.thermofisher.cdcam.model.HttpServiceResponse;
import com.thermofisher.cdcam.services.HttpService;
import com.thermofisher.cdcam.services.ResetPasswordService;
import com.thermofisher.cdcam.utils.AccountUtils;
import org.apache.http.HttpEntity;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ActiveProfiles("test")
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = CdcamApplication.class)
public class ResetPasswordServiceTests {

    @InjectMocks
    ResetPasswordService resetPasswordService;

    @Mock
    HttpService httpService;

    @Before
    public void setup() {
        ReflectionTestUtils.setField(resetPasswordService, "supportedLocales", "en_US,en_DE,es_AR,es_ES,en_CL,fr_FR,ko_KR,ja_JP,zh_CN");
    }

    @Test
    public void sendResetPasswordConfirmation_givenAccountWithValidFormat_thenResetPasswordConfirmationEmailPostRequestShouldBeMade() throws IOException {
        StatusLine mockStatusLine = Mockito.mock(StatusLine.class);
        HttpEntity mockEntity = Mockito.mock(HttpEntity.class);
        CloseableHttpResponse mockHttpCloseableResponse = Mockito.mock(CloseableHttpResponse.class);

        HttpServiceResponse mockHttpResponse = HttpServiceResponse.builder()
                .closeableHttpResponse(mockHttpCloseableResponse)
                .build();

        Mockito.when(mockStatusLine.getStatusCode()).thenReturn(200);
        Mockito.when(mockHttpResponse.getCloseableHttpResponse().getStatusLine()).thenReturn(mockStatusLine);
        Mockito.when(mockHttpResponse.getCloseableHttpResponse().getEntity()).thenReturn(mockEntity);
        Mockito.when(httpService.post(any(), any())).thenReturn(mockHttpResponse);

        AccountInfo accountInfo = AccountUtils.getSiteAccount();

        resetPasswordService.sendResetPasswordConfirmation(accountInfo);

        verify(httpService, times(1)).post(any(), any());
    }

    @Test
    public void sendResetPasswordConfirmation_givenResetPasswordConfirmationEmailPostRequestReturnsDifferentThan200_noExceptionShouldOccur() throws IOException {
        StatusLine mockStatusLine = Mockito.mock(StatusLine.class);
        HttpEntity mockEntity = Mockito.mock(HttpEntity.class);
        CloseableHttpResponse mockHttpCloseableResponse = Mockito.mock(CloseableHttpResponse.class);

        HttpServiceResponse mockHttpResponse = HttpServiceResponse.builder()
                .closeableHttpResponse(mockHttpCloseableResponse)
                .build();

        Mockito.when(mockStatusLine.getStatusCode()).thenReturn(500);
        Mockito.when(mockHttpResponse.getCloseableHttpResponse().getStatusLine()).thenReturn(mockStatusLine);
        Mockito.when(mockHttpResponse.getCloseableHttpResponse().getEntity()).thenReturn(mockEntity);
        Mockito.when(httpService.post(any(), any())).thenReturn(mockHttpResponse);

        AccountInfo accountInfo = AccountUtils.getSiteAccount();

        resetPasswordService.sendResetPasswordConfirmation(accountInfo);

        verify(httpService, times(1)).post(any(), any());
    }

    @Test(expected = IOException.class)
    public void sendResetPasswordConfirmation_givenResetPasswordConfirmationEmailPostRequestFails_ExceptionShouldBeThrown() throws IOException {
        CloseableHttpResponse mockHttpCloseableResponse = Mockito.mock(CloseableHttpResponse.class);
        HttpServiceResponse mockHttpResponse = HttpServiceResponse.builder()
                .closeableHttpResponse(mockHttpCloseableResponse)
                .build();

        Mockito.when(mockHttpResponse.getCloseableHttpResponse().getEntity()).thenReturn(null);
        Mockito.when(httpService.post(any(), any())).thenReturn(mockHttpResponse);

        AccountInfo accountInfo = AccountUtils.getSiteAccount();

        resetPasswordService.sendResetPasswordConfirmation(accountInfo);
    }

    @Test
    public void sendResetPasswordConfirmation_givenUserLocaleIsNotChinese_ItShouldFormatLocaleWithCountry() throws IOException, JSONException {
        // given
        String localeName = "es";
        String country = "mx";
        String expectedLocale = "es_MX";

        StatusLine mockStatusLine = Mockito.mock(StatusLine.class);
        HttpEntity mockEntity = Mockito.mock(HttpEntity.class);
        CloseableHttpResponse mockHttpCloseableResponse = Mockito.mock(CloseableHttpResponse.class);

        HttpServiceResponse mockHttpResponse = HttpServiceResponse.builder()
                .closeableHttpResponse(mockHttpCloseableResponse)
                .build();

        Mockito.when(mockStatusLine.getStatusCode()).thenReturn(200);
        Mockito.when(mockHttpResponse.getCloseableHttpResponse().getStatusLine()).thenReturn(mockStatusLine);
        Mockito.when(mockHttpResponse.getCloseableHttpResponse().getEntity()).thenReturn(mockEntity);
        Mockito.when(httpService.post(any(), any())).thenReturn(mockHttpResponse);

        AccountInfo accountInfo = AccountUtils.getSiteAccount();

        accountInfo.setLocaleName(localeName);
        accountInfo.setCountry(country);

        ArgumentCaptor<JSONObject> requestObjectCaptor = ArgumentCaptor.forClass(JSONObject.class);

        // when
        resetPasswordService.sendResetPasswordConfirmation(accountInfo);
        verify(httpService).post(any(), requestObjectCaptor.capture());
        JSONObject requestBody = requestObjectCaptor.getValue();

        // then
        Assert.assertEquals(requestBody.getString("locale"), expectedLocale);
    }

    @Test
    public void sendResetPasswordConfirmation_givenUserLocaleIsChinese_ItShouldUseDefaultChineseLocale() throws IOException, JSONException {
        // given
        String localeName = "zh-cn";
        String expectedLocale = "zh_CN";

        StatusLine mockStatusLine = Mockito.mock(StatusLine.class);
        HttpEntity mockEntity = Mockito.mock(HttpEntity.class);
        CloseableHttpResponse mockHttpCloseableResponse = Mockito.mock(CloseableHttpResponse.class);

        HttpServiceResponse mockHttpResponse = HttpServiceResponse.builder()
                .closeableHttpResponse(mockHttpCloseableResponse)
                .build();

        Mockito.when(mockStatusLine.getStatusCode()).thenReturn(200);
        Mockito.when(mockHttpResponse.getCloseableHttpResponse().getStatusLine()).thenReturn(mockStatusLine);
        Mockito.when(mockHttpResponse.getCloseableHttpResponse().getEntity()).thenReturn(mockEntity);
        Mockito.when(httpService.post(any(), any())).thenReturn(mockHttpResponse);

        AccountInfo accountInfo = AccountUtils.getSiteAccount();

        accountInfo.setLocaleName(localeName);

        ArgumentCaptor<JSONObject> requestObjectCaptor = ArgumentCaptor.forClass(JSONObject.class);

        // when
        resetPasswordService.sendResetPasswordConfirmation(accountInfo);
        verify(httpService).post(any(), requestObjectCaptor.capture());
        JSONObject requestBody = requestObjectCaptor.getValue();

        // then
        Assert.assertEquals(requestBody.getString("locale"), expectedLocale);
    }

    @Test
    public void sendResetPasswordConfirmation_givenUserLocaleIsAlreadyFormated_ItShouldSetSameLocaleFormat() throws IOException, JSONException {
        // given
        String localeName = "EN_us";
        String expectedLocale = "en_US";

        StatusLine mockStatusLine = Mockito.mock(StatusLine.class);
        HttpEntity mockEntity = Mockito.mock(HttpEntity.class);
        CloseableHttpResponse mockHttpCloseableResponse = Mockito.mock(CloseableHttpResponse.class);

        HttpServiceResponse mockHttpResponse = HttpServiceResponse.builder()
                .closeableHttpResponse(mockHttpCloseableResponse)
                .build();

        Mockito.when(mockStatusLine.getStatusCode()).thenReturn(200);
        Mockito.when(mockHttpResponse.getCloseableHttpResponse().getStatusLine()).thenReturn(mockStatusLine);
        Mockito.when(mockHttpResponse.getCloseableHttpResponse().getEntity()).thenReturn(mockEntity);
        Mockito.when(httpService.post(any(), any())).thenReturn(mockHttpResponse);

        AccountInfo accountInfo = AccountUtils.getSiteAccount();

        accountInfo.setLocaleName(localeName);

        ArgumentCaptor<JSONObject> requestObjectCaptor = ArgumentCaptor.forClass(JSONObject.class);

        // when
        resetPasswordService.sendResetPasswordConfirmation(accountInfo);
        verify(httpService).post(any(), requestObjectCaptor.capture());
        JSONObject requestBody = requestObjectCaptor.getValue();

        // then
        Assert.assertEquals(requestBody.getString("locale"), expectedLocale);
    }
}
