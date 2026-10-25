package managers;

import accounts.PlayerAcc;
import characters.Hero;

import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.Random;

/**
 * Handles Character Gacha summoning mechanics.
 * Uses LinkedList, PriorityQueue (Heaps), and Queue strictly adhering to DSA
 * constraints.
 *
 * Duplicate hero policy:
 * If a rolled hero is already owned by the player it is automatically
 * converted to shards based on the hero's star level:
 * 1-Star → 1 shard
 * 2-Star → 2 shards
 * 3-Star → 5 shards
 * 4-Star → 10 shards
 * 5-Star → 20 shards
 * 6-Star → 40 shards
 */
public class CharacterGachaManager {
    public static final int SINGLE_PULL_SHARDS = 1;
    public static final int MULTI_PULL_SHARDS = 10;

    // Shards awarded when a duplicate hero is rolled, indexed by star level (index 0 unused)
    private static final int[] DUP_SHARDS_PER_STAR = { 0, 1, 2, 5, 10, 20, 40 };

    private final CharacterDataManager charData;
    private final Random rng = new Random();

    public CharacterGachaManager(CharacterDataManager charData) {
        this.charData = charData;
    }

    /**
     * Executes a single character gacha draw.
     * Costs 1 shard.
     */
    public GachaResult drawSingle(PlayerAcc player) {
        if (player.getShards() < SINGLE_PULL_SHARDS) {
            return new GachaResult(false, "Not enough shards! You need " + SINGLE_PULL_SHARDS + " shard.");
        }

        player.setShards(player.getShards() - SINGLE_PULL_SHARDS);
        GachaResult res = new GachaResult(true, "Summon successful!");
        Hero hero = rollSingleHero(false);
        if (hero != null) {
            awardHero(player, res, hero);
        }
        return res;
    }

    /**
     * Executes a 10x character gacha draw.
     * Costs 10 shards and guarantees at least one 3-Star or higher character.
     */
    public GachaResult drawTen(PlayerAcc player) {
        if (player.getShards() < MULTI_PULL_SHARDS) {
            return new GachaResult(false, "Not enough shards! You need " + MULTI_PULL_SHARDS + " shards.");
        }

        player.setShards(player.getShards() - MULTI_PULL_SHARDS);
        GachaResult res = new GachaResult(true, "10x Summon successful!");

        boolean guaranteedTriggered = false;
        for (int i = 0; i < 10; i++) {
            boolean guarantee = (i == 9 && !guaranteedTriggered);
            Hero hero = rollSingleHero(guarantee);
            if (hero != null) {
                if (hero.getStars() >= 3) {
                    guaranteedTriggered = true;
                }
                awardHero(player, res, hero);
            }
        }
        return res;
    }

    /**
     * Rolls a random star tier (1-6) and instantiates a hero from the pool.
     */
    private Hero rollSingleHero(boolean guaranteeAtLeast3Star) {
        int stars = rollStarTier(guaranteeAtLeast3Star);
        LinkedList<Hero> pool = charData.getHeroesByStars(stars);

        if (pool.isEmpty()) {
            pool = charData.getAllHeroes();
        }

        if (pool.isEmpty())
            return null;

        Hero template = pool.get(rng.nextInt(pool.size()));
        return charData.createNewHeroInstance(template.getId());
    }

    /**
     * Rolls star level based on gacha odds:
     * 6-Star: 0.5%
     * 5-Star: 2.5%
     * 4-Star: 7.0%
     * 3-Star: 15.0%
     * 2-Star: 30.0%
     * 1-Star: 45.0%
     */
    private int rollStarTier(boolean guaranteeAtLeast3Star) {
        int roll = rng.nextInt(1000); // 0 - 999
        if (roll < 5)
            return 6; // 0.5%
        if (roll < 30)
            return 5; // 2.5%
        if (roll < 100)
            return 4; // 7.0%
        if (roll < 250)
            return 3; // 15.0%

        if (guaranteeAtLeast3Star) {
            // Re-roll between 3 to 6 star
            int gRoll = rng.nextInt(100);
            if (gRoll < 3)
                return 6;
            if (gRoll < 15)
                return 5;
            if (gRoll < 40)
                return 4;
            return 3;
        }

        if (roll < 550)
            return 2; // 30.0%
        return 1; // 45.0%
    }

    /**
     * Awards a hero to the player.
     * If the hero is a duplicate (player already owns it), converts it to shards
     * based on its star level instead.
     */
    private void awardHero(PlayerAcc player, GachaResult res, Hero hero) {
        boolean added = player.addHero(hero);
        if (added) {
            res.addHero(hero);
        } else {
            // Duplicate — compensate with shards
            int stars = Math.max(1, Math.min(6, hero.getStars()));
            int shards = DUP_SHARDS_PER_STAR[stars];
            player.setShards(player.getShards() + shards);
            res.addDuplicateHero(hero, shards);
        }
    }

    // =========================================================================
    // Inner class: GachaResult
    // =========================================================================

    public static class GachaResult {
        private final boolean success;
        private final String message;
        private final LinkedList<Hero> summonedHeroes = new LinkedList<>();
        // PriorityQueue (Min/Max Heap) to sort highest rarity pulls first
        private final PriorityQueue<Hero> highestRankPulls = new PriorityQueue<>();

        /** Tracks duplicate heroes and the shards they were converted to. */
        private final LinkedList<DuplicateHeroEntry> duplicateHeroes = new LinkedList<>();

        public GachaResult(boolean success, String message) {
            this.success = success;
            this.message = message;
        }

        public void addHero(Hero hero) {
            if (hero != null) {
                summonedHeroes.add(hero);
                highestRankPulls.offer(hero);
            }
        }

        /** Records a duplicate hero that was converted to shards. */
        public void addDuplicateHero(Hero hero, int shardsAwarded) {
            if (hero != null) {
                duplicateHeroes.add(new DuplicateHeroEntry(hero, shardsAwarded));
            }
        }

        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }

        public LinkedList<Hero> getSummonedHeroes() {
            return summonedHeroes;
        }

        public PriorityQueue<Hero> getHighestRankPulls() {
            return highestRankPulls;
        }

        public LinkedList<DuplicateHeroEntry> getDuplicateHeroes() {
            return duplicateHeroes;
        }

        /** Returns true if any rolled hero was a duplicate. */
        public boolean hasDuplicateHeroes() {
            return !duplicateHeroes.isEmpty();
        }

        /** Immutable record of one duplicate hero converted to shards. */
        public static class DuplicateHeroEntry {
            private final Hero hero;
            private final int shardsAwarded;

            public DuplicateHeroEntry(Hero hero, int shardsAwarded) {
                this.hero = hero;
                this.shardsAwarded = shardsAwarded;
            }

            public Hero getHero() {
                return hero;
            }

            public int getShardsAwarded() {
                return shardsAwarded;
            }
        }
    }
}
