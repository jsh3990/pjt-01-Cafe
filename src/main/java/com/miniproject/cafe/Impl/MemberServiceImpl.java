package com.miniproject.cafe.Impl;

import com.miniproject.cafe.Mapper.MemberMapper;
import com.miniproject.cafe.Service.MemberService;
import com.miniproject.cafe.VO.MemberVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MemberServiceImpl implements MemberService {

    @Autowired
    private MemberMapper memberMapper;

    @Override
    public int MemberRegister(MemberVO memberVO) {
        return memberMapper.MemberRegister(memberVO);
    }

    @Override
    public MemberVO MemberLogin(MemberVO memberVO) {
        return memberMapper.MemberLogin(memberVO);
    }

    @Override
    public boolean isEmailDuplicate(String email) {
        return memberMapper.isEmailDuplicate(email);
    }

    @Override
    public boolean isIdDuplicate(String uId) {
        return memberMapper.isIdDuplicate(uId);
    }
}
