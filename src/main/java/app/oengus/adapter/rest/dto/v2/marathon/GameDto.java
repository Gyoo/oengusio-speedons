package app.oengus.adapter.rest.dto.v2.marathon;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class GameDto {
    private int id;
    private String name;
    private String description;
    private String console;
    private String contentWarnings;
    private List<String> themes = new ArrayList<>();
    private boolean emulated;
}
