package com.example.pma.content;

import com.example.pma.model.Route;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Content {

    /**
     * An array of sample routes.
     */
    public static final List<Route> routes = new ArrayList<Route>();

    /**
     * A map of sample routes, by ID.
     */
    public static final Map<String, Route> routesMap = new HashMap<String, Route>();

    private static final int COUNT = 25;

    static {
        // Add some sample route.
        for (int i = 1; i <= COUNT; i++) {
            addRoute(createDummyRoute(i));
        }
    }

    private static void addRoute(Route route) {
        routes.add(route);
        routesMap.put(route.id, route);
    }

    private static Route createDummyRoute(int position) {
        return new Route(String.valueOf(position), "Route " + position, makeDetails(position));
    }

    private static String makeDetails(int position) {
        StringBuilder builder = new StringBuilder();
        builder.append("Details about route: ").append(position);
        for (int i = 0; i < position; i++) {
            builder.append("\nMore details information here.");
        }
        return builder.toString();
    }

}
