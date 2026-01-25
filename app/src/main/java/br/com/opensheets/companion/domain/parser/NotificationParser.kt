package br.com.opensheets.companion.domain.parser

import br.com.opensheets.companion.data.local.dao.KeywordsSettingsDao
import br.com.opensheets.companion.data.local.entities.KeywordsSettingsEntity
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

data class ParsedNotification(
    val merchantName: String? = null,
    val amount: Double? = null,
    val date: Date? = null,
    val transactionType: String? = null // "Despesa" or "Receita"
)

/**
 * Parser for financial notifications.
 * Uses generic patterns to extract transaction data from notification text.
 * Classification keywords are loaded from database settings.
 */
@Singleton
class NotificationParser @Inject constructor(
    private val keywordsSettingsDao: KeywordsSettingsDao
) {

    /**
     * Parse notification and classify transaction type.
     * This is a suspend function to load keywords from database.
     */
    suspend fun parse(packageName: String, title: String?, text: String): ParsedNotification {
        val fullText = listOfNotNull(title, text).joinToString(" ")
        
        // Load keywords fresh from database
        val settings = keywordsSettingsDao.get() ?: KeywordsSettingsEntity()
        val expenseKeywords = settings.getExpenseKeywordsList()
        val incomeKeywords = settings.getIncomeKeywordsList()

        return ParsedNotification(
            amount = extractAmount(fullText),
            merchantName = extractMerchant(fullText),
            transactionType = inferTransactionType(fullText, expenseKeywords, incomeKeywords)
        )
    }

    private fun extractAmount(text: String): Double? {
        val patterns = listOf(
            Regex("""R\$\s*([\d.]+,\d{2})"""),
            Regex("""R\$\s*([\d]+,\d{2})"""),
            Regex("""RS\s*([\d.]+,\d{2})""", RegexOption.IGNORE_CASE),
            Regex("""([\d.]+,\d{2})\s*reais""", RegexOption.IGNORE_CASE)
        )

        for (pattern in patterns) {
            val match = pattern.find(text)
            if (match != null) {
                val value = match.groupValues[1]
                    .replace(".", "")
                    .replace(",", ".")
                return value.toDoubleOrNull()
            }
        }
        return null
    }

    private fun extractMerchant(text: String): String? {
        val patterns = listOf(
            Regex("""(?:em|no|na)\s+([A-Z][A-Z0-9\s*]+)""", RegexOption.IGNORE_CASE),
            Regex("""-\s*([A-Z][A-Z0-9\s]+)\s*-"""),
            Regex("""(?:compra|pagamento).*?(?:em|no|na)\s+(.+?)(?:\.|,|$)""", RegexOption.IGNORE_CASE)
        )

        for (pattern in patterns) {
            val match = pattern.find(text)
            if (match != null) {
                return match.groupValues[1].trim().take(50)
            }
        }
        return null
    }

    private fun inferTransactionType(
        text: String,
        expenseKeywords: List<String>,
        incomeKeywords: List<String>
    ): String {
        val lowerText = text.lowercase()

        // Check income keywords first (less common)
        if (incomeKeywords.isNotEmpty() && incomeKeywords.any { it.isNotEmpty() && lowerText.contains(it) }) {
            return "Receita"
        }

        // Check expense keywords
        if (expenseKeywords.isNotEmpty() && expenseKeywords.any { it.isNotEmpty() && lowerText.contains(it) }) {
            return "Despesa"
        }

        // Default to expense (most notifications are expenses)
        return "Despesa"
    }
}



