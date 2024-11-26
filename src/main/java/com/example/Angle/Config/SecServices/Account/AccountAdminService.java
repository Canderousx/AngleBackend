package com.example.Angle.Config.SecServices.Account;

import com.example.Angle.Config.Exceptions.MediaNotFoundException;
import com.example.Angle.Config.Exceptions.CredentialExistsException;
import com.example.Angle.Config.Exceptions.TokenExpiredException;
import com.example.Angle.Config.Models.*;
import com.example.Angle.Config.Responses.SimpleResponse;
import com.example.Angle.Config.SecRepositories.AccountRepository;
import com.example.Angle.Config.SecRepositories.UserRoleRepository;
import com.example.Angle.Config.SecServices.Account.Interfaces.AccountAdminServiceInterface;
import com.example.Angle.Config.SecServices.JwtService;
import com.example.Angle.Repositories.CommentRepository;
import com.example.Angle.Repositories.VideoRepository;
import com.example.Angle.Services.Email.MaintenanceMailsService;
import com.example.Angle.Services.Images.ImageConverterService;
import com.example.Angle.Services.Images.ImageSaveService;
import com.example.Angle.Services.Images.ImageUploadService;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.coyote.BadRequestException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.tomcat.util.http.fileupload.InvalidFileNameException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class AccountAdminService implements AccountAdminServiceInterface {

    private final Logger log = LogManager.getLogger(AccountAdminService.class);

    private final AccountRepository accountRepository;

    private final AccountRetrievalService accountRetrievalService;

    private final VideoRepository videoRepository;

    private final CommentRepository commentRepository;

    private final ImageUploadService imageUploadService;

    private final ImageSaveService imageSaveService;

    private final ImageConverterService imageConverterService;

    private final UserRoleRepository userRoleRepository;

    private final PasswordEncoder passwordEncoder;

    private final MaintenanceMailsService maintenanceMailsService;

    private final UserAccountService userAccountService;

    private final AuthenticationManager authenticationManager;

    private final JwtService jwtService;

    private final String loginFailMessage = "Incorrect username or password!";


    @Autowired
    public AccountAdminService(AccountRepository accountRepository, JwtService jwtService, AuthenticationManager authenticationManager, UserAccountService userAccountService, AccountRetrievalService accountRetrievalService, VideoRepository videoRepository, CommentRepository commentRepository, ImageUploadService imageUploadService, ImageSaveService imageSaveService, ImageConverterService imageConverterService, UserRoleRepository userRoleRepository, PasswordEncoder passwordEncoder, MaintenanceMailsService maintenanceMailsService) {
        this.accountRepository = accountRepository;
        this.accountRetrievalService = accountRetrievalService;
        this.videoRepository = videoRepository;
        this.commentRepository = commentRepository;
        this.imageUploadService = imageUploadService;
        this.imageSaveService = imageSaveService;
        this.imageConverterService = imageConverterService;
        this.userRoleRepository = userRoleRepository;
        this.passwordEncoder = passwordEncoder;
        this.maintenanceMailsService = maintenanceMailsService;
        this.userAccountService = userAccountService;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    @Override
    public void changeAvatar(String id, MultipartFile avatar) throws IOException {
        if(!imageUploadService.checkExtension(avatar)){
            throw new InvalidFileNameException("","File extension not supported!");
        }
        Account account = accountRetrievalService.getCurrentUser();
        account.setAvatar(imageSaveService.saveUserAvatar(imageConverterService.convertAvatarToBase64(avatar), id));
        this.addUser(account);

    }

    @Override
    public void removeDislikeInteractions(String videoId) {
        List<Account> dislikingAccounts = accountRepository.findUsersWhoDislikeVideo(videoId);
        if(dislikingAccounts.isEmpty()){
            log.info("No one disliked requested video");
            return;
        }
        dislikingAccounts.forEach(account ->{
            account.getDislikedVideos().remove(videoId);
            accountRepository.save(account);
        });
        log.info("Dislike Interactions removed successfully");
    }

    @Override
    public void removeLikeInteractions(String videoId) {
        List<Account> likingAccounts = accountRepository.findUsersWhoLikeVideo(videoId);
        if(likingAccounts.isEmpty()){
            log.info("No one liked requested video");
            return;
        }
        likingAccounts.forEach(account ->{
            account.getLikedVideos().remove(videoId);
            accountRepository.save(account);
        });
        log.info("Like Interactions removed successfully");
    }

    @Override
    public void banAccount(String id) {
        Account account = accountRepository.findById(id).orElse(null);
        if(account == null){
            throw new UsernameNotFoundException("Account doesn't exist!");
        }
        account.setActive(false);
        accountRepository.save(account);
        videoRepository.banAllUserVideos(id);
        commentRepository.banAllUserComments(id);
    }

    @Override
    public void unbanAccount(String id) {
        Account account = accountRepository.findById(id).orElse(null);
        if(account == null){
            throw new UsernameNotFoundException("Account doesn't exist!");
        }
        account.setActive(true);
        accountRepository.save(account);
        videoRepository.unbanAllUserVideos(id);
        commentRepository.unbanAllUserComments(id);
    }

    @Override
    public void singup(Account account) throws MediaNotFoundException, CredentialExistsException {
        if(!accountRetrievalService.usernameExists(account.getUsername()) && !accountRetrievalService.emailExists(account.getEmail())){
            Set<UserRole> defaultRoles = new HashSet<>();
            defaultRoles.add(userRoleRepository.findByName("ROLE_USER").orElse(null));
            if(!defaultRoles.contains(null)){
                account.setRoles(defaultRoles);
            }else{
                throw new RuntimeException("User Roles not found in a database! Please check the database!");
            }
            account.setConfirmed(false);
            account.setPassword(passwordEncoder.encode(account.getPassword()));
            addUser(account);
            maintenanceMailsService.confirmationEmail(account.getEmail());
            return;
        }
        throw new CredentialExistsException("Account already exist!");

    }

    @Override
    public void confirmAccount(String token) throws MediaNotFoundException, BadRequestException {
        Account account = accountRetrievalService.getUserByUsername(jwtService.extractUsername(token));
        if(jwtService.validateEmailConfirmationToken(token)){
            account.setConfirmed(true);
            addUser(account);
            return;
        }
        maintenanceMailsService.confirmationEmail(account.getEmail());
        throw new BadRequestException("Confirmation timeout! New confirmation email has been sent!");
    }

    @Override
    public void restorePassword(PasswordRestoreRequest restoreRequest, HttpServletRequest request) throws TokenExpiredException {
        String token = restoreRequest.getToken();
        try {
            if (jwtService.validatePasswordRecoveryToken(token, request.getRemoteAddr())) {
                String username = jwtService.extractUsername(token);
                userAccountService.changeUserPassword(username, restoreRequest.getNewPassword());
                maintenanceMailsService.passwordChangeMail(username);
                jwtService.invalidateToken(token);
                return;
            }
        }catch (ExpiredJwtException | MediaNotFoundException e){
            throw new TokenExpiredException("Access denied. Please try again.");
        }
    }

    @Override
    public AuthRes login(AuthReq authReq, HttpServletRequest request) throws BadRequestException, MediaNotFoundException {
        String userIP = request.getRemoteAddr();
        try {
            userAccountService.checkEmailConfirmation(authReq.getEmail());
        } catch (BadRequestException e) {
            maintenanceMailsService.confirmationEmail(authReq.getEmail());
            throw new BadRequestException(e.getMessage());
        } catch (MediaNotFoundException | UsernameNotFoundException e) {
            throw new BadRequestException(loginFailMessage);
        }
        try{
            if(!accountRetrievalService.isActive(authReq.getEmail())){
                throw new BadRequestException("Account banned!");
            }
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(authReq.getEmail(),authReq.getPassword())
            );
            if(authentication.isAuthenticated()){
                return AuthRes.builder()
                        .authToken(jwtService.generateToken(authReq.getEmail(),userIP))
                        .build();
            }else{
                throw new BadCredentialsException(loginFailMessage);
            }

        }catch (AuthenticationException e){
            throw new BadCredentialsException(loginFailMessage);
        }
    }

    @Override
    public void addUser(Account account) {
        this.accountRepository.save(account);
    }
}
