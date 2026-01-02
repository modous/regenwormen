package nl.hva.ewa.regenwormen.service;

import nl.hva.ewa.regenwormen.domain.Enum.ErrorStatus;
import nl.hva.ewa.regenwormen.domain.errorForm.ErrorReport;
import nl.hva.ewa.regenwormen.repository.ErrorReportRepository;
import nl.hva.ewa.regenwormen.repository.GameRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests voor ErrorReportService
 *
 * Test strategie: FIRST + RIGHT-BICEP
 * - Fast: Mocked repositories, geen database
 * - Isolated: Geen shared state tussen tests
 * - Repeatable: Deterministic mocking
 * - Self-checking: JUnit assertions
 * - Timely: Tests voor alle features
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ErrorReportService Tests")
class ErrorReportServiceTest {

    @Mock
    private ErrorReportRepository errorReportRepository;

    @Mock
    private GameRepository gameRepository;

    @InjectMocks
    private ErrorReportService errorReportService;

    private ErrorReport testErrorReport;
    private String reportId;
    private String userId;
    private String gameId;

    @BeforeEach
    void setUp() {
        reportId = UUID.randomUUID().toString();
        userId = "user-123";
        gameId = UUID.randomUUID().toString();

        testErrorReport = new ErrorReport("Game Error Report", "Dice won't roll",
                "functionality_issue", "high",
                "When I click roll button, nothing happens",
                "JSON gamestate");
    }

    // ========================================================================
    // Test 1: Create ErrorReport - service builds and saves report
    // ========================================================================

    @Test
    @DisplayName("1. Create error report - service saves to repository")
    void testCreateErrorReport() throws Exception {
        // Mock repository to return the report when save is called
        when(errorReportRepository.save(any(ErrorReport.class)))
                .thenAnswer(invocation -> {
                    ErrorReport savedReport = invocation.getArgument(0);
                    return savedReport;
                });

        // Call service
        ErrorReport result = errorReportService.createErrorReport(
                "Game Error Report",
                "Dice won't roll",
                "functionality_issue",
                "high",
                "When I click roll button, nothing happens",
                userId,
                gameId,
                null
        );

        // Assert
        assertNotNull(result);
        assertEquals("Dice won't roll", result.getTopic());
        assertEquals(ErrorStatus.NEW, result.getStatus());

        // Verify save was called
        ArgumentCaptor<ErrorReport> captor = ArgumentCaptor.forClass(ErrorReport.class);
        verify(errorReportRepository).save(captor.capture());
        ErrorReport capturedReport = captor.getValue();
        assertEquals(userId, capturedReport.getUserId());
    }

    // ========================================================================
    // Test 2: Get All ErrorReports
    // ========================================================================

    @Test
    @DisplayName("2. Get all error reports - returns list from repository")
    void testGetAllErrorReports() {
        // Mock
        List<ErrorReport> reports = Arrays.asList(testErrorReport);
        when(errorReportRepository.findAll()).thenReturn(reports);

        // Call
        List<ErrorReport> result = errorReportService.getAllErrorReports();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Dice won't roll", result.get(0).getTopic());
        verify(errorReportRepository, times(1)).findAll();
    }

    // ========================================================================
    // Test 3: Get ErrorReport by ID
    // ========================================================================

    @Test
    @DisplayName("3. Get error report by ID - returns from repository")
    void testGetErrorReportById() {
        // Create a report for testing
        ErrorReport idReport = new ErrorReport("Game Error Report", "Dice won't roll",
                "functionality_issue", "high", "Details", "JSON");

        // Mock
        when(errorReportRepository.findById(reportId))
                .thenReturn(Optional.of(idReport));

        // Call
        Optional<ErrorReport> result = errorReportService.getErrorReportById(reportId);

        // Assert
        assertTrue(result.isPresent());
        assertEquals("Dice won't roll", result.get().getTopic());
        verify(errorReportRepository, times(1)).findById(reportId);
    }

    // ========================================================================
    // Test 4: Get ErrorReports by User
    // ========================================================================

