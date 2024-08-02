package com.example.Angle.Services.Email.Interfaces;

import com.example.Angle.Config.Exceptions.MediaNotFoundException;

public interface MaintenanceMailsInterface {

    void restorePassword(String email, String userIP);

    void passwordChangeMail(String username) throws MediaNotFoundException;

    void confirmationEmail(String email) throws MediaNotFoundException;


}
