package com.thermofisher.cdcam.services;

import com.thermofisher.cdcam.utils.Utils;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

import org.springframework.stereotype.Service;

@Service
public class LocaleNameService {
	private Map<String, String> dictionary = new HashMap<String, String>(){{
		put("af","en_AF");
		put("al","en_AL");
		put("dz","en_DZ");
		put("ad","en_AD");
		put("ao","en_AO");
		put("aq","en_AQ");
		put("ag","en_AG");
		put("ar","es_AR");
		put("aw","en_AW");
		put("am","en_AM");
		put("au","en_AU");
		put("at","en_AT");
		put("az","en_AZ");
		put("bs","en_BS");
		put("bh","en_BH");
		put("bd","en_BD");
		put("bb","en_BB");
		put("by","en_BY");
		put("be","en_BE");
		put("bz","en_BZ");
		put("bj","en_BJ");
		put("bm","en_BM");
		put("bt","en_BT");
		put("bo","en_BO");
		put("ba","en_BA");
		put("bw","en_BW");
		put("bv","en_BV");
		put("br","pt_BR");
		put("bn","en_BN");
		put("bg","en_BG");
		put("bf","en_BF");
		put("bi","en_BI");
		put("kh","en_KH");
		put("cm","en_CM");
		put("ca","en_CA");
		put("ic","en_IC");
		put("cv","en_CV");
		put("ky","en_KY");
		put("cf","en_CF");
		put("td","en_TD");
		put("cl","es_CL");
		put("cn","zh_CN");
		put("co","en_CO");
		put("km","en_KM");
		put("cg","en_CG");
		put("cd","en_CD");
		put("cr","en_CR");
		put("ci","en_CI");
		put("hr","en_HR");
		put("cw","en_CW");
		put("cy","en_CY");
		put("cz","en_CZ");
		put("dk","en_DK");
		put("dj","en_DJ");
		put("dm","en_DM");
		put("do","en_DO");
		put("ec","en_EC");
		put("eg","en_EG");
		put("sv","en_SV");
		put("er","en_ER");
		put("ee","en_EE");
		put("et","en_ET");
		put("fo","en_FO");
		put("fj","en_FJ");
		put("fi","en_FI");
		put("fr","fr_FR");
		put("fx","en_FX");
		put("gf","en_GF");
		put("pf","en_PF");
		put("ga","en_GA");
		put("gm","en_GM");
		put("ge","en_GE");
		put("de","de_DE");
		put("gh","en_GH");
		put("gi","en_GI");
		put("gr","en_GR");
		put("gl","en_GL");
		put("gd","en_GD");
		put("gp","en_GP");
		put("gu","en_GU");
		put("gt","en_GT");
		put("gn","en_GN");
		put("gy","en_GY");
		put("ht","en_HT");
		put("hn","en_HN");
		put("hk","en_HK");
		put("hu","en_HU");
		put("is","en_IS");
		put("in","en_IN");
		put("id","en_ID");
		put("iq","en_IQ");
		put("ie","en_IE");
		put("il","en_IL");
		put("it","en_IT");
		put("jm","en_JM");
		put("jp","ja_JP");
		put("jo","en_JO");
		put("kz","en_KZ");
		put("ke","en_KE");
		put("kr","ko_KR");
		put("kw","en_KW");
		put("kg","en_KG");
		put("la","en_LA");
		put("lv","en_LV");
		put("lb","en_LB");
		put("ls","en_LS");
		put("lr","en_LR");
		put("ly","en_LY");
		put("li","en_LI");
		put("lt","en_LT");
		put("lu","en_LU");
		put("mo","en_MO");
		put("mk","en_MK");
		put("mg","en_MG");
		put("mw","en_MW");
		put("my","en_MY");
		put("mv","en_MV");
		put("ml","en_ML");
		put("mt","en_MT");
		put("mq","en_MQ");
		put("mr","en_MR");
		put("mu","en_MU");
		put("yt","en_YT");
		put("mx","es_MX");
		put("md","en_MD");
		put("mc","en_MC");
		put("mn","en_MN");
		put("me","en_ME");
		put("ms","en_MS");
		put("ma","en_MA");
		put("mz","en_MZ");
		put("mm","en_MM");
		put("na","en_NA");
		put("np","en_NP");
		put("nl","en_NL");
		put("nc","en_NC");
		put("nz","en_NZ");
		put("ni","en_NI");
		put("ne","en_NE");
		put("ng","en_NG");
		put("no","en_NO");
		put("om","en_OM");
		put("pk","en_PK");
		put("ps","en_PS");
		put("pa","en_PA");
		put("pg","en_PG");
		put("py","en_PY");
		put("pe","en_PE");
		put("ph","en_PH");
		put("pl","en_PL");
		put("pt","pt_PT");
		put("pr","en_PR");
		put("qa","en_QA");
		put("re","en_RE");
		put("ro","en_RO");
		put("ru","ru_RU");
		put("rw","en_RW");
		put("gs","en_GS");
		put("kn","en_KN");
		put("lc","en_LC");
		put("vc","en_VC");
		put("sm","en_SM");
		put("st","en_ST");
		put("sa","en_SA");
		put("sn","en_SN");
		put("rs","en_RS");
		put("sc","en_SC");
		put("sl","en_SL");
		put("sg","en_SG");
		put("sk","en_SK");
		put("si","en_SI");
		put("so","en_SO");
		put("za","en_ZA");
		put("es","es_ES");
		put("lk","en_LK");
		put("sh","en_SH");
		put("sr","en_SR");
		put("sj","en_SJ");
		put("sz","en_SZ");
		put("se","en_SE");
		put("ch","en_CH");
		put("sy","en_SY");
		put("tw","zt_TW");
		put("tj","en_TJ");
		put("tz","en_TZ");
		put("th","en_TH");
		put("tg","en_TG");
		put("to","en_TO");
		put("tt","en_TT");
		put("tn","en_TN");
		put("tr","en_TR");
		put("tm","en_TM");
		put("tc","en_TC");
		put("ug","en_UG");
		put("ua","en_UA");
		put("ae","en_AE");
		put("uk","en_UK");
		put("us","en_US");
		put("um","en_UM");
		put("uy","en_UY");
		put("uz","en_UZ");
		put("va","en_VA");
		put("ve","en_VE");
		put("vn","en_VN");
		put("vg","en_VG");
		put("vi","en_VI");
		put("eh","en_EH");
		put("ye","en_YE");
		put("zm","en_ZM");
		put("zw","en_ZW");
	}};
	
	public String getLocale(String locale, String country) {
		String localeName = null;
		if(isLocaleValid(locale)){
			return Utils.parseLocale(locale);
		}
		
		localeName = getDefaultLocaleByCountry(country);
		
		if (!Objects.isNull(localeName)) {
			return Utils.parseLocale(localeName);
		}
		
		return localeName;
	}
	
	private boolean isLocaleValid(String locale) {
		return Pattern.compile("^[a-z]+_[A-Z]+$").matcher(locale).matches();
	}
	
	private String getDefaultLocaleByCountry(String country) {
		return dictionary.get(country.toLowerCase());
	}
}