package hellojpa;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import java.time.LocalDateTime;
import java.util.List;

public class JpaMain {

    public static void main(String[] args) {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("ex1");
        EntityManager em = emf.createEntityManager();

        EntityTransaction tx = em.getTransaction();
        tx.begin();

        try {
            //code

            Team team = new Team();
            team.setName("TEAM");
            em.persist(team);

            Member member = new Member();
            member.setUsername("USER");
            member.setTeam(team);

            em.persist(member);

            em.flush();
            em.clear();
            
            Member m = em.find(Member.class, member.getId()); // 해당 값이 실제 사용되는 시점에 쿼리 날림

            System.out.println("m = " + m.getTeam().getClass()); //Proxy
            //System.out.println("================");
            m.getTeam().getName(); // 실제 team을 사용하는 시점에 초기화(DB 조회)
            //System.out.println("================");
            tx.commit();
            //code
        } catch (Exception e) {
            tx.rollback();
        } finally {
            em.close();
        }
        emf.close();
    }
}
