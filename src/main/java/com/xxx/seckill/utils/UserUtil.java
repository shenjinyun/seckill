package com.xxx.seckill.utils;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.util.Date;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xxx.seckill.pojo.User;
import com.xxx.seckill.vo.ResponseBean;


import java.util.ArrayList;
import java.util.List;

/**
 * 生成用户工具类
 */
public class UserUtil {
    private static void createUser(int count) throws Exception {
        List<User> users = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            User user = new User();
            user.setId(13000000000L + i);
            user.setNickname("user" + i);
            user.setSalt("1a2b3c");
            user.setPassword(MD5Util.inputPassToDBPass("123456", user.getSalt()));
            user.setLoginCount(1);
            user.setRegisterDate(new Date());

            users.add(user);
        }
        System.out.println("create user");
        // 插入数据库
        Connection connection = getConnection();
        String sql = "insert into t_user(login_count, nickname, register_date, salt, password, id) values(?,?,?,?,?,?)";
        PreparedStatement pstmt = connection.prepareStatement(sql);
        for (User user : users) {
            pstmt.setInt(1, user.getLoginCount());
            pstmt.setString(2, user.getNickname());
            pstmt.setTimestamp(3, new Timestamp(user.getRegisterDate().getTime()));
            pstmt.setString(4, user.getSalt());
            pstmt.setString(5, user.getPassword());
            pstmt.setLong(6, user.getId());
            pstmt.addBatch();
        }
        pstmt.executeBatch();
        pstmt.clearParameters();
        connection.close();
        System.out.println("insert to db");
        // 登录，生成userTicket

        /*
        * 这里要用doLogin，而不是toLogin，不然会产生json错误
        *错误：Unexpected character ('<' (code 60)): expected a valid value (JSON String, Number, Array, Object or token 'null', 'true' or 'false')
        解决方法：将请求地址中/toLogin改为doLogin；
        说明：因为toLogin方法会跳到login.html页面，然后才会协同登录信息返回，再进行doLogin方法获取相对应的userTicket的cookie值。可能前段页面有些字符无法解析。但是我们只用将用户信息返回给doLogin方法即可，所以不需要先利用toLogin方法跳到login.html页面。
        *
        */
        String urlString = "http://43.142.78.15:8080/login/doLogin";
        File file = new File("C:\\Users\\shenjinyun\\Desktop\\seckill\\test\\config.txt");
        if (file.exists()) {
            file.delete();
        }
        RandomAccessFile raf = new RandomAccessFile(file, "rw");
        file.createNewFile();
        raf.seek(0);
        for (int i = 0; i < users.size(); i++) {
            User user = users.get(i);
            URL url = new URL(urlString);
            HttpURLConnection co = (HttpURLConnection) url.openConnection();
            co.setRequestMethod("POST");
            co.setDoOutput(true);
            OutputStream out = co.getOutputStream();
            String params = "mobile=" + user.getId() + "&password=" + MD5Util.inputPassToFromPass("123456");
            // String params = "    ";
            out.write(params.getBytes());
            out.flush();
            InputStream inputStream = co.getInputStream();
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            byte[] buff = new byte[1024];
            int len = 0;
            while ((len = inputStream.read(buff)) >= 0) {
                bout.write(buff, 0, len);
            }
            inputStream.close();
            bout.close();
            String response = new String(bout.toByteArray());
            ObjectMapper mapper = new ObjectMapper();
            ResponseBean responseBean = mapper.readValue(response, ResponseBean.class);
            String userTicket = ((String) responseBean.getObj());
            System.out.println("create userTicket: " + user.getId());
            String row = user.getId() + "," + userTicket;
            raf.seek(raf.length());
            raf.write(row.getBytes());
            raf.write("\r\n".getBytes());
            System.out.println("write to file: " + user.getId());
        }
        raf.close();
        System.out.println("over");
    }

    private static Connection getConnection() throws Exception {
        String url = "jdbc:mysql://43.142.78.15:3306/seckill?useUnicode=true&characterEncoding=UTF-8&serverTimeZone=Asia/Shanghai";
        String username = "xxxx"; // root用户不能远程连接
        String password = "123456";
        String driver = "com.mysql.cj.jdbc.Driver";
        Class.forName(driver);
        return DriverManager.getConnection(url, username, password);
    }

    public static void main(String[] args) throws Exception {
        createUser(5000);
    }
}
