package com.disp.celesma.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "task_roadmap_entries")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskRoadmapEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "task_id")
    private Task task;

    @ManyToOne
    @JoinColumn(name = "branch_id")
    private RoadmapBranch branch;

    private Integer orderInBranch;
}
