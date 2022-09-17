package com.final2.yoseobara.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;

@Getter
@NoArgsConstructor
public class MapRequestDto {
    private HashMap<String, HashMap<String, Double>> bounds;
}
