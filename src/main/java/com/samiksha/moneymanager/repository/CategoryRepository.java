package com.samiksha.moneymanager.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.samiksha.moneymanager.entity.CategoryEntity;

public interface CategoryRepository extends JpaRepository<CategoryEntity, Long>{

	//select * from tbl_categories where profile_id = ?1
	List<CategoryEntity> findByProfileId(Long profileId);
	
	//select * from tbl_categories where id = ?1 and profile_id = ?2
	Optional<CategoryEntity> findByIdAndProfileId(Long id, Long profileId);
	
	//select * from tbl_categories where type = ?1 and profile_id = ?2
	List<CategoryEntity> findByTypeAndProfileId(String type, Long profileId);
	
	Boolean existsByNameAndProfileId(String name, Long profileId);
}
