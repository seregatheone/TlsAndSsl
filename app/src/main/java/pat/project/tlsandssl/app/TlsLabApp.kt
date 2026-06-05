package pat.project.tlsandssl.app

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import pat.project.tlsandssl.core.ContentLabel
import pat.project.tlsandssl.enterprise.EnterpriseContent
import pat.project.tlsandssl.lab.CertificateInfo
import pat.project.tlsandssl.lab.LabViewModel
import pat.project.tlsandssl.learning.LearningContent

private object Routes {
    const val HOME = "home"
    const val LEARN = "learn"
    const val HANDSHAKE = "handshake"
    const val THREATS = "threats"
    const val LAB = "lab"
    const val IMPLEMENTATIONS = "implementations"
    const val ENTERPRISE = "enterprise"
}

@Composable
fun TlsLabApp(navController: NavHostController = rememberNavController()) {
    NavHost(navController = navController, startDestination = Routes.HOME) {
        composable(Routes.HOME) { HomeScreen(navController::navigate) }
        composable(Routes.LEARN) { Page("Учебный маршрут", navController::popBackStack) { LearningScreen(navController::navigate) } }
        composable(Routes.HANDSHAKE) { Page("TLS 1.3 handshake", navController::popBackStack) { HandshakeScreen() } }
        composable(Routes.THREATS) { Page("Угрозы и защиты", navController::popBackStack) { ThreatScreen() } }
        composable(Routes.LAB) { Page("HTTPS Inspector", navController::popBackStack) { LabScreen() } }
        composable(Routes.IMPLEMENTATIONS) { Page("Реализации", navController::popBackStack) { ImplementationScreen() } }
        composable(Routes.ENTERPRISE) { Page("Enterprise playbook", navController::popBackStack) { EnterpriseScreen() } }
    }
}

@Composable
private fun HomeScreen(navigate: (String) -> Unit) {
    Scaffold { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Text("TLS LAB", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                Text("Увидеть. Запустить. Понять зачем.", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black)
                Text(
                    "Наглядный Android-проект о защищённом транспорте: от первого ClientHello до ротации сертификатов в большом бизнесе.",
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(Modifier.height(8.dp))
                LabelBadge(ContentLabel.RUNNABLE)
            }
            item {
                SectionCard("01 · Понять протокол", "Интерактивный handshake, сертификаты, ключи и границы TLS.") {
                    Button(onClick = { navigate(Routes.LEARN) }) { Text("Начать маршрут") }
                    TextButton(onClick = { navigate(Routes.HANDSHAKE) }) { Text("Открыть handshake") }
                }
            }
            item {
                SectionCard("02 · Проверить соединение", "Реальный HTTPS-запрос: TLS version, cipher suite, цепочка X.509 и pinning.") {
                    Button(onClick = { navigate(Routes.LAB) }) { Text("Запустить Inspector") }
                }
            }
            item {
                SectionCard("03 · Увидеть реализации", "System trust, custom CA, certificate pinning и mutual TLS без trust-all shortcuts.") {
                    Button(onClick = { navigate(Routes.IMPLEMENTATIONS) }) { Text("Смотреть код") }
                    TextButton(onClick = { navigate(Routes.THREATS) }) { Text("Угрозы и anti-patterns") }
                }
            }
            item {
                SectionCard("04 · Применить в бизнесе", "Edge termination, service mTLS, zero trust, ownership, ротация и incident response.") {
                    Button(onClick = { navigate(Routes.ENTERPRISE) }) { Text("Открыть playbook") }
                }
            }
            item { SafetyNotice() }
        }
    }
}

@Composable
private fun LearningScreen(navigate: (String) -> Unit) {
    itemsList {
        item {
            Intro("Рекомендуемый порядок", "Начните с угрозы, затем разберите identity и ключи. После этого дополнительные контроли будут выглядеть не магией, а инженерным выбором.")
        }
        items(LearningContent.lessons.sortedBy { it.order }) { lesson ->
            SectionCard("${lesson.order}. ${lesson.title}", lesson.summary) {
                Text("Зачем: ${lesson.why}")
                CodeBlock(lesson.visual)
                Text("Граница: ${lesson.limitation}", color = MaterialTheme.colorScheme.secondary)
                LabelBadge(lesson.label)
            }
        }
        item {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { navigate(Routes.HANDSHAKE) }) { Text("Handshake") }
                OutlinedButton(onClick = { navigate(Routes.THREATS) }) { Text("Threat model") }
            }
        }
        item { Text("Словарь", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) }
        items(LearningContent.glossary.entries.toList()) { entry ->
            SectionCard(entry.key, entry.value)
        }
    }
}

