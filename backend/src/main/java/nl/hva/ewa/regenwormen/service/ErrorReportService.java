package nl.hva.ewa.regenwormen.service;

import nl.hva.ewa.regenwormen.domain.errorForm.ErrorReport;
import nl.hva.ewa.regenwormen.domain.Enum.ErrorStatus;
import nl.hva.ewa.regenwormen.domain.Game;
import nl.hva.ewa.regenwormen.repository.ErrorReportRepository;
import nl.hva.ewa.regenwormen.repository.GameRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ErrorReportService {

    @Autowired
    private ErrorReportRepository errorReportRepository;

    @Autowired
    private GameRepository gameRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final String UPLOAD_DIR = "uploads/error-reports/";

    /**
     * Nieuw error report aanmaken
     * Backend haalt Game object op via gameId en serialiseert het naar JSON
     */
    public ErrorReport createErrorReport(String name, String topic, String category,
                                        String priority, String details,
                                        String userId, String gameId, MultipartFile snapshotFile) throws IOException {

        // 🎮 Haal Game object op via gameId
        String gameStateJson = null;
        if (gameId != null && !gameId.isEmpty()) {
            Optional<Game> gameOpt = gameRepository.findById(gameId);
            if (gameOpt.isPresent()) {
                Game game = gameOpt.get();
                // Serialiseer Game object naar JSON
                gameStateJson = objectMapper.writeValueAsString(game);
            }
        }

        // Bouw ErrorReport
        ErrorReport errorReport = new ErrorReport(name, topic, category, priority, details, gameStateJson);

        // Extra velden
        if (userId != null) {
            errorReport.setUserId(userId);
        }
        if (gameId != null) {
            errorReport.setGameId(gameId);
        }

        // Snapshot file opslaan
        if (snapshotFile != null && !snapshotFile.isEmpty()) {
            String fileName = saveSnapshotFile(snapshotFile);
            errorReport.setSnapshotFileName(fileName);
            errorReport.setSnapshotFilePath(UPLOAD_DIR + fileName);
        }

        return errorReportRepository.save(errorReport);
    }

    /**
     * Alle error reports ophalen
     */
    public List<ErrorReport> getAllErrorReports() {
        return errorReportRepository.findAll();
    }

    /**
     * Error report op ID ophalen
     */
    public Optional<ErrorReport> getErrorReportById(String id) {
        return errorReportRepository.findById(id);
    }

    /**
     * Error reports van een gebruiker ophalen
     */
    public List<ErrorReport> getErrorReportsByUserId(String userId) {
        return errorReportRepository.findByUserId(userId);
    }

    /**
     * Error reports van een game ophalen
     */
    public List<ErrorReport> getErrorReportsByGameId(String gameId) {
        return errorReportRepository.findByGameId(gameId);
    }

    /**
     * Error reports filteren op status
     */
    public List<ErrorReport> getErrorReportsByStatus(ErrorStatus status) {
        return errorReportRepository.findByStatus(status);
    }

    /**
     * Error reports filteren op game en status
     */
    public List<ErrorReport> getErrorReportsByGameAndStatus(String gameId, ErrorStatus status) {
        return errorReportRepository.findByGameIdAndStatus(gameId, status);
    }

    /**
     * Status van een error report bijwerken
     */
    public ErrorReport updateErrorReportStatus(String id, ErrorStatus newStatus) {
        Optional<ErrorReport> errorReportOpt = errorReportRepository.findById(id);
        if (errorReportOpt.isPresent()) {
            ErrorReport errorReport = errorReportOpt.get();
            errorReport.setStatus(newStatus);
            errorReport.setUpdatedAt(LocalDateTime.now());
            return errorReportRepository.save(errorReport);
        }
        throw new RuntimeException("ErrorReport niet gevonden met ID: " + id);
    }

    /**
     * Error report bijwerken
     */
    public ErrorReport updateErrorReport(String id, String name, String topic, String category,
                                        String priority, String details,
                                        ErrorStatus status) {
        Optional<ErrorReport> errorReportOpt = errorReportRepository.findById(id);
        if (errorReportOpt.isPresent()) {
            ErrorReport errorReport = errorReportOpt.get();

            if (name != null) errorReport.setName(name);
            if (topic != null) errorReport.setTopic(topic);
            if (category != null) errorReport.setCategory(category);
            if (priority != null) errorReport.setPriority(priority);
            if (details != null) errorReport.setDetails(details);
            if (status != null) errorReport.setStatus(status);

            errorReport.setUpdatedAt(LocalDateTime.now());
            return errorReportRepository.save(errorReport);
        }
        throw new RuntimeException("ErrorReport niet gevonden met ID: " + id);
    }

    /**
     * Error report verwijderen
     */
    public void deleteErrorReport(String id) {
        if (!errorReportRepository.existsById(id)) {
            throw new RuntimeException("ErrorReport niet gevonden met ID: " + id);
        }
        errorReportRepository.deleteById(id);
    }

    /**
     * Snapshot file opslaan in bestandssysteem
     */
    private String saveSnapshotFile(MultipartFile file) throws IOException {
        Path uploadPath = Paths.get(UPLOAD_DIR);

        // Directory aanmaken als het niet bestaat
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Unieke bestandsnaam genereren
        String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
        Path filePath = uploadPath.resolve(fileName);

        // File opslaan
        Files.copy(file.getInputStream(), filePath);

        return fileName;
    }
}
