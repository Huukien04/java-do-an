package com.hcmute.topicmanagement.security;

import com.hcmute.topicmanagement.entity.Student;
import com.hcmute.topicmanagement.entity.Teacher;
import com.hcmute.topicmanagement.entity.User;
import com.hcmute.topicmanagement.repository.StudentRepository;
import com.hcmute.topicmanagement.repository.TeacherRepository;
import com.hcmute.topicmanagement.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private final TeacherRepository teacherRepository;
    private final StudentRepository studentRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		 System.out.println("========== LOGIN ==========");
    System.out.println("Username: " + username);

    User user = userRepository.findByUsername(username)
        .orElseThrow(() -> {
            System.out.println("❌ KHÔNG TÌM THẤY USER: " + username);
            return new UsernameNotFoundException("User not found");
        });

    System.out.println("✅ Tìm thấy user: " + user.getUsername());
    System.out.println("Password DB: " + user.getPassword());
    System.out.println("Role: " + user.getRole());

        // User user = userRepository.findByUsername(username)
        //         .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy người dùng với username: " + username));

        Teacher teacher = teacherRepository.findById(user.getId()).orElse(null);
        Student student = studentRepository.findById(user.getId()).orElse(null);

        return new CustomUserDetails(user, teacher, student);
    }
}
