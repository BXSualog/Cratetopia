package managers;

import accounts.PlayerAcc;
import characters.Hero;

import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.Random;

/**
 * CharacterGachaManager – handles all hero gacha (summoning) mechanics.
 *
 * OOP Concepts demonstrated here
 * --------------------------------
 * ENCAPSULATION:
 *   The summoning logic (star-tier rolling, hero awarding) is hidden inside
 *   private methods.  The outside world only calls drawSingle() or drawTen()
 *   and receives a GachaResult.
 *
 * INNER CLASSES:
 *   GachaResult and DuplicateHeroEntry are declared as static nested classes
 *   inside CharacterGachaManager.  They are logically part of this class but
 *   are public so Main can read the results.
 *
 * DSA Concepts demonstrated here
 * --------------------------------
 * LinkedList<Hero>  summonedHeroes  (inside GachaResult)
 *   Stores the heroes pulled in order.  LinkedList supports fast add-to-tail.
 *
 * PriorityQueue<Hero>  highestRankPulls  (inside GachaResult)
 *   A min-heap (but Hero.compareTo is reversed so it acts like a MAX-heap).
 *   hero.compareTo() puts higher-star heroes first, so peek() always gives
 *   us the best hero pulled in this session.
 *   offer() (insert): O(log n)  |  peek() (read top): O(1)
 *
 * Duplicate hero policy:
 *   If the rolled hero is already in the player's roster, it converts to shards:
 *     3★ → 2   |  4★ → 5   |  5★ → 10  |  6★ → 25
 */
public class CharacterGachaManager {

    // Cost constants (public so Main can display them without magic numbers)
    public static final int SINGLE_PULL_SHARDS = 1;   // cost for one pull
    public static final int MULTI_PULL_SHARDS  = 10;  // cost for 10x pull

    // Shards given when a DUPLICATE hero is rolled (3★ to 6★).
    // Index = star level (indices 0–2 unused; indices 3–6 are valid).
    // 3★ -> 2 shards | 4★ -> 5 shards | 5★ -> 10 shards | 6★ -> 25 shards
    private static final int[] DUP_SHARDS_PER_STAR = { 0, 0, 0, 2, 5, 10, 25 };

    // CharacterDataManager provides the hero pool (all hero templates by class/stars)
    private final CharacterDataManager charData;

    // Random number generator — used for all probability rolls
    private final Random rng = new Random();

    /**
     * Constructor – injects the CharacterDataManager so this class can ask for
     * hero pools without knowing how heroes are loaded.
     *
     * @param charData  The shared character data source.
     */
    public CharacterGachaManager(CharacterDataManager charData) {
        this.charData = charData;
    }

    // =========================================================================
    // Public API
    // =========================================================================

    /**
     * Executes a SINGLE gacha pull (costs SINGLE_PULL_SHARDS shards).
     *
     * Steps:
     *   1. Check the player has enough shards.
     *   2. Deduct the cost.
     *   3. Roll one hero and award it (or convert to shards if duplicate).
     *   4. Return a GachaResult describing what happened.
     *
     * @param player  The player who is pulling.
     * @return        A GachaResult (check isSuccess() first).
     */
    public GachaResult drawSingle(PlayerAcc player) {
        // Guard: not enough shards → return a failure result
        if (player.getShards() < SINGLE_PULL_SHARDS) {
            return new GachaResult(false,
                    "Not enough shards! You need " + SINGLE_PULL_SHARDS + " shard" + (SINGLE_PULL_SHARDS > 1 ? "s" : "") + ".");
        }

        // Deduct cost from the player's shard balance
        player.setShards(player.getShards() - SINGLE_PULL_SHARDS);

        GachaResult res = new GachaResult(true, "Summon successful!");

        // Roll one hero (no guaranteed 3★ for a single pull)
        Hero hero = rollSingleHero(false);
        if (hero != null) {
            awardHero(player, res, hero);
        }
        return res;
    }

