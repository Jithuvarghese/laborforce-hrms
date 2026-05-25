package com.example.demo.workforce;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "attendance_logs",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_attendance_worker_clockin", columnNames = {"worker_id", "clock_in"})
        },
        indexes = {
                @Index(name = "idx_attendance_worker", columnList = "worker_id"),
                @Index(name = "idx_attendance_date", columnList = "date")
        })
public class AttendanceLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "worker_id", nullable = false)
    private Worker worker;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "site_id", nullable = false)
    private Site site;

    @NotNull
    @Column(nullable = false)
    private LocalDateTime clockIn;

    private LocalDateTime clockOut;

    @Column(precision = 8, scale = 2)
    private BigDecimal totalHoursWorked;

    @Column(precision = 8, scale = 2)
    private BigDecimal overtimeHours;

    @Builder.Default
    @Column(nullable = false)
    private boolean flagged = false;

    @NotNull
    @Column(nullable = false)
    private LocalDate date;

    @PrePersist
    public void prePersist() {
        if (clockIn != null && date == null) {
            this.date = clockIn.toLocalDate();
        }
    }
}
