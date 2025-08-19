package app.oengus.adapter.jpa.mapper;

import app.oengus.domain.submission.Game;
import app.oengus.adapter.jpa.entity.GameEntity;
import org.mapstruct.*;

@Mapper(
    componentModel = "spring",
    injectionStrategy = InjectionStrategy.CONSTRUCTOR,
    unmappedSourcePolicy = ReportingPolicy.ERROR,
    unmappedTargetPolicy = ReportingPolicy.ERROR,
    uses = {
        CategoryMapper.class,
        IncentiveMapper.class,
    }
)
public interface GameMapper {

    @BeanMapping(ignoreUnmappedSourceProperties = { "themes" })
    @Mapping(target = "submissionId", source = "submission.id")
    @Mapping(target = "themes", expression = "java(entity.getThemes() == null || entity.getThemes().isBlank() ? List.of() : List.of(entity.getThemes().split(\",\")))")
    Game toDomain(GameEntity entity);

    @Mapping(target = "submission", ignore = true)
    @Mapping(target = "submission.id", source = "submissionId")
    @Mapping(target = "themes", expression = "java(String.join(\",\", game.getThemes()))")
//    @InheritInverseConfiguration(name = "toDomain")
    GameEntity fromDomain(Game game);
}
