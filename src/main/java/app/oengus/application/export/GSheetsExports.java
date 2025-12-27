package app.oengus.application.export;

import app.oengus.application.UserLookupService;
import app.oengus.domain.marathon.Marathon;
import app.oengus.domain.submission.Category;
import app.oengus.domain.submission.Game;
import app.oengus.domain.submission.Opponent;
import app.oengus.domain.submission.Submission;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.*;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.ServiceAccountCredentials;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.Strings;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.*;

@Service
@Log4j2
public class GSheetsExports {

    private static final List<String> SCOPES = Collections.singletonList(SheetsScopes.SPREADSHEETS);
    private final Sheets service;
    private final DataSource dataSource;

    private final UserLookupService userService;

    @Value("${oengus.spreadsheetId}")
    private String spreadsheetId;

    public GSheetsExports(DataSource dataSource, UserLookupService userService) {
        try {
            final NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
            this.service = new Sheets.Builder(httpTransport, GsonFactory.getDefaultInstance(), getCredentials(httpTransport))
                .setApplicationName("Oengus Speedons").build();
            log.info("Google Sheets service initialized");
        } catch (GeneralSecurityException | IOException e) {
            throw new RuntimeException(e);
        }
        this.dataSource = dataSource;
        this.userService = userService;
    }

    private HttpCredentialsAdapter getCredentials(final NetHttpTransport httpTransport) throws IOException {
        ServiceAccountCredentials credentials = (ServiceAccountCredentials) GoogleCredentials.getApplicationDefault();
        credentials.createScoped(SCOPES);
        return new HttpCredentialsAdapter(credentials);
    }

