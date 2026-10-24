package accounts;

public class PlayerAcc extends Account {
    private int coins;
    private int shards;
    private int heroesCollected;
    private int itemsOwned;

    public PlayerAcc(String username, String password) {
        this(username, password, 500, 0, 0, 0);
    }

    public PlayerAcc(String username, String password, int coins, int shards, int heroesCollected, int itemsOwned) {
        super(username, password);
        this.coins = coins;
        this.shards = shards;
        this.heroesCollected = heroesCollected;
        this.itemsOwned = itemsOwned;
    }

    public int getCoins() {
        return coins;
    }

    public void setCoins(int coins) {
        this.coins = coins;
    }

    public int getShards() {
        return shards;
    }

    public void setShards(int shards) {
        this.shards = shards;
    }

    public int getHeroesCollected() {
        return heroesCollected;
    }

    public void setHeroesCollected(int heroesCollected) {
        this.heroesCollected = heroesCollected;
    }

    public int getItemsOwned() {
        return itemsOwned;
    }

    public void setItemsOwned(int itemsOwned) {
        this.itemsOwned = itemsOwned;
    }
}
