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
import net.coobird.thumbnailator.Thumbnails;
import net.coobird.thumbnailator.name.Rename;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import javax.transaction.Transactional;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
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

    // 이미지 업로드
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
                    case ".bmp" -> contentType = "image/bmp";
                    case ".jpg" -> contentType = "image/jpg";
                    case ".jpeg" -> contentType = "image/jpeg";
                    case ".png" -> contentType = "image/png";
                }

                ObjectMetadata metadata = new ObjectMetadata();
                metadata.setContentType(contentType);
                metadata.setContentLength(file.getSize());

                // 첫번째 파일로 썸네일 생성
                if (file.equals(files[0])) {
                    fileUrls.add(uploadThumbnail(file, saveFileName, contentType));
                }

                // PutObjectRequest 이용하여 파일 생성 없이 바로 업로드
                amazonS3Client.putObject(new PutObjectRequest(bucket, saveFileName, file.getInputStream(), metadata)
                        .withCannedAcl(CannedAccessControlList.PublicRead));
            } catch (IOException e) {
                throw new InvalidValueException(ErrorCode.UPLOAD_FAILED);
            }
            // URL 받아올 때 한글 파일명 깨짐 방지
            fileUrls.add(URLDecoder.decode(amazonS3Client.getUrl(bucket, saveFileName).toString(), StandardCharsets.UTF_8));
        }
        return fileUrls; // url string 리턴 (썸네일 포함)
    }

    // 이미지 삭제
    public void deleteFile(String fileUrl) {
        try {
            // url에서 파일명만 추출
            amazonS3Client.deleteObject(bucket, fileUrl.split("/")[3]);
        } catch (Exception e) {
            throw new InvalidValueException(ErrorCode.DELETE_FAILED);
        }
    }

    // 이미지 수정 -> 기존 이미지 삭제 후 새 이미지 업로드 ( 더 좋은 방법은? )
    @Transactional
    public List<String> updateFile(List<String> deleteFileUrls, String deleteThumbnailUrl, MultipartFile[] newFiles) {
        // 기존 파일 삭제
        if (deleteFileUrls != null) {
            for (String fileUrl : deleteFileUrls) {
                deleteFile(fileUrl);
            }
            deleteFile(deleteThumbnailUrl);
        }
        // 새 이미지 업로드
        return uploadFile(newFiles); // url string 리턴
    }

    // 썸네일 파일 업로드
    public String uploadThumbnail(MultipartFile file, String saveFileName, String contentType) {
        String thumbnailFileName;
        try {
            // S3를 위한 썸네일 이미지 만들기
            BufferedImage bufferedImage = ImageIO.read(file.getInputStream());
            BufferedImage thumbnail = Thumbnails.of(bufferedImage).size(200, 200).asBufferedImage();
            ByteArrayOutputStream thumbOutput = new ByteArrayOutputStream();
            String imageType = contentType;
            ImageIO.write(thumbnail, Objects.requireNonNull(imageType.substring(imageType.indexOf("/") + 1)), thumbOutput);

            // 썸네일 메타데이터
            ObjectMetadata thumbnailMetadata = new ObjectMetadata();
            byte[] thumbnailBytes = thumbOutput.toByteArray();
            thumbnailMetadata.setContentLength(thumbnailBytes.length);
            thumbnailMetadata.setContentType(contentType);

            // 썸네일 파일명
            thumbnailFileName = String.valueOf(new StringBuilder(saveFileName).insert(saveFileName.indexOf("."), "(thumbnail)"));

            // 썸네일 업로드
            InputStream thumbnailInput = new ByteArrayInputStream(thumbnailBytes);
            amazonS3Client.putObject(new PutObjectRequest(bucket, thumbnailFileName, thumbnailInput, thumbnailMetadata)
                    .withCannedAcl(CannedAccessControlList.PublicRead));

            thumbnailInput.close();
            thumbOutput.close();

        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (Exception e) {
            throw new InvalidValueException(ErrorCode.UPLOAD_FAILED);
        }
        return URLDecoder.decode(amazonS3Client.getUrl(bucket, thumbnailFileName).toString(), StandardCharsets.UTF_8);
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

