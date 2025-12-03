package nl.hva.ewa.regenwormen.repository;

import nl.hva.ewa.regenwormen.domain.errorForm.ErrorReport;
import nl.hva.ewa.regenwormen.domain.Enum.ErrorStatus;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.stream.Collectors;

@Primary
@Repository
public class ErrorReportMockRepository implements ErrorReportRepository {

    private final Map<String, ErrorReport> errorReports = new HashMap<>();

    public ErrorReportMockRepository() {
        // Initialize with some mock data for testing
        ErrorReport mockReport1 = new ErrorReport(
                "Game Crash",
                "Game crashed when rolling dice",
                "functionality_issue",
                "high",
                "The game froze and I couldn't continue playing.",
                "{\"gameId\": \"abc123\", \"players\": 2}"
        );
        mockReport1.setUserId("user1");
        mockReport1.setGameId("game1");
        mockReport1.setStatus(ErrorStatus.NEW);

        ErrorReport mockReport2 = new ErrorReport(
                "UI Bug",
                "Tiles not displaying correctly",
                "ui_bug",
                "medium",
                "Some tiles appear off-screen on mobile.",
                "{\"gameId\": \"xyz789\", \"players\": 3}"
        );
        mockReport2.setUserId("user2");
        mockReport2.setGameId("game2");
        mockReport2.setStatus(ErrorStatus.IN_PROGRESS);

        errorReports.put(mockReport1.getId(), mockReport1);
        errorReports.put(mockReport2.getId(), mockReport2);
    }

    @Override
    public ErrorReport save(ErrorReport errorReport) {
        errorReports.put(errorReport.getId(), errorReport);
        return errorReport;
    }

    @Override
    public Optional<ErrorReport> findById(String id) {
        return Optional.ofNullable(errorReports.get(id));
    }

    @Override
    public List<ErrorReport> findAll() {
        return new ArrayList<>(errorReports.values());
    }

    @Override
    public List<ErrorReport> findByUserId(String userId) {
        return errorReports.values().stream()
                .filter(report -> userId.equals(report.getUserId()))
                .collect(Collectors.toList());
    }

    @Override
    public List<ErrorReport> findByGameId(String gameId) {
        return errorReports.values().stream()
                .filter(report -> gameId.equals(report.getGameId()))
                .collect(Collectors.toList());
    }

    @Override
    public List<ErrorReport> findByStatus(ErrorStatus status) {
        return errorReports.values().stream()
                .filter(report -> status.equals(report.getStatus()))
                .collect(Collectors.toList());
    }

    @Override
    public List<ErrorReport> findByGameIdAndStatus(String gameId, ErrorStatus status) {
        return errorReports.values().stream()
                .filter(report -> gameId.equals(report.getGameId()) && status.equals(report.getStatus()))
                .collect(Collectors.toList());
    }

    @Override
    public void deleteById(String id) {
        errorReports.remove(id);
    }

    @Override
    public boolean existsById(String id) {
        return errorReports.containsKey(id);
    }
}

