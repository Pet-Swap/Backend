package fr.petswap.backend.dao.jpa;

import jakarta.persistence.*;
import lombok.*;
import java.util.Set;

@Entity
@Table(name = "pets")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Pet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pet_id")
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "owner_id", nullable = false)
    private Profile owner;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 50)
    private String species;

    @Column(length = 50)
    private String breed;

    private Integer age;

    @Column(name = "photo_url")
    private String photoUrl;

    @Column(name = "special_notes")
    private String specialNotes;

    @OneToMany(mappedBy = "pet")
    private Set<Listing> listings;
}