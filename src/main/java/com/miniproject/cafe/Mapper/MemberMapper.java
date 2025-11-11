package com.miniproject.cafe.Mapper;

import com.miniproject.cafe.VO.MemberVO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface MemberMapper {
    int MemberRegister(MemberVO memberVO); //회원가입
    MemberVO MemberLogin(MemberVO memberVO); //로그인
    boolean isEmailDuplicate(String email); //이메일 중복확인
    boolean isIdDuplicate(String uId); //아이디 중복확인
}
