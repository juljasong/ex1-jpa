package hellojpa;

import org.hibernate.Criteria;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

public class JpaMain {

    public static void main(String[] args) {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("ex1");
        EntityManager em = emf.createEntityManager();

        EntityTransaction tx = em.getTransaction();
        tx.begin();

        try {
            //code

            Member member = new Member();
            member.setUsername("Song");
            em.persist(member);

            //flush -> commit, query

            List<Member> resultList = em.createNativeQuery("select MEMBER_ID, city, street, zipcode from MEMBER", Member.class).getResultList();

            for (Member m : resultList) {
                System.out.println(m.getUsername());
            }

            //code
        } catch (Exception e) {
            tx.rollback();
        } finally {
            em.close();
        }
        emf.close();
    }
}
