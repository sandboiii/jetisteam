package bruhcollective.itaysonlab.jetisteam.usecases

import bruhcollective.itaysonlab.jetisteam.repository.AuthRepository
import javax.inject.Inject

class BeginSignIn @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(
        username: String,
        password: String
    ): Boolean {
        return authRepository.getRsaKey(username).let { keyAndTimestamp ->
            authRepository.beginAuthSessionViaCredentials(username, keyAndTimestamp.second, authRepository.encryptPassword(password, keyAndTimestamp.first))
        }
    }
}