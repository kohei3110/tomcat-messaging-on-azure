package com.example;

import org.seasar.framework.container.S2Container;
import org.seasar.framework.container.factory.S2ContainerFactory;

public class App {
    private static final String PATH = "app.dicon";

    public static void main(String[] args) {
        S2Container container = S2ContainerFactory.create(PATH);
        container.init();
        try {
            MessageProvider messageProvider = (MessageProvider) container.getComponent(MessageProvider.class);
            System.out.println(messageProvider.getMessage());
        } finally {
            container.destroy();
        }
    }
}