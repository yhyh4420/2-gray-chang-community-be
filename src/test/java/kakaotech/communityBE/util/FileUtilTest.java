package kakaotech.communityBE.util;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class FileUtilTest {

    private static final String BASE_UPLOAD_DIR = System.getProperty("user.dir") + "/uploads/";
    private static final String PROFILE_DIR = BASE_UPLOAD_DIR + "profile/";
    private static final String POST_DIR = BASE_UPLOAD_DIR + "postImage/";

    private MultipartFile imageFile;

    @BeforeEach
    void setUp() {
        imageFile = mock(MultipartFile.class);
    }

    @Test
    void 프로필이미지_저장_커스텀이미지() throws IOException {
        //given
        when(imageFile.isEmpty()).thenReturn(false);
        when(imageFile.getOriginalFilename()).thenReturn("test.jpg");
        //when
        String savedPath = FileUtil.saveProfileImage(imageFile);
        //then
        assertThat(savedPath).isNotNull();
        assertThat(savedPath).contains("/uploads/profile/");
        assertThat(savedPath).contains("test.jpg");
    }

    @Test
    void 프로필이미지_저장_이미지없을때_기본이미지() throws IOException {
        //given
        when(imageFile.isEmpty()).thenReturn(true);
        //when
        String savedPath = FileUtil.saveProfileImage(imageFile);
        //then
        assertThat(savedPath).isNull();
    }

    @Test
    void 게시글이미지_저장_커스텀이미지() throws IOException {
        //given
        when(imageFile.isEmpty()).thenReturn(false);
        when(imageFile.getOriginalFilename()).thenReturn("test.jpg");
        //when
        String savedPath = FileUtil.savePostImage(imageFile);
        //then
        assertThat(savedPath).isNotNull();
        assertThat(savedPath).contains("/uploads/postImage/");
        assertThat(savedPath).contains("test.jpg");
    }

    @Test
    void 게시글이미지_저장_이미지없을때() throws IOException {
        //given
        when(imageFile.isEmpty()).thenReturn(true);
        //when
        String savedPath = FileUtil.savePostImage(imageFile);
        //then
        assertThat(savedPath).isNull();
    }

    @Test
    void 이미지삭제_null일때() throws IOException {
        //given
        String filePath = null;
        //when
        boolean isDeleted = FileUtil.deleteFile(filePath);
        //then
        assertThat(isDeleted).isFalse();
    }

    @Test
    void 이미지삭제_기본프로필일때() throws IOException {
        //given
        String filePath = "/uploads/profile/" + "default-profile.png";
        //when
        boolean isDeleted = FileUtil.deleteFile(filePath);
        //then
        assertThat(isDeleted).isFalse();
    }

    @Test
    void 이미지삭제() throws IOException {
        // given
        when(imageFile.isEmpty()).thenReturn(false);
        when(imageFile.getOriginalFilename()).thenReturn("test-delete.png");

        String savedPath = FileUtil.saveProfileImage(imageFile);
        File savedFile = new File(PROFILE_DIR + savedPath.substring(savedPath.lastIndexOf("/") + 1));

        // when
        boolean deleteResult = FileUtil.deleteFile(savedPath);

        // then
        assertThat(savedFile.exists()).isFalse();
    }

    @Test
    void getDefaultProfileImageTest(){
        String defaultProfileImage = FileUtil.getDefaultProfileImage();
        assertThat(defaultProfileImage).isEqualTo("/uploads/profile/default-profile.png");
    }

    @Test
    void getResignProfileImageTest(){
        String resignProfileImage = FileUtil.getResignProfileImage();
        assertThat(resignProfileImage).isEqualTo("/uploads/profile/resign.png");
    }
}