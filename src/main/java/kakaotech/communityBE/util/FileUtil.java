package kakaotech.communityBE.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class FileUtil {

    private static final Logger logger = LoggerFactory.getLogger(FileUtil.class);

    private static final String BASE_UPLOAD_DIR = System.getProperty("user.dir") + "/uploads/";
    private static final String PROFILE_DIR = BASE_UPLOAD_DIR + "profile/";
    private static final String POST_DIR = BASE_UPLOAD_DIR + "postImage/";
    private static final String DEFAULT_PROFILE_IMAGE = PROFILE_DIR + "default-profile.jpg";
    private static final String RESIGN_PROFILE_IMAGE = PROFILE_DIR + "resign.png";

    public static String saveProfileImage(MultipartFile file) throws IOException {
        return saveFile(file, PROFILE_DIR);
    }

    public static String savePostImage(MultipartFile file) throws IOException {
        return saveFile(file, POST_DIR);
    }

    public static boolean deleteFile(String filePath) {
        if (filePath == null || filePath.equals(DEFAULT_PROFILE_IMAGE)) {
            return false; // 기본 프로필 이미지는 삭제하지 않음
        }

        File file = new File(filePath.substring(1));
        if (file.exists()) {
            boolean deleted = file.delete();
            logger.info("파일 삭제 {}: {}", deleted ? "성공" : "실패", file.getAbsolutePath());
            return deleted;
        }
        return false;
    }

    public static String getDefaultProfileImage() {
        return DEFAULT_PROFILE_IMAGE;
    }

    public static String getResignProfileImage() {
        return RESIGN_PROFILE_IMAGE;
    }

    private static String saveFile(MultipartFile imageFile, String directory) throws IOException {
        if (imageFile == null || imageFile.isEmpty()) {
            return null;
        }

        File uploadDir = new File(directory);
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }

        String fileName = UUID.randomUUID().toString() + "_" + imageFile.getOriginalFilename();
        logger.info("저장될 파일 이름 : {}", fileName);
        File destFile = new File(directory + fileName);
        imageFile.transferTo(destFile);
        logger.info("파일 저장 완료: {}", destFile.getAbsolutePath());

        return "/uploads/" + directory.substring(BASE_UPLOAD_DIR.length()) + fileName;
    }
}
