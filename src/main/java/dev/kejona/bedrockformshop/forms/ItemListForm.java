package dev.kejona.bedrockformshop.forms;

import dev.kejona.bedrockformshop.BedrockFormShop;
import dev.kejona.bedrockformshop.utils.ShopType;
import dev.kejona.bedrockformshop.utils.Permission;
import dev.kejona.bedrockformshop.utils.Placeholders;
import org.bukkit.configuration.file.FileConfiguration;
import org.geysermc.cumulus.form.SimpleForm;
import org.geysermc.cumulus.util.FormImage;
import org.geysermc.floodgate.api.FloodgateApi;

import java.util.*;

public class ItemListForm {

    public FileConfiguration config = BedrockFormShop.getInstance().getConfig();
    // A form with all shop items as buttons.
    public void itemList(UUID uuid, String category) {
        // Form Builder
        SimpleForm.Builder form = SimpleForm.builder()
        .title(Placeholders.placeholder(config.getString("form." + category + ".title"), category))
        .content(Placeholders.placeholder(config.getString("form." + category + ".content"), category));
        // Get all Buttons in config.
        Set<String> listButtons = Objects.requireNonNull(config.getConfigurationSection("form." + category + ".buttons")).getKeys(false);
        List<String> buttons = new ArrayList<>(listButtons);
        List<String> disabledButtons = new ArrayList<>();
        // Loop all buttons and add them to form.
        for (String button : buttons) {
            // Check if player has permission to this button. if not button will not be generated.
            String getPerm = config.getString("form." + category + ".buttons." + button + ".permission");

            if (Permission.valueOf(getPerm).checkPermission(uuid)) {
                String imageLocation = config.getString("form." + category + ".buttons." + button + ".image");
                String getItemName = config.getString("form." + category + ".buttons." + button + ".item");
                // Check if image is url or path.
                if (imageLocation != null) {
                    // Image default will get images from our github repo.
                    if (imageLocation.equalsIgnoreCase("default")) {
                        // Image is path.
                        assert getItemName != null;
                        form.button(button, FormImage.Type.URL, "https://raw.githubusercontent.com/Jens-Co/MinecraftItemImages/main/" + getItemName.toLowerCase() + ".png");
                    } else if (imageLocation.startsWith("http")) {
                        // Image is url.
                        form.button(button, FormImage.Type.URL, imageLocation);
                    } else {
                        // Image is path.
                        form.button(button, FormImage.Type.PATH, imageLocation);
                    }
                }
            } else {
                buttons.removeAll(disabledButtons);
            }
        }

        // Handle buttons responses.
        form.closedOrInvalidResultHandler(response -> {
            response.isClosed();
            response.isInvalid();
        });
        // response is valid
        form.validResultHandler(response -> {
            String clickedButton = buttons.get(response.clickedButtonId());
            String shopType = config.getString("form." + category + ".buttons." + clickedButton + ".type");
            SellBuyForm sellbuy = new SellBuyForm();
            // Loop all shop types and check if the clicked button is of that type.
            for (ShopType type : ShopType.values()) {
                if (type.name().equals(shopType)) {
                    // Check shop types
                    if (ShopType.ITEM == type || ShopType.ENCHANTMENT == type || ShopType.POTION == type) {
                        // Get item name from config.
                        String itemStackName = config.getString("form." + category + ".buttons." + clickedButton + ".item");
                        sellbuy.buysellForm(uuid, itemStackName, clickedButton, category, shopType);
                    }
                    if (ShopType.COMMAND == type) {
                        String command = config.getString("form." + category + ".buttons." + clickedButton + ".command");
                        sellbuy.buysellForm(uuid, command, clickedButton, category, shopType);
                    }
                }
            }
        });
        // Build form and send to player.
        FloodgateApi.getInstance().sendForm(uuid, form.build());
    }
}
