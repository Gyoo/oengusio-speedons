package app.oengus.domain.submission;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@RequiredArgsConstructor
public class Game {
    private final int id;
    private final int submissionId;

    private String name;
    private String description;
    private String console;
    private String contentWarnings;
    private List<String> themes;
    private boolean emulated;

    private List<Category> categories = new ArrayList<>();
    private List<Incentive> incentives = new ArrayList<>();
}
