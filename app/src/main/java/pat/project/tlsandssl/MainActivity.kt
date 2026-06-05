package pat.project.tlsandssl

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import pat.project.tlsandssl.app.TlsLabApp
import pat.project.tlsandssl.ui.theme.TlsAndSslTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TlsAndSslTheme {
                TlsLabApp()
            }
        }
    }
}
