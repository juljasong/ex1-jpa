package hellojpa;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
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

            Address address = new Address("city", "street", "10000");

            Member member = new Member();
            member.setUsername("ABC");
            member.setHomeAddress(address);

            member.getFavoriteFoods().add("치킨");
            member.getFavoriteFoods().add("족발");
            member.getFavoriteFoods().add("떡볶이");

            member.getAddressHistory().add(new AddressEntity("oldCity1", "oldStreet1", "10001"));
            member.getAddressHistory().add(new AddressEntity("oldCity2", "oldStreet2", "10002"));
            em.persist(member);

            em.flush();
            em.clear();

            System.out.println("==========START===========");
            Member findMember = em.find(Member.class, member.getId());

            /* 조회
            List<Address> findAddressHistory = findMember.getAddressHistory();
            for (Address addressLoof : findAddressHistory) {
                System.out.println(addressLoof.getCity());
            }

            Set<String> findFavoriteFoods = findMember.getFavoriteFoods();
            for (String favoriteFood : findFavoriteFoods) {
                System.out.println(favoriteFood);
            }
            */

            // 수정할 땐 통째로 교체해야 함
            findMember.setHomeAddress(new Address("newCity",
                    findMember.getHomeAddress().getStreet(), findMember.getHomeAddress().getZipcode()));

            // 치킨 -> 한식
            findMember.getFavoriteFoods().remove("치킨");
            findMember.getFavoriteFoods().add("한식");

            System.out.println("==========UPDATE===========");

            // 주소: oldCity1 -> newCity1
            findMember.getAddressHistory().remove(new AddressEntity("oldCity1", "oldStreet1", "10001"));
            findMember.getAddressHistory().add(new AddressEntity("oldCity3", "oldStreet1", "10001"));
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
