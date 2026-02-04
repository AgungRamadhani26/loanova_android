package com.example.loanova_android.domain.usecase.auth

import app.cash.turbine.test
import com.example.loanova_android.core.common.Resource
import com.example.loanova_android.domain.repository.IAuthRepository
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

/**
 * Unit Test untuk ForgotPasswordUseCase
 * 
 * Test Cases:
 * 1. execute_withValidEmail_returnsLoadingThenSuccess
 * 2. execute_withInvalidEmail_returnsLoadingThenError
 * 3. execute_callsRepositoryWithCorrectEmail
 */
class ForgotPasswordUseCaseTest {

    // Mock dependency
    private lateinit var mockRepository: IAuthRepository
    
    // System Under Test (SUT)
    private lateinit var forgotPasswordUseCase: ForgotPasswordUseCase

    @Before
    fun setUp() {
        // Initialize mock
        mockRepository = mock()
        
        // Create UseCase with mocked repository
        forgotPasswordUseCase = ForgotPasswordUseCase(mockRepository)
    }

    /**
     * Test Case 1: Ketika email valid, UseCase harus return Loading lalu Success
     */
    @Test
    fun `execute with valid email returns Loading then Success`() = runTest {
        // Given: Repository returns success flow
        val testEmail = "test@example.com"
        val successMessage = "Link reset password telah dikirim ke email Anda"
        
        whenever(mockRepository.forgotPassword(testEmail)).thenReturn(
            flow {
                emit(Resource.Loading())
                emit(Resource.Success(successMessage))
            }
        )

        // When: Execute UseCase
        forgotPasswordUseCase.execute(testEmail).test {
            // Then: First emission should be Loading
            val loadingState = awaitItem()
            assertTrue("First emission should be Loading", loadingState is Resource.Loading)
            
            // Then: Second emission should be Success with correct message
            val successState = awaitItem()
            assertTrue("Second emission should be Success", successState is Resource.Success)
            assertEquals(successMessage, (successState as Resource.Success).data)
            
            // Verify no more emissions
            awaitComplete()
        }
    }

    /**
     * Test Case 2: Ketika email tidak terdaftar, UseCase harus return Loading lalu Error
     */
    @Test
    fun `execute with unregistered email returns Loading then Error`() = runTest {
        // Given: Repository returns error flow
        val testEmail = "notfound@example.com"
        val errorMessage = "User dengan email notfound@example.com tidak ditemukan"
        
        whenever(mockRepository.forgotPassword(testEmail)).thenReturn(
            flow {
                emit(Resource.Loading())
                emit(Resource.Error(errorMessage))
            }
        )

        // When: Execute UseCase
        forgotPasswordUseCase.execute(testEmail).test {
            // Then: First emission should be Loading
            val loadingState = awaitItem()
            assertTrue("First emission should be Loading", loadingState is Resource.Loading)
            
            // Then: Second emission should be Error with correct message
            val errorState = awaitItem()
            assertTrue("Second emission should be Error", errorState is Resource.Error)
            assertEquals(errorMessage, (errorState as Resource.Error).message)
            
            // Verify no more emissions
            awaitComplete()
        }
    }

    /**
     * Test Case 3: Verify UseCase memanggil repository dengan email yang benar
     */
    @Test
    fun `execute calls repository with correct email`() = runTest {
        // Given
        val testEmail = "verify@example.com"
        
        whenever(mockRepository.forgotPassword(testEmail)).thenReturn(
            flow {
                emit(Resource.Loading())
                emit(Resource.Success("Success"))
            }
        )

        // When: Execute UseCase and collect all emissions
        forgotPasswordUseCase.execute(testEmail).test {
            awaitItem() // Loading
            awaitItem() // Success
            awaitComplete()
        }

        // Then: Verify repository was called with correct email
        verify(mockRepository).forgotPassword(testEmail)
    }
}
