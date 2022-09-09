package com.final2.yoseobara.service;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.final2.yoseobara.exception.ErrorCode;
import com.final2.yoseobara.exception.InvalidValueException;
import com.final2.yoseobara.repository.MemberRepository;
import com.final2.yoseobara.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class S3Service {
    private final AmazonS3Client amazonS3Client;
    private final MemberRepository memberRepository;
    private final PostRepository postRepository;
    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    public List<String> uploadFile(MultipartFile[] files) throws IllegalArgumentException {
        List<String> fileUrls = new ArrayList<>();
        for (MultipartFile file : files) {

            // Objects.requireNonNull 을 사용할 수도 있음
            if (Objects.isNull(file) || file.isEmpty()) {
                throw new InvalidValueException(ErrorCode.FILE_NOT_FOUND);
            }

            String fileName = file.getOriginalFilename().toLowerCase();
            String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss", Locale.KOREA));
            String saveFileName = now + "-" + fileName;

            // String uuid = UUID.randomUUID().toString(); // UUID 로 파일을 저장한다면 확장자만 붙여도 괜찮음
            String extension = fileName.substring(fileName.lastIndexOf("."));
            String contentType = "";

            try {
                if (saveFileName.contains("..")) {
                    throw new InvalidValueException(ErrorCode.INVALID_FILE_NAME);
                }

                if (!(fileName.endsWith(".bmp") || fileName.endsWith(".jpg") || fileName.endsWith(".jpeg") || fileName.endsWith(".png"))) {
                    throw new InvalidValueException(ErrorCode.INVALID_IMAGE_FILE_EXTENSION);
                }

                // content type을 지정해서 올려주지 않으면 자동으로 "application/octet-stream"으로 고정이 되어
                // 링크 클릭시 웹에서 열리는게 아니라 자동 다운이 시작됨
                switch (extension) {
                    case "bmp" -> contentType = "image/bmp";
                    case "jpg" -> contentType = "image/jpg";
                    case "jpeg" -> contentType = "text/jpeg";
                    case "png" -> contentType = "text/png";
                }

                ObjectMetadata metadata = new ObjectMetadata();
                metadata.setContentType(contentType);
                metadata.setContentLength(file.getSize());

                // PutObjectRequest 이용하여 파일 생성 없이 바로 업로드
                amazonS3Client.putObject(new PutObjectRequest(bucket, saveFileName, file.getInputStream(), metadata)
                        .withCannedAcl(CannedAccessControlList.PublicRead));
            } catch (IOException e) {
                throw new InvalidValueException(ErrorCode.UPLOAD_FAILED);
            }
            fileUrls.add(amazonS3Client.getUrl(bucket, saveFileName).toString());
        }
        return fileUrls;    ///url string 리턴
    }
}

// 아래 방식의 장점은??
//    public S3RequestDto S3UserImageUpload(MultipartFile file, Long Id, String username) throws IOException {
//        Member memberFoundById = memberRepository.findById(Id)
//                .orElseThrow(() -> new IllegalArgumentException("해당 유저는 존재하지 않습니다."));
//
//        if (memberFoundById.getUsername().equals(username)) {
//            File uploadFile = convert(file).orElseThrow(() -> new IllegalArgumentException("파일 업로드에 실패하였습니다."));
//            String dir = "static/images/".concat(String.valueOf(Id));
//            return upload(uploadFile, dir);
//        }
//        return null;
//    }
//
//    private S3RequestDto upload(File uploadFile, String dir) {
//        String sourceName = uploadFile.getName();
//        String sourceExt = FilenameUtils.getExtension(sourceName).toLowerCase();
//
//        String fileName = dir + "/" + LocalDateTime.now().toString().concat(".").concat(sourceExt);
//        String uploadImageUrl = putS3(uploadFile, fileName);
//        removeNewFile(uploadFile);
//
//        return S3RequestDto.builder()
//                .imageUrl(uploadImageUrl)
//                .build();
//    }
//
//    private void removeNewFile(File targetFile) {
//        if (targetFile.delete()) {
//            log.info("파일이 삭제되었습니다.");
//        } else {
//            log.info("파일이 삭제되지 못했습니다.");
//        }
//    }
//
//    private String putS3(File uploadFile, String fileName) {
//        try {
//            amazonS3Client.putObject(new PutObjectRequest(bucket, fileName, uploadFile)
//                    .withCannedAcl(CannedAccessControlList.PublicRead));
//        } catch (Exception e) {
//            log.error("이미지 s3 업로드 실패");
//            log.error(e.getMessage());
//            removeNewFile(uploadFile);
//            throw new RuntimeException();
//        }
//
//        return amazonS3Client.getUrl(bucket, fileName).toString();
//    }
//
//    private Optional<File> convert(MultipartFile file) throws IOException {
//        File convertFile = new File(Objects.requireNonNull(file.getOriginalFilename()));
//        if (convertFile.createNewFile()) {
//            try (FileOutputStream fos = new FileOutputStream(convertFile)) {
//                fos.write(file.getBytes());
//            }
//            return Optional.of(convertFile);
//        }
//        return Optional.empty();
//    }
//}

