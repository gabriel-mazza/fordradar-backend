package br.com.fiap.fordradar.models;

import br.com.fiap.fordradar.security.CryptoConverter;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "customer_predictions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
@EntityListeners(AuditingEntityListener.class)
public class CustomerPrediction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 17)
    private String vin;

    @Convert(converter = CryptoConverter.class)
    @Column(name = "customer_name", length = 600)
    private String customerName;

    @Convert(converter = CryptoConverter.class)
    @Column(name = "customer_email", length = 600)
    private String customerEmail;

    @Convert(converter = CryptoConverter.class)
    @Column(length = 600)
    private String phone;

    @Column(name = "retention_score", precision = 5, scale = 2)
    private BigDecimal retentionScore;

    @CreatedDate
    @Column(name = "prediction_date", updatable = false)
    private LocalDateTime predictionDate;
}

