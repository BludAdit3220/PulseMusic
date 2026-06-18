/*
 * PulseMusic (2026)
 * © Aditya Parasher — github.com/BludAdit3220
 * GPL-3.0 License | Contributors: see git history
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 */

package com.pulsemusic.music.viewmodels

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import java.util.concurrent.atomic.AtomicInteger
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import com.pulsemusic.music.R
import com.pulsemusic.music.ai.AiModelOption
import com.pulsemusic.music.ai.AiServiceConfig
import com.pulsemusic.music.ai.AiTextService
import com.pulsemusic.music.constants.AiApiKeyKey
import com.pulsemusic.music.constants.AiApiValidationStatus
import com.pulsemusic.music.constants.AiApiValidationStatusKey
import com.pulsemusic.music.constants.AiCustomEndpointKey
import com.pulsemusic.music.constants.AiCustomModelKey
import com.pulsemusic.music.constants.AiProvider
import com.pulsemusic.music.constants.AiProviderKey
import com.pulsemusic.music.constants.AiSelectedModelKey
import com.pulsemusic.music.extensions.toEnum
import com.pulsemusic.music.utils.dataStore
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

data class AiIntegrationActionState(
    val isTesting: Boolean = false,
    val isFetchingModels: Boolean = false,
    val errorMessage: String? = null,
)

private const val MaxInlineErrorLength = 140

@HiltViewModel
class AiIntegrationSettingsViewModel
@Inject
constructor(
    @ApplicationContext private val context: Context,
) : ViewModel() {
    private val modelsCacheFile = File(context.cacheDir, "ai_models_cache.json")

    private val _actionState = MutableStateFlow(AiIntegrationActionState())
    val actionState: StateFlow<AiIntegrationActionState> = _actionState.asStateFlow()

    private val _events = MutableSharedFlow<String>()
    val events: SharedFlow<String> = _events.asSharedFlow()

    private val _availableModels = MutableStateFlow<List<AiModelOption>>(emptyList())
    val availableModels: StateFlow<List<AiModelOption>> = _availableModels.asStateFlow()
    private var fetchModelsJob: Job? = null
    private val fetchModelsRequestId = AtomicInteger()

    init {
        loadModelsFromCache()
    }

    private fun loadModelsFromCache() {
        viewModelScope.launch(Dispatchers.IO) {
            if (modelsCacheFile.exists()) {
                try {
                    val json = modelsCacheFile.readText()
                    val array = JSONArray(json)
                    val models = List(array.length()) { i ->
                        val obj = array.getJSONObject(i)
                        AiModelOption(obj.getString("id"), obj.getString("displayName"))
                    }
                    _availableModels.value = models
                } catch (_: Exception) {}
            }
        }
    }

    private fun saveModelsToCache(models: List<AiModelOption>) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val array = JSONArray()
                models.forEach { model ->
                    array.put(JSONObject().put("id", model.id).put("displayName", model.displayName))
                }
                modelsCacheFile.writeText(array.toString())
            } catch (_: Exception) {}
        }
    }

    private fun deleteModelsCache() {
        if (modelsCacheFile.exists()) {
            modelsCacheFile.delete()
        }
    }

    fun clearAvailableModels() {
        fetchModelsRequestId.incrementAndGet()
        fetchModelsJob?.cancel()
        fetchModelsJob = null
        _availableModels.value = emptyList()
        deleteModelsCache()
        _actionState.value = _actionState.value.copy(
            isFetchingModels = false,
            errorMessage = null,
        )
    }

    fun clearError() {
        _actionState.value = _actionState.value.copy(errorMessage = null)
    }

    fun fetchModels(provider: AiProvider, apiKey: String, customEndpoint: String) {
        if (fetchModelsJob?.isActive == true) return
        val requestId = fetchModelsRequestId.incrementAndGet()
        fetchModelsJob = viewModelScope.launch(Dispatchers.IO) {
            _actionState.value = _actionState.value.copy(
                isFetchingModels = true,
                errorMessage = null,
            )
            _availableModels.value = emptyList()
            try {
                val config = AiServiceConfig(
                    provider = provider,
                    apiKey = apiKey,
                    customEndpoint = customEndpoint,
                    model = "",
                )
                val models = AiTextService.fetchModels(config)
                if (requestId == fetchModelsRequestId.get()) {
                    _availableModels.value = models
                    saveModelsToCache(models)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                if (requestId == fetchModelsRequestId.get()) {
                    _actionState.value = _actionState.value.copy(
                        errorMessage = e.shortMessage(context.getString(R.string.ai_model_fetch_failed)),
                    )
                }
            } finally {
                if (requestId == fetchModelsRequestId.get()) {
                    _actionState.value = _actionState.value.copy(isFetchingModels = false)
                    fetchModelsJob = null
                }
            }
        }
    }

    fun testApi() {
        if (_actionState.value.isTesting) return
        viewModelScope.launch(Dispatchers.IO) {
            _actionState.value = _actionState.value.copy(
                isTesting = true,
                errorMessage = null,
            )
            try {
                AiTextService.test(readConfig())
                context.dataStore.edit { prefs ->
                    prefs[AiApiValidationStatusKey] = AiApiValidationStatus.SUCCESS.name
                }
                _actionState.value = _actionState.value.copy(errorMessage = null)
                _events.emit(context.getString(R.string.ai_api_connected))
            } catch (e: Exception) {
                context.dataStore.edit { prefs ->
                    prefs[AiApiValidationStatusKey] = AiApiValidationStatus.FAILED.name
                }
                _actionState.value = _actionState.value.copy(
                    errorMessage = e.shortMessage(context.getString(R.string.ai_api_test_failed)),
                )
            } finally {
                _actionState.value = _actionState.value.copy(isTesting = false)
            }
        }
    }

    private suspend fun readConfig(): AiServiceConfig {
        val prefs = context.dataStore.data.first()
        val provider = prefs[AiProviderKey].toEnum(AiProvider.NONE)
        val model = if (provider == AiProvider.CUSTOM) {
            prefs[AiCustomModelKey].orEmpty()
        } else {
            prefs[AiSelectedModelKey].orEmpty()
        }
        return AiServiceConfig(
            provider = provider,
            apiKey = prefs[AiApiKeyKey].orEmpty(),
            customEndpoint = prefs[AiCustomEndpointKey].orEmpty(),
            model = model,
        )
    }

    private fun Throwable.shortMessage(fallback: String): String {
        val raw = localizedMessage?.takeIf { it.isNotBlank() } ?: fallback
        val message = raw
            .lineSequence()
            .firstOrNull { it.isNotBlank() }
            ?.replace(Regex("\\s+"), " ")
            ?.trim()
            .orEmpty()
            .ifBlank { fallback }
            .removePrefix("AI API failed ")
            .replace(Regex("^\\((\\d{3})\\):\\s*"), "HTTP $1: ")
        return if (message.length <= MaxInlineErrorLength) {
            message
        } else {
            message.take(MaxInlineErrorLength).trimEnd() + "..."
        }
    }
}
