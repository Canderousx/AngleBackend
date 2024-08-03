package com.example.Angle.Config.SecServices.Account.Interfaces;

import com.example.Angle.Config.Models.Account;

public interface AccountAdminServiceInterface {

    void removeDislikeInteractions(String videoId);

    void removeLikeInteractions(String videoId);

    void banAccount(String id);

    void unbanAccount(String id);

    void addUser(Account account);





}
