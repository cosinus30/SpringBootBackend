package com.innova.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "attempt")
public class Attempt {
    @Id
    @Column(name = "ip")
    private String ip;

    @Column(name = "attempt_counter")
    private int attemptCounter;

    public Attempt(){}

    public Attempt(String ip, int attemptCounter) {
        this.ip = ip;
        this.attemptCounter = attemptCounter;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getAttemptCounter() {
        return attemptCounter;
    }

    public void setAttemptCounter(int attemptCounter) {
        this.attemptCounter = attemptCounter;
    }
}
