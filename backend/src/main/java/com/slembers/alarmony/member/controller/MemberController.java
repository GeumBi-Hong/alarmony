package com.slembers.alarmony.member.controller;


import com.slembers.alarmony.global.jwt.SecurityUtil;
import com.slembers.alarmony.member.dto.request.ReissueTokenDto;
import com.slembers.alarmony.member.dto.request.SignUpDto;
import com.slembers.alarmony.member.dto.response.CheckDuplicateDto;
import com.slembers.alarmony.member.dto.response.TokenResponseDto;
import com.slembers.alarmony.member.service.EmailVerifyService;
import com.slembers.alarmony.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/members")
@RequiredArgsConstructor
@Slf4j
public class MemberController {

    private final MemberService memberService;
    private final EmailVerifyService emailVerifyService;

    /**
     * 회원가입
     */

    @PostMapping("/sign-up")
    public ResponseEntity<String> signUp(@Valid @RequestBody SignUpDto signUpDto) {

        log.info("[회원 가입 Controller 진입]");
        memberService.signUp(signUpDto);
        return new ResponseEntity<>("회원 가입 성공", HttpStatus.CREATED);

    }

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
     * 인증 이메일 확인
     */
    @GetMapping("/verify/{key}")
    public ResponseEntity<String> getVerify(@PathVariable String key) {

        emailVerifyService.verifyEmail(key);
        return new ResponseEntity<>("이메일 인증에 성공하였습니다.", HttpStatus.OK);

    }


    @GetMapping("/test")
    public void test(@AuthenticationPrincipal User user) {
        user.getAuthorities();
        log.info("test진입");
        log.info("test 진입함@@@@@@@@@@@@@@@@@@@@@" + SecurityUtil.getCurrentUsername().get()); //

    }

    /**
     * Access 토큰 및 Refresh 토큰 재발급
     */
    @PostMapping("/refresh")
    public ResponseEntity<TokenResponseDto> refresh(@RequestBody ReissueTokenDto reissueTokenDto) {

        return new ResponseEntity<>(memberService.reissueToken(reissueTokenDto), HttpStatus.OK);

    }

}
