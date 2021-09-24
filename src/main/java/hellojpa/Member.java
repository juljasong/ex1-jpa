package hellojpa;

import javax.persistence.*;
import java.util.Date;

@Entity
//@Table(name = "MEMBER")
public class Member {

    @Id
    private Long id;

    //@Column(unique = true, length = 10)
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

    private Member() {}

    public Member(Long id, String name) {
        this.id = id;
        this.username = name;
    }
}
