package net.stirdrem.overgeared.client;

public class ClientInit {

    public static void init() {

        // ✅ Register config screen ONLY on client
//        ModLoadingContext.get().registerExtensionPoint(
//                ConfigScreenHandler.ConfigScreenFactory.class,
//                () -> new ConfigScreenHandler.ConfigScreenFactory(
//                        (minecraft, parentScreen) -> new OvergearedConfigScreen(parentScreen)
//                )
//        );
    }
}
