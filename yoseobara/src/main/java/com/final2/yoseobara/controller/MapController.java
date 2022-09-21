package com.final2.yoseobara.controller;

import com.final2.yoseobara.dto.response.ResponseDto;
import com.final2.yoseobara.service.MapService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/map")
public class MapController {
    private final MapService mapService;

    // 지오코딩
    @GetMapping("/coordinate")
    public ResponseDto<?> getCoordinate(@RequestParam String address) {
        return ResponseDto.success(mapService.getCoordinate(address));
    }
    // 리버스 지오코딩
    @GetMapping("/address")
    public ResponseDto<?> getAddress(@RequestParam("lng") Double lng,
                                     @RequestParam("lat") Double lat) {

        return ResponseDto.success(mapService.getAddress(lat, lng));
    }
}
