package app.oengus.adapter.rest.dto.v2.marathon.request;

import app.oengus.adapter.rest.dto.v2.marathon.ThemeDto;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@Schema(description = "Update themes for a marathon")
public class ThemesUpdateRequest {
    // TODO: limit the amount of themes that can be added
    @NotNull
    @Schema(description = "The themes to be updated, only themes of type SUBMISSION are supported", required = true)
    private Map<String, List<ThemeDto>> themes;
}
