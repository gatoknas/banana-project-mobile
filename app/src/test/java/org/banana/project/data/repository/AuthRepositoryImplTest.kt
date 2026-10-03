package org.banana.project.data.repository

import android.content.SharedPreferences
import kotlinx.coroutines.runBlocking
import org.banana.project.data.network.BananaApi
import org.banana.project.data.network.models.CategoryDto
import org.banana.project.data.network.models.LoginRequest
import org.banana.project.data.network.models.LoginResponse
import org.banana.project.data.network.models.ProductDto
import org.banana.project.data.network.models.RefreshRequest
import org.banana.project.data.network.models.SaleRequestDto
import org.banana.project.data.network.models.SaleResponseDto
import org.junit.Assert.assertEquals
import org.junit.Test
import retrofit2.Call

class AuthRepositoryImplTest {

    private class FakeApi : BananaApi {
        var shouldSucceed = true
        override suspend fun login(request: LoginRequest): LoginResponse {
            if (shouldSucceed) {
                return LoginResponse("token_123", "refresh_123")
            }
            throw Exception("HTTP 401")
        }

        override fun refreshToken(request: RefreshRequest): Call<LoginResponse> {
            throw NotImplementedError("Not needed for this test")
        }

        override suspend fun getProducts(): List<ProductDto> = emptyList()

        override suspend fun getCategories(): List<CategoryDto> = emptyList()

        override suspend fun createSale(request: SaleRequestDto): SaleResponseDto =
            SaleResponseDto(status = "success", message = "ok", saleId = 1L)
    }

    private class FakeSharedPreferences : SharedPreferences {
        val map = mutableMapOf<String, String>()

        override fun getAll(): MutableMap<String, *> = map
        override fun getString(key: String, defValue: String?): String? = map[key] ?: defValue
        override fun getStringSet(key: String, defValues: MutableSet<String>?): MutableSet<String>? = null
        override fun getInt(key: String, defValue: Int): Int = 0
        override fun getLong(key: String, defValue: Long): Long = 0L
        override fun getFloat(key: String, defValue: Float): Float = 0f
        override fun getBoolean(key: String, defValue: Boolean): Boolean = false
        override fun contains(key: String): Boolean = map.containsKey(key)
        override fun registerOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener?) {}
        override fun unregisterOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener?) {}

        override fun edit(): SharedPreferences.Editor = FakeEditor(map)
    }

    private class FakeEditor(val map: MutableMap<String, String>) : SharedPreferences.Editor {
        override fun putString(key: String, value: String?): SharedPreferences.Editor {
            if (value != null) map[key] = value else map.remove(key)
            return this
        }
        override fun putStringSet(key: String, values: MutableSet<String>?): SharedPreferences.Editor = this
        override fun putInt(key: String, value: Int): SharedPreferences.Editor = this
        override fun putLong(key: String, value: Long): SharedPreferences.Editor = this
        override fun putFloat(key: String, value: Float): SharedPreferences.Editor = this
        override fun putBoolean(key: String, value: Boolean): SharedPreferences.Editor = this
        override fun remove(key: String): SharedPreferences.Editor {
            map.remove(key)
            return this
        }
        override fun clear(): SharedPreferences.Editor {
            map.clear()
            return this
        }
        override fun commit(): Boolean = true
        override fun apply() {}
    }

    data class TestCase(
        val name: String,
        val shouldSucceed: Boolean,
        val expectedResult: Boolean,
        val expectedToken: String?,
        val expectedRefresh: String?
    )

    @Test
    fun `test login TDT`() = runBlocking {
        val testCases = listOf(
            TestCase("Successful login", true, true, "token_123", "refresh_123"),
            TestCase("Failed login", false, false, null, null)
        )

        for (tc in testCases) {
            val fakeApi = FakeApi().apply { shouldSucceed = tc.shouldSucceed }
            val fakePrefs = FakeSharedPreferences()
            val repo = AuthRepositoryImpl(fakeApi, fakePrefs)

            val result = repo.login("user", "pass")
            
            assertEquals(tc.name, tc.expectedResult, result.isSuccess)
            assertEquals(tc.name, tc.expectedToken, repo.getAccessToken())
            assertEquals(tc.name, tc.expectedRefresh, repo.getRefreshToken())
        }
    }
}