    /**
     * Executes a 10x gacha pull (costs MULTI_PULL_SHARDS shards).
     * Guarantees at least ONE hero of 3★ or higher among the 10 results.
     *
     * Steps:
     *   1. Check shards.
     *   2. Deduct cost.
     *   3. Roll 10 heroes; if the 10th would be below 3★ and no 3★+ was
     *      rolled yet, force a guaranteed re-roll at 3★+.
     *   4. Return a GachaResult.
     *
     * @param player  The player who is pulling.
     * @return        A GachaResult with up to 10 heroes.
     */
    public GachaResult drawTen(PlayerAcc player) {
        if (player.getShards() < MULTI_PULL_SHARDS) {
            return new GachaResult(false,
                    "Not enough shards! You need " + MULTI_PULL_SHARDS + " shards.");
        }

        player.setShards(player.getShards() - MULTI_PULL_SHARDS);
        GachaResult res = new GachaResult(true, "10x Summon successful!");

        boolean guaranteedTriggered = false; // tracks if we've already seen a 4★+

        for (int i = 0; i < 10; i++) {
            // On the LAST pull (i == 9), if no 4★+ was seen yet, force one
            boolean guarantee = (i == 9 && !guaranteedTriggered);

            Hero hero = rollSingleHero(guarantee);
            if (hero != null) {
                if (hero.getStars() >= 4) {
                    guaranteedTriggered = true; // we've satisfied the 4★+ guarantee
                }
                awardHero(player, res, hero);
            }
        }
        return res;
    }

    // =========================================================================
    // Private Rolling Logic
    // =========================================================================

    /**
     * Rolls a random star tier (3–6), then picks a random hero from that tier's pool.
     * Returns a NEW hero instance (not the shared template object).
     *
     * @param guaranteeAtLeast4Star  If true, the star tier is forced to be 4–6.
     * @return                       A new Hero instance, or null if no pool exists.
     */
    private Hero rollSingleHero(boolean guaranteeAtLeast4Star) {
        int stars = rollStarTier(guaranteeAtLeast4Star); // decide rarity first (3–6)

        // Get the pool of all heroes with exactly 'stars' stars
        LinkedList<Hero> pool = charData.getHeroesByStars(stars);

        // Fallback: if no heroes exist for that tier (e.g. 3★), fall back to 4★ or all heroes
        if (pool.isEmpty()) {
            pool = charData.getHeroesByStars(4);
        }
        if (pool.isEmpty()) {
            pool = charData.getAllHeroes();
        }
        if (pool.isEmpty()) return null; // no heroes at all (should never happen)

        // Pick a RANDOM hero from the pool (uniform random index 0..size-1)
        Hero template = pool.get(rng.nextInt(pool.size()));

        // Return a fresh instance so each summoned hero has its own equipment slots
        return charData.createNewHeroInstance(template.getId());
    }

    /**
     * Rolls a star level (3–6) based on weighted probability.
     * Heroes start at 3★ to 6★ (no 1★ or 2★ heroes).
     *
     * Probability table:
     *   6★:  2.0%  (roll 0–19)
     *   5★:  8.0%  (roll 20–99)
     *   4★: 25.0%  (roll 100–349)
     *   3★: 65.0%  (roll 350–999)
     *
     * 10x Pull Guarantee Table (guaranteeAtLeast4Star):
     *   6★:  5.0%  (roll 0–4)
     *   5★: 25.0%  (roll 5–29)
     *   4★: 70.0%  (roll 30–99)
     *
     * @param guaranteeAtLeast4Star  Enforce 4★ minimum (for 10x pull guarantee).
     * @return                       A star level between 3 and 6.
     */
    private int rollStarTier(boolean guaranteeAtLeast4Star) {
        if (guaranteeAtLeast4Star) {
            int gRoll = rng.nextInt(100); // 0–99 for the 4★+ guarantee sub-roll
            if (gRoll < 5)  return 6;     // 5.0%
            if (gRoll < 30) return 5;     // 25.0%
            return 4;                     // 70.0%
        }

        int roll = rng.nextInt(1000); // 0 – 999
        if (roll < 20)  return 6; // 2.0%
        if (roll < 100) return 5; // 8.0%
        if (roll < 350) return 4; // 25.0%
        return 3;                 // 65.0%
    }

    /**
     * Awards a hero to the player after a roll.
     *
     * If the player already owns a hero with the same ID (duplicate):
     *   → Don't add the hero again; instead give shard compensation.
     *
     * Either way, the result is recorded in the GachaResult so the UI can
     * display what the player got.
     *
     * @param player  The player receiving the hero (or shards).
     * @param res     The result object to record the outcome in.
     * @param hero    The hero that was rolled.
     */
    private void awardHero(PlayerAcc player, GachaResult res, Hero hero) {
        // PlayerAcc.addHero() returns false if the hero is already in the roster
        boolean added = player.addHero(hero);

        if (added) {
            // New hero — add it to the summon result list and priority queue
            res.addHero(hero);
        } else {
            // Duplicate — compensate with shards based on the hero's star level (3 to 6)
            int stars  = Math.max(3, Math.min(6, hero.getStars()));
            int shards = DUP_SHARDS_PER_STAR[stars];
            player.setShards(player.getShards() + shards); // add shards to balance
            res.addDuplicateHero(hero, shards);            // record in result
        }
    }

