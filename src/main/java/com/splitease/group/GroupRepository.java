package com.splitease.group;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GroupRepository extends JpaRepository<Group, Long> {

    @Query("""
            SELECT g FROM Group g
            JOIN GroupMember gm ON gm.group = g
            WHERE gm.user.id = :userId
            ORDER BY g.createdAt DESC
            """)
    List<Group> findAllByUserId(@Param("userId") Long userId);

    @Query("SELECT COUNT(gm) FROM GroupMember gm WHERE gm.group.id = :groupId")
    long countMembersByGroupId(@Param("groupId") Long groupId);
}