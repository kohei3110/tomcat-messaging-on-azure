package com.example;

import com.example.service.MyService;
import org.seasar.framework.container.factory.SingletonS2ContainerFactory;

public class Main {
    public static void main(String[] args) {
        SingletonS2ContainerFactory.init();
        MyService myService = (MyService) SingletonS2ContainerFactory.getContainer().getComponent(MyService.class);
        myService.execute();
        SingletonS2ContainerFactory.destroy();
    }
}