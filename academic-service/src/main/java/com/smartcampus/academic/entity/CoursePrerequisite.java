package com.smartcampus.academic.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "course_prerequisites")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CoursePrerequisite {

    @EmbeddedId
    private CoursePrerequisiteId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("courseId")
    @JoinColumn(name = "course_id")
    private Course course;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("prerequisiteId")
    @JoinColumn(name = "prerequisite_id")
    private Course prerequisite;
}
