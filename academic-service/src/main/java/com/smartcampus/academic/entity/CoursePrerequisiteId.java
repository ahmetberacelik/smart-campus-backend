package com.smartcampus.academic.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class CoursePrerequisiteId implements Serializable {

    @Column(name = "course_id")
    private Long courseId;

    @Column(name = "prerequisite_id")
    private Long prerequisiteId;
}
