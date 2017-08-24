package com.library.essay.persistence.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.library.essay.persistence.entities.Essay;

public interface EssayRepository extends JpaRepository<Essay, Long>{

}
