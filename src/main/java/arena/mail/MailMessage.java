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

import arena.utils.SimpleValueObject;



public class MailMessage extends SimpleValueObject {
    
    private Long id;
    private String toAddress;
    private String ccAddress;
    private String bccAddress;
    private String senderAddress;
    private String replyToAddress;
    private String subject;
    private String bodyText;
    private String bodyHtml;
    private String smtpInReplyToHeader;
    private int retryCount;
    private Date nextRetryTimestamp;
    private Date sendingStartTimestamp;

    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getToAddress() {
        return toAddress;
    }
    public void setToAddress(String toAddress) {
        this.toAddress = toAddress;
    }
    public String getCcAddress() {
        return ccAddress;
    }
    public void setCcAddress(String ccAddress) {
        this.ccAddress = ccAddress;
    }
    public String getBccAddress() {
        return bccAddress;
    }
    public void setBccAddress(String bccAddress) {
        this.bccAddress = bccAddress;
    }
    public String getSenderAddress() {
        return senderAddress;
    }
    public void setSenderAddress(String senderAddress) {
        this.senderAddress = senderAddress;
    }
    public String getReplyToAddress() {
        return replyToAddress;
    }
    public void setReplyToAddress(String replyToAddress) {
        this.replyToAddress = replyToAddress;
    }
    public String getSubject() {
        return subject;
    }
    public void setSubject(String subject) {
        this.subject = subject;
    }
    public String getBodyText() {
        return bodyText;
    }
    public void setBodyText(String bodyText) {
        this.bodyText = bodyText;
    }
    public String getBodyHtml() {
        return bodyHtml;
    }
    public void setBodyHtml(String bodyHtml) {
        this.bodyHtml = bodyHtml;
    }
    public String getSmtpInReplyToHeader() {
        return smtpInReplyToHeader;
    }
    public void setSmtpInReplyToHeader(String smtpInReplyToHeader) {
        this.smtpInReplyToHeader = smtpInReplyToHeader;
    }
    public int getRetryCount() {
        return retryCount;
    }
    public void setRetryCount(int retryCount) {
        this.retryCount = retryCount;
    }
    public Date getNextRetryTimestamp() {
        return nextRetryTimestamp;
    }
    public void setNextRetryTimestamp(Date nextRetryTimestamp) {
        this.nextRetryTimestamp = nextRetryTimestamp;
    }
    public Date getSendingStartTimestamp() {
        return sendingStartTimestamp;
    }
    public void setSendingStartTimestamp(Date sendingStartTimestamp) {
        this.sendingStartTimestamp = sendingStartTimestamp;
    }
    
    // Methods for the digester population
    public void addToAddress(String name, String email) {
        String address = MailAddressUtils.formatAddress(name, email);
        if (getToAddress() == null) {
            setToAddress(address);
        } else if (getToAddress().trim().equals("")) {
            setToAddress(address);
        } else {
            setToAddress(getToAddress() + "," + address);
        }
    }
    public void addCcAddress(String name, String email) {
        String address = MailAddressUtils.formatAddress(name, email);
        if (getCcAddress() == null) {
            setCcAddress(address);
        } else if (getCcAddress().trim().equals("")) {
            setCcAddress(address);
        } else {
            setCcAddress(getCcAddress() + "," + address);
        }
    }
    public void addBccAddress(String name, String email) {
        String address = MailAddressUtils.formatAddress(name, email);
        if (getBccAddress() == null) {
            setBccAddress(address);
        } else if (getBccAddress().trim().equals("")) {
            setBccAddress(address);
        } else {
            setBccAddress(getBccAddress() + "," + address);
        }
    }
}
