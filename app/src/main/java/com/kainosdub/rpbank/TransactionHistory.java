package com.kainosdub.rpbank;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class TransactionHistory {
    Date dateTime;
    String toFrom;
    double amount, balance;


    public TransactionHistory(Date dateTime, String toFrom, double amount, double balance) {
        this.dateTime = dateTime;
        this.toFrom = toFrom;
        this.amount = amount;
        this.balance = balance;
    }
}
