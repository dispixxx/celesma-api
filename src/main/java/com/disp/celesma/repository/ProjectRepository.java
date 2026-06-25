package com.disp.celesma.repository;

import com.disp.celesma.model.Project;
import com.disp.celesma.model.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {
    List<Project> findByNameContainingIgnoreCase(String query);

    @EntityGraph(attributePaths = {"ownerUser", "members", "members.user", "applicants"})
    @Query("SELECT p FROM Project p WHERE p.id = :id")
    Optional<Project> findByIdWithOwner(Long id);

    List<Project> findProjectsByApplicants(User user);
}
