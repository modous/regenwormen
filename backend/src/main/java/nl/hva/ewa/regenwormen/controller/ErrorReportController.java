package nl.hva.ewa.regenwormen.controller;

import lombok.extern.slf4j.Slf4j;
import nl.hva.ewa.regenwormen.domain.errorForm.ErrorReport;
import nl.hva.ewa.regenwormen.domain.dto.ErrorReportDTO;
import nl.hva.ewa.regenwormen.domain.Enum.ErrorStatus;
import nl.hva.ewa.regenwormen.service.ErrorReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/api/error-reports")
@CrossOrigin(origins = "*")
public class ErrorReportController {

    private final ErrorReportService errorReportService;
    private final ObjectMapper objectMapper;

    @Autowired
    public ErrorReportController(ErrorReportService errorReportService) {
        this.errorReportService = errorReportService;
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Nieuw error report aanmaken met game state JSON
     * POST /api/error-reports
     *
     * Body: multipart/form-data
     * - errorReport: JSON string met ErrorReportDTO (zonder gameStateJson)
     * - snapshot: file (optional)
     *
     * Backend haalt Game object op via gameId en serialiseert het naar JSON
     */
    @PostMapping
    public ResponseEntity<ErrorReport> createErrorReport(
            @RequestPart("errorReport") String errorReportJson,
            @RequestPart(value = "snapshot", required = false) MultipartFile snapshotFile) {
        try {
            log.info("Received error report JSON: {}", errorReportJson);
            // Parse JSON string naar ErrorReportDTO
            ErrorReportDTO errorReportDTO = objectMapper.readValue(errorReportJson, ErrorReportDTO.class);

            // Backend haalt Game object op en serialiseert het
            ErrorReport errorReport = errorReportService.createErrorReport(
                    errorReportDTO.getName(),
                    errorReportDTO.getTopic(),
                    errorReportDTO.getCategory(),
                    errorReportDTO.getPriority(),
                    errorReportDTO.getDetails(),
                    errorReportDTO.getUserId(),
                    errorReportDTO.getGameId(),
                    snapshotFile
            );

            return ResponseEntity.status(HttpStatus.CREATED).body(errorReport);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Alle error reports ophalen
     * GET /api/error-reports
     */
    @GetMapping
    public ResponseEntity<List<ErrorReport>> getAllErrorReports() {
        List<ErrorReport> reports = errorReportService.getAllErrorReports();
        return ResponseEntity.ok(reports);
    }

    /**
     * Error report op ID ophalen
     * GET /api/error-reports/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ErrorReport> getErrorReportById(@PathVariable String id) {
        Optional<ErrorReport> errorReport = errorReportService.getErrorReportById(id);
        return errorReport.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    /**
     * Error reports van een gebruiker ophalen
     * GET /api/error-reports/user/{userId}
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<ErrorReport>> getErrorReportsByUserId(@PathVariable String userId) {
        List<ErrorReport> reports = errorReportService.getErrorReportsByUserId(userId);
        return ResponseEntity.ok(reports);
    }

    /**
     * Error reports van een game ophalen
     * GET /api/error-reports/game/{gameId}
     */
    @GetMapping("/game/{gameId}")
    public ResponseEntity<List<ErrorReport>> getErrorReportsByGameId(@PathVariable String gameId) {
        List<ErrorReport> reports = errorReportService.getErrorReportsByGameId(gameId);
        return ResponseEntity.ok(reports);
    }

    /**
     * Error reports filteren op status
     * GET /api/error-reports/status/{status}
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<List<ErrorReport>> getErrorReportsByStatus(@PathVariable String status) {
        try {
            ErrorStatus errorStatus = ErrorStatus.valueOf(status.toUpperCase());
            List<ErrorReport> reports = errorReportService.getErrorReportsByStatus(errorStatus);
            return ResponseEntity.ok(reports);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    /**
     * Error reports filteren op game en status
     * GET /api/error-reports/game/{gameId}/status/{status}
     */
    @GetMapping("/game/{gameId}/status/{status}")
    public ResponseEntity<List<ErrorReport>> getErrorReportsByGameAndStatus(
            @PathVariable String gameId,
            @PathVariable String status) {
        try {
            ErrorStatus errorStatus = ErrorStatus.valueOf(status.toUpperCase());
            List<ErrorReport> reports = errorReportService.getErrorReportsByGameAndStatus(gameId, errorStatus);
            return ResponseEntity.ok(reports);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    /**
     * Status van een error report bijwerken
     * PATCH /api/error-reports/{id}/status
     */
    @PatchMapping("/{id}/status")
    public ResponseEntity<ErrorReport> updateErrorReportStatus(
            @PathVariable String id,
            @RequestParam String status) {
        try {
            ErrorStatus errorStatus = ErrorStatus.valueOf(status.toUpperCase());
            ErrorReport updatedReport = errorReportService.updateErrorReportStatus(id, errorStatus);
            return ResponseEntity.ok(updatedReport);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    /**
     * Error report bijwerken
     * PUT /api/error-reports/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<ErrorReport> updateErrorReport(
            @PathVariable String id,
            @RequestBody ErrorReportDTO errorReportDTO) {
        try {
            ErrorReport updatedReport = errorReportService.updateErrorReport(
                    id,
                    errorReportDTO.getName(),
                    errorReportDTO.getTopic(),
                    errorReportDTO.getCategory(),
                    errorReportDTO.getPriority(),
                    errorReportDTO.getDetails(),
                    errorReportDTO.getStatus()
            );
            return ResponseEntity.ok(updatedReport);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    /**
     * Error report verwijderen
     * DELETE /api/error-reports/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteErrorReport(@PathVariable String id) {
        try {
            errorReportService.deleteErrorReport(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
}
