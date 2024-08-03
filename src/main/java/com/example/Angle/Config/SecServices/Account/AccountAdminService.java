package com.example.Angle.Config.SecServices.Account;

import com.example.Angle.Config.Models.Account;
import com.example.Angle.Config.SecRepositories.AccountRepository;
import com.example.Angle.Config.SecServices.Account.Interfaces.AccountAdminServiceInterface;
import com.example.Angle.Repositories.CommentRepository;
import com.example.Angle.Repositories.VideoRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AccountAdminService implements AccountAdminServiceInterface {

    private final Logger log = LogManager.getLogger(AccountAdminService.class);

    private final AccountRepository accountRepository;

    private final VideoRepository videoRepository;

    private final CommentRepository commentRepository;


    @Autowired
    public AccountAdminService(AccountRepository accountRepository,
                               VideoRepository videoRepository,
                               CommentRepository commentRepository) {
        this.accountRepository = accountRepository;
        this.videoRepository = videoRepository;
        this.commentRepository = commentRepository;
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
    public void addUser(Account account) {
        this.accountRepository.save(account);
    }
}
