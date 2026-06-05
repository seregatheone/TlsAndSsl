package pat.project.tlsandssl.lab

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class LabUiState(
    val url: String = "https://example.com",
    val loading: Boolean = false,
    val result: InspectionResult? = null,
    val failure: LabFailure? = null,
    val pinMessage: String? = null
)

class LabViewModel(private val inspector: TlsInspector = TlsInspector()) : ViewModel() {
    private val _state = MutableStateFlow(LabUiState())
    val state: StateFlow<LabUiState> = _state.asStateFlow()

    fun setUrl(value: String) {
        _state.value = _state.value.copy(url = value, failure = null, pinMessage = null)
    }

    fun inspect() {
        val url = _state.value.url
        _state.value = _state.value.copy(loading = true, failure = null, pinMessage = null)
        viewModelScope.launch {
            when (val result = inspector.inspect(url)) {
                is LabResult.Success -> _state.value = _state.value.copy(loading = false, result = result.value)
                is LabResult.Failure -> _state.value = _state.value.copy(loading = false, result = null, failure = result.error)
            }
        }
    }

    fun verifyPin(matching: Boolean) {
        val inspected = _state.value.result ?: return
        _state.value = _state.value.copy(loading = true, pinMessage = null)
        viewModelScope.launch {
            when (val result = inspector.verifyPin(inspected, matching)) {
                is LabResult.Success -> _state.value = _state.value.copy(loading = false, pinMessage = result.value)
                is LabResult.Failure -> _state.value = _state.value.copy(loading = false, pinMessage = result.error.title)
            }
        }
    }
}
