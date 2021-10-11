package creo.mod.koreanchat;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.InBedChatScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.SignEditScreen;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerWakeUpEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Field;

@Mod("koreanchat")
public class KoreanChat {
    private static final Logger LOGGER = LogManager.getLogger();

    public static boolean korean = false;
    public static char colorChar = 888;

    public KoreanChat() {
        MinecraftForge.EVENT_BUS.register(this);
    }

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
                        /**
                         * find various
                         try {
                         LOGGER.info(field.getName() + " : " + (String) field.get(guiChat));
                         }catch(Exception e) {

                         }
                         */
                        if (field.getName().equals("f_95576_") || field.getName().equals("initial")) {
                            defaultString = (String) field.get(guiChat);
                            break;
                        }
                    }
                } catch (Exception e) {

                }

                event.setGui(new KoreanChatScreen(defaultString, isSleep));
            } else if (gui instanceof SignEditScreen) {
                SignEditScreen guiEditSign = (SignEditScreen) gui;
                SignBlockEntity tileSign = null;

                try {
                    Class<?> objClass = guiEditSign.getClass();
                    Field[] fields = objClass.getDeclaredFields();
                    for (Field field : fields) {
                        field.setAccessible(true);
                        /**
                         * find various
                         try {
                         LOGGER.info(field.getName() + " : " + (SignBlockEntity) field.get(guiEditSign));
                         }catch(Exception e) {

                         }
                         */

                        if (field.getName().equals("f_99254_") || field.getName().equals("sign")) {
                            tileSign = (SignBlockEntity) field.get(guiEditSign);
                            break;
                        }
                    }
                } catch (Exception e) {

                }

                if (tileSign != null)
                    event.setGui(new KoreanSignEditScreen(tileSign, Minecraft.getInstance().isTextFilteringEnabled()));
            }
        }

        @SubscribeEvent
        public static void onPlayerWakeUpEvent(final PlayerWakeUpEvent event) {
            event.getPlayer().closeContainer();
        }

        @SubscribeEvent
        public static void onClientChatReceived(final ClientChatReceivedEvent event) {
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
            }
        }
    }
}
