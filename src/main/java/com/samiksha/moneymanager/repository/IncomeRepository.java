package com.samiksha.moneymanager.repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.samiksha.moneymanager.entity.ExpenseEntity;
import com.samiksha.moneymanager.entity.IncomeEntity;

public interface IncomeRepository extends JpaRepository<IncomeEntity, Long>{
	
	//select * from tbl_incomes where profile_id = ?1 order by date desc
		List<IncomeEntity> findByProfileIdOrderByDateDesc(Long profileId);
		
		//select * from tbl_incomes where profile_id = ?1 order by date desc limit 5	
		List<IncomeEntity> findTop5ByProfileIdOrderByDateDesc(Long profileId);
		
		@Query("SELECT SUM(i.amount) FROM IncomeEntity i WHERE i.profile.id = :profileId")
		BigDecimal findTotalIncomeByProfile(@Param("profileId") Long profileId);
		
		//SELECT * FROM tbl_incomes WHERE profile_id = ?1 AND date BETWEEN ?2 AND ?3 AND name LIKE %?4%
		List<IncomeEntity> findByProfileIdAndDateBetweenAndNameContainingIgnoreCase(
				Long profileId,
				LocalDate startDate,
				LocalDate endDate,
				String keyword,
				Sort sort
		);

		//select * from tbl_incomes where profile_id = ?1 and date between ?2 and ?3
		List<IncomeEntity> findByProfileIdAndDateBetween(Long profileId, LocalDate startDate, LocalDate endDate);

		
}
