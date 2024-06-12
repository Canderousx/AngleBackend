package com.example.Angle.Config.SecServices;


import com.example.Angle.Config.Exceptions.MediaNotFoundException;
import com.example.Angle.Config.Models.Account;
import com.example.Angle.Config.Models.AccountRes;
import com.example.Angle.Config.Models.UserRole;
import com.example.Angle.Config.SecRepositories.AccountRepository;
import com.example.Angle.Models.ReportTypes;
import com.example.Angle.Repositories.CommentRepository;
import com.example.Angle.Repositories.VideoRepository;
import com.example.Angle.Services.ImageService;
import org.apache.coyote.BadRequestException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


@Service
public class AccountService {

    @Autowired
    ImageService imageService;

    @Autowired
    VideoRepository videoRepository;

    @Autowired
    CommentRepository commentRepository;

    private final Logger log = LogManager.getLogger(AccountService.class);

    @Autowired
    private AccountRepository accountRepository;

    public boolean isActive(String accountId){
        return accountRepository.isActive(accountId);
    }

    public boolean isAdmin() throws BadRequestException {
        Account account = this.getCurrentUser();
        for(UserRole role : account.getRoles()){
            if(role.getName().equals("ROLE_ADMIN")){
                log.info("USER {"+account.getUsername()+"} admin access confirmed!");
                return true;
            }
        }
        log.info("USER {"+account.getUsername()+"} admin access denied!");
        return false;
    }

    public Account getCurrentUser() throws BadRequestException {
        Account account = accountRepository
                .findByUsername(
                        SecurityContextHolder.getContext().getAuthentication().getName()
                ).orElse(null);
        if(account == null){
            log.error("Current user not found!");
            throw new BadRequestException("You need to log in first!");
        }
        return account;
    }

    public void removeDislikeInteractions(String videoId){
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

    public void removeLikeInteractions(String videoId){
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




    public void banAccount(String id){
        Account account = accountRepository.findById(id).orElse(null);
        if(account == null){
            throw new UsernameNotFoundException("Account doesn't exist!");
        }
        account.setActive(false);
        accountRepository.save(account);
        videoRepository.banAllUserVideos(id);
        commentRepository.banAllUserComments(id);

    }
    public void unbanAccount(String id){
        Account account = accountRepository.findById(id).orElse(null);
        if(account == null){
            throw new UsernameNotFoundException("Account doesn't exist!");
        }
        account.setActive(true);
        accountRepository.save(account);
        videoRepository.unbanAllUserVideos(id);
        commentRepository.unbanAllUserComments(id);
    }


    public void addUser(Account account){
        this.accountRepository.save(account);
    }


    public boolean unbanUser(String userId){
        Account account = this.accountRepository.findById(userId).orElse(null);
        if(account !=null){
            account.setActive(true);
            this.accountRepository.save(account);
            log.info("User ["+userId+"] has been unbanned");
            return true;
        }else{
            log.error("Requested user ["+userId+"] not FOUND!");
            return false;
        }
    }

    public List<Account> getBannedUsers(){
        log.info("Sending Banned Users");
        return new ArrayList<>(this.accountRepository.findByActive(false));
    }

    public List<Account>getAllUsers(){
        List<Account> accounts = new ArrayList<>();
        this.accountRepository.findAll().forEach(accounts::add);
        log.info("Sending All Users ["+accounts.size()+"]");
        return accounts;
    }

    public Account getUser(String userId) throws MediaNotFoundException {
        Account account = this.accountRepository.findById(userId).orElse(null);
        if(account == null){
            log.error("Requested user id: "+userId+" not FOUND!");
            throw new UsernameNotFoundException("User doesn't exists!");
        }
        return account;
    }

    public Account getMediaAuthor(String mediaType, String mediaId) throws BadRequestException {
        if(mediaType.equals(ReportTypes.VIDEO.name())){
            return accountRepository.findVideoAuthor(mediaId).get(0);
        }
        if(mediaType.equals(ReportTypes.COMMENT.name())){
            return accountRepository.findCommentAuthor(mediaId).get(0);
        }
        log.error("Accounts involved in report don't exists! ERROR");
        throw new BadRequestException("Internal Server Error");
    }



    public Account processAvatar(Account user) throws MediaNotFoundException {
        try {
            user.setAvatar(
                    imageService.readImage(
                            user.getAvatar()
                    ).getContent()
            );
        } catch (IOException | ClassNotFoundException e) {
            log.error("ERROR LOADING USER AVATAR ID: "+user.getId());
            throw new MediaNotFoundException("Couldn't load user avatar!");
        }
        return user;
    }

    public boolean usernameExists(String username){
        return this.accountRepository.findByUsername(username).isPresent();
    }

    public boolean emailExists(String email){

        return this.accountRepository.findByEmail(email).isPresent();
    }

//    public boolean isActive(String userId){
//        Account account = this.accountRepository.findById(userId).orElse(null);
//        if(account!=null){
//            return account.isActive();
//        }
//        return false;
//    }

    public AccountRes generateAccountResponse(String accountId) throws IOException, ClassNotFoundException {
        Account account = accountRepository.findById(accountId).orElse(null);
        if(account == null){
            throw new UsernameNotFoundException("Account doesn't exists!");
        };
        return AccountRes
                .builder()
                .id(account.getId().toString())
                .email(account.getEmail())
                .username(account.getUsername())
                .subscribers(account.getSubscribers().size())
                .avatar(imageService.readImage(account.getAvatar()).getContent())
                .build();
    }

    public AccountRes generateAccountResponse(Account account) throws IOException, ClassNotFoundException {
        return AccountRes
                .builder()
                .id(account.getId().toString())
                .email(account.getEmail())
                .username(account.getUsername())
                .subscribers(account.getSubscribers().size())
                .avatar(imageService.readImage(account.getAvatar()).getContent())
                .build();
    }



}
