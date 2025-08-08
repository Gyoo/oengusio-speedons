package app.oengus.adapter.rest.dto.v1;

import app.oengus.adapter.rest.dto.OpponentCategoryDto;
import app.oengus.domain.submission.IncentiveType;
import app.oengus.domain.submission.RunType;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;

import java.time.Duration;
import java.util.List;

@Getter
@Setter
public class V1IncentiveDto {
    private int id;
    private int gameId;
    private String name;
    private Duration duration;
    private String description;
    private IncentiveType incentiveType;
}
