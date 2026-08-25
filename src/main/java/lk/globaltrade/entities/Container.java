package lk.globaltrade.entities;

import jakarta.persistence.*;

@Entity
@Table(name = "containers")
public class Container {

    public enum Status {
        AVAILABLE, RESERVED, IN_TRANSIT, UNAVAILABLE
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "container_number", length = 45, unique = true)
    private String containerNumber;

    @Enumerated(EnumType.STRING)
    @Column(length = 45)
    private Status status;

    public Container() {
    }

    public Container(String containerNumber, Status status) {
        this.containerNumber = containerNumber;
        this.status = status;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getContainerNumber() {
        return containerNumber;
    }

    public void setContainerNumber(String containerNumber) {
        this.containerNumber = containerNumber;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }
}
