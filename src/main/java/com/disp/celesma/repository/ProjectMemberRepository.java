package com.disp.celesma.repository;

import com.disp.celesma.model.ProjectMember;
import com.disp.celesma.model.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectMemberRepository extends JpaRepository<ProjectMember, Long> {


    @Query("SELECT pm FROM ProjectMember pm " +
            "JOIN FETCH pm.project p " +
            "JOIN FETCH p.ownerUser " +
            "WHERE pm.user = :user")
    List<ProjectMember> findAllByUserWithProjectAndOwner(@Param("user") User user);

    @EntityGraph(attributePaths = {"user"})
    List<ProjectMember> findByProjectId(Long projectId);

    Optional<ProjectMember> findByProjectIdAndUserId(Long projectId, Long userId);

    boolean existsByProjectIdAndUserId(Long projectId, Long userId);

}
