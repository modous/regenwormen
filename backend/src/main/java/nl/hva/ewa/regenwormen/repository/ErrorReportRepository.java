package nl.hva.ewa.regenwormen.repository;

import nl.hva.ewa.regenwormen.domain.errorForm.ErrorReport;
import nl.hva.ewa.regenwormen.domain.Enum.ErrorStatus;

import java.util.List;
import java.util.Optional;

public interface ErrorReportRepository {
    ErrorReport save(ErrorReport errorReport);
    Optional<ErrorReport> findById(String id);
    List<ErrorReport> findAll();
    List<ErrorReport> findByUserId(String userId);
    List<ErrorReport> findByGameId(String gameId);
    List<ErrorReport> findByStatus(ErrorStatus status);
    List<ErrorReport> findByGameIdAndStatus(String gameId, ErrorStatus status);
    void deleteById(String id);
    boolean existsById(String id);
}

