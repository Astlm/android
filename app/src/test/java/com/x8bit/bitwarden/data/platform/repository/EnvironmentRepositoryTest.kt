package com.x8bit.bitwarden.data.platform.repository

import app.cash.turbine.test
import com.x8bit.bitwarden.data.auth.datasource.disk.model.EnvironmentUrlDataJson
import com.x8bit.bitwarden.data.platform.datasource.disk.EnvironmentDiskSource
import com.x8bit.bitwarden.data.platform.repository.model.Environment
import com.x8bit.bitwarden.data.platform.repository.util.toEnvironmentUrls
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.onSubscription
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class EnvironmentRepositoryTest {
    private val fakeEnvironmentDiskSource = FakeEnvironmentDiskSource()

    @OptIn(ExperimentalCoroutinesApi::class)
    private val repository = EnvironmentRepositoryImpl(
        environmentDiskSource = fakeEnvironmentDiskSource,
        dispatcher = UnconfinedTestDispatcher(),
    )

    @BeforeEach
    fun setUp() {
        mockkStatic(ENVIRONMENT_EXTENSIONS_PATH)
    }

    @AfterEach
    fun tearDown() {
        unmockkStatic(ENVIRONMENT_EXTENSIONS_PATH)
    }

    @Test
    fun `environment should pull from and update EnvironmentDiskSource`() {
        val environmentUrlDataJson = mockk<EnvironmentUrlDataJson>()
        val environment = mockk<Environment>() {
            every { environmentUrlData } returns environmentUrlDataJson
        }
        every { environmentUrlDataJson.toEnvironmentUrls() } returns environment

        // The repository exposes a non-null default value when the disk source is empty
        assertNull(fakeEnvironmentDiskSource.preAuthEnvironmentUrlData)
        assertEquals(
            Environment.Us,
            repository.environment,
        )

        // Updating the repository updates the disk source
        repository.environment = environment
        assertEquals(
            environmentUrlDataJson,
            fakeEnvironmentDiskSource.preAuthEnvironmentUrlData,
        )

        // Updating the disk source updates the repository
        fakeEnvironmentDiskSource.preAuthEnvironmentUrlData = null
        assertEquals(
            Environment.Us,
            repository.environment,
        )
        fakeEnvironmentDiskSource.preAuthEnvironmentUrlData = environmentUrlDataJson
        assertEquals(
            environment,
            repository.environment,
        )
    }

    @Test
    fun `environmentStateFow should react to changes in environment`() = runTest {
        val environmentUrlDataJson = mockk<EnvironmentUrlDataJson>()
        val environment = mockk<Environment>() {
            every { environmentUrlData } returns environmentUrlDataJson
        }
        every { environmentUrlDataJson.toEnvironmentUrls() } returns environment

        repository.environmentStateFlow.test {
            // The initial values of the Flow and the property are in sync
            assertEquals(
                Environment.Us,
                repository.environment,
            )
            assertEquals(
                Environment.Us,
                awaitItem(),
            )

            // Updating the property causes a flow emissions
            repository.environment = environment
            assertEquals(environment, awaitItem())
        }
    }
}

private const val ENVIRONMENT_EXTENSIONS_PATH =
    "com.x8bit.bitwarden.data.platform.repository.util.EnvironmentExtensionsKt"

private class FakeEnvironmentDiskSource : EnvironmentDiskSource {
    override var preAuthEnvironmentUrlData: EnvironmentUrlDataJson? = null
        set(value) {
            field = value
            mutablePreAuthEnvironmentUrlDataFlow.tryEmit(value)
        }

    override val preAuthEnvironmentUrlDataFlow: Flow<EnvironmentUrlDataJson?>
        get() = mutablePreAuthEnvironmentUrlDataFlow
            .onSubscription { emit(preAuthEnvironmentUrlData) }

    private val mutablePreAuthEnvironmentUrlDataFlow =
        MutableSharedFlow<EnvironmentUrlDataJson?>(
            replay = 1,
            extraBufferCapacity = Int.MAX_VALUE,
        )
}