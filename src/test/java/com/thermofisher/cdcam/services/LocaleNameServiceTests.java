package com.thermofisher.cdcam.services;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class LocaleNameServiceTests {

	@InjectMocks
	LocaleNameService localeNameService;
	@BeforeEach
	public void setup() {
		MockitoAnnotations.openMocks(this);
	}
	@Test
	public void getLocale_givenAValidLocale_shouldReturnSameLocaleName() {
		// given
		String localeMock = "en_US";
		String countryMock = "US";
		String expectedLocale = "en";
		
		// when 
		String result = localeNameService.getLocale(localeMock, countryMock);
		
		// then
		assertEquals(result, expectedLocale);
	}
	
	@Test
	public void getLocale_givenAInvalidLocale_shouldReturnDefaultLocaleByCountry() {
		// given
		String localeMock = "es";
		String countryMock = "MX";
		String expectedLocale = "es";
		
		// when 
		String result = localeNameService.getLocale(localeMock, countryMock);
		
		// then
		assertEquals(result, expectedLocale);
	}
	
	@Test
	public void getLocale_givenAInvalidLocalAndInvalidCountry_shouldReturnNull() {
		// given
		String localeMock = "";
		String countryMock = "AX";
		
		// when 
		String result = localeNameService.getLocale(localeMock, countryMock);
		
		// then
		assertNull(result);
	}
	
	@Test
	public void getLocale_givenChinaLocale_shouldReturnChinaLocale() {
		// given
		String localeMock = "zh_CN";
		String countryMock = "cn";
		String expectedLocale = "zh-cn";
		
		// when 
		String result = localeNameService.getLocale(localeMock, countryMock);
		
		// then
		assertEquals(result, expectedLocale);
	}
	
	@Test
	public void getLocale_givenTaiwanLocale_shouldReturnTaiwanLocale() {
		// given
		String localeMock = "zt_TW";
		String countryMock = "tw";
		String expectedLocale = "zh-tw";
		
		// when 
		String result = localeNameService.getLocale(localeMock, countryMock);
		
		// then
		assertEquals(result, expectedLocale);
	}
}
