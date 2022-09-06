package com.final2.yoseobara.dto.request;

import lombok.Getter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Getter
public class PostRequestDto {

    private String title;
    private String content;
    private Float address;
    private ArrayList<HashMap<String,Float>> location;

}