    // =========================================================================
    // Inner class: GachaResult
    // =========================================================================

    /**
     * GachaResult – a value object that captures everything that happened in
     * one gacha draw session (single or 10x).
     *
     * OOP Concept – INNER CLASS (static nested):
     *   Declared as "static class" inside CharacterGachaManager so it is
     *   logically grouped here, but any class can create a GachaResult object
     *   without needing an instance of CharacterGachaManager.
     *
     * DSA Concepts:
     *   LinkedList<Hero>  summonedHeroes    – new heroes in pull order
     *   PriorityQueue<Hero> highestRankPulls – max-heap ordered by star level;
     *                                          peek() gives the best hero pulled
     *   LinkedList<DuplicateHeroEntry>       – duplicate heroes that became shards
     */
    public static class GachaResult {

        private final boolean success; // true if the pull itself succeeded
        private final String  message; // status message (e.g. "Summon successful!")

        // DSA: LinkedList — keeps pull results in the order they were rolled
        private final LinkedList<Hero> summonedHeroes = new LinkedList<>();

        // DSA: PriorityQueue (Heap) — Hero.compareTo() puts highest stars first.
        //       offer() is O(log n), peek() is O(1) — gives the top hero cheaply.
        private final PriorityQueue<Hero> highestRankPulls = new PriorityQueue<>();

        // Tracks heroes that were duplicates (converted to shards instead)
        private final LinkedList<DuplicateHeroEntry> duplicateHeroes = new LinkedList<>();

        /**
         * Creates a new result with the given success flag and message.
         *
         * @param success  true = pull worked; false = not enough shards, etc.
         * @param message  Human-readable outcome message.
         */
        public GachaResult(boolean success, String message) {
            this.success = success;
            this.message = message;
        }

        /**
         * Records a newly obtained hero.
         * Adds to both the ordered LinkedList and the priority-queue heap.
         *
         * @param hero  The hero that was added to the player's roster.
         */
        public void addHero(Hero hero) {
            if (hero != null) {
                summonedHeroes.add(hero);    // O(1) add to tail of LinkedList
                highestRankPulls.offer(hero); // O(log n) heap insert
            }
        }

        /**
         * Records a duplicate hero that was converted to shards.
         *
         * @param hero           The duplicate hero that could not be added.
         * @param shardsAwarded  How many shards were given as compensation.
         */
        public void addDuplicateHero(Hero hero, int shardsAwarded) {
            if (hero != null) {
                duplicateHeroes.add(new DuplicateHeroEntry(hero, shardsAwarded));
            }
        }

        // --- Getters ---

        /** Returns true if the gacha pull itself was successful (enough shards). */
        public boolean isSuccess()                              { return success; }

        /** Returns the status message string. */
        public String getMessage()                              { return message; }

        /** Returns the list of new heroes pulled in order. */
        public LinkedList<Hero> getSummonedHeroes()             { return summonedHeroes; }

        /**
         * Returns the PriorityQueue sorted highest-star-first.
         * Call peek() to see the best hero without removing it.
         */
        public PriorityQueue<Hero> getHighestRankPulls()        { return highestRankPulls; }

        /** Returns the list of duplicate-hero entries. */
        public LinkedList<DuplicateHeroEntry> getDuplicateHeroes() { return duplicateHeroes; }

        /** Returns true if any rolled hero was already owned (duplicate). */
        public boolean hasDuplicateHeroes()                     { return !duplicateHeroes.isEmpty(); }

        // =====================================================================
        // Inner class: DuplicateHeroEntry
        // =====================================================================

        /**
         * DuplicateHeroEntry – an immutable record of one hero-to-shards conversion.
         *
         * OOP Concept – NESTED INNER CLASS & IMMUTABILITY:
         *   Declared inside GachaResult.  All fields are private & final so this
         *   object can never be changed after creation — it is a safe "snapshot".
         */
        public static class DuplicateHeroEntry {

            private final Hero hero;         // the hero that was already owned
            private final int  shardsAwarded; // how many shards were given instead

            /**
             * @param hero          The duplicate hero.
             * @param shardsAwarded Shards awarded as compensation.
             */
            public DuplicateHeroEntry(Hero hero, int shardsAwarded) {
                this.hero          = hero;
                this.shardsAwarded = shardsAwarded;
            }

            /** Returns the duplicate hero object. */
            public Hero getHero()          { return hero; }

            /** Returns the number of shards given for this duplicate. */
            public int  getShardsAwarded() { return shardsAwarded; }
        }
    }
}
