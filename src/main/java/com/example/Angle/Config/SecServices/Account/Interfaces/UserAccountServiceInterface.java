package com.example.Angle.Config.SecServices.Account.Interfaces;

import com.example.Angle.Config.Exceptions.MediaNotFoundException;
import org.apache.coyote.BadRequestException;

public interface UserAccountServiceInterface {

    void changeUserPassword(String username, String newPassword) throws MediaNotFoundException;

    void checkEmailConfirmation(String email) throws MediaNotFoundException, BadRequestException;








}
