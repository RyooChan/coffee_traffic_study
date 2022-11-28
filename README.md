# 2022-fbcks97-naver.com

# 카카오페이 사전과제

* 다수의 서버에 다수의 인스턴스로 동작할 때에 어떻게 문제가 없도록 했는가?
    * server내부의 세션이나 인메모리 캐싱을 하지 않음.
    * 외부 Redis, MySql활용
* 동시성 이슈 / 일관성 고려
    * 다중 쓰레드 동작 중 발생 가능 문제점 처리
        * Lock이나 Redis를 활용한 문제 처리

## 기능 구현 및 관련 정합성 관리

* 커피 메뉴 목록 조회 API
    * 목록 조회
        * Redis Cache 활용
* 포인트 충전 API
    * 포인트 충전
        * Pessimistic Lock 활용
* 커피 주문/결제 API
    * 포인트 사용
        * Pessimistic Lock 활용
    * 인기 메뉴 추가
        * Redis zSetOperations 활용
    * Mock API 페이정보 전송
        * PostMan활용
* 인기 메뉴 조회 API
    * Redis zSetOperations + sorted set 활용

---

## 커피 메뉴 목록 조회 API

### Logic

> 최초 로직

1. 메뉴 목록 조회 요청 
2. server측에서 MySql에 해당 코피 목록 검색
3. 검색된 목록 Redis에 Cache
4. 검색된 목록 Return

> 이후 로직

1. 메뉴 목록 조회 요청
2. Server측에서 Redis에 Cache된 Data 바로 Return

### 구현 이유

* MySql검색 시 데이터 Redis 캐싱
    * 커피 메뉴의 변경은 자주 일어나지 않을 것이라 판단했다.
    * 커피 메뉴의 검색은 자주 일어날 것이라 판단했다.


---

## 포인트 충전 API

### Logic

> 충전 로직

1. 해당 유저 포인트 충전 요청
2. Pessimistic lock을 통해 충전 중 다른 Thread 접근 차단
3. 충전 완료
4. 충전 결과 Return

### 구현 이유

* PessimisicLock을 통한 비관적 lock 진행
    * Redis를 사용하지 않은 이유
        * 한 사람의 포인트에 관한 내용이기 때문에 대량의 트래픽이 걸리거나 많은 충돌이 발생하지 않을 것이라 판단하였기 때문이다.
        * 한번에 한사람이 자신의 포인트 충전을 하는 것이기 때문.
    * OptimisticLock을 사용하지 않은 이유
        * Pessimistic Lock은 작업 도중에 Lock을 걸어 다른 쓰레드의 접근 자체를 차단하기 때문에 versioning을 통해 정합성을 맞추는 Optimistic Lock에 비해 데이터 정합성을 더 잘 보장할 수 있다고 생각했기 때문이다.
            * 포인트의 경우는 실제 돈과 연결되기 때문에 정합성이 중요하다 판단하였다.

---

## 커피 주문/결제 API

`포인트 사용 -> 커피 인기 증가 -> 외부 플랫폼 결제 결과 전달` 순서로 진행.

### Logic

> 포인트 사용 로직

1. 커피 주문 요청
2. 해당 유저 포인트 사용 요청
3. Pessimistic lock을 통해 충전 중 다른 Thread 접근 차단
4. 사용 완료 / 포인트 부족시 사용 불가
5. 사용 결과 Return 

> 커피 인기 증가 로직

1. 커피 주문 완료(실패시 Count안됨)
2. 해당 커피 인기 증가 요청
3. `전체인기` 를 key로 갖는 zSetOperations의 score+1
4. `당일인기` 를 key로 갖는 zSetOperations의 score+1
    5. 매일 00시00분00초마다, `7일전인기` 를 key로 갖는 zSetOperations의 score이 `전체인기`를 key로 갖는 zSetOperations의 에서 minus된다.

> 외부 플랫폼 결제 결과 전달

1. 커피 주문 및 score+1 완료
2. 외부 플랫폼에의 결제 결과 전달 요청
3. 전달 진행
4. 외부 플래랫폼에서의 response Return

### 구현 이유

> 포인트 사용 로직

