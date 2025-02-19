package com.example.ice.entity;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class User {

    private String name;
    private Integer age;
    private Student student;




    //  重写hashcode和equals方法
    @Override
    public boolean equals(Object obj) {
        User user = (User) obj;
        return user.getName().equals(this.name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

}
