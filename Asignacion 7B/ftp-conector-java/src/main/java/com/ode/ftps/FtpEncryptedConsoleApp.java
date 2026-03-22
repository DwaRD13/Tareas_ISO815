package com.ode.ftps;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FtpEncryptedConsoleApp {
    private static final String DIVIDER = "============================================================";
    private static final String SUBDIVIDER = "------------------------------------------------------------";

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in, StandardCharsets.UTF_8);
        printHeader("FTP Encrypted Console");

        while (true) {
            imprimirMenu();
            String opcion = leerConDefault(scanner, "Selecciona opcion", "1");

            try {
                switch (opcion.trim()) {
                    case "1" -> {
                        enviarArchivo(scanner);
                        esperarEnterYLimpiar(scanner);
                    }
                    case "2" -> {
                        recibirYDesencriptar(scanner);
                        esperarEnterYLimpiar(scanner);
                    }
                    case "3" -> {
                        verArchivoLocal(scanner);
                        esperarEnterYLimpiar(scanner);
                    }
                    case "4" -> {
                        printInfo("Saliendo...");
                        return;
                    }
                    default -> printInfo("Opcion invalida");
                }
            } catch (Exception ex) {
                printError("Operacion fallida", ex.getMessage());
                esperarEnterYLimpiar(scanner);
            }
        }
    }

    private static void imprimirMenu() {
        System.out.println();
        System.out.println(DIVIDER);
        System.out.println(" MENU PRINCIPAL");
        System.out.println(SUBDIVIDER);
        System.out.println("  1) Enviar archivo encriptado por FTP");
        System.out.println("  2) Recibir, desencriptar y guardar TXT");
        System.out.println("  3) Ver TXT local");
        System.out.println("  4) Salir");
        System.out.println(DIVIDER);
    }

    private static void enviarArchivo(Scanner scanner) throws Exception {
        printHeader("Enviar Archivo");
        String origen = leerConDefault(scanner, "Fuente [1=demo, 2=archivo local]", "1");

        String txtContent;
        if ("2".equals(origen.trim())) {
            String localPath = leerConDefault(scanner, "Ruta del TXT local", "nomina.txt");
            txtContent = Files.readString(Path.of(localPath), StandardCharsets.UTF_8);
        } else {
            txtContent = String.join("\n",
                "001|402-0000000-1|Juan Perez|1500.0|Humano",
                "002|001-0000000-2|Ana Gomez|2200.5|Humano",
                "003|031-0000000-3|Luis Marte|1800.75|Humano"
            );
        }

        String defaultBaseName = "NOMINA_UNIPAGO_" + LocalDate.now() + ".enc";
        String fileNameBase = leerConDefault(scanner, "Nombre base remoto", defaultBaseName);

        String passphrase = obtenerPassphrase();
        byte[] encryptedPayload = CryptoChunkCodec.construirPayloadEncriptado(txtContent, passphrase);

        FTPClient client = connectFtp();
        try {
            List<String> partFiles = uploadEnPartes(client, encryptedPayload, fileNameBase);
            String manifest = buildManifestJson(partFiles, encryptedPayload.length);
            String manifestName = fileNameBase + ".manifest.json";
            uploadBytes(client, manifest.getBytes(StandardCharsets.UTF_8), manifestName);

            printInfo("Enviado OK");
            printKeyValue("Partes", String.valueOf(partFiles.size()));
            printKeyValue("Manifest", manifestName);
        } finally {
            cerrarFtp(client);
        }
    }

    private static void recibirYDesencriptar(Scanner scanner) throws Exception {
        printHeader("Recibir y Desencriptar");
        String manifestRemote = leerConDefault(scanner, "Manifest remoto", "NOMINA_UNIPAGO_" + LocalDate.now() + ".enc.manifest.json");
        String downloadDirText = leerConDefault(scanner, "Directorio local de descarga", "received");
        String outputFileName = leerConDefault(scanner, "Nombre del TXT de salida", "nomina_recibida.txt");

        Path downloadDir = Path.of(downloadDirText);
        Files.createDirectories(downloadDir);

        ManifestData manifestData;
        byte[] payload;

        FTPClient client = connectFtp();
        try {
            byte[] manifestBytes = downloadBytes(client, manifestRemote);
            Path manifestLocalPath = downloadDir.resolve(manifestRemote);
            Files.write(manifestLocalPath, manifestBytes);

            manifestData = parseManifest(new String(manifestBytes, StandardCharsets.UTF_8));
            payload = downloadAndConcatParts(client, manifestData.files, downloadDir);
        } finally {
            cerrarFtp(client);
        }

        if (manifestData.totalEncryptedBytes != null && manifestData.totalEncryptedBytes != payload.length) {
            throw new IllegalStateException(
                "Tamaño reconstruido no coincide. esperado=" + manifestData.totalEncryptedBytes + " actual=" + payload.length
            );
        }

        String passphrase = obtenerPassphrase();
        String txt = CryptoChunkCodec.desencriptarPayload(payload, passphrase);

        Path outputPath = downloadDir.resolve(outputFileName);
        Files.writeString(outputPath, txt, StandardCharsets.UTF_8);

        printInfo("TXT recuperado");
        printKeyValue("Ruta", outputPath.toAbsolutePath().toString());
        printHeader("Contenido");
        mostrarContenidoFormateado(txt);
    }

    private static void verArchivoLocal(Scanner scanner) throws Exception {
        printHeader("Ver TXT Local");
        String pathText = leerConDefault(scanner, "Ruta del TXT", "received/nomina_recibida.txt");
        Path path = Path.of(pathText);
        String content = Files.readString(path, StandardCharsets.UTF_8);

        printKeyValue("Archivo", path.toAbsolutePath().toString());
        printHeader("Contenido");
        mostrarContenidoFormateado(content);
    }

    private static FTPClient connectFtp() throws Exception {
        String host = getEnv("FTP_HOST", "127.0.0.1");
        int port = Integer.parseInt(getEnv("FTP_PORT", "21"));
        String user = getEnv("FTP_USER", "user");
        String pass = getEnv("FTP_PASSWORD", "DaMrLicey#13");

        FTPClient client = new FTPClient();
        client.setConnectTimeout(10_000);
        client.setDefaultTimeout(10_000);
        client.connect(host, port);

        int replyCode = client.getReplyCode();
        if (!FTPReply.isPositiveCompletion(replyCode)) {
            throw new IllegalStateException("No se pudo conectar al servidor FTP. Codigo=" + replyCode);
        }

        if (!client.login(user, pass)) {
            throw new IllegalStateException("Credenciales FTP invalidas");
        }

        client.enterLocalPassiveMode();
        client.setFileType(FTP.BINARY_FILE_TYPE);
        return client;
    }

    private static void cerrarFtp(FTPClient client) {
        if (client == null) {
            return;
        }

        try {
            if (client.isConnected()) {
                client.logout();
            }
        } catch (Exception ignore) {
        }

        try {
            if (client.isConnected()) {
                client.disconnect();
            }
        } catch (Exception ignore) {
        }
    }

    private static List<String> uploadEnPartes(FTPClient client, byte[] encryptedBuffer, String fileNameBase)
        throws Exception {
        int totalChunks = (int) Math.ceil((double) encryptedBuffer.length / CryptoChunkCodec.CHUNK_SIZE_BYTES);
        List<String> partFiles = new ArrayList<>();

        for (int i = 0; i < totalChunks; i++) {
            int start = i * CryptoChunkCodec.CHUNK_SIZE_BYTES;
            int end = Math.min(start + CryptoChunkCodec.CHUNK_SIZE_BYTES, encryptedBuffer.length);
            int len = end - start;

            byte[] chunk = new byte[len];
            System.arraycopy(encryptedBuffer, start, chunk, 0, len);

            String partName = String.format("%s.part%03d", fileNameBase, i + 1);
            uploadBytes(client, chunk, partName);
            partFiles.add(partName);
        }

        return partFiles;
    }

    private static byte[] downloadAndConcatParts(FTPClient client, List<String> files, Path downloadDir) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        for (String file : files) {
            byte[] bytes = downloadBytes(client, file);
            Files.write(downloadDir.resolve(file), bytes);
            out.write(bytes);
        }

        return out.toByteArray();
    }

    private static void uploadBytes(FTPClient client, byte[] data, String remoteFile) throws Exception {
        try (ByteArrayInputStream in = new ByteArrayInputStream(data)) {
            boolean ok = client.storeFile(remoteFile, in);
            if (!ok) {
                throw new IllegalStateException("No se pudo subir el archivo " + remoteFile
                    + ". Reply=" + client.getReplyString());
            }
        }
    }

    private static byte[] downloadBytes(FTPClient client, String remoteFile) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        boolean ok = client.retrieveFile(remoteFile, out);
        if (!ok) {
            throw new IllegalStateException("No se pudo descargar " + remoteFile + ". Reply=" + client.getReplyString());
        }
        return out.toByteArray();
    }

    private static ManifestData parseManifest(String json) {
        Pattern totalBytesPattern = Pattern.compile("\\\"totalEncryptedBytes\\\"\\s*:\\s*(\\d+)");
        Matcher totalBytesMatcher = totalBytesPattern.matcher(json);
        Integer totalBytes = null;
        if (totalBytesMatcher.find()) {
            totalBytes = Integer.parseInt(totalBytesMatcher.group(1));
        }

        Pattern filePattern = Pattern.compile("\\\"([^\\\"]+\\.part\\d{3})\\\"");
        Matcher fileMatcher = filePattern.matcher(json);
        List<String> files = new ArrayList<>();
        while (fileMatcher.find()) {
            files.add(fileMatcher.group(1));
        }

        if (files.isEmpty()) {
            throw new IllegalArgumentException("Manifest sin archivos de partes");
        }

        return new ManifestData(files, totalBytes);
    }

    private static String buildManifestJson(List<String> files, int totalBytes) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        sb.append("  \"version\": 1,\n");
        sb.append("  \"chunkSizeBytes\": ").append(CryptoChunkCodec.CHUNK_SIZE_BYTES).append(",\n");
        sb.append("  \"totalChunks\": ").append(files.size()).append(",\n");
        sb.append("  \"totalEncryptedBytes\": ").append(totalBytes).append(",\n");
        sb.append("  \"algorithm\": \"").append(CryptoChunkCodec.ENCRYPTION_ALGORITHM_LABEL).append("\",\n");
        sb.append("  \"files\": [\n");

        for (int i = 0; i < files.size(); i++) {
            sb.append("    \"").append(files.get(i)).append("\"");
            if (i < files.size() - 1) {
                sb.append(',');
            }
            sb.append("\n");
        }

        sb.append("  ]\n");
        sb.append("}\n");
        return sb.toString();
    }

    private static String leerConDefault(Scanner scanner, String label, String defaultValue) {
        System.out.print("\n> " + label + " [" + defaultValue + "]: ");
        String line = scanner.nextLine();
        if (line == null || line.isBlank()) {
            return defaultValue;
        }
        return line.trim();
    }

    private static String obtenerPassphrase() {
        String envPassphrase = System.getenv("FILE_ENCRYPTION_PASSPHRASE");
        if (envPassphrase != null && !envPassphrase.trim().isEmpty()) {
            return envPassphrase.trim();
        }

        String devFallback = "dev-local-only-change-this-passphrase-2026";
        printInfo("[WARN] FILE_ENCRYPTION_PASSPHRASE no definida. Usando clave de desarrollo temporal.");
        return devFallback;
    }

    private static String getEnv(String key, String defaultValue) {
        String value = System.getenv(key);
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        return value;
    }

    private static final class ManifestData {
        final List<String> files;
        final Integer totalEncryptedBytes;

        private ManifestData(List<String> files, Integer totalEncryptedBytes) {
            this.files = files;
            this.totalEncryptedBytes = totalEncryptedBytes;
        }
    }

    private static void mostrarContenidoFormateado(String txt) {
        String[] rows = txt.split("\\R");
        if (rows.length == 0) {
            printInfo("Sin contenido");
            return;
        }

        boolean formatoNomina = true;
        List<String[]> parsed = new ArrayList<>();
        for (String row : rows) {
            if (row == null || row.isBlank()) {
                continue;
            }

            String[] parts = row.split("\\|", -1);
            if (parts.length != 5) {
                formatoNomina = false;
                break;
            }
            parsed.add(parts);
        }

        if (!formatoNomina || parsed.isEmpty()) {
            System.out.println(SUBDIVIDER);
            for (String row : rows) {
                if (!row.isBlank()) {
                    System.out.println("  " + row);
                }
            }
            System.out.println(SUBDIVIDER);
            return;
        }

        System.out.println("+------+---------------+----------------------+----------+----------+");
        System.out.println("| ID   | CEDULA        | NOMBRE               | MONTO    | ARS      |");
        System.out.println("+------+---------------+----------------------+----------+----------+");
        for (String[] parts : parsed) {
            System.out.printf(
                "| %-4s | %-13s | %-20s | %8s | %-8s |%n",
                parts[0],
                parts[1],
                parts[2],
                parts[3],
                parts[4]
            );
        }
        System.out.println("+------+---------------+----------------------+----------+----------+");
    }

    private static void printHeader(String title) {
        System.out.println();
        System.out.println(DIVIDER);
        System.out.println(" " + title);
        System.out.println(DIVIDER);
    }

    private static void printInfo(String message) {
        System.out.println("[INFO] " + message);
    }

    private static void printError(String title, String detail) {
        System.err.println();
        System.err.println(DIVIDER);
        System.err.println(" ERROR: " + title);
        System.err.println(SUBDIVIDER);
        System.err.println(" " + detail);
        System.err.println(DIVIDER);
    }

    private static void printKeyValue(String key, String value) {
        System.out.printf("%-12s: %s%n", key, value);
    }

    private static void esperarEnterYLimpiar(Scanner scanner) {
        System.out.println();
        System.out.print("Presiona Enter para volver al menu...");
        scanner.nextLine();
        limpiarConsola();
    }

    private static void limpiarConsola() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }
}
