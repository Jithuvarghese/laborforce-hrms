package com.example.demo.workforce;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkerRepository extends JpaRepository<Worker, Long> {

    Optional<Worker> findByPhoneAndActiveTrue(String phone);

    List<Worker> findByActiveTrue();

    Optional<Worker> findByIdAndActiveTrue(Long id);
}
