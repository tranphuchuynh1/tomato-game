package com.raindrop.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import java.util.HashMap;
import java.util.Map;

public class ItemManager {
    private static final String PREFS_NAME = "game_items";
    private static final String CURRENT_ITEM_KEY = "current_item";

    // Item definitions
    public static class Item {
        public String id;
        public String name;
        public String texturePath;
        public int price;
        public boolean isPurchased;
        public boolean isEquipped;

        public Item(String id, String name, String texturePath, int price) {
            this.id = id;
            this.name = name;
            this.texturePath = texturePath;
            this.price = price;
            this.isPurchased = false;
            this.isEquipped = false;
        }
    }

    private Map<String, Item> items;
    private Preferences prefs;
    private String currentEquippedItem;

    public ItemManager() {
        prefs = Gdx.app.getPreferences(PREFS_NAME);
        initializeItems();
        loadItemStates();
    }

    private void initializeItems() {
        items = new HashMap<>();

        // Default item (always available)
        Item basket = new Item("basket", "Basket", "basket.png", 0);
        basket.isPurchased = true;
        items.put("basket", basket);

        // Store items - Bao gồm cả spaceship
        items.put("bowl", new Item("bowl", "Bowl", "bowl.png", 100));
        items.put("box", new Item("box", "Box", "box.png", 200));
        items.put("bucket", new Item("bucket", "Bucket", "bucket.png", 300));

        // QUAN TRỌNG: Thêm spaceship vào ItemManager
        items.put("spaceship", new Item("spaceship", "Phi thuyen", "spaceship.png", 0)); // Price = 0 vì mua bằng tiền thật
    }

    private void loadItemStates() {
        // Load purchased items
        for (Item item : items.values()) {
            // FIX: Kiểm tra null trước khi dùng equals()
            if (item.id != null) {
                item.isPurchased = prefs.getBoolean("purchased_" + item.id, "basket".equals(item.id));
            }
        }

        // Load current equipped item
        currentEquippedItem = prefs.getString(CURRENT_ITEM_KEY, "basket");

        // FIX: Reset tất cả isEquipped trước
        for (Item item : items.values()) {
            item.isEquipped = false;
        }

        // Set equipped cho item hiện tại
        if (currentEquippedItem != null && items.containsKey(currentEquippedItem)) {
            items.get(currentEquippedItem).isEquipped = true;
        } else {
            // Fallback to basket if current item is invalid
            currentEquippedItem = "basket";
            items.get("basket").isEquipped = true;
        }
    }

    public void saveItemStates() {
        // Save purchased items
        for (Item item : items.values()) {
            if (item.id != null) {
                prefs.putBoolean("purchased_" + item.id, item.isPurchased);
            }
        }

        // Save current equipped item
        if (currentEquippedItem != null) {
            prefs.putString(CURRENT_ITEM_KEY, currentEquippedItem);
        }
        prefs.flush();
    }

    public Item getItem(String itemId) {
        return items.get(itemId);
    }

    public Map<String, Item> getAllItems() {
        return items;
    }

    public String getCurrentEquippedTexture() {
        if (currentEquippedItem != null) {
            Item currentItem = items.get(currentEquippedItem);
            if (currentItem != null && currentItem.texturePath != null) {
                return currentItem.texturePath;
            }
        }
        return "basket.png"; // Fallback
    }

    public boolean canPurchase(String itemId, long playerTomatoes) {
        Item item = items.get(itemId);
        if (item == null || item.isPurchased) {
            return false;
        }
        return playerTomatoes >= item.price;
    }

    // FIX: Cải thiện method purchaseItem để hoạt động với cả spaceship
    public void purchaseItem(String itemId, long playerTomatoes) {
        Item item = items.get(itemId);
        if (item != null) {
            // Đối với spaceship hoặc items có price = 0, không cần kiểm tra canPurchase
            if (item.price == 0 || canPurchase(itemId, playerTomatoes)) {
                item.isPurchased = true;
                prefs.putBoolean("purchased_" + itemId, true);
                prefs.flush();

                Gdx.app.log("ItemManager", "Item purchased: " + itemId + " (isPurchased: " + item.isPurchased + ")");
            }
        } else {
            Gdx.app.error("ItemManager", "Item not found: " + itemId);
        }
    }

    public void equipItem(String itemId) {
        if (items.containsKey(itemId) && items.get(itemId).isPurchased) {
            // Unequip current item
            if (currentEquippedItem != null && items.containsKey(currentEquippedItem)) {
                items.get(currentEquippedItem).isEquipped = false;
            }

            // Equip new item
            currentEquippedItem = itemId;
            items.get(itemId).isEquipped = true;

            // FIX: Sử dụng cùng preferences instance
            prefs.putString(CURRENT_ITEM_KEY, itemId);
            prefs.flush();

            Gdx.app.log("ItemManager", "Item equipped: " + itemId);
        } else {
            Gdx.app.error("ItemManager", "Cannot equip item: " + itemId +
                " (exists: " + items.containsKey(itemId) +
                ", purchased: " + (items.get(itemId) != null ? items.get(itemId).isPurchased : "null") + ")");
        }
    }

    public String getCurrentEquippedItem() {
        return currentEquippedItem;
    }

    public boolean isItemPurchased(String itemId) {
        Item item = items.get(itemId);
        boolean purchased = item != null && item.isPurchased;
        Gdx.app.log("ItemManager", "isItemPurchased(" + itemId + "): " + purchased);
        return purchased;
    }

    public boolean isItemEquipped(String itemId) {
        boolean equipped = itemId != null && itemId.equals(currentEquippedItem);
        Gdx.app.log("ItemManager", "isItemEquipped(" + itemId + "): " + equipped + " (current: " + currentEquippedItem + ")");
        return equipped;
    }
}
