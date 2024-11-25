package com.example.Angle.Config.SecServices.Account.Interfaces;

import com.example.Angle.Config.Models.Account;
import org.apache.coyote.BadRequestException;

import java.io.IOException;
import java.util.List;

public interface AccountRetrievalServiceInterface {
    record AccountRecord(
            String id,
            String username,
            int subscribers,
            List<String> subscribedIds,
            String avatar

    ) {};
    boolean usernameExists(String username);

    boolean emailExists(String email);

    boolean isActive(String accountId);

    boolean isAdmin() throws BadRequestException;

    AccountRecord generateAccountResponse(String accountId) throws IOException, ClassNotFoundException;

    AccountRecord generateAccountResponse(Account account) throws IOException, ClassNotFoundException;

    Account getCurrentUser() throws BadRequestException;

    List<Account> getBannedUsers();


    List<Account>getAllUsers();

    Account getUser(String userId);

    Account getUserByEmail(String email);

    Account getUserByUsername(String username);

    Account getMediaAuthor(String mediaType, String mediaId) throws BadRequestException;
}
