package br.com.doistech.apicondomanagersaas.service.storage;

import br.com.doistech.apicondomanagersaas.domain.aws.AwsCredential;
import br.com.doistech.apicondomanagersaas.service.AwsCredentialService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetUrlRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.IOException;
import java.net.URI;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;

@Service
public class S3StorageService {

    private final AwsCredentialService awsCredentialService;

    public S3StorageService(AwsCredentialService awsCredentialService) {
        this.awsCredentialService = awsCredentialService;
    }

    public StoredObject upload(
            MultipartFile file,
            String folder,
            String defaultBaseName,
            Set<String> allowedExtensions,
            long maxSize
    ) {
        validateFile(file, allowedExtensions, maxSize);

        String original = StringUtils.cleanPath(file.getOriginalFilename() == null ? defaultBaseName : file.getOriginalFilename());
        String ext = getExt(original);
        String safeBaseName = sanitizeBaseName(stripExt(original), defaultBaseName);
        String finalName = Instant.now().toEpochMilli()
                + "_" + UUID.randomUUID().toString().replace("-", "")
                + "_" + safeBaseName
                + "." + ext;
        String key = folder + "/" + finalName;
        String contentType = StringUtils.hasText(file.getContentType()) ? file.getContentType() : "application/octet-stream";
        AwsCredential config = awsCredentialService.getEntity();

        try (S3Client s3Client = buildClient(config)) {
            s3Client.putObject(
                PutObjectRequest.builder()
                        .bucket(config.getBucket())
                        .key(key)
                        .contentType(contentType)
                        .build(),
                RequestBody.fromBytes(file.getBytes())
            );

            return new StoredObject(
                    key,
                    resolveUrl(s3Client, config.getBucket(), key),
                    finalName,
                    contentType,
                    file.getSize()
            );
        } catch (IOException | S3Exception e) {
            throw new IllegalStateException("Falha ao enviar arquivo para o storage.", e);
        }
    }

    public void deleteByUrl(String url) {
        if (!StringUtils.hasText(url)) {
            return;
        }

        AwsCredential config = awsCredentialService.getEntity();
        String key = extractKey(url, config.getBucket());
        if (!StringUtils.hasText(key)) {
            return;
        }

        try (S3Client s3Client = buildClient(config)) {
            s3Client.deleteObject(DeleteObjectRequest.builder()
                    .bucket(config.getBucket())
                    .key(key)
                    .build());
        } catch (S3Exception ignored) {
            // Não bloqueia exclusão do registro se o arquivo já não existir ou houver falha no storage.
        }
    }

    private void validateFile(MultipartFile file, Set<String> allowedExtensions, long maxSize) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Arquivo obrigatório.");
        }

        if (file.getSize() > maxSize) {
            throw new IllegalArgumentException("Arquivo excede o limite permitido.");
        }

        String original = StringUtils.cleanPath(file.getOriginalFilename() == null ? "" : file.getOriginalFilename());
        String ext = getExt(original);
        if (!allowedExtensions.contains(ext)) {
            throw new IllegalArgumentException("Extensão não permitida: ." + ext);
        }
    }

    private String resolveUrl(S3Client s3Client, String bucket, String key) {
        return s3Client.utilities()
                .getUrl(GetUrlRequest.builder().bucket(bucket).key(key).build())
                .toExternalForm();
    }

    private String extractKey(String url, String bucket) {
        try {
            URI uri = URI.create(url);
            String path = uri.getPath();
            if (!StringUtils.hasText(path)) {
                return null;
            }

            String normalized = path.startsWith("/") ? path.substring(1) : path;
            String bucketPrefix = bucket + "/";
            if (normalized.startsWith(bucketPrefix)) {
                return normalized.substring(bucketPrefix.length());
            }
            return normalized;
        } catch (Exception ignored) {
            return null;
        }
    }

    private S3Client buildClient(AwsCredential config) {
        var builder = S3Client.builder()
                .region(Region.of(config.getRegion()))
                .credentialsProvider(
                        StaticCredentialsProvider.create(
                                AwsBasicCredentials.create(config.getAccessKey(), config.getSecretKey())
                        )
                )
                .serviceConfiguration(
                        S3Configuration.builder()
                                .pathStyleAccessEnabled(Boolean.TRUE.equals(config.getPathStyleAccess()))
                                .build()
                );

        if (StringUtils.hasText(config.getEndpoint())) {
            builder.endpointOverride(URI.create(config.getEndpoint()));
        }

        return builder.build();
    }

    private String getExt(String name) {
        int i = name.lastIndexOf('.');
        if (i < 0) {
            return "";
        }
        return name.substring(i + 1).toLowerCase();
    }

    private String stripExt(String name) {
        int i = name.lastIndexOf('.');
        if (i < 0) {
            return name;
        }
        return name.substring(0, i);
    }

    private String sanitizeBaseName(String base, String fallback) {
        String sanitized = base.replace("\\", "/");
        int slash = sanitized.lastIndexOf('/');
        if (slash >= 0) {
            sanitized = sanitized.substring(slash + 1);
        }

        sanitized = sanitized.replaceAll("[^a-zA-Z0-9._-]", "_");
        if (sanitized.length() > 80) {
            sanitized = sanitized.substring(0, 80);
        }
        if (sanitized.isBlank()) {
            sanitized = fallback;
        }
        return sanitized;
    }
}
