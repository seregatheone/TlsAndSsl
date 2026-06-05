package pat.project.tlsandssl

import java.net.SocketTimeoutException
import javax.net.ssl.SSLHandshakeException
import javax.net.ssl.SSLPeerUnverifiedException
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import pat.project.tlsandssl.core.ContentLabel
import pat.project.tlsandssl.enterprise.EnterpriseContent
import pat.project.tlsandssl.lab.FailureKind
import pat.project.tlsandssl.lab.UrlPolicy
import pat.project.tlsandssl.lab.toLabFailure
import pat.project.tlsandssl.learning.LearningContent

class TlsLabUnitTest {
    @Test
    fun learningPathHasStableOrderAndRequiredTerms() {
        assertEquals(listOf(1, 2, 3, 4), LearningContent.lessons.map { it.order })
        assertTrue(listOf("SSL", "TLS", "HTTPS", "CA", "Pinning", "mTLS").all(LearningContent.glossary::containsKey))
        assertEquals("Application Data", LearningContent.handshake.last().title)
        assertTrue(LearningContent.unsafeTrustAll.contains("УЯЗВИМО"))
    }

    @Test
    fun unsafeContentIsNeverRunnable() {
        assertFalse(LearningContent.lessons.any { it.label == ContentLabel.UNSAFE })
        assertTrue(LearningContent.unsafeTrustAll.contains("hostnameVerifier"))
    }

    @Test
    fun urlPolicyAllowsOnlyCompleteHttpsUrls() {
        assertNull(UrlPolicy.validate("https://example.com/path"))
        assertEquals(FailureKind.INVALID_URL, UrlPolicy.validate("http://example.com")?.kind)
        assertEquals(FailureKind.INVALID_URL, UrlPolicy.validate("not a url")?.kind)
    }

    @Test
    fun failuresAreCategorizedForTheUser() {
        assertEquals(FailureKind.TIMEOUT, SocketTimeoutException().toLabFailure().kind)
        assertEquals(FailureKind.HOSTNAME, SSLHandshakeException("hostname mismatch").toLabFailure().kind)
        assertEquals(FailureKind.CERTIFICATE, SSLHandshakeException("bad certificate").toLabFailure().kind)
        assertEquals(FailureKind.CERTIFICATE, SSLPeerUnverifiedException("Certificate pinning failure").toLabFailure().kind)
    }

    @Test
    fun enterpriseGuidanceChangesWithConstraints() {
        val public = EnterpriseContent.recommend(publicApi = true, privateServices = false, managedDevices = false, clientIdentity = false)
        assertTrue(public.any { it.startsWith("System trust") })
        assertTrue(public.any { it.startsWith("Pinning") })

        val internal = EnterpriseContent.recommend(publicApi = false, privateServices = true, managedDevices = true, clientIdentity = true)
        assertTrue(internal.any { it.startsWith("Private CA") })
        assertTrue(internal.any { it.startsWith("mTLS") })
        assertEquals(6, EnterpriseContent.lifecycle.size)
        assertTrue(EnterpriseContent.scenarios.all { it.owner.isNotBlank() && it.trustBoundary.isNotBlank() })
    }
}
