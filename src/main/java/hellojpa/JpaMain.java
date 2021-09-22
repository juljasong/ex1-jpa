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

            //em.persist(member);

            //Member findMember = em.find(Member.class, 1L);// Entity.Class, PK
            //em.remove(findMember);
            //findMember.setName("User1");


            List<Member> result = em.createQuery("select m from Member as m", Member.class).getResultList();
            for(Member m : result) {
                System.out.println(m.getId() + " | " + m.getName());
            }

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
