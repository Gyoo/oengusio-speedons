package app.oengus.adapter.jpa.mapper;

import app.oengus.adapter.jpa.entity.SubmissionEntity;
import app.oengus.adapter.jpa.entity.ThemeEntity;
import app.oengus.domain.marathon.Theme;
import app.oengus.domain.submission.Submission;
import org.mapstruct.*;

@Mapper(
    componentModel = "spring",
    injectionStrategy = InjectionStrategy.CONSTRUCTOR,
    unmappedSourcePolicy = ReportingPolicy.ERROR,
    unmappedTargetPolicy = ReportingPolicy.ERROR
)
public interface ThemeEntityMapper {
    @Mapping(target = "marathonId", source = "marathon.id")
    Theme toDomain(ThemeEntity themeEntity);

    @InheritInverseConfiguration(name = "toDomain")
    ThemeEntity fromDomain(Theme theme);
}