* PessimisicLock을 통한 비관적 lock 진행
    * Redis를 사용하지 않은 이유
        * 한 사람의 포인트에 관한 내용이기 때문에 대량의 트래픽이 걸리거나 많은 충돌이 발생하지 않을 것이라 판단하였기 때문이다.
        * 한번에 한사람이 자신의 포인트 사용을 하는 것이기 때문.
    * OptimisticLock을 사용하지 않은 이유
        * Pessimistic Lock은 작업 도중에 Lock을 걸어 다른 쓰레드의 접근 자체를 차단하기 때문에 versioning을 통해 정합성을 맞추는 Optimistic Lock에 비해 데이터 정합성을 더 잘 보장할 수 있다고 생각했기 때문이다.
            * 포인트의 경우는 실제 돈과 연결되기 때문에 정합성이 중요하다 판단하였다.

> 커피 인기 증가 로직

* zSetOperations를 사용
    * 이를 사용해서 Sorted Set을 이용하여 랭킹 구현.
    * increaseHit를 통한 score값 증가.
        * ![](https://i.imgur.com/mNRpsUP.png)
        * ![](https://i.imgur.com/9SEG8JD.png)
        * 메소드를 확인해 보면 increaseHit Method는 just의 synchronizer를 이용하여 동기화가 진행된다.
            * 이를 사용하면 따로 lock이 없이도 동시성 이슈와 데이터 일관성을 유지할 수 있다고 판단하였다.
* `전체인기`, `당일인기`를 사용한 Redis 사용
    * 이유 후술

> 외부 플랫폼 결제 결과 전달

* PostMan 이용.

---

## 인기 메뉴 조회 API

### Logic

> 인기 메뉴 조회 로직

1. `전체인기` 를 key로 갖는 zSetOperations의 데이터를 sorted set

### 구현 이유

* Sorted Set이 갖는 장점
    * 빠른 속도로 정확한 값을 가져올 수 있다.
    * 동일한 score인 경우 key를 내림차순으로 결과 Return
    * double형 score이기 때문에 주의가 필요하다.
        * 컴퓨터에서는 실수가 표현할 수 없느 정수값들이 존재하기 때문이다.

---

## 고민 사항

---

## 인기 메뉴 출력 방식에의 고민

### 1. RDB에 값을 저장하고 불러오기

가장 기본적으로, 인기있는 메뉴를 요청할 때에 이를 RDB에서 가져와서 보여주는 방법이다.

> 데이터 저장

1. 매번 주문을 할 때마다 그 값을 RDB에 저장하기
2. 요청이 있을 때마다 RDB에서 7일치 값을 가져와서 보여주기

메뉴별 주문 횟수가 정확해야 하기 때문에
매일마다 주문 시 RDB에 저장해 두고 이를 7일치를 불러와서 진행하는 방식이다.

해당 방식은 **매번 실제로 DB에서 값을 가져와야 하기에(오늘치를 가져오면 값이 달라질수 있으므로) 다수의 트래픽을 받을 수 없다**는 문제가 있었다.

### 2. RDB에서 지난 6일치의 값을 가져와 캐싱하고 오늘 데이터는 Redis에 보관하기

이전 데이터를 Mysql에서 가져와 캐싱하고, 당일 데이터를 Redis에 보관하며 결제 시마다 score+1을 해주는 방법이다.

> 당일 데이터 Redis사용법

1. 매번 주문을 할 때마다 Redis에 zSetOperations의 incremetScore 진행
2. 오늘이 끝날 때에 RDB에 write back 값 저장
3. Redis비우기

> 전체 조회수 구하는 방법

1. 오늘이 시작할 때에 `7일 전`부터 `오늘 전날`까지의 값을 가져와서 Redis에 저장하기
2. 오늘의 주문 data는 redis에 이미 보관되어 있으므로 가져온다.
3. 인기 메뉴를 구해올 때에 이 두개를 더해서 보여준다.

* 장점
    * 정확한 값을 필요로 할 때에 좋은 방법이라 판단
        * RDB에 있는 전날까지의 데이터는 변경될 일이 적기 때문.
        * Redis의 값은 결제 시마다 변경 가능
* 단점
    * 성능 상의 문제
        * `7일전` ~ `1일전` 까지의 값들을 하나하나 가져와서 zSetOperations에 캐싱
            * O(N) * 6
        * `당일`의 데이터와 `이전` 데이터를 zSetOperations에서 더함
            * O(N)
        * 즉 (O(N) * 날짜수) 가 발생할 것 같았다.
            * Redis는 O(N)을 지양.
        * **대규모 트래픽에 더 맞는 방식이 필요하지 않을까?**

### 3. 전체 데이터를 Redis에 날짜별로 보관해서 진행하기

> 모든 날짜의 데이터를 Redis에 보관하며 진행하는 방식

### 3-1. 데이터를 필요로 할 때에 합치기

이 방식은 모든 데이터를 날짜별로 보관하고, 그걸 필요로 할 때에 활용하는 방법이다.

> `결제 당일 데이터` 를 zSetOperations에 보관

1. zSetOperations에서 key로 `hit:날짜` 를 통해 해당 날짜의 데이터를 넣어준다.

> 데이터 요청 시

1. `hit:오늘-7` ~ `hit:오늘` 의 score 합산
2. Sorted Set진행 후 데이터 출력

* 장점
    * 모든 날짜를 보관할 수 있기 때문에 원하는 기간의 데이터를 바로 보여줄 수 있음.
* 단점
    * 인기메뉴를 검색하는 경우가 결제하는 경우보다 많을 것이다.
        * 검색 때마다 모든 날짜의 데이터를 더해야 하기 때문에 O(N)이 날짜 수만큼 발생
    * Redis에 너무 많은 데이터가 들어가게 된다.
    * **인기메뉴를 검색할 때에 성능 저하를 줄일 방법은 없을까?**

### 3-2. (AS-IS) 모든 결제데이터 - 8일전의 결제데이터를 보관하기

`전체인기`, `당일인기` 두 가지 방법으로 데이터를 처리하는 방법.

> 인기 저장

1. `당일` key를 갖는 zSetOperations에 score+1.
2. `전체` key를 갖는 zSetOperations에 score+1.

> 인기 확인

1. `당일` key를 갖는 zSetOperations 사용
2. Sorted Set이후 해당 값 출력

> 인기 조작

1. 매일 00시 00분 00초 schedule
2. 해당 시간에 `전체` zSetOperations에서 `해당날짜-7`의 데이터 제거
3. 남은 데이터는 7일전의 값이 제거된(6~당일)의 datas

* 장점
    * 성능 상 이점
        * 인기메뉴를 검색하는 로직에서는 Sorted Set만 진행
        * 7일전 데이터 제거 때에도 O(N) 1회 발생
            * 이것도 지양해야 하지만 다른 방식보다는 적다.
* 단점
    * Redis에의 부하
        * Redis가 너무 많은 데이터를 보관하게 된다.
    * 취약
        * 데이터를 만들어 내는것이 아니라, 있던 것들을 조작하기 때문에 서버에 문제가 발생하거나 스펙이 변경되면 문제가 발생한다.
    * 개선 방안
        * Batch를 사용하여 날마다 데이터를 저장한다면 위의 단점을 많이 개선시킬수 있을 것이다.

이번 과제에서의 요청사항인 동시성 이슈와 데이터 일관성 유지, 다량의 트래픽 처리 가능성에 중점을 두어 해당 방법으로 진행하였다.

---

### 추가 고민

* 만약 Coffee가 재고를 갖게 된다면?
    * Coffee가 재고를 갖게 되면 남은 커피의 양과 관련없이 이를 요청하는 상황이 있을 수 있다.
        * 해당 경우의 동시성 이슈도 고려하면 좋을 것 같다.
* kafka, Elastic Search를 이용한 방안
    * kafka는 여러 요청을 Queue에 저장하고, 이후 Produce한다.
    * Logstash에서 해당 데이터를 Consume해 온다.
    * ElasticSearch에 데이터를 인덱싱한다.
    * 7일 조회에 관한 요청을 ElasticSearch에 진행한다.
    * 이와 같은 방법을 생각해 보았고, 나중에 해보고 싶다.
* Ehcache 인메모리 캐시 이용
    * 커피 메뉴 / 인기 메뉴와 같은 내용은 모든 유저에게 공통이다.
    * 해당 데이터의 경우 데이터가 적다.
    * Ehcache를 사용하면 redis보다 더 빠른 속도로 제공 가능하다.
    * 다만 Ehcache의 경우 다중 서버 환경에서의 설정이 복잡하다.
* nGrinder등을 이용한 성능테스트
    * 다양한 방법으로 실제 성능 테스트를 진행해보고, 성능 최적화를 해보고 싶다.

---

감사합니다!!
