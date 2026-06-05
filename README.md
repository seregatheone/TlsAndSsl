# TLS Lab for Android

Наглядный Jetpack Compose-проект о TLS: что протокол защищает, как Android проверяет сервер и как эти механизмы эксплуатируют в большом бизнесе.

## Что можно увидеть и запустить

1. **Учебный маршрут** — угрозы, сертификаты, ключи, trust store, pinning и mTLS.
2. **TLS 1.3 handshake** — пошагово показывает negotiation, server identity, key agreement и encrypted application data.
3. **HTTPS Inspector** — выполняет реальный безопасный запрос и показывает TLS version, cipher suite и X.509 chain.
4. **Pinning lab** — сравнивает matching pin с ожидаемым отказом для wrong pin.
5. **Реализации** — concrete walkthroughs для system trust, restricted custom CA и mTLS.
6. **Enterprise playbook** — edge TLS termination, service-to-service mTLS, zero trust, certificate lifecycle, observability и incident response.

## Зачем это бизнесу

TLS защищает токены, персональные и платёжные данные в недоверенной сети. В крупной системе основной вызов не в вызове `SSLContext`, а в управлении тысячами identities и сертификатов:

- кто владеет доменом и сертификатом;
- где хранится приватный ключ;
- как сертификат выпускается и автоматически ротируется;
- как избежать downtime при ротации и pinning;
- как обнаружить expiry, handshake failures и неизвестный сертификат;
- как быстро восстановиться после компрометации.

Публичные mobile API обычно начинают с system trust. Private CA применяют для внутренних identities. mTLS нужен, когда сервер обязан криптографически проверить клиента или workload. Pinning используют только при подтверждённой threat model и готовой процедуре ротации.

## Границы безопасности

- Приложение принимает только HTTPS URL.
- Проверка цепочки сертификатов и hostname никогда не отключается.
- Trust-all показан только как неисполняемый anti-pattern.
- Private CA и mTLS не содержат встроенных приватных ключей: production credentials должны поступать из Android KeyStore, KMS/HSM или управляемого provisioning.
- Реальный network smoke test отключён по умолчанию, чтобы обычные тесты не зависели от интернета.

## Запуск и проверка

```bash
./gradlew :app:assembleDebug
./gradlew :app:testDebugUnitTest
./gradlew :app:lintDebug
```

Compose/instrumentation tests:

```bash
./gradlew :app:connectedDebugAndroidTest
```

Opt-in real HTTPS smoke test передаёт instrumentation argument `runTlsSmoke=true`; используйте его только на устройстве с доступом в интернет.
