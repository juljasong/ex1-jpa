package hellojpa;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
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
            member.setUsername("ABC");
            em.persist(member);
            //Member findMember = em.find(Member.class, 2L);

            //findMember.setName("user2");

            //(DB에 저장되기 전) 1차 캐시에서 조회
            //Member findMember = em.find(Member.class, 2L);
            //System.out.println(findMember.getName());

            //준영속
            //em.detach(member);

            //삭제
            //em.remove(member);

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
