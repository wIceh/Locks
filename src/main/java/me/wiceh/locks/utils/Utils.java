package me.wiceh.locks.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.util.ArrayList;
import java.util.List;

public class Utils {

    public static List<Component> parseComponentList(List<String> list) {
        List<Component> components = new ArrayList<>();
        list.forEach(s -> {
            components.add(MiniMessage.miniMessage().deserialize(s));
        });
        return components;
    }
}
