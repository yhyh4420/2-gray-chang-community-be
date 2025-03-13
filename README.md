# 2-gray-chang-community-be
kakaotech bootcamp community be(SpringBoot)

### 문제 1
1. 세션 생성은 성공적이고 클라이언트로 전달됨(로그인 메서드 로그에서 확인)
2. 그러나 세션 검증api에서 자꾸 null을 반환하는 현상 발생

생각한거
1. 처음에는 ```HttpServletRequest```를 파라미터로 받아 서버 인메모리를 사용하려고 했음
    ```java
    @GetMapping("/auth/session-user")
    public ResponseEntity<?> getSessionUser(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        //session 있는지 검증로직
        Object userId = session.getAttribute("userId");
        // userId가 있는지 검증하는 로직
        logger.info("세션 유지 중 - 유저 ID: {}", userId);
        return ResponseEntity.ok().body(userId);
    }
    ```
2. 로그 확인 결과 다음과 같은 결과가 나오는 것을 확인
    ```
   세션은 유효한데 userId 없음 : 7
   ```
3. 그러니까 세션은 클라이언트가 서버로 잘 전달을 해주는데, 이를 매치하지 못하는 것으로 추정했다.
4. userId를 검증하는 로직은 아래와 같다.
    ```java
   Object userId = session.getAttribute("userId");
    ```
5. 세션에서 userId를 조회하지 못해 null을 반환하는 상황이었다.
6. 당장 세션 발급 방식을 변경할 수도 있었지만, 세션기반 로그인의 본질에 대해 조금 더 집중했다.
    * 로그인 시 사용자에게 세션을 발급하고, 사용자는 인가가 필요한 모든 로직에 세션을 제출하면 그걸 **세션스토리지** 에서 비교하고 확인하여 접근 여부를 결정한다.
7. userId를 세션에 포함하여 발급하면 향후 유저아이디가 노출된다는 단점이 있어, 세션발급에 userId를 추가하는 방식은 제외했다.
8. 대신 서버에서 세션스토리지를 생성하여 거기에서 sessionId와 userId를 매핑하는 것으로 결정했다.
    * 지금은 권한에 따른 별도 인가 과정이 없다. 그래서 sessionId-userId 매핑만으로 가능하지만, 만약 권한이 세분화된다면 새로운 방식을 고민해야된다.(Redis 도입?)\
9. sessionId는 uuid로 생성하여 128비트 16진수 난수로 생성하였다.
    * 혹시 세션아이디가 겹치지 않을까? 하는 만약의 불안감에 sessionId + userId를 sha256으로 해싱하여 세션아이디로 리턴하는 방식을 고려했으나,
        * 단순 세션 발급에 보안 알고리즘이 들어가버리면 최초 세션 발급 시(로그인 시) 동시접속 유저가 많아지면 세션스토리지 접근 시 병목현상이 발생할 것 같았다.
        * 그리고 기존 uuid 기반 발급은 어차피 의미없는 난수 생성이므로 보안성, 식별성에 해싱 도입은 큰 이점이 없다고 생각했다.