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
 *     1★ → 3   |  2★ → 5   |  3★ → 10  |  4★ → 20  |  5★ → 40  |  6★ → 70
 */
public class CharacterGachaManager {

    // Cost constants (public so Main can display them without magic numbers)
    public static final int SINGLE_PULL_SHARDS = 5;   // cost for one pull
    public static final int MULTI_PULL_SHARDS  = 45;  // cost for 10x pull (discounted)

    // Shards given when a DUPLICATE hero is rolled.
    // Index = star level (index 0 unused; indices 1-6 are valid).
    // Example: DUP_SHARDS_PER_STAR[3] = 10 means a duplicate 3★ gives 10 shards.
    private static final int[] DUP_SHARDS_PER_STAR = { 0, 3, 5, 10, 20, 40, 70 };

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
                    "Not enough shards! You need " + SINGLE_PULL_SHARDS + " shards.");
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

        boolean guaranteedTriggered = false; // tracks if we've already seen a 3★+

        for (int i = 0; i < 10; i++) {
            // On the LAST pull (i == 9), if no 3★+ was seen yet, force one
            boolean guarantee = (i == 9 && !guaranteedTriggered);

            Hero hero = rollSingleHero(guarantee);
            if (hero != null) {
                if (hero.getStars() >= 3) {
                    guaranteedTriggered = true; // we've satisfied the guarantee
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
     * Rolls a random star tier, then picks a random hero from that tier's pool.
     * Returns a NEW hero instance (not the shared template object).
     *
     * @param guaranteeAtLeast3Star  If true, the star tier is forced to be 3–6.
     * @return                       A new Hero instance, or null if no pool exists.
     */
    private Hero rollSingleHero(boolean guaranteeAtLeast3Star) {
        int stars = rollStarTier(guaranteeAtLeast3Star); // decide rarity first

        // Get the pool of all heroes with exactly 'stars' stars
        LinkedList<Hero> pool = charData.getHeroesByStars(stars);

        // Fallback: if no heroes exist for that tier, use the entire hero pool
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
     * Rolls a star level (1–6) based on weighted probability.
     *
     * Probability table:
     *   6★: 0.5%   (roll 0–4)
     *   5★: 2.5%   (roll 5–29)
     *   4★: 7.0%   (roll 30–99)
     *   3★: 15.0%  (roll 100–249)
     *   2★: 30.0%  (roll 250–549) — only if guaranteeAtLeast3Star is false
     *   1★: 45.0%  (roll 550–999) — only if guaranteeAtLeast3Star is false
     *
     * When guaranteeAtLeast3Star is true, rolls below 3★ are re-rolled
     * using a separate 3–6★ only probability table.
     *
     * DSA note – this is a simple cumulative-probability check, not a sort.
     * We roll a number 0–999 and check which bucket it falls in.
     *
     * @param guaranteeAtLeast3Star  Enforce 3★ minimum (for 10x pull guarantee).
     * @return                       A star level between 1 and 6.
     */
    private int rollStarTier(boolean guaranteeAtLeast3Star) {
        int roll = rng.nextInt(1000); // 0 – 999

        // Check from rarest to most common
        if (roll < 5)   return 6; // 0.5%
        if (roll < 30)  return 5; // 2.5%
        if (roll < 100) return 4; // 7.0%
        if (roll < 250) return 3; // 15.0%

        // At this point the roll is >= 250, which normally maps to 1★ or 2★.
        // But if the guarantee is active, we must re-roll at 3★+.
        if (guaranteeAtLeast3Star) {
            int gRoll = rng.nextInt(100); // 0–99 for the guarantee sub-roll
            if (gRoll < 3)  return 6;    // 3%
            if (gRoll < 15) return 5;    // 12%
            if (gRoll < 40) return 4;    // 25%
            return 3;                    // 60%
        }

        if (roll < 550) return 2; // 30.0%
        return 1;                 // 45.0%
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
            // Duplicate — compensate with shards based on the hero's star level
            // Math.max/min clamp stars to valid array indices [1, 6]
            int stars  = Math.max(1, Math.min(6, hero.getStars()));
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
