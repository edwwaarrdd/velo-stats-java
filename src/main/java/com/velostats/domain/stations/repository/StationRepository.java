package com.velostats.domain.stations.repository;

import com.velostats.domain.stations.entity.Station;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

/**
 * Every query against stations lives here.
 */
public interface StationRepository extends JpaRepository<Station, String> {

    /**
     * Every station, hydrated, ordered the way the operator's feed and the other backends present
     * them, so the list endpoint is stable between runs.
     */
    @Query("SELECT s FROM Station s ORDER BY s.stationId")
    List<Station> findAllForApi();
}
