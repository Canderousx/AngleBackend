package com.example.Angle.Const;

public enum StoragePaths {

    VIDEOS_PATH("E:\\IT\\Angle\\Backend\\src\\main\\resources\\uploads\\videos"),

    IMAGES_PATH("E:\\IT\\Angle\\Backend\\src\\main\\resources\\uploads\\images"),;

    private final String paths;

    StoragePaths(String path){
        this.paths = path;
    }

    public String getPath(){
        return paths;
    }
}
