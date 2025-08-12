package app.oengus.domain.marathon;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@RequiredArgsConstructor
public class Theme {
    private final int id;
    private String marathonId;

    private String section;
    private String name;
}
