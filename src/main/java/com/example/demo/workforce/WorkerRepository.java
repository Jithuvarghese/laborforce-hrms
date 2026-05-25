package com.example.demo.workforce;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkerRepository extends JpaRepository<Worker, Long> {

    Optional<Worker> findByPhoneAndActiveTrue(String phone);

    List<Worker> findByActiveTrue();

    Page<Worker> findByActiveTrue(Pageable pageable);

    Optional<Worker> findByIdAndActiveTrue(Long id);
}
