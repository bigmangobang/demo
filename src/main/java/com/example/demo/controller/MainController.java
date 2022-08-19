package com.example.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@RequestMapping( "" )
@Controller
public class MainController extends BaseController{

    @RequestMapping("home")
    public ModelAndView home( HttpServletRequest request ) {
        ModelAndView view = new ModelAndView( );
        Map<String, Object> param = new HashMap<>( );
        view.setViewName( "hero" );
        param.put( "sid", new Date(  ).getTime() );
//        param.put( "ctx",request.getScheme()+"://" + request.getServerName()+ ":" + request.getServerPort() );
        view.addAllObjects( param );
        return view;
    }
    @RequestMapping("socket")
    @ResponseBody
    public HashMap<String,Object> socket( HttpServletRequest request ) {
        HashMap<String, Object> map = new HashMap<>( );
        map.put( "test","ad" );
//        String imgUrl = "https://game.gtimg.cn/images/lol/act/img/champion/" + alias + ".png";

        return map;
    }

}