    @Async
    @Transactional
    public void updateSubmission(Submission submission, Marathon marathon) {
        try {
            log.info("Start updating Submission {} on work sheet", submission.getId());
            Spreadsheet spreadsheet = service.spreadsheets().get(spreadsheetId)
                .setIncludeGridData(true)
                .execute();

            List<ValueRange> updateData = new ArrayList<>();
            List<Request> deleteData = new ArrayList<>();
            List<List<Object>> createData = new ArrayList<>();
            for(Sheet sheet : spreadsheet.getSheets()){
                Map<String, Integer> existingEntries = new HashMap<>();
                Map<Integer, Pair<Integer, String>> opponentEntriesToUpdate = new HashMap<>();
                if(!sheet.getProperties().getTitle().equals("Première Passe") && !sheet.getProperties().getTitle().startsWith("Tag:")) continue;
                for (GridData gridData : sheet.getData()) {
                    List<RowData> rowData = gridData.getRowData();
                    if (rowData != null && !rowData.isEmpty()) {
                        for (int i = 0; i < rowData.size(); i++) {
                            RowData row = rowData.get(i);
                            List<CellData> values = row.getValues();
                            for (int j = 0; j < values.size(); j++) {
                                CellData cell = values.get(j);
                                if (Strings.CS.startsWith(cell.getFormattedValue(), submission.getId() + ";")) {
                                    existingEntries.put(cell.getFormattedValue(), i + 1);
                                }
                                int finalJ = j;
                                if (submission.getOpponents().stream().anyMatch(opponent -> Strings.CS.endsWith(cell.getFormattedValue(), ";" + opponent.getCategoryId()) &&
                                    !values.get(finalJ + 1).getFormattedValue().contains(submission.getUser().getDisplayName()))) {
                                    opponentEntriesToUpdate.put(Integer.valueOf(cell.getFormattedValue().split(";")[2]), Pair.of(i + 1, values.get(finalJ + 1).getFormattedValue()));
                                }
                            }
                        }
                    }
                }

                if(!submission.getOpponents().isEmpty()){
                    updateData.addAll(updateOpponentEntries(submission, opponentEntriesToUpdate, sheet));
                }
                updateData.addAll(updateExistingEntries(submission, existingEntries, sheet));
                if(sheet.getProperties().getTitle().equals("Première Passe")) {
                    createData.addAll(createNewEntries(submission, existingEntries));
                }
                deleteData.addAll(deleteMissingEntries(submission, existingEntries, sheet));
            }

            if(!updateData.isEmpty()){
                try {
                    BatchUpdateValuesRequest batchBody = new BatchUpdateValuesRequest().setValueInputOption("USER_ENTERED").setData(updateData);
                    BatchUpdateValuesResponse response = service.spreadsheets().values().batchUpdate(spreadsheetId, batchBody).execute();

                    log.info("Updated {} rows", response.getTotalUpdatedRows());
                } catch (IOException e) {
                    log.error("Error when updating opponents", e);
                    throw new RuntimeException(e);
                }
            }

            if(!deleteData.isEmpty()){
                try {
                    BatchUpdateSpreadsheetRequest body = new BatchUpdateSpreadsheetRequest().setRequests(deleteData);
                    BatchUpdateSpreadsheetResponse response = service.spreadsheets().batchUpdate(spreadsheetId, body).execute();

                    log.info("Deleted {} rows", deleteData.size());
                } catch (IOException e) {
                    log.error("Error when deleting entries", e);
                    throw new RuntimeException(e);
                }
            }

            if(!createData.isEmpty()){
                ValueRange appendBody = new ValueRange().setValues(createData);
                try {
                    AppendValuesResponse appendResult = service.spreadsheets().values()
                        .append(spreadsheetId, "'Première Passe'!A1", appendBody)
                        .setValueInputOption("USER_ENTERED")
                        .setInsertDataOption("INSERT_ROWS")
                        .setIncludeValuesInResponse(true)
                        .execute();

                    log.info("Created {} new entries", appendResult.getUpdates().getUpdatedRows());
                } catch (IOException e) {
                    log.error("Error when creating entries", e);
                    throw new RuntimeException(e);
                }
            }

            log.info("Submission {} updated on work sheet", submission.getId());

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private List<Request> deleteMissingEntries(Submission submission, Map<String, Integer> existingEntries, Sheet sheet) {
        Set<String> submissionEntries = new HashSet<>();
        submission.getGames().forEach(game -> {
            game.getCategories().forEach(category -> {
                submissionEntries.add(String.join(";", Integer.valueOf(submission.getId()).toString(), Integer.valueOf(game.getId()).toString(), Integer.valueOf(category.getId()).toString()));
            });
        });
        Set<String> deletedEntries = new HashSet<>(existingEntries.keySet());
        deletedEntries.removeAll(submissionEntries);
        List<Request> requests = new ArrayList<>();
        if (!deletedEntries.isEmpty()) {
            deletedEntries.forEach(deletedEntry -> {
                DeleteDimensionRequest deleteRequest = new DeleteDimensionRequest()
                    .setRange(
                        new DimensionRange()
                            .setSheetId(sheet.getProperties().getSheetId())
                            .setDimension("ROWS")
                            .setStartIndex(existingEntries.get(deletedEntry) - 1)
                            .setEndIndex(existingEntries.get(deletedEntry))
                    );
                requests.add(new Request().setDeleteDimension(deleteRequest));
            });
        }
        return requests;
    }

    private List<ValueRange> updateExistingEntries(Submission submission, Map<String, Integer> existingEntries, Sheet sheet) {
        List<List<Object>> categories = new ArrayList<>();
        submission.getGames().forEach(game -> {
            game.getCategories().forEach(category -> {
                String entryId = String.join(";", Integer.valueOf(submission.getId()).toString(), Integer.valueOf(game.getId()).toString(), Integer.valueOf(category.getId()).toString());
                if (existingEntries.containsKey(entryId)) {
                    categories.add(formatRow(submission, game, category, entryId));
                }
            });
        });
        List<ValueRange> data = new ArrayList<>();
        if (!categories.isEmpty()) {
            categories.forEach(row -> {
                data.add(new ValueRange().setRange("'" + sheet.getProperties().getTitle() + "'!A" + existingEntries.get(row.getFirst())).setValues(List.of(row)));
            });
        }
        return data;
    }

    private List<ValueRange> updateOpponentEntries(Submission submission, Map<Integer, Pair<Integer, String>> opponentEntriesToUpdate, Sheet sheet) {
        List<ValueRange> data = new ArrayList<>();
        submission.getOpponents().forEach(opponent -> {
            if (opponentEntriesToUpdate.containsKey(opponent.getCategoryId())) {
                data.add(new ValueRange().setRange("'" + sheet.getProperties().getTitle() + "'!B" + opponentEntriesToUpdate.get(opponent.getCategoryId()).getLeft()).setValues(List.of(List.of(opponentEntriesToUpdate.get(opponent.getCategoryId()).getRight() + "," + submission.getUser().getDisplayName()))));
            }
        });
        return data;
    }

    private List<List<Object>> createNewEntries(Submission submission, Map<String, Integer> existingEntries) {
        List<List<Object>> categories = new ArrayList<>();
        submission.getGames().forEach(game -> {
            game.getCategories().forEach(category -> {
                String entryId = String.join(";", Integer.valueOf(submission.getId()).toString(), Integer.valueOf(game.getId()).toString(), Integer.valueOf(category.getId()).toString());
                if (!existingEntries.containsKey(entryId)) {
                    categories.add(formatRow(submission, game, category, entryId));
                }
            });
        });
        return categories;
    }

    private List<Object> formatRow(Submission submission, Game game, Category category, String entryId) {
        List<String> runners = new ArrayList<>();
        runners.add(submission.getUser().getDisplayName());
        runners.addAll(category.getOpponents().stream().map(opponent -> userService.findById(opponent.getUserId()).get().getDisplayName()).toList());
        List<String> videos = new ArrayList<>();
        videos.add(category.getVideo());
        videos.addAll(category.getOpponents().stream().map(Opponent::getVideo).toList());
        return List.of(
            entryId,
            String.join(", ", runners.toArray(new String[0])),
            game.getName(),
            category.getName(),
            category.getType().toString(),
            category.getCanRace() ? "Oui" : "Non",
            TimeHelpers.formatDuration(category.getEstimate()),
            String.join(", ", game.getThemes().toArray(new String[0])),
            String.join(", ", videos.toArray(new String[0])),
            game.getDescription(),
            category.getDescription(),
            category.getHighlights(),
            game.getContentWarnings()
        );
    }
}
