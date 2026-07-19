package com.samiksha.moneymanager.controller;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.samiksha.moneymanager.entity.ProfileEntity;
import com.samiksha.moneymanager.service.EmailService;
import com.samiksha.moneymanager.service.ExcelService;
import com.samiksha.moneymanager.service.ExpenseService;
import com.samiksha.moneymanager.service.IncomeService;
import com.samiksha.moneymanager.service.ProfileService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/email")
@RequiredArgsConstructor
public class EmailController {
	
	private final ExcelService excelService;
	private final IncomeService incomeService;
	private final ExpenseService expenseService;
	private final EmailService emailService;
	private final ProfileService profileService;
	
	@GetMapping("/income-excel")
	public ResponseEntity<Void> emailIncomeExcel() throws IOException {
		ProfileEntity profile = profileService.getCurrentProfile();
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		excelService.writeIncomesToExcel(baos, incomeService.getCurrentMonthIncomesForCurrentUser());
		emailService.sendEmailWithAttachment(profile.getEmail(),
				"Your Income Excel Report", 
				"<p>Please find attached your income report.</p>", 
				baos.toByteArray(), 
				"income.xlsx");
		return ResponseEntity.ok(null);
	}
	
	@GetMapping("/expense-excel")
	public ResponseEntity<Void> emailExpenseExcel() throws IOException {
		ProfileEntity profile = profileService.getCurrentProfile();
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		excelService.writeExpensesToExcel(baos, expenseService.getCurrentMonthExpensesForCurrentUser());
		emailService.sendEmailWithAttachment(profile.getEmail(),
				"Your Expense Excel Report", 
				"<p>Please find attached your expense report.</p>", 
				baos.toByteArray(), 
				"expense.xlsx");
		return ResponseEntity.ok(null);
	}
}
