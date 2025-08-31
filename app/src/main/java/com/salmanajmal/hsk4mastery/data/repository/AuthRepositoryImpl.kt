package com.salmanajmal.hsk4mastery.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

// Offline clean slate stub. No DI, no network. Replace later with local-only auth if needed.
class AuthRepositoryImpl : AuthRepository {
    override suspend fun login(email: String, password: String): Result<Unit> =
        Result.failure(UnsupportedOperationException("Offline stub"))

    override suspend fun register(username: String, email: String, password: String): Result<Unit> =
        Result.failure(UnsupportedOperationException("Offline stub"))

    override fun getToken(): Flow<String?> = flowOf(null)
}
