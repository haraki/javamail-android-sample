package com.example.mharaki.javamail_sample;

import android.os.AsyncTask;
import android.util.Log;

import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.Properties;

import javax.mail.AuthenticationFailedException;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class AsyncSendMail extends AsyncTask<String, Integer, Integer> {
    private class SmtpParams {
        private String account;
        private String password;
        private String address;
        private String charset;
        private Properties props;

        public SmtpParams(String account, String password, String address, String charset, Properties props)
        {
            this.account = account;
            this.password = password;
            this.address = address;
            this.charset = charset;
            this.props = props;
        }

        public String getAccount() { return this.account; }
        public String getPassword() { return this.password; }
        public String getAddress() { return this.address; }
        public String getCharset() { return this.charset; }
        public Properties getProps() { return this.props; }
    }

    final SmtpParams gmail_ssl = new SmtpParams(
        "",                         // account (ex:example@gmail.com)
        "",                         // password (ex:passw0rd)
        "",                         // mail address (ex:example@gmail.com)
        "UTF-8",
        new Properties()
        {
            {
                put("mail.debug", "true");
                put("mail.debug.auth", "true");
                put("mail.transport.protocol", "smtp");
                put("mail.smtp.host", "smtp.gmail.com");
                put("mail.smtp.port", "465");
                put("mail.smtp.auth", "true");
                put("mail.smtp.quitwait", "false");
                put("mail.smtp.socketFactory.port", "465");
                put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
                put("mail.smtp.socketFactory.fallback", "false");
                put("mail.smtp.connectiontimeout", 60000);
                put("mail.smtp.timeout", 60000);
            }
        }
    );

    final SmtpParams gmail_tls = new SmtpParams(
        "",                         // account (ex:example@gmail.com)
        "",                         // password (ex:passw0rd)
        "",                         // mail address (ex:example@gmail.com)
        "UTF-8",
        new Properties()
        {
            {
                put("mail.debug", "true");
                put("mail.transport.protocol", "smtp");
                put("mail.smtp.host", "smtp.gmail.com");
                put("mail.smtp.port", "587");
                put("mail.smtp.auth", "true");
                put("mail.smtp.starttls.enable", "true");
                put("mail.smtp.connectiontimeout", 60000);
                put("mail.smtp.timeout", 60000);
            }
        }
    );

    final SmtpParams yahoo = new SmtpParams(
        "",                         // account (ex:example)
        "",                         // password (ex:passw0rd)
        "",                         // mail address (ex:example@yahoo.co.jp)
        "ISO-2022-JP",
        new Properties()
        {
            {
                put("mail.debug", "true");
                put("mail.transport.protocol", "smtp");
                put("mail.smtp.host", "smtp.mail.yahoo.co.jp");
                put("mail.smtp.port", "587");
                put("mail.smtp.auth", "true");
            }
        }
    );

    static final String[] toMailAddress = { "" };       // destinasion (ex:mail@example.com)

    @Override
    protected Integer doInBackground(String... params) {
        String subject = params[0];
        String message = params[1];
        final SmtpParams smtpParams = gmail_ssl;
//        final SmtpParams smtpParams = gmail_tls;
//        final SmtpParams smtpParams = yahoo;

        Log.i("TestLog", "sendmail : " + message);

        Properties props = smtpParams.getProps();

        int ret = 0;
        for(int tryCount = 0;tryCount < 3;tryCount++) {
            try {
                if(ret == -2)
                {
                    // MessagingException が発生したら、ipv4アドレスで送信してみる
                    // If MessagingException occurs, use the ipv4 address.
                    InetAddress[] addresses = InetAddress.getAllByName(props.getProperty("mail.smtp.host"));
                    for (InetAddress address : addresses) {
                        Log.i("TestLog", "host : " + address.getHostName() + ", address : " + address.getHostAddress());

                        if(address.getHostAddress().indexOf(".") >= 0) {
                            props.setProperty("mail.smtp.host", address.getHostAddress());

                            break;
                        }
                    }
                }

                Session session = Session.getInstance(props, new Authenticator() {
                    @Override
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(smtpParams.getAccount(), smtpParams.getPassword());
                    }
                });
                session.setDebug(true);

                MimeMessage msg = new MimeMessage(session);

                msg.setHeader("Content-Type", "text/html");
                msg.setSentDate(new Date());
                msg.setSubject(subject, smtpParams.getCharset());
                msg.setFrom(new InternetAddress(smtpParams.getAddress(), "AsyncSendMail", smtpParams.getCharset()));
                msg.setSender(new InternetAddress(smtpParams.getAddress()));

                InternetAddress[] addresses = new InternetAddress[toMailAddress.length];
                for (int i = 0; i < toMailAddress.length; i++) {
                    addresses[i] = new InternetAddress(toMailAddress[i]);
                }

                msg.setRecipients(Message.RecipientType.TO, addresses);
                msg.setText(message, smtpParams.getCharset(), "plain");

                Transport.send(msg);

                Log.i("TestLog", "succeed.");

                ret = 0;

                break;
            } catch (AuthenticationFailedException ex) {
                Log.e("TestLog", "occurred exception. : " + ex.toString());
                ex.printStackTrace();

                ret = -1;
            } catch (MessagingException ex) {
                Log.e("TestLog", "occurred exception. : " + ex.toString());
                ex.printStackTrace();

                ret = -2;
            } catch (UnsupportedEncodingException ex) {
                Log.e("TestLog", "occurred exception. : " + ex.toString());
                ex.printStackTrace();

                ret = -3;
            } catch (UnknownHostException ex) {
                Log.e("TestLog", "occurred exception. : " + ex.toString());
                ex.printStackTrace();

                ret = -4;
            } catch (Exception ex) {
                Log.e("TestLog", "occurred exception. : " + ex.toString());
                ex.printStackTrace();

                ret = -10000;
            }

            try {
                Thread.sleep(100);
            } catch (InterruptedException ex) {
            }
        }

        return ret;
    }
}
