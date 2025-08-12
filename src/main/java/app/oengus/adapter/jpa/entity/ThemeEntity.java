package app.oengus.adapter.jpa.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "theme")
public class ThemeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "marathon_id")
    private MarathonEntity marathon;

    @Column(name = "section")
    @NotNull
    private String section;

    @Column(name = "name")
    @NotNull
    private String name;

    /**
     * @param marathonId the id of the marathon
     * @return A fake marathon instance with this id
     */
    public static MarathonEntity ofId(String marathonId) {
        final MarathonEntity marathon = new MarathonEntity();
        marathon.setId(marathonId);

        return marathon;
    }
}
