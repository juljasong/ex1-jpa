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

# 20210925_필드와 컬럼 매핑

````java
@Entity
//@Table(name = "MEMBER")
public class Member {
    @Id
    private Long id;

    @Column(name = "name")
    private String username;

    private Integer age;

    @Enumerated(EnumType.STRING) // ENUM 사용시
    private RoleType roleType;

    @Temporal(TemporalType.TIMESTAMP) // TemporalType 사용시 : Date, TIME, TIMESTAMP
    private Date createdDate;

    @Temporal(TemporalType.TIMESTAMP)
    private Date lastModifiedDate;

    @Lob // VARCHAR 보다 긴 문장
    private String description;
}
````
=>
create table Member (
id bigint not null,
age integer,
createdDate timestamp,
description clob,
lastModifiedDate timestamp,
roleType varchar(255),
name varchar(255),
primary key (id)
)

- @Column : 컬럼 매핑
  - name : 필드와 매핑할 테이블의 컬럼 이름 (default: 객체 필드 이름)
  - insertable, updatable : 등록, 변경 여부 (default: TRUE)
  - nullable(DDL) : null 값 허용 여부 설정. false로 설정하면 DDL 생성 시 not null 제약 조건 붙음.
  - unique(DDL) : @Table의 uniqueConstraints와 같지만 한 컬럼에 간단히 유니크 제약조건을 걸 때 사용한다. 단 유니크 컬럼의 이름은 랜덤으로 지정되니 사용하지 않음.
  - columnDefinition(DDL) : 데이터 베이스 컬럼 정보를 직접 줄 수 있음. 
    - ex) columnDefinition = "varchar(100) default 'EMPTY'"
  - length(DDL) : 문자 길이 제약 조건, String 타입에만 사용
  - precision, scale(DDL) : BigDecimal/BigInteger 타입에서 사용.
    - precision : 소수점을 포함한 전체 자릿수
    - scale : 소수의 자릿수
- @Temporal : 날짜 타입 매핑. LocalDate, LocalDateTime 사용 시에는 생략 가능
  - TemporalType.TIME, TemporalType.DATE, TemporalType.TIMESTAMP
- @Enumerated : enum 타입 매핑
    - EnumType.ORDINAL : enum 순서를 DB에 저장. 사용 X
    - EnumType.STRING : enum 이름을 DB에 저장
- @Lob : BLOB, CLOB 매핑.
  - BLOB : 매핑하는 문자열이 String이 아닌 경우 자동 지정
  - CLOB : 매핑하는 문자열이 String인 경우 자동 지정
- @Transient : 특정 필드를 매핑에서 제외
  - DB 저장 및 조회 X
  - 주로 메모리상에서만 임시로 어떤 값을 보관하고 싶을 때 사용
  
# 20210926_기본 키 매핑
- @Id : 직접 할당
- @GeneratedValue : 자동 생성 ex) @GeneratedValue(strategy = GenerationType.AUTO)
  - AUTO : 방언에 따라 자동 지정, 기본값 
  
  - IDENTITY : 데이터베이스에 위임, MYSQL
    - 이 경우, commit 이전에 insert 쿼리가 날아감(DB에 접근하기 전까지 PK를 알 수 없기 때문에)
  
  - SEQUENCE : 데이터베이스 시퀀스 오브젝트 사용, ORACLE
    - @SequenceGenerator 필요
      - sequenceName : DB에 등록되어 있는 시퀀스 이름
      - initialValue : DDL 생성 시에만 사용, 시퀀스 DDL을 생성할 때 처음 1로 시작하는 수 지정
      - allocationSize(default: 50) : 시퀀스 한 번 호출에 증가하는 수(성능 최적화에 사용됨). DB 시퀀스 값이 하나씩 증가하도록 설정되어 있으면 이 값을 반드시 1로 설정해야 함.
      - catalog, schema : DB catalog, schema 이름
