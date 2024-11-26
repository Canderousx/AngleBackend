package com.example.Angle.Config.SecServices.Account.Interfaces;

import com.example.Angle.Config.Exceptions.MediaNotFoundException;
import com.example.Angle.Config.Exceptions.CredentialExistsException;
import com.example.Angle.Config.Exceptions.TokenExpiredException;
import com.example.Angle.Config.Models.Account;
import com.example.Angle.Config.Models.AuthReq;
import com.example.Angle.Config.Models.AuthRes;
import com.example.Angle.Config.Models.PasswordRestoreRequest;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.coyote.BadRequestException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface AccountAdminServiceInterface {

    void removeDislikeInteractions(String videoId);

    void removeLikeInteractions(String videoId);

    void banAccount(String id);

    void unbanAccount(String id);

    void addUser(Account account);

    void changeAvatar(String id, MultipartFile avatar) throws IOException;

    void singup(Account account) throws MediaNotFoundException, CredentialExistsException;

    AuthRes login(AuthReq authReq, HttpServletRequest request) throws BadRequestException, MediaNotFoundException;

    void confirmAccount(String token) throws MediaNotFoundException, BadRequestException;

    void restorePassword(PasswordRestoreRequest restoreRequest, HttpServletRequest request) throws TokenExpiredException;







}
