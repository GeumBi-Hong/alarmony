package com.slembers.alarmony.member.controller;


import com.slembers.alarmony.member.dto.request.SignUpRequestDto;
import com.slembers.alarmony.member.dto.response.CheckDuplicateDto;
import com.slembers.alarmony.member.service.EmailVerifyService;
import com.slembers.alarmony.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;
    private final EmailVerifyService emailVerifyService;


    /**
     * 아이디 중복 체크
     **/
    @GetMapping("/check-id")
    public ResponseEntity<CheckDuplicateDto> checkForDuplicateId(@RequestParam("username") String username) {

        return new ResponseEntity<>(memberService.checkForDuplicateId(username), HttpStatus.OK);
    }


    /**
     * 이메일 중복 체크
     */
    @GetMapping("/check-email")
    public ResponseEntity<CheckDuplicateDto> checkForDuplicateEmail(@RequestParam("email") String email) {
        return new ResponseEntity<>(memberService.checkForDuplicateEmail(email), HttpStatus.OK);
    }

    /**
     * 닉네임 중복 체크
     */

    @GetMapping("/check-nickname")
    public ResponseEntity<CheckDuplicateDto> checkForDuplicateNickname(@RequestParam("nickname") String nickname) {
        return new ResponseEntity<>(memberService.checkForDuplicateNickname(nickname), HttpStatus.OK);
    }


    /**
     * 회원가입
     */

    @PostMapping()
    public ResponseEntity<String> signUp(@Valid @RequestBody SignUpRequestDto signUpRequestDto) {


        memberService.signUp(signUpRequestDto);

        return new ResponseEntity<>("회원 가입 성공", HttpStatus.CREATED);
    }

    /**
     * 인증 이메일 확인
     */
    @GetMapping("/verify/{key}")
    public ResponseEntity<String> getVerify(@PathVariable String key) {

        try {
            emailVerifyService.verifyEmail(key);
            return ResponseEntity.ok("Email verified successfully");
        } catch (Exception e) {
            // Handle other exceptions
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal server error: " + e.getMessage());
        }

    }

}
