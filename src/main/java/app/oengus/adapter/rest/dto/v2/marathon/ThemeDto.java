package app.oengus.adapter.rest.dto.v2.marathon;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ThemeDto(
    @Schema(description = "The unique identifier of the theme", required = true)
    int id,

    @NotNull
    @Size(max = 50)
    @Schema(description = "Theme name", required = true)
    String name
) {
}