@Composable
private fun HandshakeScreen() {
    var index by rememberSaveable { mutableIntStateOf(0) }
    val step = LearningContent.handshake[index]
    itemsList {
        item { Intro("Что происходит до HTTPS", "Шаг ${index + 1} из ${LearningContent.handshake.size}. Нажимайте дальше и следите, когда появляются identity, ключи и шифрование.") }
        item {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                LearningContent.handshake.forEachIndexed { position, item ->
                    FilterChip(selected = position == index, onClick = { index = position }, label = { Text("${position + 1}. ${item.title}") })
                }
            }
        }
        item {
            SectionCard(step.title, "${step.from}  →  ${step.to}") {
                KeyValue("Сообщение", step.message)
                KeyValue("Что достигнуто", step.achieved)
                KeyValue("Что видит наблюдатель", step.observerSees)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = { index = (index - 1).coerceAtLeast(0) }, enabled = index > 0) { Text("Назад") }
                    Button(onClick = { index = (index + 1).coerceAtMost(LearningContent.handshake.lastIndex) }, enabled = index < LearningContent.handshake.lastIndex) { Text("Дальше") }
                }
            }
        }
        item {
            SectionCard("Ключевой вывод", "HTTP-данные отправляются только после создания аутентифицированного защищённого канала. TLS скрывает содержимое, но не весь сетевой metadata.")
        }
    }
}

@Composable
private fun ThreatScreen() {
    itemsList {
        item { Intro("Угроза → контроль → ограничение", "Сильная архитектура начинается с threat model, а не с выбора самой сложной технологии.") }
        items(LearningContent.threats) { threat ->
            SectionCard(threat.threat, threat.control) {
                Text(threat.explanation)
                Text("Ограничение: ${threat.limitation}", color = MaterialTheme.colorScheme.secondary)
            }
        }
        item {
            SectionCard("Почему trust-all опасен", "Он создаёт шифрование до злоумышленника и отключает аутентификацию сервера.", danger = true) {
                LabelBadge(ContentLabel.UNSAFE)
                CodeBlock(LearningContent.unsafeTrustAll)
                Text("Этот код показан только для распознавания уязвимости и нигде в приложении не исполняется.")
            }
        }
        item { SectionCard("SSL vs TLS", LearningContent.glossary.getValue("SSL") + " " + LearningContent.glossary.getValue("TLS")) }
    }
}

@Composable
private fun LabScreen(vm: LabViewModel = viewModel()) {
    val state by vm.state.collectAsState()
    itemsList {
        item {
            Intro("Живой безопасный эксперимент", "Inspector сохраняет системную проверку сертификата и hostname. Cleartext HTTP и trust-all здесь невозможно запустить.")
            LabelBadge(ContentLabel.RUNNABLE)
        }
        item {
            OutlinedTextField(
                value = state.url,
                onValueChange = vm::setUrl,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("HTTPS URL") },
                singleLine = true
            )
            Spacer(Modifier.height(8.dp))
            Button(onClick = vm::inspect, enabled = !state.loading) { Text("Проверить TLS") }
        }
        if (state.loading) item { Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator() } }
        state.failure?.let { failure ->
            item { SectionCard(failure.title, failure.detail, danger = true) }
        }
        state.result?.let { result ->
            item {
                SectionCard("${result.host} · HTTP ${result.statusCode}", "Hostname verification и системная цепочка доверия пройдены.") {
                    KeyValue("TLS version", result.tlsVersion)
                    KeyValue("Cipher suite", result.cipherSuite)
                    KeyValue("Peer chain", "${result.certificates.size} certificate(s)")
                }
            }
            item {
                SectionCard("Certificate pinning", "Сравните дополнительное ограничение доверия с ожидаемым отказом.") {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = { vm.verifyPin(true) }, enabled = !state.loading) { Text("Matching pin") }
                        OutlinedButton(onClick = { vm.verifyPin(false) }, enabled = !state.loading) { Text("Wrong pin") }
                    }
                    state.pinMessage?.let { Text(it, fontWeight = FontWeight.Bold) }
                    Text("Production: используйте backup pins, overlap ротации и план восстановления.", color = MaterialTheme.colorScheme.secondary)
                }
            }
            items(result.certificates) { certificate -> CertificateCard(certificate) }
        }
    }
}

