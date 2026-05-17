package com.capstone.javabox.csit228.games.ultimatettt;

public class Card {

    public enum Rarity { COMMON, RARE }
    public enum TargetType { INSTANT, TARGET_CELL, TARGET_SMALL_BOARD }

    public enum CardType {
        // Common
        SKIP_ENEMY_TURN     (Rarity.COMMON, TargetType.INSTANT,           "Skip Enemy Turn",      "Enemy loses their next turn."),
        DELETE_1_ENEMY      (Rarity.COMMON, TargetType.TARGET_CELL,       "Delete 1 Enemy",       "Remove one enemy symbol from the board."),
        DELETE_2_ENEMY      (Rarity.COMMON, TargetType.INSTANT,           "Delete 2 Random",      "Removes 2 random enemy symbols."),
        SUMMON_ALLY         (Rarity.COMMON, TargetType.INSTANT,           "Summon Ally",          "Places 1 random ally symbol on an empty cell."),
        BLOCK_CELL          (Rarity.COMMON, TargetType.TARGET_CELL,       "Block a Cell",         "Places a blocker on any empty cell."),
        STEAL               (Rarity.COMMON, TargetType.INSTANT,           "Steal",                "Takes a random card from opponent's hand."),
        REDRAW              (Rarity.COMMON, TargetType.INSTANT,           "Redraw",               "Discard hand and draw 3 new cards."),
        ANCHOR              (Rarity.COMMON, TargetType.TARGET_CELL,       "Anchor",               "Lock a cell you own — immune to Nuke and Delete."),
        DOUBLE_TURN         (Rarity.COMMON, TargetType.INSTANT,           "Double Turn",          "Play twice in a row this turn."),

        // Rare
        NUKE                (Rarity.RARE,   TargetType.TARGET_SMALL_BOARD,"☢ Nuke",               "Clears an entire 3x3 board (anchored cells survive)."),
        SWAP_SYMBOLS        (Rarity.RARE,   TargetType.INSTANT,           "⇄ Swap Symbols",       "All X become O and all O become X on the whole board."),
        CLAIM               (Rarity.RARE,   TargetType.TARGET_SMALL_BOARD,"★ Claim",              "Instantly claim any unclaimed big board."),
        MIRROR              (Rarity.RARE,   TargetType.INSTANT,           "↩ Mirror",             "Copy the last card your opponent played.");

        public final Rarity rarity;
        public final TargetType targetType;
        public final String displayName;
        public final String description;

        CardType(Rarity rarity, TargetType targetType, String displayName, String description) {
            this.rarity = rarity;
            this.targetType = targetType;
            this.displayName = displayName;
            this.description = description;
        }
    }

    private final CardType type;

    public Card(CardType type) {
        this.type = type;
    }

    public CardType getType()           { return type; }
    public String getDisplayName()      { return type.displayName; }
    public String getDescription()      { return type.description; }
    public Rarity getRarity()           { return type.rarity; }
    public TargetType getTargetType()   { return type.targetType; }

    @Override
    public String toString() { return type.displayName; }
}
