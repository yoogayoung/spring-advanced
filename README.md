# 스프링 핵심 원리-고급편(김영한) 학습 1
>> https://www.inflearn.com/course/%EC%8A%A4%ED%94%84%EB%A7%81-%ED%95%B5%EC%8B%AC-%EC%9B%90%EB%A6%AC-%EA%B3%A0%EA%B8%89%ED%8E%B8

#
## _💻로그 추적기 구현_

- ThreadLocal, 템플릿 메서드 패턴, 템플릿 콜백 패턴을 적용하여 로그 추적기를 구현한다.
- Project: Gradle Project
- Language: Java
- Spring Boot: 2.5.x
- Dependencies: Spring Web, Lombok

#
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

#
### Version 0 - 프로세스 생성
- Controller -> Service -> Repository의 기본 흐름을 만든다.
- 실행 : http://localhost:8080/v0/request?itemId=hello
- 결과 : ok

#
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
- 정상 실행 로그 결과
```sh
[11111111] OrderController.request()
[22222222] OrderService.orderItem()
[33333333] OrderRepository.save()
[33333333] OrderRepository.save() time=1000ms
[22222222] OrderService.orderItem() time=1001ms
[11111111] OrderController.request() time=1001ms
```
- 예외 실행 로그 결과
```sh
[5e110a14] OrderController.request()
[6bc1dcd2] OrderService.orderItem()
[48ddffd6] OrderRepository.save()
[48ddffd6] OrderRepository.save() time=0ms ex=java.lang.IllegalStateException:예외 발생!
[6bc1dcd2] OrderService.orderItem() time=6ms ex=java.lang.IllegalStateException: 예외 발생!
[5e110a14] OrderController.request() time=7ms ex=java.lang.IllegalStateException: 예외 발생!
```

#
### Version 2 - 파라미터로 동기화 개발
- 트랜잭션ID와 메서드 호출의 깊이를 표현하기 위해 첫 로그에서 사용한 트랜잭션ID 와 깊이(level)를 다음 로그에 넘겨준다.
- 실행
  - 정상 : http://localhost:8080/v2/request?itemId=hello
  - 예외 : http://localhost:8080/v2/request?itemId=ex
- 정상 실행 로그 결과
```sh
[c80f5dbb] OrderController.request()
[c80f5dbb] |-->OrderService.orderItem()
[c80f5dbb] | |-->OrderRepository.save()
[c80f5dbb] | |<--OrderRepository.save() time=1005ms
[c80f5dbb] |<--OrderService.orderItem() time=1014ms
[c80f5dbb] OrderController.request() time=1017ms
```
- 예외 실행 로그 결과
```sh
[ca867d59] OrderController.request()
[ca867d59] |-->OrderService.orderItem()
[ca867d59] | |-->OrderRepository.save()
[ca867d59] | |<X-OrderRepository.save() time=0ms ex=java.lang.IllegalStateException: 예외 발생!
[ca867d59] |<X-OrderService.orderItem() time=7ms ex=java.lang.IllegalStateException: 예외 발생!
[ca867d59] OrderController.request() time=7ms ex=java.lang.IllegalStateException: 예외 발생!
```
- 문제 : 동기화는 성공했지만, 로그를 출력하는 모든 메서드에 TraceId 파라미터를 추가해야 하는 문제가 발생한다.

#
### Version 3 - 쓰레드 로컬 동기화 개발
- LogTrace 인터페이스, FieldLogTrace 클래스를 생성한다.
- TraceId 를 동기화 할 때 파라미터가 아닌 TraceId traceIdHolder 필드를 사용한다.
   - 테스트 코드 : advanced/src/test/java/hello/advanced/trace/logtrace/FieldLogTraceTest.
- 이 때 동시에 여러 사용자가 요청하면 로그가 섞여서 출력되는 동시성 문제가 발생한다.
  - 동시성 문제를 ThreadLocal을 사용하여 해결한다.
