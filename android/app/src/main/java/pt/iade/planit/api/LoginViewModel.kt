// LoginViewModel.kt (ou em sua Activity)
package pt.iade.planit

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import pt.iade.planit.api.Event
import pt.iade.planit.api.EventDetailsResponse
import pt.iade.planit.api.RetrofitInstance
import pt.iade.planit.api.User
import pt.iade.planit.api.UserCredentials

class LoginViewModel : ViewModel() {

    fun registerUser(name: String, email: String, password: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val user = User(name = name, email = email, password = password)
                val response = RetrofitInstance.api.register(user)
                Log.d("Register", "Usuário registrado: $response")
            } catch (e: Exception) {
                Log.e("Register", "Erro ao registrar usuário", e)
            }
        }
    }
    fun getUserEvents(userId: Int, onResult: (List<Event>) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val events = RetrofitInstance.api.getUserEvents(userId)
                withContext(Dispatchers.Main) {
                    onResult(events)
                }
            } catch (e: Exception) {
                Log.e("Events", "Error fetching user events", e)
                withContext(Dispatchers.Main) {
                    onResult(emptyList())
                }
            }
        }
    }

    fun loginUser(
        email: String,
        password: String,
        onSuccess: (Int) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = RetrofitInstance.api.login(UserCredentials(email, password))
                withContext(Dispatchers.Main) {
                    onSuccess(response.id)
                }
            } catch (e: retrofit2.HttpException) {
                val errorMessage = when (e.code()) {
                    400 -> "Requisição inválida. Verifique os dados fornecidos."
                    401 -> "E-mail ou senha incorretos."
                    403 -> "Acesso negado. Verifique suas permissões."
                    404 -> "Usuário não encontrado."
                    500 -> "Erro no servidor. Tente novamente mais tarde."
                    else -> "Erro desconhecido: ${e.message()}"
                }
                withContext(Dispatchers.Main) {
                    onError(errorMessage)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onError("Erro ao conectar ao servidor: ${e.message ?: "Erro desconhecido"}")
                }
            }
        }
    }

    fun getConfirmedEvents(userId: Int, onResult: (List<Event>) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val events = RetrofitInstance.api.getConfirmedEvents(userId)
                withContext(Dispatchers.Main) {
                    onResult(events)
                }
            } catch (e: Exception) {
                Log.e("ConfirmedEvents", "Erro ao buscar eventos confirmados", e)
                withContext(Dispatchers.Main) {
                    onResult(emptyList())
                }
            }
        }
    }

    fun createEvent(
        userId: Int,
        title: String,
        description: String,
        date: String,
        photoUrl: String?,
        latitude: Double,
        longitude: Double,
        address: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val event = Event(
            userId = userId,
            title = title,
            description = description,
            date = date,
            photoUrl = photoUrl,
            latitude = latitude,
            longitude = longitude,
            address = address
        )

        viewModelScope.launch {
            try {
                RetrofitInstance.api.createEvent(event)
                onSuccess()
            } catch (e: retrofit2.HttpException) {
                val errorMessage = when (e.code()) {
                    400 -> "Dados inválidos. Verifique os campos."
                    401 -> "Usuário não autorizado."
                    403 -> "Acesso negado."
                    404 -> "Recurso não encontrado."
                    500 -> "Erro no servidor. Tente novamente mais tarde."
                    else -> "Erro desconhecido: ${e.message}"
                }
                onError(errorMessage)
            } catch (e: Exception) {
                onError("Erro ao conectar com o servidor: ${e.message ?: "Erro desconhecido"}")
            }
        }
    }

}