@Composable
private fun CertificateCard(certificate: CertificateInfo) {
    var expanded by remember { mutableStateOf(false) }
    SectionCard(certificate.subject.substringBefore(","), "${certificate.validity} · ${certificate.publicKeyAlgorithm}") {
        KeyValue("Issuer", certificate.issuer)
        KeyValue("Valid until", certificate.validUntil)
        TextButton(onClick = { expanded = !expanded }) { Text(if (expanded) "Скрыть детали" else "Показать детали") }
        if (expanded) {
            KeyValue("Valid from", certificate.validFrom)
            KeyValue("Signature", certificate.signatureAlgorithm)
            KeyValue("Serial", certificate.serialNumber)
            KeyValue("SHA-256", certificate.fingerprintSha256)
            KeyValue("Public-key pin", certificate.publicKeyPin)
        }
    }
}

@Composable
private fun ImplementationScreen() {
    itemsList {
        item { Intro("Четыре модели доверия", "Выбирайте контроль по identity, threat model и способности бизнеса безопасно его эксплуатировать.") }
        item {
            ImplementationCard(
                "System trust · публичные API",
                ContentLabel.RUNNABLE,
                "Android проверяет публичную цепочку CA и hostname. Это разумный default для большинства публичных API.",
                """val client = OkHttpClient()
val request = Request.Builder()
    .url("https://api.company.com")
    .build()
client.newCall(request).execute()"""
            )
        }
        item {
            ImplementationCard(
                "Certificate pinning · дополнительное ограничение",
                ContentLabel.RUNNABLE,
                "Принимается только ожидаемый public key. Требуются backup pins и согласованная ротация.",
                """val pinner = CertificatePinner.Builder()
    .add("api.company.com", "sha256/<SPKI pin>")
    .build()
val client = OkHttpClient.Builder()
    .certificatePinner(pinner)
    .build()"""
            )
        }
        item {
            ImplementationCard(
                "Restricted custom CA · внутреннее доверие",
                ContentLabel.PRODUCTION,
                "Создайте отдельный KeyStore только с нужным private CA. Не добавляйте trust-all TrustManager.",
                """val store = KeyStore.getInstance(KeyStore.getDefaultType())
store.load(null)
store.setCertificateEntry("company-ca", companyCa)
val tmf = TrustManagerFactory.getInstance(
    TrustManagerFactory.getDefaultAlgorithm()
)
tmf.init(store)
val context = SSLContext.getInstance("TLS")
context.init(null, tmf.trustManagers, null)"""
            )
        }
        item {
            ImplementationCard(
                "mTLS · identity клиента",
                ContentLabel.PRODUCTION,
                "Клиентский приватный ключ должен поступать из Android KeyStore или управляемого хранилища, а не из Git.",
                """val kmf = KeyManagerFactory.getInstance(
    KeyManagerFactory.getDefaultAlgorithm()
)
kmf.init(clientKeyStore, keyPassword)
val context = SSLContext.getInstance("TLS")
context.init(kmf.keyManagers, tmf.trustManagers, null)
// Pass context.socketFactory + X509TrustManager to OkHttp"""
            )
        }
        item { SafetyNotice() }
    }
}

