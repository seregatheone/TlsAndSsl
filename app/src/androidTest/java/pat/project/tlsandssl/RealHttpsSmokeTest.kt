package pat.project.tlsandssl

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Test
import org.junit.runner.RunWith
import pat.project.tlsandssl.lab.LabResult
import pat.project.tlsandssl.lab.TlsInspector

@RunWith(AndroidJUnit4::class)
class RealHttpsSmokeTest {
    @Test
    fun trustedEndpointExposesHandshakeMetadata() = runBlocking {
        val enabled = InstrumentationRegistry.getArguments().getString("runTlsSmoke") == "true"
        assumeTrue("Enable with -e runTlsSmoke true", enabled)

        val result = TlsInspector().inspect("https://example.com")
        assertTrue(result is LabResult.Success && result.value.certificates.isNotEmpty())
    }
}