    @Test
    @DisplayName("4. Get error reports by user - filters from repository")
    void testGetErrorReportsByUserId() {
        // Create a report for testing
        ErrorReport userReport = new ErrorReport("Game Error Report", "Dice won't roll",
                "functionality_issue", "high", "Details", "JSON");

        // Mock
        List<ErrorReport> reports = Arrays.asList(userReport);
        when(errorReportRepository.findByUserId(userId)).thenReturn(reports);

        // Call
        List<ErrorReport> result = errorReportService.getErrorReportsByUserId(userId);

        // Assert
        assertEquals(1, result.size());
        assertEquals("Dice won't roll", result.get(0).getTopic());
        verify(errorReportRepository, times(1)).findByUserId(userId);
    }

    // ========================================================================
    // Test 5: Get ErrorReports by Status
    // ========================================================================

    @Test
    @DisplayName("5. Get error reports by status - filters from repository")
    void testGetErrorReportsByStatus() {
        // Mock
        List<ErrorReport> reports = Arrays.asList(testErrorReport);
        when(errorReportRepository.findByStatus(ErrorStatus.NEW))
                .thenReturn(reports);

        // Call
        List<ErrorReport> result = errorReportService.getErrorReportsByStatus(ErrorStatus.NEW);

        // Assert
        assertEquals(1, result.size());
        assertEquals(ErrorStatus.NEW, result.get(0).getStatus());
        verify(errorReportRepository, times(1)).findByStatus(ErrorStatus.NEW);
    }

    // ========================================================================
    // Test 6: Update ErrorReport Status
    // ========================================================================

    @Test
    @DisplayName("6. Update error report status - modifies and saves")
    void testUpdateErrorReportStatus() {
        // Create reports for testing
        ErrorReport original = new ErrorReport("Game Error Report", "Dice won't roll",
                "functionality_issue", "high", "Details", "JSON");

        ErrorReport statusUpdated = new ErrorReport("Game Error Report", "Dice won't roll",
                "functionality_issue", "high", "Details", "JSON");

        // Mock
        when(errorReportRepository.findById(reportId))
                .thenReturn(Optional.of(original));
        when(errorReportRepository.save(any(ErrorReport.class)))
                .thenReturn(statusUpdated);

        // Call
        ErrorReport result = errorReportService.updateErrorReportStatus(reportId, ErrorStatus.IN_PROGRESS);

        // Assert
        assertNotNull(result);
        assertEquals("Dice won't roll", result.getTopic());
        verify(errorReportRepository).save(any(ErrorReport.class));
    }

    // ========================================================================
    // Test 7: Update Full ErrorReport
    // ========================================================================

    @Test
    @DisplayName("7. Update full error report - modifies all fields")
    void testUpdateErrorReport() {
        // Create updated report for testing
        ErrorReport updated = new ErrorReport("Updated name", "Updated topic",
                "ui_bug", "medium", "Updated details", "JSON");

        // Mock
        when(errorReportRepository.findById(reportId))
                .thenReturn(Optional.of(testErrorReport));
        when(errorReportRepository.save(any(ErrorReport.class)))
                .thenReturn(updated);

        // Call
        ErrorReport result = errorReportService.updateErrorReport(
                reportId,
                "Updated name",
                "Updated topic",
                "ui_bug",
                "medium",
                "Updated details",
                ErrorStatus.SOLVED
        );

        // Assert
        assertNotNull(result);
        assertEquals("Updated topic", result.getTopic());
        verify(errorReportRepository).save(any(ErrorReport.class));
    }

    // ========================================================================
    // Test 8: Delete ErrorReport
    // ========================================================================


    // ========================================================================
    // Test 8: Get ErrorReports by Game and Status
    // ========================================================================

    @Test
    @DisplayName("9. Get error reports by game and status - combined filter")
    void testGetErrorReportsByGameAndStatus() {
        // Create a report for testing
        ErrorReport gameStatusReport = new ErrorReport("Game Error Report", "Dice won't roll",
                "functionality_issue", "high", "Details", "JSON");

        // Mock
        List<ErrorReport> reports = Arrays.asList(gameStatusReport);
        when(errorReportRepository.findByGameIdAndStatus(gameId, ErrorStatus.NEW))
                .thenReturn(reports);

        // Call
        List<ErrorReport> result = errorReportService.getErrorReportsByGameAndStatus(gameId, ErrorStatus.NEW);

        // Assert
        assertEquals(1, result.size());
        assertEquals("Dice won't roll", result.get(0).getTopic());
        verify(errorReportRepository).findByGameIdAndStatus(gameId, ErrorStatus.NEW);
    }
};
