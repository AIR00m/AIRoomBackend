package com.airoom.airoom.member.model.service;

import com.airoom.airoom.member.entity.Student;
import com.airoom.airoom.member.entity.Teacher;
import com.airoom.airoom.member.model.dto.StudentDto;
import com.airoom.airoom.member.model.dto.TeacherDto;
import com.airoom.airoom.member.model.repository.StudentRepository;
import com.airoom.airoom.member.model.repository.TeacherRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberService {
    private final StudentRepository studentRepository;
    private final TeacherRepository teacherRepository;
    private final PasswordEncoder passwordEncoder;

    private void assertUniqueId(String memberId) {
        if (studentRepository.existsByMemberId(memberId) || teacherRepository.existsByMemberId(memberId)) {
            throw new IllegalArgumentException("이미 존재하는 아이디입니다.");
        }

    }

    @Transactional
    public void enrollStudent(StudentDto studentDto) {
        assertUniqueId(studentDto.getMemberId());

        Student s = Student.builder()
                .memberId(studentDto.getMemberId())
                .memberPw(passwordEncoder.encode(studentDto.getMemberPw()))
                .memberName(studentDto.getMemberName())
                .memberAge(studentDto.getMemberAge())
                .memberSchool(studentDto.getMemberSchool())
                .memberEmail(studentDto.getMemberEmail())
                .memberGender(studentDto.getMemberGender())
                .memberGrade(studentDto.getMemberGrade())
                .memberClass(studentDto.getMemberClass())
                .studentClassNo(studentDto.getMemberClassNo())
                .build();

        studentRepository.save(s);
    }

    @Transactional
    public void enrollTeacher(TeacherDto teacherDto) {
        assertUniqueId(teacherDto.getMemberId());

        Teacher t = Teacher.builder()
                .memberId(teacherDto.getMemberId())
                .memberPw(passwordEncoder.encode(teacherDto.getMemberPw()))
                .memberName(teacherDto.getMemberName())
                .memberAge(teacherDto.getMemberAge())
                .memberSchool(teacherDto.getMemberSchool())
                .memberEmail(teacherDto.getMemberEmail())
                .memberGender(teacherDto.getMemberGender())
                .memberGrade(teacherDto.getMemberGrade())
                .memberClass(teacherDto.getMemberClass())
                .build();

        teacherRepository.save(t);
    }


}
