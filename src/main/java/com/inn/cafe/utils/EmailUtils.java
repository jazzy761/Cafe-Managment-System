package com.inn.cafe.utils;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EmailUtils {

    @Autowired
    private JavaMailSender emailSender;

    public void sentSimpleMessage(String to, String subject, String text , List<String> list){
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("k224006@nu.edu.pk");
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);

        if(list != null && list.size()>0){
            message.setCc(getCcArray(list));
        }
        emailSender.send(message);

    }

    private String[] getCcArray(List<String> ccList){
        String[] ccArray = new String[ccList.size()];
        for (int i =0 ; i<ccList.size(); i++){
            ccArray[i] = ccList.get(i);
        }
        return ccArray;
    }

    public void forgotMail(String to , String subject , String password) throws MessagingException {
        MimeMessage message = emailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);
        helper.setFrom("k224006@nu.edu.pk");
        helper.setTo(to);
        helper.setSubject(subject);
        String htmlMsg = "<p><p> Your Login details for Cafe Managment System</b><br>Email "+ to +"<br><b> Password:<b>"+password+"</br><a href=\"http://localhost:4200/\">Click here to login </a></p>";
        message.setContent(htmlMsg, "text/html");
        emailSender.send(message);
    }

}
