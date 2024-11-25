package com.example.Angle.Config.SecServices.Account.Interfaces;

import com.example.Angle.Config.Models.Account;
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





}
