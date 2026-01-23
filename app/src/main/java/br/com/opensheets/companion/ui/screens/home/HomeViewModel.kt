package br.com.opensheets.companion.ui.screens.home

import android.content.ComponentName
import android.content.Context
import android.provider.Settings
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.opensheets.companion.data.local.dao.AppConfigDao
import br.com.opensheets.companion.data.local.dao.NotificationDao
import br.com.opensheets.companion.data.local.entities.NotificationEntity
import br.com.opensheets.companion.data.local.entities.SyncStatus
import br.com.opensheets.companion.service.CaptureNotificationListenerService
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject

data class NotificationUiItem(
    val id: String,
    val appName: String,
    val title: String?,
    val text: String,
    val parsedAmount: String?,
    val parsedName: String?,
    val syncStatus: SyncStatus,
    val timestamp: String,
    val timestampFull: String
)

enum class SyncStatusFilter {
    ALL, PENDING, SYNCED, FAILED
}

data class HomeUiState(
    val pendingCount: Int = 0,
    val syncedToday: Int = 0,
    val lastSyncTime: String? = null,
    val hasNotificationPermission: Boolean = false,
    val enabledAppsCount: Int = 0,
    // History
    val notifications: List<NotificationUiItem> = emptyList(),
    val selectedFilter: SyncStatusFilter = SyncStatusFilter.ALL,
    val isLoadingNotifications: Boolean = true
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val notificationDao: NotificationDao,
    private val appConfigDao: AppConfigDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val dateFormat = SimpleDateFormat("dd/MM HH:mm", Locale.getDefault())
    private val dateFormatFull = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())

    init {
        loadStats()
        loadNotifications()
        checkNotificationPermission()
    }

    private fun loadStats() {
        viewModelScope.launch {
            // Count pending notifications
            val pendingCount = notificationDao.countPending()

            // Count synced today
            val todayStart = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis
            val syncedToday = notificationDao.countSince(todayStart)

            // Get enabled apps count
            val enabledApps = appConfigDao.getEnabled()

            _uiState.value = _uiState.value.copy(
                pendingCount = pendingCount,
                syncedToday = syncedToday,
                enabledAppsCount = enabledApps.size
            )
        }
    }

    fun loadNotifications() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingNotifications = true)

            val notifications = notificationDao.getRecent(100)
            val filteredNotifications = filterNotifications(notifications, _uiState.value.selectedFilter)
            val uiItems = filteredNotifications.map { it.toUiItem() }

            _uiState.value = _uiState.value.copy(
                isLoadingNotifications = false,
                notifications = uiItems
            )
        }
    }

    fun setFilter(filter: SyncStatusFilter) {
        _uiState.value = _uiState.value.copy(selectedFilter = filter)
        loadNotifications()
    }

    fun deleteNotification(id: String) {
        viewModelScope.launch {
            notificationDao.delete(id)
            loadNotifications()
            loadStats()
        }
    }

    private fun filterNotifications(
        notifications: List<NotificationEntity>,
        filter: SyncStatusFilter
    ): List<NotificationEntity> {
        return when (filter) {
            SyncStatusFilter.ALL -> notifications
            SyncStatusFilter.PENDING -> notifications.filter {
                it.syncStatus == SyncStatus.PENDING_SYNC
            }
            SyncStatusFilter.SYNCED -> notifications.filter {
                it.syncStatus == SyncStatus.SYNCED
            }
            SyncStatusFilter.FAILED -> notifications.filter {
                it.syncStatus == SyncStatus.SYNC_FAILED
            }
        }
    }

    private fun NotificationEntity.toUiItem(): NotificationUiItem {
        return NotificationUiItem(
            id = id,
            appName = sourceAppName ?: sourceApp,
            title = originalTitle,
            text = originalText,
            parsedAmount = parsedAmount?.let { "R$ %.2f".format(it) },
            parsedName = parsedName,
            syncStatus = syncStatus,
            timestamp = dateFormat.format(Date(createdAt)),
            timestampFull = dateFormatFull.format(Date(createdAt))
        )
    }

    private fun checkNotificationPermission() {
        val hasPermission = isNotificationListenerEnabled()
        _uiState.value = _uiState.value.copy(hasNotificationPermission = hasPermission)
    }

    private fun isNotificationListenerEnabled(): Boolean {
        val componentName = ComponentName(context, CaptureNotificationListenerService::class.java)
        val enabledListeners = Settings.Secure.getString(
            context.contentResolver,
            "enabled_notification_listeners"
        )
        return enabledListeners?.contains(componentName.flattenToString()) == true
    }

    fun requestNotificationPermission() {
        // This should open the notification listener settings
        // The actual navigation should be handled by the UI
    }

    fun refreshStats() {
        loadStats()
        loadNotifications()
        checkNotificationPermission()
    }
}
