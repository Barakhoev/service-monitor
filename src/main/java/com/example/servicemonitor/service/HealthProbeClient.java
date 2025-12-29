package com.example.servicemonitor.service;

import com.example.servicemonitor.config.AppMonitoringProperties;
import com.example.servicemonitor.domain.ErrorType;
import com.example.servicemonitor.domain.ServiceStatus;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import javax.net.ssl.SSLException;
import java.io.IOException;
import java.net.ConnectException;
import java.net.URI;
import java.net.UnknownHostException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.time.Duration;

@Component
public class HealthProbeClient {

    private final ObjectMapper objectMapper;
    private final AppMonitoringProperties props;

    private final HttpClient httpClient;

    public record ProbeResult(
        ServiceStatus status,
        Integer httpStatus,
        ErrorType errorType,
        Integer latencyMs,
        String rawBody,
        String errorMessage
    ) {}

    public HealthProbeClient(ObjectMapper objectMapper, AppMonitoringProperties props) {
        this.objectMapper = objectMapper;
        this.props = props;
        this.httpClient = HttpClient.newBuilder()
            .connectTimeout(props.getConnectTimeout())
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();
    }

    public ProbeResult probe(String healthUrl) {
        long startNs = System.nanoTime();
        try {
            HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(healthUrl))
                .timeout(props.getRequestTimeout())
                .header("User-Agent", props.getUserAgent())
                .GET()
                .build();

            HttpResponse<byte[]> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofByteArray());
            int latencyMs = (int) Math.max(0, (System.nanoTime() - startNs) / 1_000_000);

            int code = resp.statusCode();
            byte[] bodyBytes = resp.body() == null ? new byte[0] : resp.body();
            String body = limitBody(bodyBytes, props.getMaxBodyBytes());

            if (code < 200 || code >= 300) {
                ErrorType et = (code >= 400 && code < 500) ? ErrorType.HTTP_4XX : ErrorType.HTTP_5XX;
                return new ProbeResult(ServiceStatus.DOWN, code, et, latencyMs, body, "HTTP " + code);
            }

            // 2xx: parse JSON and read root.status
            try {
                JsonNode root = objectMapper.readTree(bodyBytes);
                JsonNode statusNode = root.get("status");
                if (statusNode == null || statusNode.isNull()) {
                    return new ProbeResult(ServiceStatus.DOWN, code, ErrorType.MISSING_STATUS, latencyMs, body, "Missing field 'status'");
                }
                String s = statusNode.asText("");
                if ("UP".equalsIgnoreCase(s)) {
                    return new ProbeResult(ServiceStatus.UP, code, ErrorType.NONE, latencyMs, body, null);
                }
                if ("DOWN".equalsIgnoreCase(s)) {
                    return new ProbeResult(ServiceStatus.DOWN, code, ErrorType.NONE, latencyMs, body, null);
                }
                // Unknown status value -> treat as DOWN for statistics
                return new ProbeResult(ServiceStatus.DOWN, code, ErrorType.UNKNOWN_ERROR, latencyMs, body, "Unknown status value: " + s);
            } catch (IOException jsonEx) {
                return new ProbeResult(ServiceStatus.DOWN, code, ErrorType.INVALID_JSON, latencyMs, body, "Invalid JSON: " + jsonEx.getMessage());
            }

        } catch (Exception ex) {
            int latencyMs = (int) Math.max(0, (System.nanoTime() - startNs) / 1_000_000);
            ErrorType et = mapException(ex);
            return new ProbeResult(ServiceStatus.DOWN, null, et, latencyMs, null, ex.getClass().getSimpleName() + ": " + ex.getMessage());
        }
    }

    private String limitBody(byte[] bodyBytes, int maxBytes) {
        if (bodyBytes == null || bodyBytes.length == 0) return "";
        int len = Math.min(bodyBytes.length, Math.max(0, maxBytes));
        return new String(bodyBytes, 0, len);
    }

    private ErrorType mapException(Throwable ex) {
        Throwable t = ex;
        while (t.getCause() != null && t != t.getCause()) {
            // unwrap common wrappers
            if (t instanceof java.util.concurrent.CompletionException || t instanceof java.util.concurrent.ExecutionException) {
                t = t.getCause();
                continue;
            }
            break;
        }

        if (t instanceof HttpTimeoutException) return ErrorType.TIMEOUT;
        if (t instanceof UnknownHostException) return ErrorType.DNS_ERROR;
        if (t instanceof ConnectException) return ErrorType.CONNECTION_REFUSED;
        if (t instanceof SSLException) return ErrorType.SSL_ERROR;
        // java.net.http may throw IOException with message about timeout etc.
        if (t instanceof IOException io) {
            String msg = (io.getMessage() == null) ? "" : io.getMessage().toLowerCase();
            if (msg.contains("timed out") || msg.contains("timeout")) return ErrorType.TIMEOUT;
        }
        return ErrorType.UNKNOWN_ERROR;
    }
}
