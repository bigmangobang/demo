package com.example.demo.utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Component
@ServerEndpoint( "/webSocket/{sid}" )
public class DemoEndPoint {
    public DemoEndPoint() {

    }
    private static AtomicInteger onlineNum = new AtomicInteger( );

    private static ConcurrentHashMap<String, Session> sessionPools = new ConcurrentHashMap<>( );

    private static ConcurrentHashMap<String, HashMap<String, Object>> data = new ConcurrentHashMap<>( );
    private static List<HashMap<String, Object>> heroList = getHeroList( );

    private static List<String> atmLeft ;

    private static List<String> atmRight ;

    public void sendMessage( Session session, String message ) throws IOException {
        if ( session != null ) {
            synchronized ( session ) {
                System.out.println( "send message -> " + message );
                session.getBasicRemote( ).sendText( message );
            }
        }
    }


    public void sendMessageToUser( String sid, String message ) throws IOException {
        Session session = sessionPools.get( sid );
        sendMessage( session, message );
    }

    public void sendMessageToAll( String message ) throws IOException {

        for ( String s : sessionPools.keySet( ) ) {
            Session session = sessionPools.get( s );
            sendMessage( session, message );
        }
    }

    @OnOpen
    public void onOpen( Session session, @PathParam( value = "sid" ) String sid ) {
        sessionPools.put( sid, session );

        onlineNum.incrementAndGet( );

        if(atmLeft.size() <= atmRight.size() ){
            atmLeft.add( sid );
        }else{
            atmRight.add( sid );
        }

        System.out.println( "user " + sid + " add websocket,current users is " + onlineNum );

        try {
            sendMessageToAll( "member_join|" + sid );
            StringBuilder msg = new StringBuilder( );
            msg.append( "init" );
            for ( String s : sessionPools.keySet( ) ) {
                if ( msg.length( ) == 0 ) {
                    msg.append( s );
                } else {
                    msg.append( "|" ).append( s );
                }
            }
            sendMessageToUser( msg.toString( ),sid );
        } catch ( Exception e ) {
            e.printStackTrace( );
        }

    }

    @OnClose
    public void onClose( @PathParam( "sid" ) String sid ) throws IOException {
        sessionPools.remove( sid );
        sendMessageToAll( "member_quit|" + sid );
        onlineNum.decrementAndGet( );

        System.out.println( sid + " close websocket,current users is " + onlineNum );
    }

    @OnMessage
    public void onMessage( String message ) throws IOException {
        String[]  msg= message.split( "\\|" );
        Random rand = new Random();
        switch ( msg[0] ){
            case "start":
                StringBuilder result = new StringBuilder( "hero" );
                for ( String s : sessionPools.keySet( ) ) {
                    int                     randomIndex   = rand.nextInt(heroList.size());
                    HashMap<String, Object> randomElement = heroList.get(randomIndex);
                    result.append( "|" ).append(  s)
                            .append( "," ).append( randomElement.get( "title" ) )
                            .append( "," ).append( randomElement.get( "alias" ) )
                            .append( "," ).append( randomElement.get( "selectAudio" ) );
                    heroList.remove(randomIndex);
                }
                sendMessageToAll( result.toString() );
                break;
        }
    }
//https://game.gtimg.cn/images/lol/act/img/js/hero/6.js  获取英雄信息
    @OnError
    public void onError( Session session, Throwable thrown ) {
        System.out.println( "error occured" );
        thrown.printStackTrace( );
    }

    private static List<HashMap<String, Object>> getHeroList( ) {
        HttpHeaders headers = new HttpHeaders( );
        HttpMethod  method  = HttpMethod.GET;
        // 将请求头部和参数合成一个请求
        MultiValueMap<String, String>             params        = new HttpHeaders( );
        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>( params, headers );
        // 执行HTTP请求，将返回的结构使用String类格式化
        ResponseEntity<String>        response     = new RestTemplate( ).exchange( "https://game.gtimg.cn/images/lol/act/img/js/heroList/hero_list.js", method, requestEntity, String.class );
        String                        responseBody = response.getBody( );
        JSONObject                    jsonObject   = JSONObject.parseObject( responseBody );
        List<HashMap<String, Object>> heroLst      = new ArrayList<>( );
        assert jsonObject != null;
        for ( String s : jsonObject.keySet( ) ) {
            if ( "hero".equals( s ) ) {
                JSONArray heroList = ( JSONArray ) jsonObject.get( s );
                for ( Object o : heroList ) {
                    JSONObject              heroJson = ( JSONObject ) o;
                    HashMap<String, Object> heroMap  = new HashMap<>( );
                    for ( String s1 : heroJson.keySet( ) ) {
                        heroMap.put( s1, heroJson.get( s1 ) );
                    }
                    heroLst.add( heroMap );
                }
            }
        }
        return heroLst;
    }

}