package app.oengus.factory.submission;

import app.oengus.domain.submission.Game;
import app.oengus.factory.AbstractFactory;
import org.apache.logging.log4j.util.Strings;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.List;

import static app.oengus.util.StringUtils.limit;

@Component
public class GameFactory extends AbstractFactory<Game> {
    @NotNull
    @Override
    public Game getObject() {
        return this.withSubmissionId(faker.number().randomDigit());
    }

    public Game withSubmissionId(int submissionId) {
        final var game = new Game(-1, submissionId);

        game.setName(faker.appliance().equipment());
        game.setDescription(limit(faker.lorem().paragraph(10), 500));
        game.setConsole(faker.videoGame().platform());
        game.setContentWarnings(Strings.join(faker.lorem().words(3), ','));
        game.setEmulated(faker.bool().bool());
        game.setThemes(faker.lorem().words(2));

        return game;
    }

    @Override
    public Class<?> getObjectType() {
        return Game.class;
    }
}
