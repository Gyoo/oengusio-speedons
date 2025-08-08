package app.oengus.adapter.jpa.mapper;

import app.oengus.adapter.jpa.entity.CategoryEntity;
import app.oengus.adapter.jpa.entity.IncentiveEntity;
import app.oengus.domain.submission.Category;
import app.oengus.domain.submission.Incentive;
import org.mapstruct.*;

@Mapper(
    componentModel = "spring",
    injectionStrategy = InjectionStrategy.CONSTRUCTOR,
    unmappedSourcePolicy = ReportingPolicy.ERROR,
    unmappedTargetPolicy = ReportingPolicy.ERROR,
    uses = {
        GameMapper.class,
    }
)
public interface IncentiveMapper {
    @Mapping(target = "gameId", source = "game.id")
    @Mapping(target = "id", source = "id")
    Incentive toDomain(IncentiveEntity entity);

    @Mapping(target = "fresh", ignore = true)
    @InheritInverseConfiguration(name = "toDomain")
    IncentiveEntity fromDomain(Incentive incentive);
}
