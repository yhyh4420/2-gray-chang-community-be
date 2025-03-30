## 나만의 커뮤니티를 만들자

-------

![Image](https://github.com/user-attachments/assets/b113fe7c-a7bd-4701-97a2-82c1ada4f1fe)

-------

## 📁프로젝트 구조

```
src
├── main
│   ├── java
│   │   └── kakaotech
│   │       └── communityBE
│   │           ├── controller     // API 엔드포인트
│   │           ├── dto            // 요청/응답 DTO 클래스
│   │           ├── entity         // JPA 엔티티 클래스
│   │           ├── repository     // 데이터베이스 접근 레이어
│   │           ├── service        // 비즈니스 로직 처리
│   │           ├── util           // 파일 업로드 등 공통 유틸리티
│   │           └── config         // 보안, 세션 등 환경 설정
│   └── resources
│       ├── application.yml        // 기본 환경 설정
│       └── static
│           └── images             // 정적 이미지 리소스
├── test
│   └── java
│       └── kakaotech
│           └── communityBE
│               └── ...            // 각 계층별 테스트 코드
uploads
├── profile                         // 업로드된 프로필 이미지 저장소(기본이미지, 탈퇴이미지 포함)
└── postImage                       // 업로드된 게시물 이미지 저장소
```

🌼FE 링크 : https://github.com/yhyh4420/2-gray-chang-community

---

## 🚀 주요 기능

- **회원 기능**
    - 회원가입 / 로그인 / 로그아웃
    - 세션 기반 로그인 상태 유지 (Redis 사용)
    - 프로필 이미지 업로드 및 프로필 미선택 시 기본 이미지 제공, 탈퇴 시 soft delete 위한 탈퇴 유저 기본이미지 제공
- **게시글 기능**
    - 게시글 생성 / 조회 / 수정 / 삭제
    - 이미지 포함 게시물 업로드 가능
- **댓글 기능**
    - 댓글 작성 / 수정 / 삭제
- **좋아요 기능**
    - 게시글에 대한 좋아요/취소
- **기타**
    - Redis 기반 세션 관리
    - 파일 업로드 및 삭제

---

## 📌 API 엔드포인트 정리

### 1. User

| 기능      | 메서드      | 엔드포인트                   | 설명                                       |
|---------|----------|-------------------------|------------------------------------------|
| 회원 가입   | `POST`   | `/auth/signup`          | 신규 유저 회원가입                               |
| 회원 탈퇴   | `DELETE` | `/auth/resign`          | 유저 회원 탈퇴, 탈퇴 시 회원정보 soft-delete 전략으로 삭제됨 |
| 로그인     | `POST`   | `/auth/login`           | 로그인, 검증 후 세션아이디 부여                       |
| 로그아웃    | `POST`   | `auth/logout`           | 로그아웃, 세션아이디 삭제                           |
| 세션확인    | `GET`    | `/auth/session-user`    | 사용자의 세션아이디가 유효한지 검증                      |
| 닉네임 수정  | `PUT`    | `/auth/update/nickname` | 닉네임 변경 후 user db에 반영                     |
| 비밀번호 수정 | `PUT`    | `/auth/update/password` | 비밀번호 변경 후 user db에 반영                    |

### 2. Post

| 기능          | 메서드      | 엔드포인트                   | 설명                                  |
|-------------|----------|-------------------------|-------------------------------------|
| 모든 게시물 불러오기 | `GET`    | `/posts`                | 모든 게시글 불러오기                         |
| 게시물 불러오기    | `GET`    | `/posts/{postId}`       | 선택한 게시글 조회, 조회 시 조회수가 증가함           |
| 게시물 생성하기    | `POST`   | `/posts`                | 게시글 생성하여 posts db에 저장               |
| 게시물 수정하기    | `PUT`    | `/posts/{postId}`       | 게시글 수정하여 posts db에 저장               |
| 게시글 삭제하기    | `DELETE` | `/posts/{postId}`       | 게시글 삭제                              |
| 좋아요 누르기     | `PUT`    | `/posts/{postId}/likes` | 게시글에 좋아요를 누르고 posts db, like db에 반영 |

### 3. Comment

| 기능         | 메서드      | 엔드포인트                                 | 설명                              |
|------------|----------|---------------------------------------|---------------------------------|
| 모든 댓글 불러오기 | `GET`    | `/posts/{postId}/comment`             | 특정 게시글 내 모든 댓글 가져오기             |
| 댓글 생성하기    | `POST`   | `/posts/{postId}/comment`             | 특정 게시글에 댓글 생성                   |
| 댓글 수정하기    | `PUT`    | `/posts/{postId}/comment/{commdntId}` | 특정 게시글의 댓글 수정, 수정 시 본인 확인 로직 포함 |
| 댓글 삭제하기    | `DELETE` | `/posts/{postId}/comment/{commdntId}` | 특정 게시글의 댓글 삭제, 삭제 시 본인 확인 로직 포함 |

-----

## ⚙️ 기술 스택

| 분류         | 기술                                                 |
|------------|----------------------------------------------------|
| **백엔드**    | Java 21, Spring Boot                               |
| **데이터베이스** | MySQL                                              |
| **ORM**    | Spring Data JPA                                    |
| **보안**     | Spring Security                                    |
| **세션 관리**  | Redis                                              |
| **테스트**    | JUnit 5, AssertJ, DataJpaTest, WebMvcTest, Mockito |
| **기타**     | MultipartFile, File I/O, REST API                  |

---
트러블슈팅 및 고도화 연구, 회고 : [링크](retrospect.md)