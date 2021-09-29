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

            Member member = new Member();
            member.setUsername("USER");

            em.persist(member);

            em.flush();
            em.clear();

            //Member findMember = em.find(Member.class, member.getId());
            //System.out.println("===" + findMember.getUsername());
            
            Member findMember = em.getReference(Member.class, member.getId()); // 해당 값이 실제 사용되는 시점에 쿼리 날림
            System.out.println("===" + findMember.getUsername());

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
