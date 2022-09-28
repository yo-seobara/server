package com.final2.yoseobara.controller;

import com.final2.yoseobara.dto.request.HeartDto;
import com.final2.yoseobara.exception.ErrorCode;
import com.final2.yoseobara.exception.InvalidValueException;
import com.final2.yoseobara.service.HeartService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.io.IOException;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/heart")
public class HeartController {

    private final HeartService heartService;

    @PostMapping
    public ResponseEntity<HeartDto> heart(@RequestBody @Valid HeartDto heartDto,
                                          @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization) {

        if (authorization == null) {
            throw new InvalidValueException(ErrorCode.UNAUTHORIZED);
        }

        heartService.heart(heartDto, authorization.substring(7));

        return new ResponseEntity<>(heartDto, HttpStatus.CREATED);
    }

    @DeleteMapping
    public ResponseEntity<HeartDto> unHeart(@RequestBody @Valid HeartDto heartDto,
                                            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization) {

        if (authorization == null) {
            throw new InvalidValueException(ErrorCode.UNAUTHORIZED);
        }

        heartService.unHeart(heartDto, authorization.substring(7));

        return new ResponseEntity<>(heartDto, HttpStatus.OK);
    }

}