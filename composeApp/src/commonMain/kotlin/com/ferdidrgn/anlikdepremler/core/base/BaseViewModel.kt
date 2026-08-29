package com.ferdidrgn.anlikdepremler.core.base

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.*

abstract class BaseViewModel<State, Event>(initialState: State) : ViewModel() {

    private val _uiState = MutableStateFlow(initialState)
    val uiState: StateFlow<State> = _uiState.asStateFlow()

    protected fun updateState(reduce: (State) -> State) {
        _uiState.update(reduce)
    }

    abstract fun onEvent(event: Event)
}