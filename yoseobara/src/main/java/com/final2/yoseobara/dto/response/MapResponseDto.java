package com.final2.yoseobara.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
public class MapResponseDto {
    private String address;
    private Double lat;
    private Double lng;

    @Builder
    public MapResponseDto(String address, Double lat, Double lng) {
        this.address = address;
        this.lat = lat;
        this.lng = lng;
    }
}
