package app.oengus.adapter.rest.dto.v1;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class V1GameDto {
    private int id;
    private int submissionId;
    private String name;
    private String description;
    private String console;
    private String contentWarnings;
    private List<String> themes;
    private boolean emulated;
    private List<V1CategoryDto> categories;
    private List<V1IncentiveDto> incentives;
}
