package com.globaltrade.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "ships")
public class Ship {

    public enum Status {
        AT_PORT, IN_TRANSIT, MAINTENANCE
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(length = 45)
    private String name;

    private Integer capacity;

    // This is the field the EJB Timer Service updates on schedule —
    // nothing else in the app should write to it directly.
    @Enumerated(EnumType.STRING)
    @Column(length = 45)
    private Status status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "current_port_id")
    private Port currentPort;

    public Ship() {
    }

    public Ship(String name, Integer capacity, Status status, Port currentPort) {
        this.name = name;
        this.capacity = capacity;
        this.status = status;
        this.currentPort = currentPort;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getCapacity() {
        return capacity;
    }

    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Port getCurrentPort() {
        return currentPort;
    }

    public void setCurrentPort(Port currentPort) {
        this.currentPort = currentPort;
    }
}
