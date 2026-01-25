package br.com.opensheets.companion.ui.screens.settings.keywords

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.opensheets.companion.data.local.dao.KeywordsSettingsDao
import br.com.opensheets.companion.data.local.entities.KeywordsSettingsEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class KeywordsSettingsUiState(
    val triggerKeywords: List<String> = emptyList(),
    val expenseKeywords: List<String> = emptyList(),
    val incomeKeywords: List<String> = emptyList(),
    val isLoading: Boolean = true,
    val showAddTriggerDialog: Boolean = false,
    val showAddExpenseDialog: Boolean = false,
    val showAddIncomeDialog: Boolean = false,
    val newKeyword: String = "",
    val showResetDialog: Boolean = false,
    val saveSuccess: Boolean = false
)

@HiltViewModel
class KeywordsSettingsViewModel @Inject constructor(
    private val keywordsSettingsDao: KeywordsSettingsDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(KeywordsSettingsUiState())
    val uiState: StateFlow<KeywordsSettingsUiState> = _uiState.asStateFlow()

    init {
        loadKeywords()
    }

    private fun loadKeywords() {
        viewModelScope.launch {
            val settings = keywordsSettingsDao.get() ?: KeywordsSettingsEntity()
            _uiState.value = _uiState.value.copy(
                triggerKeywords = settings.getTriggerKeywordsList(),
                expenseKeywords = settings.getExpenseKeywordsList(),
                incomeKeywords = settings.getIncomeKeywordsList(),
                isLoading = false
            )
        }
    }

    // Trigger Keywords
    fun showAddTriggerDialog() {
        _uiState.value = _uiState.value.copy(showAddTriggerDialog = true, newKeyword = "")
    }

    fun hideAddTriggerDialog() {
        _uiState.value = _uiState.value.copy(showAddTriggerDialog = false, newKeyword = "")
    }

    fun addTriggerKeyword() {
        val keyword = _uiState.value.newKeyword.trim().lowercase()
        if (keyword.isNotEmpty() && keyword !in _uiState.value.triggerKeywords) {
            val newList = _uiState.value.triggerKeywords + keyword
            _uiState.value = _uiState.value.copy(
                triggerKeywords = newList,
                showAddTriggerDialog = false,
                newKeyword = ""
            )
            saveKeywords()
        }
    }

    fun removeTriggerKeyword(keyword: String) {
        val newList = _uiState.value.triggerKeywords - keyword
        _uiState.value = _uiState.value.copy(triggerKeywords = newList)
        saveKeywords()
    }

    // Expense Keywords
    fun showAddExpenseDialog() {
        _uiState.value = _uiState.value.copy(showAddExpenseDialog = true, newKeyword = "")
    }

    fun hideAddExpenseDialog() {
        _uiState.value = _uiState.value.copy(showAddExpenseDialog = false, newKeyword = "")
    }

    fun addExpenseKeyword() {
        val keyword = _uiState.value.newKeyword.trim().lowercase()
        if (keyword.isNotEmpty() && keyword !in _uiState.value.expenseKeywords) {
            val newList = _uiState.value.expenseKeywords + keyword
            _uiState.value = _uiState.value.copy(
                expenseKeywords = newList,
                showAddExpenseDialog = false,
                newKeyword = ""
            )
            saveKeywords()
        }
    }

    fun removeExpenseKeyword(keyword: String) {
        val newList = _uiState.value.expenseKeywords - keyword
        _uiState.value = _uiState.value.copy(expenseKeywords = newList)
        saveKeywords()
    }

    // Income Keywords
    fun showAddIncomeDialog() {
        _uiState.value = _uiState.value.copy(showAddIncomeDialog = true, newKeyword = "")
    }

    fun hideAddIncomeDialog() {
        _uiState.value = _uiState.value.copy(showAddIncomeDialog = false, newKeyword = "")
    }

    fun addIncomeKeyword() {
        val keyword = _uiState.value.newKeyword.trim().lowercase()
        if (keyword.isNotEmpty() && keyword !in _uiState.value.incomeKeywords) {
            val newList = _uiState.value.incomeKeywords + keyword
            _uiState.value = _uiState.value.copy(
                incomeKeywords = newList,
                showAddIncomeDialog = false,
                newKeyword = ""
            )
            saveKeywords()
        }
    }

    fun removeIncomeKeyword(keyword: String) {
        val newList = _uiState.value.incomeKeywords - keyword
        _uiState.value = _uiState.value.copy(incomeKeywords = newList)
        saveKeywords()
    }

    fun updateNewKeyword(keyword: String) {
        _uiState.value = _uiState.value.copy(newKeyword = keyword)
    }

    fun showResetDialog() {
        _uiState.value = _uiState.value.copy(showResetDialog = true)
    }

    fun hideResetDialog() {
        _uiState.value = _uiState.value.copy(showResetDialog = false)
    }

    fun resetToDefaults() {
        val defaults = KeywordsSettingsEntity()
        _uiState.value = _uiState.value.copy(
            triggerKeywords = defaults.getTriggerKeywordsList(),
            expenseKeywords = defaults.getExpenseKeywordsList(),
            incomeKeywords = defaults.getIncomeKeywordsList(),
            showResetDialog = false
        )
        saveKeywords()
    }

    private fun saveKeywords() {
        viewModelScope.launch {
            val entity = KeywordsSettingsEntity.fromKeywordLists(
                _uiState.value.triggerKeywords,
                _uiState.value.expenseKeywords,
                _uiState.value.incomeKeywords
            )
            keywordsSettingsDao.save(entity)
            _uiState.value = _uiState.value.copy(saveSuccess = true)
        }
    }

    fun clearSaveSuccess() {
        _uiState.value = _uiState.value.copy(saveSuccess = false)
    }
}

