package com.example.Angle.Models;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public enum ReportCategories {

    GraphicContent,
    Piracy,

    Scam,
    Violence;



    public static List<String> getAll(){
        List<String>categories = new ArrayList<>();
        for(ReportCategories cat : values()){
            categories.add(cat.name());
        }
        return categories;
    }


}
