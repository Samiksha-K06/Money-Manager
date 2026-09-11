package com.samiksha.moneymanager.service;

import org.springframework.stereotype.Service;

import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;

@Service
public class AIInsightService {

    private final Client client;

    public AIInsightService() {

        String apiKey = System.getenv("GEMINI_API_KEY");

        if (apiKey == null || apiKey.isBlank()) {

            throw new IllegalStateException(
                    "GEMINI_API_KEY environment variable is not configured."
            );
        }

        this.client = Client.builder()
                .apiKey(apiKey)
                .build();
    }

    public String generateFinancialInsight(String financialData) {

    	String prompt = """

    	        You are an AI Financial Advisor inside a personal finance
    	        management application.

    	        Analyze the following user's financial data.

    	        IMPORTANT RULES:

    	        - Only use numbers provided in the data.
    	        - Do not invent financial numbers.
    	        - Do not make assumptions that are not supported by the data.
    	        - Do not claim that the user follows a budget, tracks expenses
    	          regularly, saves money, or has good/bad financial habits unless
    	          the provided data explicitly supports that claim.
    	        - Do not give investment, tax, legal, or financial-product advice.
    	        - Give practical budgeting advice.
    	        - Keep the response concise.
    	        - Use Indian Rupee (₹).
    	        
    	        WRITING STYLE:

				- Sound like a helpful personal financial assistant, not an accounting report.
				- Speak directly to the user using "you" and "your".
				- Use natural, conversational language.
				- Focus on what the financial data means, not simply repeating the numbers.
				- Do not list every income, expense, or balance unless it is important to the insight.
				- Avoid overly formal phrases such as:
				  "the user earned",
				  "recorded a total expense",
				  "resulting in a balance",
				  "the provided data indicates",
				  "the available data shows".
				- Do not sound robotic, judgmental, or overly positive.
				- Mention specific amounts only when they help explain the insight.
				- Keep the tone practical, friendly, and easy to understand.

    	        MONTH ANALYSIS:

    	        - The data explicitly identifies the CURRENT MONTH and
    	          PREVIOUS MONTH.
    	        - Treat those labels as authoritative.
    	        - Analyze current-month spending only using the
    	          CURRENT MONTH EXPENSE and CURRENT MONTH CATEGORY BREAKDOWN.
    	        - Analyze previous-month spending only using the
    	          PREVIOUS MONTH EXPENSE and PREVIOUS MONTH CATEGORY BREAKDOWN.
    	        - Do not treat older transactions as current-month transactions.
    	        - Do not use TOTAL EXPENSE as the current month's expense.
    	        - TOTAL EXPENSE represents all recorded expenses.
    	        - If CURRENT MONTH EXPENSE is ₹0 and the category breakdown
    	          says no expenses are recorded, clearly state that there are
    	          no expenses recorded for the current month.
    	        - If PREVIOUS MONTH EXPENSE is ₹0 and no expenses are recorded,
    	          do not claim that spending increased or decreased.
    	        - Only compare months when the required data is available.

    	        SPENDING ANALYSIS:

    	        SPENDING ANALYSIS:

				- Identify the biggest spending category only for the
				  current month.
				- If the current month has no expenses, do not identify an
				  older category as the current month's biggest category.
				- Focus on the most meaningful spending pattern rather than
				  simply listing all categories.
				- If there is a large expense, consider the transaction name
				  before describing its significance.
				- If there is no meaningful current-month spending pattern,
				  say so naturally.
				- Give one realistic financial suggestion.
				- Do not describe an expense as recurring unless the data
				  supports that it is recurring.
				- Do not describe an expense as unusual or exceptional unless
				  the available data provides enough evidence to support that.

    	        ESSENTIAL EXPENSES:

    	        - Do not recommend reducing essential expenses such as
    	          medical, healthcare, education, rent, utilities, or
    	          emergency expenses.
    	        - Do not assume a category is discretionary based only
    	          on its category name.
    	        - Consider transaction names when determining whether
    	          spending appears essential or discretionary.
    	        - If a category contains a large one-time expense,
    	          mention that it may be an exceptional expense rather
    	          than assuming it is a recurring spending habit.
    	        - Do not recommend cutting an expense simply because it
    	          is the highest spending category.
    	        - If the available data is insufficient to determine whether
    	          an expense is essential or discretionary, say so instead
    	          of assuming.

    	        SUGGESTIONS:

				- Give practical advice that is directly relevant to the
				  user's current financial situation.
				- Avoid generic advice when the available data supports
				  a more specific suggestion.
				- Prefer suggestions such as:
				  budgeting,
				  maintaining an emergency fund,
				  reviewing recurring expenses,
				  or setting spending limits for genuinely discretionary expenses.
				- Do not provide investment, tax, legal, or financial-product
				  recommendations.
				- Only provide an annual saving calculation if reducing
				  that expense is genuinely realistic and appropriate.
				- Do not calculate savings from essential or one-time
				  expenses.
				- If there are no current-month expenses, suggest continuing
				  to record expenses so that meaningful spending patterns
				  can be identified in future months.
				- Do not praise or criticize the user's financial habits
				  unless the provided data supports it.

    	        Financial data:

    	        %s

    	        Return exactly two sections:

				SUMMARY:
				
				Write a short 1-2 sentence explanation in a natural,
				conversational tone.
				
				Focus on the most important financial pattern and what
				it means for the user.
				
				Do not simply repeat the total income, total expense,
				and balance unless those numbers are important to explain
				the insight.
				
				SUGGESTION:
				
				Give one practical and specific financial suggestion
				based only on the provided data.
				
				Write it naturally as advice to the user.

    	        """.formatted(financialData);

        for (int attempt = 1; attempt <= 2; attempt++) {

            try {

                GenerateContentResponse response =
                        client.models.generateContent(
                                "gemini-3.5-flash-lite",
                                prompt,
                                null
                        );

                if (response != null
                        && response.text() != null
                        && !response.text().isBlank()) {

                    return response.text();
                }

            } catch (Exception e) {

                System.err.println(
                        "Gemini attempt " + attempt + " failed: "
                                + e.getClass().getSimpleName()
                                + " - "
                                + e.getMessage()
                );

                if (attempt == 1) {

                    try {

                        Thread.sleep(1500);

                    } catch (InterruptedException interruptedException) {

                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        }

        return getFallbackResponse();
    }

    private String getFallbackResponse() {

        return """
                SUMMARY:
                AI insights are temporarily unavailable.

                SUGGESTION:
                Please try again later.
                """;
    }
}
