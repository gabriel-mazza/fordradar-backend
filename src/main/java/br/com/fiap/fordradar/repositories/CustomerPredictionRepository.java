package br.com.fiap.fordradar.repositories;

import br.com.fiap.fordradar.models.CustomerPrediction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CustomerPredictionRepository extends JpaRepository<CustomerPrediction, Long> {
    Optional<CustomerPrediction> findByVin(String vin);
}

