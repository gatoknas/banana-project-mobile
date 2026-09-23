package org.banana.project.presentation.login

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.banana.project.data.repository.AuthRepository
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    class FakeAuthRepository : AuthRepository {
        var shouldSucceed = true
        override suspend fun login(username: String, password: String): Result<Unit> {
            if (shouldSucceed) return Result.success(Unit)
            return Result.failure(Exception("Invalid credentials"))
        }
        override fun logout() {}
        override fun getAccessToken(): String? = null
        override fun getRefreshToken(): String? = null
        override fun getCurrentUser(): org.banana.project.model.User? = null
    }

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    data class TestCase(
        val name: String,
        val setup: (FakeAuthRepository, LoginViewModel) -> Unit,
        val event: LoginEvent,
        val verify: (LoginState) -> Unit
    )

    @Test
    fun `test state transitions`() = runTest {
        val repo = FakeAuthRepository()
        
        val testCases = listOf(
            TestCase(
                name = "Username update",
                setup = { _, _ -> },
                event = LoginEvent.UsernameChanged("admin"),
                verify = { state -> assertEquals("admin", state.username) }
            ),
            TestCase(
                name = "Password update",
                setup = { _, _ -> },
                event = LoginEvent.PasswordChanged("secret"),
                verify = { state -> assertEquals("secret", state.password) }
            ),
            TestCase(
                name = "Submit missing credentials",
                setup = { _, vm ->
                    vm.onEvent(LoginEvent.UsernameChanged(""))
                },
                event = LoginEvent.Submit(onSuccess = {}),
                verify = { state -> 
                    assertEquals("El usuario y la contraseña son requeridos.", state.errorMessage)
                }
            ),
            TestCase(
                name = "Submit valid credentials",
                setup = { r, vm ->
                    r.shouldSucceed = true
                    vm.onEvent(LoginEvent.UsernameChanged("admin"))
                    vm.onEvent(LoginEvent.PasswordChanged("secret"))
                },
                event = LoginEvent.Submit(onSuccess = {}),
                verify = { state -> 
                    assertEquals(null, state.errorMessage)
                    assertEquals(false, state.isLoading)
                }
            ),
            TestCase(
                name = "Submit invalid credentials",
                setup = { r, vm ->
                    r.shouldSucceed = false
                    vm.onEvent(LoginEvent.UsernameChanged("admin"))
                    vm.onEvent(LoginEvent.PasswordChanged("wrong"))
                },
                event = LoginEvent.Submit(onSuccess = {}),
                verify = { state -> 
                    assertEquals("Invalid credentials", state.errorMessage)
                    assertEquals(false, state.isLoading)
                }
            )
        )

        testCases.forEach { tc ->
            val vm = LoginViewModel(repo)
            tc.setup(repo, vm)
            vm.onEvent(tc.event)
            tc.verify(vm.state.value)
        }
    }
}
