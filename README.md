# 20210922_Initialize Project
- Maven 사용
  - resources/META-INF/persistence.xml : 설정 파일
  - pom.xml : Maven 외부 라이브러리 및 버전 관리

# 20210923_영속성 관리 - 내부 동작 방식
### 영속성 컨텍스트(Persistence Context)
- 엔티티를 영구 저장하는 환경
- EntityManager -> PersistenceContext(1차 캐시?) 접근
  - EntityManager.persist(entity);
- 엔티티의 생명주기
  - 비영속(new/transient)
    - 영속성 컨텍스트와 전혀 관계 없는 새로운 상태
  - 영속(managed)
    - 영속성 컨텍스트에 관리되는 상태
  - 준영속(detached)
    - 영속성 컨텍스트에 저장되었다가 분리된 상태
    - 영속성 컨텍스트가 제공하는 기능 사용하지 못함
  ````java
  em.detach(entity); //특정 엔티티만 분리
  em.clear(); //영속성 컨텍스트 모두 초기화 
  em.close(); //영속성 컨텍스트 종료
  ````
  - 삭제(removed)
    - 삭제된 상태
- 영속적 컨텍스트의 이점
  - 1차 캐시
  - 동일성(identity) 보장
    - 1차 캐시로 반복 가능한 읽기(Repeatable Read) 등급의 트랜잭션 격리 수준을 DB가 아닌 애플리케이션 차원에서 제공
  - 트랙잭션을 지원하는 쓰기 지연(transactional write-behind)
    - DB에 반영할 SQL을 생성하여 entityManager의 쓰기 지연 SQL 저장소에 쌓아둠 (batch 옵션 이용하여 제한을 둘 수 있음)
  ````java
  EntityManager em = emf.createEntityManager();
  EntityTransaction tx = em.getTransaction();
  tx.begin();
  
  em.persist(memberA);
  em.persist(memberB)
  //여기까지 INSERT SQL을 DB에 보내지 않음
  
  //커밋하는 순간 DB에 INSERT SQL을 보냄
  tx.commit();
  ````
  - 변경 감지(Dirty Checking)
    - 영속 엔티티를 조회하여 수정할 때, 1차 캐시의 엔티티와 스냅샷(영속성 컨텍스트에 들어온 최초 시점의 상태)을 비교하여 바뀐 부분이 있으면 UPDATE 쿼리를 쓰기 지연 SQL 저장소에 저장
    - 플러시 (flush): 영속성 컨텍스트의 변경 내용을 DB에 반뎡
      1. 변경감지 
      2. 수정된 엔티티 쓰기 지연 SQL 저장소에 등록
      3. 쓰기 지연 SQL 저장소의 쿼리를 DB에 전송(등록, 수정, 삭제 쿼리)
      - em.flush : 직접 호출(거의 사용하지 않음)
      - 트랜잭션 커밋, JPQL 쿼리 실행 : 플러시 자동 호출
      - 영속성 컨텍스트를 비우지 않음
      - 영속성 컨텍스트의 변경 내용을 DB에 동기화 하는 것
      - 트랜잭션이라는 작업 단위가 중요 -> 커밋 직전에만 동기화 하면 됨
  ````java
  Member findMember = em.find(Member.class, 2L);
  findMember.setName("user2"); // User2 -> user2
  
  //em.persist(findMember); 없어도 동작함
  ````
  - 지연 로딩(Lazy Loadingg)
  

# 20210924_엔티티 매핑
- 객체 @Entity : JPA가 관리하는 엔티티
  - 기본 생성자 필수
  - final 클래스, enum, interface, inner 클래스 사용 X
  - 저장할 필드에 final 사용 X
  - 속성 : name
    - JPA에서 사용할 엔티티 이름 지정
    - 기본값 : 클래스 이름 그대로 사용
    - 같은 클래스 이름이 없으면 가급적 기본값 사용한다
- 테이블 @Table
- 필드, 컬럼 @Column
- 기본 키 @Id
- 연관관계 @ManyToOne, @JoinColumn

### 데이터베이스 스키마 자동 생성
- DDL을 애플리케이션 실행 시점에 자동 생성
- 테이블 중심 -> 객체 중심
- 데이터베이스에 맞는 적절한 DDL 생성
- 생성된 DDL은 개발 장비에서만 사용하고, 운영 서버에서는 사용하지 않거나 적절히 다듬은 후 사용할 것 
  - 운영 장비에서는 절대 create, create-drop, update 사용 X
- hibernate.hbm2ddl.auto
  - create : 기존 테이블 삭제 후 다시 생성 (DROP + CREATE)
  - create-drop : create와 같으나 종료시점에 테이블 DROP
  - update : 변경분만 반영(운영 DB 사용 금지)
  - validate : 엔티티와 테이블이 정상 매핑되었는지만 확인
  - none : 사용하지 않음

### DDL 생성 기능
- 제약 조건 추가 
  - @Column(nullable = false, length = 10) : 필수 항목, 10자 초과 X
  - DDL 생성 기능에만 사용되고 JPA 실행 로직에는 영향 X