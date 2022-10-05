package com.final2.yoseobara.dto.request;

import lombok.Getter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

@Getter
public class PostRequestDto {

    private String title;
    private String content;
    private String address;
    private HashMap<String,Double> location;
    private List<Integer> deleteImageOrders;
}