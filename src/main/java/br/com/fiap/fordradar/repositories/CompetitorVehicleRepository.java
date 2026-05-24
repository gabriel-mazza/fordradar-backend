package br.com.fiap.fordradar.repositories;

import br.com.fiap.fordradar.models.CompetitorVehicle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CompetitorVehicleRepository extends JpaRepository<CompetitorVehicle, Long> {
    Optional<CompetitorVehicle> findByBrandIgnoreCaseAndModelIgnoreCaseAndVersionIgnoreCase(String brand, String model, String version);
}