```java
        @Entity
        @SequenceGenerator(name = "member_seq_generator", 
                           sequenceName = "member_Seq")
        public class Member {

        @Id
        @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "member_seq_generator") // AUTO(default)
        private Long id;
        
        }
```
- 
  - TABLE : 키 생성용 테이블 사용, 모든 DB에서 사용
    - 장점 : 모든 DB에 적용 가능
    - @TableGenerator 필요
      - name(필수) : 식별자 생성기 이름
      - table(hibernate_sequences) : 키 생성 테이블명
      - pkColumnName(sequence_name) : 시퀀스 컬럼명
      - valueColumnNa(next_val) : 시퀀스 값 컬럼명
      - pkColumnValue(엔티티 이름) : 키로 사용할 값 이름
      - initialValue(0) : 초기값, 마지막으로 생성된 값 기준
      - allocationSize(50) : 시퀀스 한 번 호출에 증가하는 수(성능 최적화에 사용)
      - catalog, schema : DB catalog, schema 이름
      - uniqueConstraints(DDL) : 유니크 제약 조건 지정
```java
@Entity
@TableGenerator(name = "MEMBER_SEQ_GENERATOR",
                table = "MY_SEQUENCES",
                pkColumnValue = "MEMBER_SEQ", allocationSize = 1)
public class Member {
    
@Id
@GeneratedValue(strategy = GenerationType.TABLE, generator = "MEMBER_SEQ_GENERATOR") // AUTO(default)
private Long id;
}
```

### 권장하는 식별자 전략
- 기본키 제약 조건 : NOT NULL, UNIQUE, 변하면 안된다.
- 미래까지 이 조건을  만족하는 자연키는 찾기 어려움 -> 대리키(대체키) 사용
- 권장 : Long + 대체키 + 키 생성 전략 사용

### 단방향 연관관계
- 방향(Direction): 단방향, 양방향
- 다중성(Multiplicity): 다대일(N:1), 일대다(1:N), 일대일(1:1), 다대다(N:M)
- 연관관계의 주인(Owner): 객체 양방향 연관관계는 관리 필요

# 20210927_양방향 연관관계와 연관관계의 주인
- @OneToMany(mappedBy = "")
  - 객체와 테이블이 관계를 맺는 차이 이해 중요
    - 객체 연관관계 = 2개
      - 회원 -> 팀 연관관계 (단방향)
      - 팀 -> 회원 연관관계 (단방향)
    - 테이블 연관관계 = 1개
      - 회원 <-> 팀 연관관계 (양방향)

### 양방향 매핑 규칙
- 객체의 두 관계 중 하나를 연관관계의 주인으로 지정
- 연관관계의 주인만이 외래 키를 관리(등록, 수정)
- 주인이 아닌 쪽은 읽기만 가능
- 주인은 mappedBy 속성 사용 X
- 주인이 아니면 mappedBy 속성으로 주인 지정
- 누구를 주인으로?
  - 외래키가 있는 곳(N)
- 주의!
  - 연관관계의 주인에 값을 입력해야 함
    - 양방향 매핑 시 순수 객체 관계를 고려하면 양쪽 다 값을 입력해야 함
    - 연관관계 편의 메소드 생성
    ````java
    public void changeTeam(Team team) {
        this.team = team;
        team.getMembers().add(this);
    }
    ````
    - 양방향 매핑 시 무한 루프 조심
      - toStringg(), lombok, JSON 생성 라이브러리
- 정리
  - 단방향 매핑만으로도 이미 연관관계 매핑은 완료
  - 양방향 매핑은 반대 방향으로 조회(객체 그래프 탐색) 기능이 추가된 것 뿐
  - JPQL에서 역방향으로 탐색할 일이 많음
  - 단방향 매핑을 잘 하고 양방향은 필요할 때 추가해도 됨(테이블에 영향 X)

# 20210928_연관관계 매핑
- 연관관계 매핑시 고려사항
  1. 다중성
     - 다대일 : @ManyToOne
     - 일대다 : @OneToMany
     - 일대일 : @OneToOne
     - 다대다 : @ManyToMany => 실무에서 쓰면 안됨
  2. 단방향, 양방향
     - 테이블
       - 외래키 하나로 양쪽 조인 가능
       - 방향이라는 개념 존재 X
     - 객체
       - 참조용 필드가 있는 쪽으로만 참조 가능
       - 한쪽만 참조하면 단방향
       - 양쪽이 서로 참조하면 양방향
  3. 연관관계의 주인 : 외래키를 관리하는 참조
     - 테이블은 외래키 하나로 두 테이블이 연관관계를 맺음
     - 객체 양방향 관계는 A->B, B->A처럼 참조가 2군데
     - 객체 양방향 관계는 참조가 2군데 있음. 둘 중 테이블의 외래키를 관리할 곳을 지정해야 함
     - 주인의 반대편 : 외래키에 영향X, 단순 조회

