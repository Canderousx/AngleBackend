package com.example.Angle.Controllers.Unauth;


import com.example.Angle.Models.Video;
import com.example.Angle.Services.VideoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/unAuth/search")
@CrossOrigin(value = {"http://localhost:4200","http://192.168.100.36:4200"})
public class SearchController {

    @Autowired
    VideoService videoService;

    @RequestMapping(value = "",method = RequestMethod.GET)
    public List<Video>search(@RequestParam String q,
                             @RequestParam String page){
        return videoService.findVideos(q,Integer.parseInt(page));
    }

    @RequestMapping(value = "/helper",method = RequestMethod.GET)
    public List<String> searchHelper(@RequestParam String q){
        return videoService.searchHelper(q);
    }




}