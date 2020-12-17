package com.fileServer.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name="users")
public class Users {

    @Id
    @Column(unique=true)
    private String user_id;
    @Column(unique=true)
    private String user_name;
    @Column
    private String password;
    @Column
    private int aouthority;
}
