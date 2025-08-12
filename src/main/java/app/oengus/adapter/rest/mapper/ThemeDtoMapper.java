package app.oengus.adapter.rest.mapper;

import app.oengus.adapter.rest.dto.v2.marathon.ThemeDto;
import app.oengus.domain.marathon.Theme;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Named;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Mapper(componentModel = "spring")
public interface ThemeDtoMapper {
    ThemeDto toDto(Theme theme);

    @InheritInverseConfiguration(name = "toDto")
    Theme fromDto(ThemeDto dto);

    @Named("toThemeMap")
    default Map<String, List<ThemeDto>> toThemeMap(List<Theme> themes) {
        Map<String, List<ThemeDto>> map = new HashMap<>();
        themes.forEach(theme -> {
            if(!map.containsKey(theme.getSection())) {
                map.put(theme.getSection(), new ArrayList<>());
            }
            map.get(theme.getSection()).add(new ThemeDto(theme.getId(), theme.getName()));
        });
        return map;
    }

    @Named("fromThemeMap")
    default List<Theme> fromThemeMap(Map<String, List<ThemeDto>> themes) {
        List<Theme> themesList = new ArrayList<>();
        themes.forEach((key, value) -> value.forEach(themeDto -> {
            Theme theme = new Theme(themeDto.id());
            theme.setSection(key);
            theme.setName(themeDto.name());
            themesList.add(theme);
        }));
        return themesList;
    }
}
