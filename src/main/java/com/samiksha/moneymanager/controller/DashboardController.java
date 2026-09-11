package com.samiksha.moneymanager.controller;

import java.time.YearMonth;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.samiksha.moneymanager.service.DashboardService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getDashboardData(
            @RequestParam(required = false) String month) {

        YearMonth selectedMonth =
                (month == null || month.isBlank())
                        ? YearMonth.now()
                        : YearMonth.parse(month);

        Map<String, Object> dashboardData =
                dashboardService.getDashboardData(selectedMonth);

        return ResponseEntity.ok(dashboardData);
    }
}