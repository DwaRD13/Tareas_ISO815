# FTP Conector Java

Consola interactiva para enviar y recibir archivos encriptados por FTP.

## Requisitos

- Java 17+
- Maven 3.9+

## Variables de entorno (opcionales)

- `FTP_HOST` (default: `127.0.0.1`)
- `FTP_PORT` (default: `21`)
- `FTP_USER` (default: `user`)
- `FTP_PASSWORD` (default: `12345`)
- `FILE_ENCRYPTION_PASSPHRASE` (si no existe, usa clave de desarrollo)

## Para correrlo

```bash
mvn -q -DskipTests package
mvn -q -DskipTests exec:java -Dexec.mainClass=com.ode.ftps.FtpEncryptedConsoleApp
```