package br.com.doistech.apicondomanagersaas.domain.aws;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "aws_credentials")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AwsCredential {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "access_key", nullable = false, length = 255)
    private String accessKey;

    @Column(name = "secret_key", nullable = false, length = 500)
    private String secretKey;

    @Column(nullable = false, length = 120)
    private String region;

    @Column(nullable = false, length = 255)
    private String bucket;

    @Column(length = 500)
    private String endpoint;

    @Column(name = "path_style_access", nullable = false)
    private Boolean pathStyleAccess;

    @Column(nullable = false)
    private Boolean ativo;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
