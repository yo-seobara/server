package com.final2.yoseobara.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@RequiredArgsConstructor
@Service
public class MapService {
    @Value("${kakao.rest-api.key}")
    String key;

    String host = "dapi.kakao.com";
    String setUrl = "https://" + host + "/v2/local/";

    // kakao map api 주소->좌표 변환
    public Map getCoordinate(String address) {
        return null;
    }

    // kakao map api 좌표->주소 변환 (도로명주소 없으면 일반 주소)
    public String getAddress(Double lat, Double lng) {

        // 다른 파라미터 사실상 필요 없기 때문에 uri로 직접 만들어서 보냄
        // 좌표계 설정 가능 (기본값 WGS84)
        // 인풋 좌표계 파라미터: input_coord
        // 아웃풋 좌표계 파라미터: output_coord
        String Url = setUrl + "geo/coord2address.json?x=" + lng + "&y=" + lat;

        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "KakaoAK " + key);
        HttpEntity httpEntity = new HttpEntity(headers);

        // json 꺼내오면서 속도가 느려진다. 좋은 파싱 방법 찾기
        Map response = restTemplate.exchange(Url, HttpMethod.GET, httpEntity, Map.class).getBody();

        if (response.get("documents").toString().equals("[]")) {
            return "해당 주소를 찾을 수 없음";
        }
        Map documents = (Map) ((List) response.get("documents")).get(0);


        Map address = documents.get("road_address") == null ? (Map) documents.get("address") : (Map) documents.get("road_address");

        return address.get("address_name").toString();
    }

    // 리버스 지오코딩 (Nominatim)
//    private final String REVERSE_GEOCODING_URL = "https://nominatim.openstreetmap.org/reverse?format=json&";
//    public String getReverseGeocoding(Double lat, Double lng) {
//        String url = REVERSE_GEOCODING_URL + "lat=" + lat + "&lon=" + lng;
//
//        RestTemplate restTemplate = new RestTemplate();
//        MapResponsetDto result = restTemplate.getForObject(url, MapResponsetDto.class);
//
//        if (result.getAddress() == null) {
//            return "주소 없음";
//        }
//
//        if (!Objects.equals(result.getAddress().getCountry(), "대한민국") || result.getAddress().getCountry() == null) {
//            return result.getDisplay_name();
//        }
//
//        String[] address = result.getDisplay_name().split(", ");
//        Collections.reverse(Arrays.asList(address));
//        // remove country
//        String[] addressWithoutCountry = Arrays.copyOfRange(address, 2, address.length);
//        String addressForSave = "(" + result.getAddress().getPostcode() + ") " + String.join(" ", addressWithoutCountry);
//
//
//        return addressForSave;
//    }
}
