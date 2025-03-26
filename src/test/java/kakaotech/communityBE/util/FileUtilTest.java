package kakaotech.communityBE.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class FileUtilTest {

    private static final String BASE_UPLOAD_DIR = System.getProperty("user.dir") + "/uploads/";
    private static final String PROFILE_DIR = BASE_UPLOAD_DIR + "profile/";
    private static final String POST_DIR = BASE_UPLOAD_DIR + "postImage/";
    private static final String DEFAULT_PROFILE = "/uploads/profile/default-profile.png";
    private static final String RESIGN_PROFILE = "/uploads/profile/resign.png";

    private MultipartFile imageFile;

    @BeforeEach
    void setUp() throws IOException {
        imageFile = mock(MultipartFile.class);

        // 실제 디렉토리 생성
        new File(PROFILE_DIR).mkdirs();
        new File(POST_DIR).mkdirs();

        // 모든 테스트에서 공통으로 사용하는 기본 mock 동작
        when(imageFile.isEmpty()).thenReturn(false);
        when(imageFile.getOriginalFilename()).thenReturn("test.jpg");

        // 핵심: transferTo() 가 실제 파일 저장하지 않도록 mock 처리
        doAnswer(invocation -> {
            File targetFile = invocation.getArgument(0);
            targetFile.createNewFile(); // 실제 파일을 만든 척
            return null;
        }).when(imageFile).transferTo(any(File.class));
    }

    @Test
    void 프로필이미지_저장_성공() throws IOException {
        String savedPath = FileUtil.saveProfileImage(imageFile);

        assertThat(savedPath).isNotNull();
        assertThat(savedPath).contains("/uploads/profile/");
        assertThat(savedPath).contains("test.jpg");
    }

    @Test
    void 게시글이미지_저장_성공() throws IOException {
        when(imageFile.getOriginalFilename()).thenReturn("post.jpg");

        String savedPath = FileUtil.savePostImage(imageFile);

        assertThat(savedPath).isNotNull();
        assertThat(savedPath).contains("/uploads/postImage/");
        assertThat(savedPath).contains("post.jpg");
    }

    @Test
    void 프로필이미지_저장_없을때_null반환() throws IOException {
        when(imageFile.isEmpty()).thenReturn(true);

        String savedPath = FileUtil.saveProfileImage(imageFile);

        assertThat(savedPath).isNull();
    }

    @Test
    void 게시글이미지_저장_없을때_null반환() throws IOException {
        when(imageFile.isEmpty()).thenReturn(true);

        String savedPath = FileUtil.savePostImage(imageFile);

        assertThat(savedPath).isNull();
    }

    @Test
    void 이미지삭제_파일이_null이면_false() {
        boolean isDeleted = FileUtil.deleteFile(null);
        assertThat(isDeleted).isFalse();
    }

    @Test
    void 이미지삭제_기본프로필이면_false() {
        boolean isDeleted = FileUtil.deleteFile(DEFAULT_PROFILE);
        assertThat(isDeleted).isFalse();
    }

    @Test
    void 기본프로필_경로반환() {
        String defaultPath = FileUtil.getDefaultProfileImage();
        assertThat(defaultPath).isEqualTo(DEFAULT_PROFILE);
    }

    @Test
    void 탈퇴프로필_경로반환() {
        String resignPath = FileUtil.getResignProfileImage();
        assertThat(resignPath).isEqualTo(RESIGN_PROFILE);
    }
}
