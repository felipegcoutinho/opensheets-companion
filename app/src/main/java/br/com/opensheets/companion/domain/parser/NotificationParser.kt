package br.com.opensheets.companion.domain.parser

import javax.inject.Inject
import javax.inject.Singleton

data class ParsedNotification(
    val merchantName: String? = null,
    val amount: Double? = null
)

/**
 * Parser for financial notifications.
 * Uses generic patterns to extract transaction data from notification text.
 */
@Singleton
class NotificationParser @Inject constructor() {

    /**
     * Parse notification to extract amount and merchant name.
     */
    fun parse(packageName: String, title: String?, text: String): ParsedNotification {
        val fullText = listOfNotNull(title, text).joinToString(" ")

        return ParsedNotification(
            amount = extractAmount(fullText),
            merchantName = extractMerchant(fullText)
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
        // Delimitadores que indicam fim do nome do estabelecimento
        val endMarkers = listOf(
            " para o cartao", " para o cartão", " para cartao", " para cartão",
            " no cartao", " no cartão", " com cartao", " com cartão",
            " cartao final", " cartão final", " com final", " final ",
            " no dia ", " às ", " as ", " dia ",
            " - r$", " - R$", ". ", ", ", " boas compras",
            " e recebeu", " e ganhou", " cashback", "\n"
        )

        // Padrões para encontrar início do estabelecimento (em ordem de prioridade)
        val startPatterns = listOf(
            // Padrão 1: "em/no/na [ESTABELECIMENTO]" - mais comum
            Regex("""(?:em|no|na)\s+([A-ZÀ-Ú][A-Za-zÀ-ú0-9\s&.*/_-]+)""", RegexOption.IGNORE_CASE),
            // Padrão 2: "pagou/comprou R$ X a [ESTABELECIMENTO]" - Mercado Pago e similares
            Regex("""(?:pagou|comprou|gastou)\s+R\$\s*[\d.,]+\s+a\s+([A-Za-zÀ-ú0-9\s&.*/_-]+)""", RegexOption.IGNORE_CASE),
            // Padrão 3: "comprar R$ X a [ESTABELECIMENTO]"
            Regex("""comprar\s+R\$\s*[\d.,]+\s+(?:em|a)\s+([A-Za-zÀ-ú0-9\s&.*/_-]+)""", RegexOption.IGNORE_CASE)
        )

        for (pattern in startPatterns) {
            val match = pattern.find(text) ?: continue
            var merchant = match.groupValues[1]

            // Encontrar o delimitador de fim mais próximo
            val lowerMerchant = merchant.lowercase()
            var endIndex = merchant.length

            for (marker in endMarkers) {
                val markerIndex = lowerMerchant.indexOf(marker.lowercase())
                if (markerIndex != -1 && markerIndex < endIndex) {
                    endIndex = markerIndex
                }
            }

            // Cortar no delimitador encontrado
            merchant = merchant.substring(0, endIndex)

            // Limpar resultado
            val cleaned = merchant
                .trim()
                .trimEnd('.', ',', '-', ' ')

            if (cleaned.length >= 2) {
                return cleaned.take(50)
            }
        }

        return null
    }
}



