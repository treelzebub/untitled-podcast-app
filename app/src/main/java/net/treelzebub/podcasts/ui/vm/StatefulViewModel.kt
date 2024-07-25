package net.treelzebub.podcasts.ui.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn


abstract class StatefulViewModel<T : Any>(initialState: T) : ViewModel() {

    companion object {
        private const val TIMEOUT = 5000L
    }

    protected val _state = MutableStateFlow(initialState)
    val state = _state.asStateFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(TIMEOUT), initialState)

//    val state: StateFlow<String>
//        field = MutableStateFlow("")
//        get() = field.asStateFlow()
//            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(TIMEOUT), initialState)
}
