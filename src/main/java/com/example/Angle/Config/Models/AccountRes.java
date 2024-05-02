package com.example.Angle.Config.Models;


import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class AccountRes {

    private String id;

    private String username;

    private String email;

    private int subscribers;

    private List<String> subscribedIds;

    private String avatar;


}
