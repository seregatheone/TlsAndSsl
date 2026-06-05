package pat.project.tlsandssl.core

enum class ContentLabel(val title: String) {
    RUNNABLE("Runnable lab"),
    CONCEPTUAL("Conceptual"),
    PRODUCTION("Production guidance"),
    UNSAFE("Небезопасно: только разбор")
}

data class Lesson(
    val id: String,
    val order: Int,
    val title: String,
    val summary: String,
    val why: String,
    val visual: String,
    val limitation: String,
    val label: ContentLabel = ContentLabel.CONCEPTUAL
)

data class HandshakeStep(
    val title: String,
    val from: String,
    val to: String,
    val message: String,
    val achieved: String,
    val observerSees: String
)

data class ThreatControl(
    val threat: String,
    val control: String,
    val explanation: String,
    val limitation: String
)

data class EnterpriseScenario(
    val title: String,
    val path: List<String>,
    val identity: String,
    val trustBoundary: String,
    val controls: String,
    val owner: String,
    val reason: String
)

data class LifecycleStage(
    val title: String,
    val owner: String,
    val practice: String
)
