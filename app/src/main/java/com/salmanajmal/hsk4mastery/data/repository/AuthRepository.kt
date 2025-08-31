package com.salmanajmal.hsk4mastery.data.repository

import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    suspend fun login(email: String, password: String): Result<Unit>
    suspend fun register(username: String, email: String, password: String): Result<Unit>
    fun getToken(): Flow<String?>
}
