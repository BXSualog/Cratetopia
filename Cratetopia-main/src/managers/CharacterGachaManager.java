package managers;

import accounts.PlayerAcc;
import characters.Hero;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
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

    // Pull rates per star level, in PERMILLE (1000 = 100%). Admin can change them.
    // Index 0 = 1★ ... index 5 = 6★. They must always add up to 1000.
    private static final String RATES_CSV = "data/gacha_config.csv";
    private static final int[] DEFAULT_RATES = { 450, 300, 150, 70, 25, 5 };
    private final int[] rates = DEFAULT_RATES.clone();

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
        loadRates();   // use the admin's saved rates if data/gacha_config.csv exists
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
     * Rolls a star level (1–6) using the current pull rates.
     *
     * We roll a number between 0 and (total of the eligible rates) and walk
     * from 6★ down to the minimum star, subtracting each rate until the roll
     * lands inside a bucket (a cumulative-probability check, not a sort).
     *
     * When guaranteeAtLeast3Star is true, only 3★–6★ are eligible, so their
     * rates are used as relative weights.
     *
     * @param guaranteeAtLeast3Star  Enforce 3★ minimum (for the 10x pull guarantee).
     * @return                       A star level between 1 and 6.
     */
    private int rollStarTier(boolean guaranteeAtLeast3Star) {
        int minStar = guaranteeAtLeast3Star ? 3 : 1;

        int total = 0;
        for (int star = minStar; star <= 6; star++) {
            total += rates[star - 1];
        }
        if (total <= 0) return minStar;   // admin set every eligible rate to 0

        int roll = rng.nextInt(total);
        for (int star = 6; star >= minStar; star--) {   // rarest first
            if (roll < rates[star - 1]) return star;
            roll -= rates[star - 1];
        }
        return minStar;
    }

    // -------------------------------------------------------------------------
    // Admin: pull rates
    // -------------------------------------------------------------------------

    /** Rate (in permille) for a star level 1–6. */
    public int getRate(int stars) {
        return rates[stars - 1];
    }

    /** Text like "1★ (45%) | 2★ (30%) | ... | 6★ (0.5%)" for menus. */
    public String getRatesDisplay() {
        StringBuilder sb = new StringBuilder();
        for (int star = 1; star <= 6; star++) {
            if (star > 1) sb.append(" | ");
            sb.append(star).append("★ (").append(RateUtil.fmt(rates[star - 1])).append("%)");
        }
        return sb.toString();
    }

    /**
     * Replaces all six rates at once. The rates must add up to exactly 100%
     * (1000 permille), otherwise nothing changes.
     *
     * @param newRates  6 values in permille, index 0 = 1★ ... index 5 = 6★.
     * @return          true if accepted and saved.
     */
    public boolean setRates(int[] newRates) {
        if (newRates == null || newRates.length != 6) return false;
        int sum = 0;
        for (int r : newRates) {
            if (r < 0) return false;
            sum += r;
        }
        if (sum != 1000) return false;
        System.arraycopy(newRates, 0, rates, 0, 6);
        saveRates();
        return true;
    }

    public void resetRates() {
        System.arraycopy(DEFAULT_RATES, 0, rates, 0, 6);
        saveRates();
    }

    private void saveRates() {
        try (PrintWriter out = new PrintWriter(new FileWriter(RATES_CSV, false))) {
            out.println("stars,rate_permille");
            for (int star = 1; star <= 6; star++) {
                out.println(star + "," + rates[star - 1]);
            }
        } catch (IOException e) {
            System.err.println("[CharacterGachaManager] Failed to save rates: " + e.getMessage());
        }
    }

    private void loadRates() {
        File file = new File(RATES_CSV);
        if (!file.exists()) return;
        int[] loaded = new int[6];
        int count = 0;
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line = reader.readLine();   // skip header
            while ((line = reader.readLine()) != null && count < 6) {
                String[] p = line.trim().split(",");
                if (p.length == 2) {
                    loaded[count++] = Integer.parseInt(p[1].trim());
                }
            }
        } catch (IOException | NumberFormatException e) {
            return;   // unreadable file -> keep the defaults
        }
        if (count == 6) {
            int sum = 0;
            for (int r : loaded) sum += r;
            if (sum == 1000) System.arraycopy(loaded, 0, rates, 0, 6);
        }
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
