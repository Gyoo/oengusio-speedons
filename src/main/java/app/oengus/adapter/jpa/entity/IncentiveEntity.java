package app.oengus.adapter.jpa.entity;

import app.oengus.domain.submission.IncentiveType;
import app.oengus.domain.submission.RunType;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.Hibernate;
import org.hibernate.validator.constraints.time.DurationMin;
import org.springframework.beans.BeanUtils;

import javax.annotation.Nullable;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Getter
@Setter
@Entity
@EqualsAndHashCode
@Table(name = "incentive")
public class IncentiveEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "game_id")
    private GameEntity game;

    @Column(name = "name")
    @NotBlank
    @Size(max = 100)
    private String name;

    @Column(name = "duration")
    private Duration duration;

    @Column(name = "description")
    @Size(max = 300)
    private String description;

    @Column(name = "incentive_type")
    private IncentiveType incentiveType;

    public static IncentiveEntity ofId(int id) {
        final var incentive = new IncentiveEntity();

        incentive.setId(id);

        return incentive;
    }

    @Deprecated(forRemoval = true)
    public IncentiveEntity fresh(GameEntity parent) {
        final IncentiveEntity incentive = new IncentiveEntity();

        BeanUtils.copyProperties(this, incentive, "game");

        incentive.setGame(parent);

        return incentive;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        IncentiveEntity that = (IncentiveEntity) o;
        return Objects.equals(id, that.id) && Objects.equals(name, that.name) && Objects.equals(duration, that.duration) && Objects.equals(description, that.description) && incentiveType == that.incentiveType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, duration, description, incentiveType);
    }
}

