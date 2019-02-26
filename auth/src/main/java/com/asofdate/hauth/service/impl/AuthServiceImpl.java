package com.asofdate.hauth.service.impl;

import com.asofdate.hauth.authentication.JwtService;
import com.asofdate.hauth.dao.jpa.SysDomainAuthorizationDao;
import com.asofdate.hauth.dto.AuthDto;
import com.asofdate.hauth.dto.RequestUserDto;
import com.asofdate.hauth.entity.SysDomainAuthorization;
import com.asofdate.hauth.service.AuthService;
import com.asofdate.utils.factory.AuthDTOFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import javax.servlet.http.HttpServletRequest;

/**
 * Created by hzwy23 on 2017/6/19.
 */
@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private SysDomainAuthorizationDao sysDomainAuthorizationDao;

    private final String READ_MODE = "r";
    private final String WRITE_MODE = "w";

    private Integer checkMode(String mode) {
        if (mode.toLowerCase().equals(READ_MODE)) {
            return 1;
        } else if (mode.toLowerCase().equals(WRITE_MODE)) {
            return 2;
        } else {
            return -1;
        }
    }


    @Override
    public AuthDto domainAuth(HttpServletRequest request, String domainId, String mode) {
        RequestUserDto user = JwtService.getConnUser(request);
        String userDomainId = user.getDomainID();
        String userId = user.getUserId();
        if (userDomainId.equals(domainId) || "admin".equals(userId)) {
            return AuthDTOFactory.getAuthDTO(true, "success");
        }
        SysDomainAuthorization item = sysDomainAuthorizationDao.findByDomainIdAndUserId(domainId, userId);
        if (item == null) {
            return AuthDTOFactory.getAuthDTO(false, "您没有被授权访问这个域");
        }
        Integer level = item.getAuthorizationLevel();
        if (level == 2) {
            return AuthDTOFactory.getAuthDTO(true, "success");
        } else if (level == 1 && checkMode(mode) == 2) {
            return AuthDTOFactory.getAuthDTO(false, "只有读取权限,没有写入权限");
        } else if (level == 1 && checkMode(mode) == 1) {
            return AuthDTOFactory.getAuthDTO(true, "success");
        }
        return AuthDTOFactory.getAuthDTO(false, "您没有被授权访问这个域");

    }
}