- 필드 대신에 쓰레드 로컬을 사용해서 데이터를 동기화하는 ThreadLocalLogTrace를 생성한다.
- Controller, Service, Repository에 ThreadLocalLogTrace를 적용한다.
- 실행
  - 정상 : http://localhost:8080/v3/request?itemId=hello
  - 예외 : http://localhost:8080/v3/request?itemId=ex
- 정상 실행 로그 결과
```sh
[f8477cfc] OrderController.request()
[f8477cfc] |-->OrderService.orderItem()
[f8477cfc] | |-->OrderRepository.save()
[f8477cfc] | |<--OrderRepository.save() time=1004ms
[f8477cfc] |<--OrderService.orderItem() time=1006ms
[f8477cfc] OrderController.request() time=1007ms
```
- 예외 실행 로그 결과
```sh
[c426fcfc] OrderController.request()
[c426fcfc] |-->OrderService.orderItem()
[c426fcfc] | |-->OrderRepository.save()
[c426fcfc] | |<X-OrderRepository.save() time=0ms ex=java.lang.IllegalStateException: 예외 발생!
[c426fcfc] |<X-OrderService.orderItem() time=7ms ex=java.lang.IllegalStateException: 예외 발생!
[c426fcfc] OrderController.request() time=7ms ex=java.lang.IllegalStateException: 예외 발생!
```
- 문제 : V0는 핵심 기능만 있지만, 로그 추적기를 추가한 V3코드는 핵심 기능과 부가 기능이 함께 섞여있어 코드가 더 많다.

#
### Version 4 - 템플릿 메서드 패턴 적용
- 핵심 기능과 부가 기능을 분리하여 모듈화 하기 위해 템플릿 메서드 패턴을 적용한다.
- 템플릿 메서드 패턴에서 부모 클래스이고, 템플릿 역할을 하는 AbstractTemplate을 생성한다.
- 테스트 코드 : advanced/src/test/java/hello/advanced/trace/template/TemplateMethodTest.java
- Controller, Service, Repository에 AbstractTemplate를 적용한다.
- 실행
  - 정상 : http://localhost:8080/v4/request?itemId=hello
- 정상 실행 로그 결과
```sh
[aaaaaaaa] OrderController.request()
[aaaaaaaa] |-->OrderService.orderItem()
[aaaaaaaa] | |-->OrderRepository.save()
[aaaaaaaa] | |<--OrderRepository.save() time=1004ms
[aaaaaaaa] |<--OrderService.orderItem() time=1006ms
[aaaaaaaa] OrderController.request() time=1007ms
```
- 문제 : 템플릿 메서드 패턴은 상속을 사용한다. 따라서 상속에서 오는 단점들을 그대로 안고간다.

#
### 전략 패턴 
- 템플릿 메서드 패턴과 비슷한 역할을 하면서 상속의 단점을 제거할 수 있는 디자인 패턴이 전략 패턴이다.
- 변하지 않는 부분을 Context 라는 곳에 두고, 변하는 부분을 Strategy 라는 인터페이스를 만들고 해당 인터페이스를 구현하도록 해서 문제를 해결한다.
- 테스트 코드
  - advanced/src/test/java/hello/advanced/trace/strategy/ContextV1Test.java
  - advanced/src/test/java/hello/advanced/trace/strategy/ContextV2Test.java

#
### Version 5 - 템플릿 콜백 패턴 적용
- 테스트 코드 : advanced/src/test/java/hello/advanced/trace/template/TemplateMethodTest.java
- 콜백을 전달하는 인터페이스 TraceCallback과 템플릿 역할을 하는 TraceTemplate 클래스를 생성한다.
- Controller, Service, Repository에 TraceTemplate를 적용한다.
- - 실행
  - 정상 : http://localhost:8080/v5/request?itemId=hello
- 정상 실행 로그 결과
```sh
[aaaaaaaa] OrderController.request()
[aaaaaaaa] |-->OrderService.orderItem()
[aaaaaaaa] | |-->OrderRepository.save()
[aaaaaaaa] | |<--OrderRepository.save() time=1001ms
[aaaaaaaa] |<--OrderService.orderItem() time=1003ms
[aaaaaaaa] OrderController.request() time=1004ms
```
