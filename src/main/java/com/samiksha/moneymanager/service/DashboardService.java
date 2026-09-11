package com.samiksha.moneymanager.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.stereotype.Service;

import com.samiksha.moneymanager.dto.AIInsightDTO;
import com.samiksha.moneymanager.dto.ExpenseDTO;
import com.samiksha.moneymanager.dto.IncomeDTO;
import com.samiksha.moneymanager.dto.RecentTransactionDTO;
import com.samiksha.moneymanager.entity.ProfileEntity;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final IncomeService incomeService;
    private final ExpenseService expenseService;
    private final ProfileService profileService;
    private final AIInsightService aiInsightService;

    public Map<String, Object> getDashboardData() {

        ProfileEntity profile =
                profileService.getCurrentProfile();

        Map<String, Object> returnValue =
                new LinkedHashMap<>();

        // ------------------------------------------------
        // Existing dashboard data
        // ------------------------------------------------

        List<IncomeDTO> latestIncomes =
                incomeService.getLatest5IncomesForCurrentUser();

        List<ExpenseDTO> latestExpenses =
                expenseService.getLatest5ExpensesForCurrentUser();

        List<RecentTransactionDTO> recentTransactions =
                Stream.concat(

                        latestIncomes.stream()
                                .map(income ->
                                        RecentTransactionDTO.builder()
                                                .id(income.getId())
                                                .profileId(profile.getId())
                                                .icon(income.getIcon())
                                                .name(income.getName())
                                                .amount(income.getAmount())
                                                .date(income.getDate())
                                                .createdAt(income.getCreatedAt())
                                                .updatedAt(income.getUpdatedAt())
                                                .type("income")
                                                .build()
                                ),

                        latestExpenses.stream()
                                .map(expense ->
                                        RecentTransactionDTO.builder()
                                                .id(expense.getId())
                                                .profileId(profile.getId())
                                                .icon(expense.getIcon())
                                                .name(expense.getName())
                                                .amount(expense.getAmount())
                                                .date(expense.getDate())
                                                .createdAt(expense.getCreatedAt())
                                                .updatedAt(expense.getUpdatedAt())
                                                .type("expense")
                                                .build()
                                )
                )
                .sorted((a, b) -> {

                    int cmp =
                            b.getDate().compareTo(a.getDate());

                    if (cmp == 0
                            && a.getCreatedAt() != null
                            && b.getCreatedAt() != null) {

                        return b.getCreatedAt()
                                .compareTo(a.getCreatedAt());
                    }

                    return cmp;
                })
                .collect(Collectors.toList());

        // ------------------------------------------------
        // Existing totals
        // ------------------------------------------------

        BigDecimal totalIncome =
                incomeService.getTotalIncomeForCurrentUser();

        BigDecimal totalExpense =
                expenseService.getTotalExpenseForCurrentUser();

        BigDecimal totalBalance =
                totalIncome.subtract(totalExpense);

        returnValue.put(
                "totalBalance",
                totalBalance
        );

        returnValue.put(
                "totalIncome",
                totalIncome
        );

        returnValue.put(
                "totalExpense",
                totalExpense
        );

        returnValue.put(
                "recent5Expenses",
                latestExpenses
        );

        returnValue.put(
                "recent5Incomes",
                latestIncomes
        );

        returnValue.put(
                "recentTransactions",
                recentTransactions
        );

        // ------------------------------------------------
        // AI Financial Advisor
        // ------------------------------------------------

        AIInsightDTO aiInsight =
                generateAIInsight(
                        totalIncome,
                        totalExpense,
                        totalBalance
                );

        returnValue.put(
                "aiInsight",
                aiInsight
        );

        return returnValue;
    }

    
    private AIInsightDTO generateAIInsight(
            BigDecimal totalIncome,
            BigDecimal totalExpense,
            BigDecimal totalBalance) {

        // ------------------------------------------------
        // Determine current month using actual calendar date
        // ------------------------------------------------

        YearMonth currentMonth = YearMonth.now();

        YearMonth previousMonth =
                currentMonth.minusMonths(1);


        // ------------------------------------------------
        // Current month dates
        // ------------------------------------------------

        LocalDate currentStart =
                currentMonth.atDay(1);

        LocalDate currentEnd =
                currentMonth.atEndOfMonth();


        // ------------------------------------------------
        // Previous month dates
        // ------------------------------------------------

        LocalDate previousStart =
                previousMonth.atDay(1);

        LocalDate previousEnd =
                previousMonth.atEndOfMonth();


        // ------------------------------------------------
        // Get actual expenses from database
        // ------------------------------------------------

        List<ExpenseDTO> currentExpenses =
                expenseService.getExpensesBetweenForCurrentUser(
                        currentStart,
                        currentEnd
                );

        List<ExpenseDTO> previousExpenses =
                expenseService.getExpensesBetweenForCurrentUser(
                        previousStart,
                        previousEnd
                );


        // ------------------------------------------------
        // Current month category totals
        // ------------------------------------------------

        Map<String, BigDecimal> currentCategories =
                calculateCategoryTotals(
                        currentExpenses
                );


        // ------------------------------------------------
        // Previous month category totals
        // ------------------------------------------------

        Map<String, BigDecimal> previousCategories =
                calculateCategoryTotals(
                        previousExpenses
                );


        // ------------------------------------------------
        // Current month total
        // ------------------------------------------------

        BigDecimal currentTotal =
                currentExpenses.stream()
                        .map(ExpenseDTO::getAmount)
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        );


        // ------------------------------------------------
        // Previous month total
        // ------------------------------------------------

        BigDecimal previousTotal =
                previousExpenses.stream()
                        .map(ExpenseDTO::getAmount)
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        );


        // ------------------------------------------------
        // Highest spending category
        // ------------------------------------------------

        String highestCategory = "N/A";

        BigDecimal highestAmount =
                BigDecimal.ZERO;

        if (!currentCategories.isEmpty()) {

            Map.Entry<String, BigDecimal> highest =
                    currentCategories.entrySet()
                            .stream()
                            .max(
                                    Map.Entry.comparingByValue()
                            )
                            .orElse(null);

            if (highest != null) {

                highestCategory =
                        highest.getKey();

                highestAmount =
                        highest.getValue();
            }
        }


        // ------------------------------------------------
        // Previous amount for highest category
        // ------------------------------------------------

        BigDecimal previousHighestAmount =
                previousCategories.getOrDefault(
                        highestCategory,
                        BigDecimal.ZERO
                );


        // ------------------------------------------------
        // Calculate percentage change
        // ------------------------------------------------

        String percentageChange = "Not available";

        if (!currentCategories.isEmpty()
                && previousHighestAmount.compareTo(BigDecimal.ZERO) > 0) {

            BigDecimal change =
                    highestAmount
                            .subtract(previousHighestAmount)
                            .divide(
                                    previousHighestAmount,
                                    4,
                                    RoundingMode.HALF_UP
                            )
                            .multiply(BigDecimal.valueOf(100));

            percentageChange =
                    change.setScale(
                            1,
                            RoundingMode.HALF_UP
                    ) + "%";
        }


        // ------------------------------------------------
        // Build financial data for AI
        // ------------------------------------------------

        String financialData =
                buildFinancialData(
                        totalIncome,
                        totalExpense,
                        totalBalance,
                        currentTotal,
                        previousTotal,
                        currentCategories,
                        previousCategories,
                        highestCategory,
                        highestAmount,
                        previousHighestAmount,
                        percentageChange
                );


        // ------------------------------------------------
        // Generate AI financial insight
        // ------------------------------------------------

        String aiResponse =
                aiInsightService.generateFinancialInsight(
                        financialData
                );


        // ------------------------------------------------
        // Separate AI response
        // ------------------------------------------------

        String summary =
                extractSection(
                        aiResponse,
                        "SUMMARY:"
                );

        String suggestion =
                extractSection(
                        aiResponse,
                        "SUGGESTION:"
                );


        return AIInsightDTO.builder()
                .summary(summary)
                .suggestion(suggestion)
                .build();
    }
  

    // ------------------------------------------------
    // Calculate category totals
    // ------------------------------------------------

    private Map<String, BigDecimal> calculateCategoryTotals(
            List<ExpenseDTO> expenses) {

        return expenses.stream()
                .collect(
                        Collectors.groupingBy(

                                expense ->
                                        expense.getCategoryName() != null
                                                ? expense.getCategoryName()
                                                : "Uncategorized",

                                LinkedHashMap::new,

                                Collectors.reducing(
                                        BigDecimal.ZERO,
                                        ExpenseDTO::getAmount,
                                        BigDecimal::add
                                )
                        )
                );
    }

    // ------------------------------------------------
    // Build financial data for AI
    // ------------------------------------------------


    private String buildFinancialData(
            BigDecimal totalIncome,
            BigDecimal totalExpense,
            BigDecimal totalBalance,
            BigDecimal currentTotal,
            BigDecimal previousTotal,
            Map<String, BigDecimal> currentCategories,
            Map<String, BigDecimal> previousCategories,
            String highestCategory,
            BigDecimal highestAmount,
            BigDecimal previousHighestAmount,
            String percentageChange) {

        YearMonth currentMonth = YearMonth.now();
        YearMonth previousMonth = currentMonth.minusMonths(1);

        StringBuilder data = new StringBuilder();

        // Overall financial information
        data.append("OVERALL FINANCIAL DATA:\n");

        data.append("TOTAL INCOME (ALL RECORDED TRANSACTIONS):\n");
        data.append("₹").append(totalIncome).append("\n\n");

        data.append("TOTAL EXPENSE (ALL RECORDED TRANSACTIONS):\n");
        data.append("₹").append(totalExpense).append("\n\n");

        data.append("CURRENT BALANCE:\n");
        data.append("₹").append(totalBalance).append("\n\n");


        // Current month
        data.append("CURRENT MONTH:\n");
        data.append(currentMonth.getMonth() + " " + currentMonth.getYear())
                .append("\n\n");

        data.append("CURRENT MONTH EXPENSE:\n");
        data.append("₹").append(currentTotal).append("\n\n");

        data.append("CURRENT MONTH CATEGORY BREAKDOWN:\n");

        if (currentCategories.isEmpty()) {

            data.append("No expenses recorded.\n");

        } else {

            currentCategories.forEach(
                    (category, amount) ->
                            data.append(category)
                                    .append(": ₹")
                                    .append(amount)
                                    .append("\n")
            );
        }


        // Previous month
        data.append("\nPREVIOUS MONTH:\n");
        data.append(previousMonth.getMonth() + " " + previousMonth.getYear())
                .append("\n\n");

        data.append("PREVIOUS MONTH EXPENSE:\n");
        data.append("₹").append(previousTotal).append("\n\n");

        data.append("PREVIOUS MONTH CATEGORY BREAKDOWN:\n");

        if (previousCategories.isEmpty()) {

            data.append("No expenses recorded.\n");

        } else {

            previousCategories.forEach(
                    (category, amount) ->
                            data.append(category)
                                    .append(": ₹")
                                    .append(amount)
                                    .append("\n")
            );
        }


        // Highest spending category
        data.append("\nHIGHEST SPENDING CATEGORY IN CURRENT MONTH:\n");

        if (currentCategories.isEmpty()) {

            data.append("None\n");

        } else {

            data.append(highestCategory)
                    .append(": ₹")
                    .append(highestAmount)
                    .append("\n");
        }


        // Previous month comparison
        data.append("PREVIOUS MONTH AMOUNT FOR THIS CATEGORY:\n");
        data.append("₹").append(previousHighestAmount).append("\n");

        data.append("PERCENTAGE CHANGE:\n");
        data.append(percentageChange).append("\n");

        return data.toString();
    }


    // ------------------------------------------------
    // Extract AI sections
    // ------------------------------------------------

    private String extractSection(
            String response,
            String section) {

        if (response == null
                || response.isBlank()) {

            return "";
        }

        int start =
                response.indexOf(section);

        if (start == -1) {

            return response.trim();
        }

        start += section.length();

        String result =
                response.substring(start);

        int nextSection =
                result.indexOf("SUGGESTION:");

        if (section.equals("SUMMARY:")
                && nextSection != -1) {

            result =
                    result.substring(
                            0,
                            nextSection
                    );
        }

        return result.trim();
    }
}