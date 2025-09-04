package ru.ext.edu.postgres.blog.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedAttributeNode;
import jakarta.persistence.NamedEntityGraph;
import jakarta.persistence.OneToMany;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.proxy.HibernateProxy;

import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "posts")
@Accessors(chain = true)
@ToString(exclude = {"likes", "user", "comments", "tags"})
@NoArgsConstructor(access = AccessLevel.PUBLIC)
@NamedEntityGraph(
        name = "Post.withTagsAndUser",
        attributeNodes = {
                @NamedAttributeNode("tags"),
                @NamedAttributeNode("user")
                //  Комментарии грузим отдельно, через репозиторий
        }
)
public class Post {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "posts_id_seq")
    @SequenceGenerator(name = "posts_id_seq", sequenceName = "posts_id_seq", allocationSize = 1)
    @Column(name = "id", unique = true, nullable = false)
    private Long id;

    @Column(name = "subject", unique = false, nullable = false)
    private String subject;

    @Column(name = "content", unique = false, nullable = true)
    private String content;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @CreationTimestamp
    @Column(name = "created_at", unique = false, nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "is_active", unique = false, nullable = false)
    private boolean isActive;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Comment> comments = new HashSet<>();

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Like> likes = new HashSet<>();

    @BatchSize(size = 50)
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "post_tags",
            joinColumns = @JoinColumn(name = "post_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private Set<Tag> tags = new HashSet<>();

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        var oEffectiveClass = o instanceof HibernateProxy proxy
                ? proxy.getHibernateLazyInitializer().getPersistentClass()
                : o.getClass();
        var thisEffectiveClass = this instanceof HibernateProxy proxy
                ? proxy.getHibernateLazyInitializer().getPersistentClass()
                : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        Post post = (Post) o;
        return getId() != null && Objects.equals(getId(), post.getId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy proxy
                ? proxy.getHibernateLazyInitializer().getPersistentClass().hashCode()
                : getClass().hashCode();
    }
}
