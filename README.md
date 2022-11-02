# [스프링 핵심 원리-고급편(김영한)] 강의 학습
## _로그 추적기 구현_

Project: Gradle Project
Language: Java
Spring Boot: 2.5.x
Dependencies: Spring Web, Lombok

### 요구사항
- 모든 PUBLIC 메서드의 호출과 응답 정보를 로그로 출력
- 애플리케이션의 흐름을 변경하면 안됨
  - 로그를 남긴다고 해서 비즈니스 로직의 동작에 영향을 주면 안됨
- 메서드 호출에 걸린 시간
- 정상 흐름과 예외 흐름 구분
- 예외 발생시 예외 정보가 남아야 함
- 메서드 호출의 깊이 표현
- HTTP 요청을 구분
  - HTTP 요청 단위로 특정 ID를 남겨서 어떤 HTTP 요청에서 시작된 것인지 명확하게 구분이 가능해야 함
  - 트랜잭션 ID (DB 트랜잭션X), 여기서는 하나의 HTTP 요청이 시작해서 끝날 때 까지를 하나의 트랜잭션이라 함
- 예시
```sh
정상 요청
[796bccd9] OrderController.request()
[796bccd9] |-->OrderService.orderItem()
[796bccd9] | |-->OrderRepository.save()
[796bccd9] | |<--OrderRepository.save() time=1004ms
[796bccd9] |<--OrderService.orderItem() time=1014ms
[796bccd9] OrderController.request() time=1016ms

예외 발생
[b7119f27] OrderController.request()
[b7119f27] |-->OrderService.orderItem()
[b7119f27] | |-->OrderRepository.save()
[b7119f27] | |<X-OrderRepository.save() time=0ms
ex=java.lang.IllegalStateException: 예외 발생!
[b7119f27] |<X-OrderService.orderItem() time=10ms
ex=java.lang.IllegalStateException: 예외 발생!
[b7119f27] OrderController.request() time=11ms
ex=java.lang.IllegalStateException: 예외 발생!
```

### Version 0 - 프로세스 생성
- Controller -> Service -> Repository의 기본 흐름을 만든다.
- 실행 : http://localhost:8080/v0/request?itemId=hello
- 결과 : ok

### Version 1 - 로그 추적기 프로토타입 개발 및 적용
- 로그 추적기를 위한 기반 데이터를 가지고 있는 TraceId, TraceStatus 클래스 생성한다.
  - TraceId 클래스 : 로그 추적기의 트랜잭션ID와 깊이를 표현
  - TraceStatus 클래스 : 로그의 상태 정보
- HelloTraceV1를 사용해서 실제 로그를 시작하고 종료할 수 있고, 로그를 출력하고 실행시간도 측정할 수 있다.
  - 테스트 코드 : advanced/src/test/java/hello/advanced/trace/hellotrace/HelloTraceV1Test.java
- Controller, Service, Repository에 로그 추적기를 적용한다.
- 실행
  - 정상 : http://localhost:8080/v1/request?itemId=hello
  - 예외 : http://localhost:8080/v1/request?itemId=ex
- 결과
  - 정상 실행 로그
```sh
[11111111] OrderController.request()
[22222222] OrderService.orderItem()
[33333333] OrderRepository.save()
[33333333] OrderRepository.save() time=1000ms
[22222222] OrderService.orderItem() time=1001ms
[11111111] OrderController.request() time=1001ms
```
  - 예외 실행 로그
```sh
[5e110a14] OrderController.request()
[6bc1dcd2] OrderService.orderItem()
[48ddffd6] OrderRepository.save()
[48ddffd6] OrderRepository.save() time=0ms ex=java.lang.IllegalStateException:예외 발생!
[6bc1dcd2] OrderService.orderItem() time=6ms ex=java.lang.IllegalStateException: 예외 발생!
[5e110a14] OrderController.request() time=7ms ex=java.lang.IllegalStateException: 예외 발생!
```
