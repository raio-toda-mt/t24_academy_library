package jp.co.metateam.library.repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import jp.co.metateam.library.model.Stock;

@Repository
public interface StockRepository extends JpaRepository<Stock, Long> {

    List<Stock> findAll();

    List<Stock> findByDeletedAtIsNull();

    List<Stock> findByDeletedAtIsNullAndStatus(Integer status);

    Optional<Stock> findById(String id);

    List<Stock> findByBookMstIdAndStatus(Long book_id, Integer status);

    // @Query("SELECT s.id FROM Stock AS s left join RentalManage AS r on s.id =
    // r.stock.id join BookMst AS b on s.bookMst.id = b.id WHERE b.id = ?1 AND
    // s.status = 0 AND (r.status is null or r.expectedRentalOn > ?2 OR ?2 >
    // r.expectedReturnOn)")
    @Query(value = "SELECT s.id, COUNT(s.id) " +
            "FROM stocks s " +
            "LEFT JOIN rental_manage rm ON s.id = rm.stock_id " +
            "JOIN book_mst bm ON s.book_id = bm.id " +
            "WHERE bm.id = ?1 AND s.status = '0' AND (rm.status IS NULL OR rm.expected_rental_on > ?2 OR ?2 > rm.expected_return_on) "
            +
            "GROUP BY s.id", nativeQuery = true)
    List<Object[]> calendar(Long bookId, Date dayOfMonth);
}
