package com.example.Angle.Config.SecServices.Account;

import com.example.Angle.Config.Exceptions.CredentialExistsException;
import com.example.Angle.Config.Models.Account;
import com.example.Angle.Config.Models.UserRole;
import com.example.Angle.Config.SecRepositories.AccountRepository;
import com.example.Angle.Config.SecServices.Account.Interfaces.AccountRetrievalServiceInterface;
import com.example.Angle.Models.ReportTypes;
import com.example.Angle.Services.Images.ImageRetrievalService;
import org.apache.coyote.BadRequestException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


@Service
public class AccountRetrievalService implements AccountRetrievalServiceInterface {

    private final AccountRepository accountRepository;

    private final Logger log = LogManager.getLogger(AccountRetrievalService.class);

    private final ImageRetrievalService imageRetrievalService;


    @Autowired
    public AccountRetrievalService(AccountRepository accountRepository,
                                   ImageRetrievalService imageRetrievalService) {

        this.accountRepository = accountRepository;
        this.imageRetrievalService = imageRetrievalService;
    }

    @Override
    public boolean usernameExists(String username) throws CredentialExistsException {
        if(this.accountRepository.existsByUsername(username)){
            throw new CredentialExistsException("Username already exists!");
        }
        return false;
    }

    @Override
    public boolean emailExists(String email) throws CredentialExistsException {
        if(this.accountRepository.existsByEmail(email)){
            throw new CredentialExistsException("Email already exists!");
        }
        return false;
    }

    @Override
    public boolean isActive(String email) {
        if(accountRepository.existsByEmail(email)){
            return accountRepository.isActive(email);
        }
        return false;
    }

    @Override
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

    private AccountRecord getAccountRecord(Account account) throws IOException, ClassNotFoundException {
        List<String> subscribedIds = account.getSubscribedIds();
        return new AccountRecord(
                account.getId(),
                account.getUsername(),
                subscribedIds.size(),
                subscribedIds,
                imageRetrievalService.getImage(account.getAvatar()).getContent()
        );
    }

    @Override
    public AccountRecord generateAccountResponse(String accountId) throws IOException, ClassNotFoundException {
        Account account = accountRepository.findById(accountId).orElse(null);
        if(account == null){
            log.info("Account doesn't exist!");
            throw new UsernameNotFoundException("Account doesn't exist!");
        }
        return getAccountRecord(account);
    }
    @Override
    public AccountRecord generateAccountResponse(Account account) throws IOException, ClassNotFoundException {
        return getAccountRecord(account);
    }

    @Override
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

    @Override
    public List<Account> getBannedUsers() {
        log.info("Sending Banned Users");
        return new ArrayList<>(this.accountRepository.findByActive(false));
    }

    @Override
    public List<Account> getAllUsers() {
        List<Account> accounts = this.accountRepository.findAll();
        log.info("Sending All Users ["+accounts.size()+"]");
        return accounts;
    }

    @Override
    public Account getUser(String userId) {
        Account account = this.accountRepository.findById(userId).orElse(null);
        if(account == null){
            log.error("Requested user id: "+userId+" not FOUND!");
            throw new UsernameNotFoundException("User doesn't exists!");
        }
        return account;
    }

    @Override
    public Account getUserByEmail(String email) {
        Account account = this.accountRepository.findByEmail(email).orElse(null);
        if(account == null){
            log.error("Requested user email: "+email+" not FOUND!");
            throw new UsernameNotFoundException("User doesn't exists!");
        }
        return account;
    }

    @Override
    public Account getUserByUsername(String username) {
        Account account = this.accountRepository.findByUsername(username).orElse(null);
        if(account == null){
            log.error("Requested user email: "+username+" not FOUND!");
            throw new UsernameNotFoundException("User doesn't exists!");
        }
        return account;
    }

    @Override
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
}
