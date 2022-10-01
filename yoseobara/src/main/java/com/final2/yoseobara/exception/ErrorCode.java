package com.final2.yoseobara.exception;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum ErrorCode {

    //Common
    SUCCESS(200, "SUCCESS", "통신에 성공했습니다."),

    //Member
    USER_NOT_FOUND(400, "USER_NOT_FOUND", "해당 유저가 존재하지 않습니다."),
    INVALID_USER(400, "INVALID_USER", "비밀번호가 일치하지 않습니다"),
    DUPLICATED_USERNAME(400, "DUPLICATED_USERNAME", "중복된 유저네임입니다."),
    DUPLICATED_NICKNAME(400, "DUPLICATED_NICKNAME", "중복된 닉네임입니다."),

    PASSWORDS_NOT_MATCHED(400, "PASSWORDS_NOT_MATCHED", "비밀번호와 비밀번호 확인이 일치하지 않습니다."),
    LOGIN_REQUIRED(400, "LOGIN_REQUIRED", "로그인이 필요합니다."),

    //Post
    POST_NOT_FOUND(400, "POST_NOT_FOUND", "존재하지 않는 게시글입니다."),
    POST_UNAUTHORIZED(401, "POST_UNAUTHORIZED", "게시글에 대한 권한이 없습니다."),
    POST_IMAGE_REQUIRED(400, "POST_IMAGE_REQUIRED", "게시글에는 이미지가 1개 이상 필요합니다."),
    POST_IMAGE_MAX(400, "POST_IMAGE_MAX", "게시글에는 이미지가 3개까지만 가능합니다."),

    //Comment
    COMMENT_NOT_FOUND(400, "COMMENT_NOT_FOUND", "존재하지 않는 댓글입니다."),
    COMMENT_UNAUTHORIZED(401, "COMMENT_UNAUTHORIZED", "댓글에 대한 권한이 없습니다."),

    //Token
    INVALID_TOKEN(400, "INVALID_TOKEN", "Token이 유효하지 않습니다."),
    TOKEN_NOT_FOUND(400, "TOKEN_NOT_FOUND", "존재하지 않는 Token 입니다."),
    NOT_LOGIN_STATE(400, "NOT_LOGIN_STATE", "로그인 상태가 아닙니다."),

    NOT_PASS_VALIDATION(400, "NOT_PASS_VALIDATION", "유효성 검사를 통과하지 못했습니다."),

    //S3
    FILE_NOT_FOUND(400, "FILE_NOT_FOUND", "파일이 존재하지 않습니다."),
    INVALID_FILE_NAME(400, "INVALID_FILE_NAME", "파일명에 포함될 수 없는 문자가 포함되어 있습니다."),
    UPLOAD_FAILED(400, "UPLOAD_FAILED", "S3 Bucket 객체 업로드 실패."),
    DELETE_FAILED(400, "DELETE_FAILED", "S3 Bucket 객체 삭제 실패."),
    INVALID_IMAGE_FILE_EXTENSION(400, "INVALID_IMAGE_FILE_EXTENSION", "bmp,jpg,jpeg,png 형식의 이미지 파일이 요구됨."),

    //좋아요
    ALREADY_HEARTED(409,"ALREADY_HEARTED","이미 좋아요 된 페이지 입니다,"),
    HEART_NOT_FOUND(400,"HEART_NOT_FOUND","해당 좋아요 정보를 찾을 수 없습니다."),
    MEMBER_NOT_FOUND(400,"MEMBER_NOT_FOUND","윺저가 존재하지 않습니다."),
    MISMATCH_JWT_USER(400,"MISMATCH_JWT_USER","일치하지 않은 토큰입니다."),
    UNAUTHORIZED(400,"UNAUTHORIZED","유요한 인증 자격 증면이 없습니다.");

    private final int status;
    private final String code;
    private final String message;
}

