/*
 * Keystone Development Framework
 * Copyright (C) 2004-2009 Rick Knowles
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Library General Public License
 * Version 2 as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License Version 2 for more details.
 *
 * You should have received a copy of the GNU Library General Public License
 * Version 2 along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package arena.mail;

import java.util.Date;
import java.util.Enumeration;
import java.util.Properties;
import java.util.StringTokenizer;

import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Mail queue simulation in the DB. This allows retry for mail 
 * 
 * @author <a href="mailto:rick_knowles@hotmail.com">Rick Knowles</a>
 * @version $Id$
 */
public class MailSender {
    private final Log log = LogFactory.getLog(MailSender.class);

    private String smtpServer = "localhost";
    private String encoding = "UTF-8";
    private String smtpServerDelimiter = ",";
    private Properties mailProperties;
    private Properties mailHeaders;
    
    public void sendThreaded(MailMessage message, SendingFailureCallback... failureCallbacks) {
        Session session = makeSession(this.smtpServer, this.smtpServerDelimiter, this.mailProperties);
        Thread th = new Thread(new SendingRunnable(message, session, failureCallbacks), 
                "mail to:" + message.getToAddress() + " subject:" + message.getSubject());
        th.setDaemon(true);
        th.run();
    }
    
    public void sendSynchronous(MailMessage message) {
        Session session = makeSession(this.smtpServer, this.smtpServerDelimiter, this.mailProperties);
        new SendingRunnable(message, session, null).run();
    }
    
    public class SendingRunnable implements Runnable { 
        
        private MailMessage source;
        private Session session;
        private SendingFailureCallback[] failureCallbacks;
        
        public SendingRunnable(MailMessage source, Session session, SendingFailureCallback[] failureCallbacks) {
            this.source = source;
            this.session = session;
            this.failureCallbacks = failureCallbacks;
        }
        
        public void run() {
            log.info("Sending mail message:" + source);
            
            try {
                MimeMessage message = new MimeMessage(session);
                message.addRecipients(javax.mail.Message.RecipientType.TO, 
                        MailAddressUtils.parseAddressList(source.getToAddress(), ",", encoding));
                message.addRecipients(javax.mail.Message.RecipientType.CC, 
                        MailAddressUtils.parseAddressList(source.getCcAddress(), ",", encoding));
                message.addRecipients(javax.mail.Message.RecipientType.BCC, 
                        MailAddressUtils.parseAddressList(source.getBccAddress(), ",", encoding));
                InternetAddress[] from = MailAddressUtils.parseAddressList(
                        source.getSenderAddress(), ",", encoding);
                if ((from != null) && (from.length > 0)) {
                    message.setFrom(from[0]);
                }
                if (source.getSmtpInReplyToHeader() != null) {
                    message.setReplyTo(MailAddressUtils.parseAddressList(
                            source.getSmtpInReplyToHeader(), ",", encoding));
                }
                if (source.getSubject() != null) {
                    message.setSubject(source.getSubject(), encoding);
                }
                
                // If both, make a multipart
                if ((source.getBodyText() != null) && (source.getBodyHtml() != null)) {
                    Multipart multipart = new MimeMultipart("alternative");
                    MimeBodyPart textBlock = new MimeBodyPart();
                    MimeBodyPart htmlBlock = new MimeBodyPart();

                    textBlock.setText(source.getBodyText(), encoding);
                    htmlBlock.setContent(source.getBodyHtml(), "text/html; charset=" + encoding);
                    multipart.addBodyPart(textBlock);
                    multipart.addBodyPart(htmlBlock);

                    message.setContent(multipart);
                } else if (source.getBodyText() != null) {
                    // If text only, set the text part
                    message.setText(source.getBodyText(), encoding);
                } else if (source.getBodyHtml() != null) {
                    // If html only, set the html part
                    message.setContent(source.getBodyHtml(), "text/html;charset=" + encoding);
                } else {
                    // otherwise unknown
                    log.warn("WARNING: Both text and html parts were null for email recipient " + 
                            message.getRecipients(javax.mail.Message.RecipientType.TO)[0]);
                }

                // Send
                message.setSentDate(new Date());
                if (mailHeaders != null) {
                    for (Enumeration<?> e = mailHeaders.propertyNames(); e.hasMoreElements(); ) {
                        String name = (String) e.nextElement();
                        message.addHeader(name, mailHeaders.getProperty(name));
                    }
                }
                Transport.send(message);
            } catch (MessagingException err) {
                if (this.failureCallbacks == null) {
                    throw new RuntimeException("Error rendering mail contents", err);
                } else {
                    log.error("Error during message send, launching error callbacks", err);
                    for (SendingFailureCallback callback : this.failureCallbacks) {
                        callback.failed(source, err);
                    }
                }
            }        
        }
    }
    
    public interface SendingFailureCallback {
        public void failed(MailMessage mailMessage, Throwable error);
    }
    
    protected static Session makeSession(String smtpServer, String smtpServerDelimiter, Properties extraMailProperties) {
        Properties mailProps = new Properties();
        mailProps.put("mail.transport.protocol", "smtp");

        // Support alternate syntax for core properties: "server,user,pass,localhost"
        StringTokenizer st = new StringTokenizer(smtpServer, smtpServerDelimiter);
        String property = null;
        for (int i = 0; st.hasMoreElements(); i++) {
            property = st.nextToken();
            
            if (!property.trim().equals("")) {
                mailProps.put(PROPS_KEYS[i], property);
            }
        }
        if (extraMailProperties != null) {
            mailProps.putAll(extraMailProperties);
        }
        
        LogFactory.getLog(MailSender.class).debug("mailProps['mail.smtp.host'] ->" + mailProps.getProperty("mail.smtp.host"));
        return Session.getInstance(mailProps, null);
    }
    
    private static final String[] PROPS_KEYS = new String[] {
        "mail.smtp.host",
        "mail.smtp.username",
        "mail.smtp.password",
        "mail.smtp.localhost"
    };
    
    public void setSmtpServer(String smtpServer) {
        this.smtpServer = smtpServer;
    }

    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    public void setSmtpServerDelimiter(String smtpServerDelimiter) {
        this.smtpServerDelimiter = smtpServerDelimiter;
    }

    public void setMailProperties(Properties mailProperties) {
        this.mailProperties = mailProperties;
    }

    public void setMailHeaders(Properties mailHeaders) {
        this.mailHeaders = mailHeaders;
    }
}
