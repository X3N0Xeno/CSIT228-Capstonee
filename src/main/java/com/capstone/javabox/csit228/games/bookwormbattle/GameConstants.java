package com.capstone.javabox.csit228.games.bookwormbattle;

public class GameConstants {
    public static final String LETTERS = "EEEEEEEEEEEEAAAAAAAAAIIIIIIIIIOOOOOOOONNNNNNRRRRRRTTTTTTLLLLSSSSUUUUUDDDDGGGBBCCMMPPFFHHVVWWYYKJXQZ";

    public enum GemType {
        POISON("#8e44ad"), HEAL("#27ae60"), POWER_DOWN("#e67e22"), FREEZE("#2980b9");
        public final String color;
        GemType(String c) { this.color = c; }
    }

    public enum CardType {
        SCRAMBLE_COL("Col Scramble"), SCRAMBLE_ROW("Row Scramble"), HEALING("Heal 5"),
        GEM_STEAL("Gem Steal"), GEM_NUKE("Gem Nuke"), LOCKDOWN("Lockdown"),
        SHOP_DELUXE("Shop Deluxe"), SHIELD("Shield"), HELP_ME("Extra Scramble");
        public final String label;
        CardType(String l) { this.label = l; }
    }

    public enum PotionType { HEALING, STRENGTH, PURIFY }
}