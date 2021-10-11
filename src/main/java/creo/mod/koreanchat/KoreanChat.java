package creo.mod.koreanchat;

import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.InBedChatScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.entity.player.PlayerWakeUpEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.event.lifecycle.InterModProcessEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fmlserverevents.FMLServerStartingEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Field;
import java.util.List;
import java.util.stream.Collectors;

@Mod("koreanchat")
public class KoreanChat {
    private static final Logger LOGGER = LogManager.getLogger();

    public static boolean korean = false;
    public static char colorChar = 888;

    public KoreanChat() {
        // Register the setup method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
        // Register the enqueueIMC method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::enqueueIMC);
        // Register the processIMC method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::processIMC);

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void setup(final FMLCommonSetupEvent event) {
        // some preinit code
        //LOGGER.info("HELLO FROM PREINIT");
        //LOGGER.info("DIRT BLOCK >> {}", Blocks.DIRT.getRegistryName());
    }

    private void enqueueIMC(final InterModEnqueueEvent event) {
        // some example code to dispatch IMC to another mod
        //InterModComms.sendTo("examplemod", "helloworld", () -> { LOGGER.info("Hello world from the MDK"); return "Hello world";});
    }

    private void processIMC(final InterModProcessEvent event) {
        // some example code to receive and process InterModComms from other mods
        /*LOGGER.info("Got IMC {}", event.getIMCStream().
                map(m->m.messageSupplier().get()).
                collect(Collectors.toList()));

         */
    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(FMLServerStartingEvent event) {
        // do something when the server starts
        // LOGGER.info("HELLO from server starting");
    }

    // You can use EventBusSubscriber to automatically subscribe events on the contained class (this is subscribing to the MOD
    // Event bus for receiving Registry Events)
    @Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
    public static class RegistryEvents {
        @SubscribeEvent
        public static void onGuiOpen(final GuiOpenEvent event) {
            // register a new block here
            Screen gui = event.getGui();
            if (gui instanceof ChatScreen) {
                ChatScreen guiChat = (ChatScreen) gui;
                boolean isSleep = false;
                if (gui instanceof InBedChatScreen)
                    isSleep = true;

                String defaultString = "";

                try {
                    Class<?> objClass = guiChat.getClass();
                    Field[] fields = objClass.getDeclaredFields();
                    for (Field field : fields) {
                        field.setAccessible(true);
                        if (field.getName().equals("field_146409_v") || field.getName().equals("initial")) {
                            defaultString = (String) field.get(guiChat);
                            break;
                        }
                    }
                } catch (Exception e) {

                }

                event.setGui(new KoreanChatScreen(defaultString, isSleep));
            }
        }

        @SubscribeEvent
        public static void onPlayerWakeUpEvent(final PlayerWakeUpEvent event) {
            event.getPlayer().closeContainer();
        }

        @SubscribeEvent
        public static void onClientChatReceived(final ClientChatReceivedEvent event) {
            LOGGER.info("onClientChatReceived");

            if (event.getMessage() instanceof TranslatableComponent) {
                TranslatableComponent message = (TranslatableComponent) event.getMessage();
                if (message.getString().contains(colorChar + "")) {
                    Object[] args = message.getArgs();

                    TextComponent chat;
                    if (args[1] instanceof String) {
                        chat = new TextComponent((String) args[1]);
                    } else if (args[1] instanceof TextComponent) {
                        chat = (TextComponent) args[1];
                    } else
                        return;

                    Style style = chat.getStyle();
                    chat = new TextComponent(chat.getText().replace(colorChar, '§'));
                    chat.setStyle(style);
                    args[1] = chat;
                    TranslatableComponent newMessage = new TranslatableComponent(message.getKey(), args);
                    newMessage.setStyle(message.getStyle());
                    event.setMessage(newMessage);
                }
            } else if (event.getMessage() instanceof TranslatableComponent) {
                /*StringTextComponent message = (StringTextComponent) event.getMessage();
                if (message.getFormattedText().contains(colorChar + "")) {
                    List<ITextComponent> args = message.getSiblings();
                    StringTextComponent chat = (StringTextComponent) args.get(1);
                    Style style = chat.getStyle();
                    chat = new StringTextComponent(chat.getText().replace(colorChar, '§'));
                    chat.setStyle(style);
                    args.set(1, chat);
                    event.setMessage(message);
                }*/
            }
        }
    }
}
