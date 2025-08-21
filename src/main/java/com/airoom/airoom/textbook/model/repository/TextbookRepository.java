package com.airoom.airoom.textbook.model.repository;

import com.airoom.airoom.textbook.entity.Textbook;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TextbookRepository extends JpaRepository<Textbook, Long> {
//    List<Textbook> findAllByMemberNo(String memberNo);
    @Query(value = "SELECT tx.* " +
            "FROM member m " +
            "JOIN classroom_teacher ct ON m.member_no = ct.member_no " +
            "JOIN textbook tx ON ct.textbook_no = tx.textbook_no " +
            "WHERE m.member_no = :memberNo",
            nativeQuery = true)
    List<Textbook> getAllTextbooksByTeacherMemberNo(@Param("memberNo") Long memberNo);
    @Query(value = """
        SELECT DISTINCT tx.*
        FROM `member` m
        JOIN classroom_student st ON st.member_no = m.member_no
        JOIN classroom c          ON c.classroom_no = st.classroom_no
        JOIN classroom_teacher ct ON ct.classroom_no = c.classroom_no
        JOIN textbook tx          ON tx.textbook_no = ct.textbook_no
        WHERE m.member_no = :memberNo
    """, nativeQuery = true)
    List<Textbook> getAllTextbooksByStudentMemberNo(@Param("memberNo") Long memberNo);
}