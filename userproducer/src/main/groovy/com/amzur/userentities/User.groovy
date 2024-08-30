package com.amzur.userentities

import io.micronaut.core.annotation.Introspected
import io.micronaut.serde.annotation.Serdeable

@Introspected
@Serdeable
class User {
    String name
    String email
    String password
    String phoneNumber
    String address
    User(String name,String email,String password,String phoneNumber,String address){
        this.name=name
        this.email=email
        this.password=password
        this.phoneNumber=phoneNumber
        this.address=address
    }

    @Override
    public String toString() {
        return "User{" +
                "name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", password='" + password + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", address='" + address + '\'' +
                '}';
    }
}