@Composable
private fun EnterpriseScreen() {
    var publicApi by rememberSaveable { mutableStateOf(true) }
    var privateServices by rememberSaveable { mutableStateOf(false) }
    var managedDevices by rememberSaveable { mutableStateOf(false) }
    var clientIdentity by rememberSaveable { mutableStateOf(false) }
    itemsList {
        item { Intro("TLS в большом бизнесе", "Сам протокол стандартный. Сложность масштаба — ownership, identities, автоматическая ротация, наблюдаемость и восстановление.") }
        item {
            SectionCard("Control selector", "Выберите ограничения системы — рекомендации обновятся.") {
                Toggle("Публичный API", publicApi) { publicApi = it }
                Toggle("Внутренние сервисы", privateServices) { privateServices = it }
                Toggle("Управляемые устройства", managedDevices) { managedDevices = it }
                Toggle("Сильная identity клиента", clientIdentity) { clientIdentity = it }
                HorizontalDivider()
                EnterpriseContent.recommend(publicApi, privateServices, managedDevices, clientIdentity).forEach { Text("• $it") }
            }
        }
        item { Text("Архитектурные сценарии", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) }
        items(EnterpriseContent.scenarios) { scenario ->
            SectionCard(scenario.title, scenario.path.joinToString("  →  ")) {
                KeyValue("Identity", scenario.identity)
                KeyValue("Trust boundary", scenario.trustBoundary)
                KeyValue("Controls", scenario.controls)
                KeyValue("Owner", scenario.owner)
                KeyValue("Зачем", scenario.reason)
            }
        }
        item { Text("Жизненный цикл сертификата", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) }
        items(EnterpriseContent.lifecycle) { stage ->
            SectionCard(stage.title, stage.owner) { Text(stage.practice) }
        }
        item {
            SectionCard("Наблюдаемость TLS", "Что измеряет зрелая платформа") {
                EnterpriseContent.observability.forEach { Text("• $it") }
            }
        }
        item {
            SectionCard("Инцидент: сертификат истёк", "План реакции и предотвращения", danger = true) {
                EnterpriseContent.incident.forEach { Text("• $it") }
            }
        }
    }
}

@Composable
private fun Page(title: String, back: () -> Unit, content: @Composable () -> Unit) {
    Scaffold(
        topBar = {
            Surface(shadowElevation = 2.dp) {
                Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                    TextButton(onClick = { back() }) { Text("← Назад") }
                    Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                }
            }
        }
    ) { padding -> Box(Modifier.padding(padding)) { content() } }
}

@Composable
private fun itemsList(content: androidx.compose.foundation.lazy.LazyListScope.() -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        content = content
    )
}

@Composable
private fun Intro(title: String, body: String) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black)
        Text(body, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
private fun SectionCard(
    title: String,
    subtitle: String,
    danger: Boolean = false,
    content: @Composable ColumnScope.() -> Unit = {}
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (danger) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.surface
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(subtitle, color = if (danger) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.secondary)
            content()
        }
    }
}

@Composable
private fun ImplementationCard(title: String, label: ContentLabel, explanation: String, code: String) {
    SectionCard(title, explanation) {
        LabelBadge(label)
        CodeBlock(code)
    }
}

@Composable
private fun CodeBlock(code: String) {
    SelectionContainer {
        Text(
            code.trimIndent(),
            modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(10.dp)).padding(12.dp),
            fontFamily = FontFamily.Monospace,
            fontSize = 12.sp
        )
    }
}

@Composable
private fun LabelBadge(label: ContentLabel) {
    val color = when (label) {
        ContentLabel.RUNNABLE -> Color(0xFF087F5B)
        ContentLabel.CONCEPTUAL -> MaterialTheme.colorScheme.primary
        ContentLabel.PRODUCTION -> Color(0xFF6C4AB6)
        ContentLabel.UNSAFE -> MaterialTheme.colorScheme.error
    }
    Text(
        label.title,
        color = Color.White,
        modifier = Modifier.background(color, RoundedCornerShape(100.dp)).padding(horizontal = 10.dp, vertical = 4.dp),
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold
    )
}

@Composable
private fun KeyValue(key: String, value: String) {
    Column {
        Text(key.uppercase(), color = MaterialTheme.colorScheme.primary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        Text(value)
    }
}

@Composable
private fun Toggle(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(label, modifier = Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun SafetyNotice() {
    SectionCard("Граница безопасности", "Лаборатория никогда не отключает проверку сертификата или имени хоста.") {
        Text("Custom CA и mTLS показаны как production walkthrough: приватные ключи должны поступать из управляемого хранилища. Trust-all показан только как уязвимость.")
        LabelBadge(ContentLabel.PRODUCTION)
    }
}
