package pat.project.tlsandssl

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import org.junit.Rule
import org.junit.Test
import pat.project.tlsandssl.app.TlsLabApp
import pat.project.tlsandssl.ui.theme.TlsAndSslTheme

class TlsLabComposeTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun homeNavigatesToHandshakeAndEnterprise() {
        composeRule.setContent { TlsAndSslTheme { TlsLabApp() } }

        composeRule.onNodeWithText("Открыть handshake").performClick()
        composeRule.onNodeWithText("TLS 1.3 handshake").assertIsDisplayed()
        composeRule.onNodeWithText("ClientHello").assertIsDisplayed()
        composeRule.onNodeWithText("Дальше").performClick()
        composeRule.onNodeWithText("ServerHello").assertIsDisplayed()

        composeRule.onNodeWithText("← Назад").performClick()
        composeRule.onNode(hasScrollAction()).performScrollToNode(hasText("Открыть playbook"))
        composeRule.onNodeWithText("Открыть playbook").performClick()
        composeRule.onNodeWithText("Enterprise playbook").assertIsDisplayed()
        composeRule.onNodeWithText("Control selector").assertIsDisplayed()
    }

    @Test
    fun labExplainsSafetyBoundary() {
        composeRule.setContent { TlsAndSslTheme { TlsLabApp() } }
        composeRule.onNodeWithText("Запустить Inspector").performClick()
        composeRule.onNodeWithText("HTTPS Inspector").assertIsDisplayed()
        composeRule.onNodeWithText("Разрешён только HTTPS", substring = true).assertDoesNotExist()
        composeRule.onNodeWithText("Inspector сохраняет системную проверку", substring = true).assertIsDisplayed()
    }
}