### 다대일 [N:1]
- 다대일 단방향
  - 가장 많이 사용하는 연관관계
- 다대일 양방향
  - 외래키가 있는 쪽이 연관관계의 주인
  - 양쪽을 서로 참조하도록 개발

### 일대다 [1:N] -> 권장하지 않음
- 일대다 단방향
  - 일대다 단방향은 일대다(1:N)에서 1이 연관관계의 주인
  - 테이블 일대다 관계는 항상 N 쪽에 외래키가 있음
  - 객체와 테이블 차이 때문에 반대편 테이블의 외래키를 관리하는 특이한 구조
  - @JoinColumn을 꼭 사용해야 함. 그렇지 않으면 조인 테이블 방식을 사용(중간에 테이블 하나 추가)
  - 단점
    - 엔티티가 관리하는 외래키가 다른 테이블에 있음
    - 연관관계 관리를 위해 추가로 UPDATE 쿼리 실행
  - 일대다 단방향 매핑 보다는 다대일 양방향 매핑 사용할 것
main.java
````java
public class JpaMain {
    public static void main(String[] args) {
      ...
      Member member = new Member();
      member.setUsername("member1");
      em.persist(member);

      Team team = new Team();
      team.setName("TeamA");
      em.persist(team);

      team.getMembers().add(member);

      tx.commit();
      ...
    }
}
````
Member.java
````java
@Entity
public class Member {

  @Id
  @GeneratedValue
  @Column(name = "MEMBER_ID")
  private Long id;

  @Column(name = "USERNAME")
  private String username;
  ...
}
````
Team.java
````java
@Entity
public class Team {

  @Id
  @GeneratedValue
  @Column(name = "TEAM_ID")
  private Long id;
  private String name;

  @OneToMany
  @JoinColumn(name = "TEAM_ID")
  private List<Member> members = new ArrayList<>();
  ...
}
````
- 일대다 양방향
  - 공식적으로 존재 X
  - @JoinColumn(insertable=false, updatable=false)
  - 읽기 전용 필드 사용하여 양방향처럼 사용하는 것
  - 다대일 양방향을 사용하자..

### 일대일(1:1)
- 반대도 일대일
- 주 테이블이나 대상 테이블 중 외래키 선택 가능
- 외래키에 DB 유니크(UNI) 제약조건 추가
- 주 테이블에 외래키 양방향
  - 다대일 양방향 매핑처럼 외래키가 있는 곳이 연관관계의 주인
  - 반대편은 mappedBy 적용
- 대상 테이블에 외래키 단방향 : JPA 지원 X
- 주 테이블에 외래키
  - 주 객체가 대상 객체의 참조를 가지는 것 처럼 주 테이블에 외래키를 두고 대상 테이블을 찾음
  - 객체지향적
  - JPA 매핑 편리
  - 장점 : 주 테이블만 조회해도 대상 테이블에 데이터가 있는지 확인 가능
  - 단점 : 값이 없으면 외래키에 NULL 적용
- 대상 테이블에 외래키
  - 대상 테이블에 외래키 존재
  - 전통적인 DB개발자 선호
  - 주 테이블과 대상 테이블을 일대일에서 일대다 관계로 변경할 때 테이블 구조 유지
  - 단점 : 프록시 기능의 한계로 지연 로딩으로 설정해도 항상 즉시 로딩됨

### 다대다(N:M) : 실무 사용 X =>연결 테이블용 엔티티 추가(연결 테이블 엔티티로 승격)
- 관계형 DB는 정규화된 테이블 2개로 다대다 관계 표현X
- 연결 테이블을 추가하여 일대다, 다대일 관계로 풀어야 함
  - 단, 연결 테이블이 단순히 연결만 하고 끝나는 것이 아닌,
  - 주문 시간, 수량 같은 데이터가 들어올 수 있음
- 객체는 컬렉션을 사용하여 객체 2개로 다대다 관계 가능
- @ManyToMany
- @JoinTable 