package hellojpa;

import javax.persistence.*;
import java.util.Date;

@Entity
@TableGenerator(name = "MEMBER_SEQ_GENERATOR",
                table = "MY_SEQUENCES",
                pkColumnValue = "MEMBER_SEQ", allocationSize = 1)
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "MEMBER_SEQ_GENERATOR") // AUTO(default)
    private Long id;

    //@Column(unique = true, length = 10)
    @Column(name = "name")
    private String username;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    //private Member() {}

}
