# 20210922_Initialize Project
- Maven 사용
  - resources/META-INF/persistence.xml : 설정 파일
  - pom.xml : Maven 외부 라이브러리 및 버전 관리

# 20210923_영속성 컨텍스트(Persistence Context)
### 영속성 컨텍스트(Persistence Context)
- 엔티티를 영구 저장하는 환경
- EntityManager -> PersistenceContext 접근
  - EntityManager.persist(entity);
- 엔티티의 생명주기
  - 비영속(new/transient)
    - 영속성 컨텍스트와 전혀 관계 없는 새로운 상태
  - 영속(managed)
    - 영속성 컨텍스트에 관리되는 상태
  - 준영속(detached)
    - 영속성 컨텍스트에 저장되었다가 분리된 상태
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
    - sdf
