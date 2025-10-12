package mx.itesm.beneficiojuventud.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import mx.itesm.beneficiojuventud.model.users.RemoteServiceUser
import mx.itesm.beneficiojuventud.model.users.UserProfile


class UserViewModel : ViewModel() {

    private val model = RemoteServiceUser

    private val _userState = MutableStateFlow(UserProfile())
    val userState: StateFlow<UserProfile> = _userState

    suspend fun getUserById(id: String) {
        _userState.value = model.getUserById(id)
    }

    suspend fun createUser(user: UserProfile) {
        _userState.value = model.createUser(user)
    }

    suspend fun updateUser(id: String, update: UserProfile) {
        _userState.value = model.updateUser(id, update)
    }

    suspend fun deleteUser(id: String) {
        model.deleteUser(id)
    }

}
