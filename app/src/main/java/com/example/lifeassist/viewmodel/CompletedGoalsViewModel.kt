package com.example.lifeassist.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lifeassist.model.Main
import com.example.lifeassist.model.Result
import com.example.lifeassist.repository.RepositoryProvider
import kotlinx.coroutines.launch

class CompletedGoalsViewModel : ViewModel() {

    private val userRepository = RepositoryProvider.userRepository

    private val _completedGoals = MutableLiveData<List<Main.Goal>>()
    val completedGoals: LiveData<List<Main.Goal>> = _completedGoals

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    fun fetchCompletedGoals(context: Context, userId: String) {
        Log.d("CompletedGoalsViewModel", "fetchCompletedGoals called with userId=$userId")
        viewModelScope.launch {
            val result = userRepository.getUserData(context, userId)
            when (result) {
                is Result.Success -> {
                    Log.d("CompletedGoalsViewModel", "fetchCompletedGoals success, filtering completed goals.")
                    val completedGoals = result.data.user.goals.filter { it.status == "completed" }
                    _completedGoals.postValue(completedGoals)
                }
                is Result.Error -> {
                    Log.e("CompletedGoalsViewModel", "fetchCompletedGoals error: ${result.message}")
                    _error.postValue(result.message)
                }
            }
        }
    }
}
