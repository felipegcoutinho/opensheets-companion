package br.com.opensheets.companion.data.local.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity to store global keywords settings for notification capture and classification.
 * Uses a singleton pattern (id = 1) since there's only one global config.
 * Keywords are stored as comma-separated strings for simplicity.
 */
@Entity(tableName = "keywords_settings")
data class KeywordsSettingsEntity(
    @PrimaryKey
    val id: Int = 1,

    @ColumnInfo(name = "trigger_keywords")
    val triggerKeywords: String = DEFAULT_TRIGGER_KEYWORDS,

    @ColumnInfo(name = "expense_keywords")
    val expenseKeywords: String = DEFAULT_EXPENSE_KEYWORDS,

    @ColumnInfo(name = "income_keywords")
    val incomeKeywords: String = DEFAULT_INCOME_KEYWORDS
) {
    companion object {
        const val DEFAULT_TRIGGER_KEYWORDS = "compra,R$,pix,transferência,débito,crédito,saque,pagamento,boleto,fatura"
        const val DEFAULT_EXPENSE_KEYWORDS = "compra,débito,pagamento,saque,transferência enviada,pix enviado,boleto,fatura,cobrança"
        const val DEFAULT_INCOME_KEYWORDS = "recebido,recebeu,depósito,transferência recebida,pix recebido,crédito,estorno,cashback"

        fun fromKeywordLists(
            triggerList: List<String>,
            expenseList: List<String>,
            incomeList: List<String>
        ): KeywordsSettingsEntity {
            return KeywordsSettingsEntity(
                triggerKeywords = triggerList.joinToString(","),
                expenseKeywords = expenseList.joinToString(","),
                incomeKeywords = incomeList.joinToString(",")
            )
        }
    }

    fun getTriggerKeywordsList(): List<String> = triggerKeywords.split(",").map { it.trim() }.filter { it.isNotEmpty() }

    fun getExpenseKeywordsList(): List<String> = expenseKeywords.split(",").map { it.trim() }.filter { it.isNotEmpty() }
    
    fun getIncomeKeywordsList(): List<String> = incomeKeywords.split(",").map { it.trim() }.filter { it.isNotEmpty() }
}

