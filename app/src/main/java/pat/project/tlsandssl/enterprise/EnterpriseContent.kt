package pat.project.tlsandssl.enterprise

import pat.project.tlsandssl.core.EnterpriseScenario
import pat.project.tlsandssl.core.LifecycleStage

object EnterpriseContent {
    val scenarios = listOf(
        EnterpriseScenario(
            "Mobile → Public API",
            listOf("Android app", "Internet", "API Gateway", "Backend"),
            "Сервер: публичный DNS; пользователь: OAuth/OIDC поверх TLS",
            "Недоверенная публичная сеть заканчивается на gateway",
            "System trust, TLS 1.2+, HSTS на web; pinning только по threat model",
            "Mobile + Platform + Security",
            "Защищает токены и персональные данные в пути."
        ),
        EnterpriseScenario(
            "Internet edge termination",
            listOf("Customer", "CDN/WAF", "Load Balancer", "Service"),
            "Публичный домен компании",
            "TLS завершается и часто начинается снова на каждом управляемом участке",
            "Автовыдача сертификатов, modern ciphers, re-encryption",
            "Edge / Platform team",
            "Централизует защиту, DDoS/WAF и управление сертификатами."
        ),
        EnterpriseScenario(
            "Service-to-service mTLS",
            listOf("Service A", "Service Mesh", "Service B"),
            "Workload identity каждого сервиса",
            "Каждый workload считается недоверенным до проверки",
            "Private CA, short-lived certificates, mTLS policy",
            "Platform / SRE",
            "Поддерживает zero trust и ограничивает lateral movement."
        ),
        EnterpriseScenario(
            "Managed devices + private API",
            listOf("Managed Android", "Corporate network", "Private API"),
            "Устройство или приложение с управляемым сертификатом",
            "Доступ разрешён только управляемым клиентам",
            "Private CA, device policy, иногда mTLS",
            "Endpoint + IAM + Security",
            "Добавляет сильный сигнал доверия устройства."
        ),
        EnterpriseScenario(
            "Regulated data path",
            listOf("Client", "Gateway", "Payment/Health service", "Audit"),
            "Сервисные и пользовательские identities",
            "Каждый переход данных документирован",
            "TLS, mTLS, key custody, audit evidence, rotation SLA",
            "Security + Compliance + Service owners",
            "Даёт проверяемые гарантии и доказательства для аудита."
        )
    )

    val lifecycle = listOf(
        LifecycleStage("Выдача", "Security / PKI", "Проверить identity, policy, SAN и допустимый срок."),
        LifecycleStage("Хранение", "Service / Platform", "Приватные ключи держать в KMS/HSM/KeyStore, не в Git."),
        LifecycleStage("Доставка", "Platform / CI-CD", "Автоматизировать deployment без раскрытия ключа людям."),
        LifecycleStage("Ротация", "Service owner + SRE", "Использовать overlap, backup pins, canary и rollback."),
        LifecycleStage("Отзыв", "Security / PKI", "Отозвать скомпрометированный identity и заменить ключ."),
        LifecycleStage("Наблюдаемость", "SRE / Security", "Алерты срока, handshake failures, inventory и CT monitoring.")
    )

    val observability = listOf(
        "Срок сертификата: алерты за 30/14/7 дней",
        "Handshake failures по сервису, версии клиента и причине",
        "Инвентаризация TLS-версий, cipher suites и владельцев",
        "Certificate Transparency monitoring для публичных доменов",
        "Аудит автоматической ротации и проверка rollback"
    )

    val incident = listOf(
        "Detect: алерт срока или всплеск handshake failures",
        "Contain: определить домены, клиентов и blast radius",
        "Replace: выпустить и развернуть сертификат с overlap",
        "Validate: проверить цепочку, SNI, клиенты и метрики",
        "Prevent: исправить ownership, автоматизацию и алерты"
    )

    fun recommend(publicApi: Boolean, privateServices: Boolean, managedDevices: Boolean, clientIdentity: Boolean): List<String> =
        buildList {
            if (publicApi) add("System trust: базовый выбор для публичного API.")
            if (privateServices) add("Private CA: управляемое доверие для внутренних identities.")
            if (managedDevices) add("Managed trust policy: контроль CA и ключей на корпоративных устройствах.")
            if (clientIdentity) add("mTLS: сильная identity клиента, требующая полного lifecycle.")
            if (publicApi && !clientIdentity) add("Pinning: только при подтверждённой угрозе и готовой ротации.")
        }
}
