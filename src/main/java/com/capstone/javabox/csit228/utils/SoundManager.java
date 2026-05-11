package com.capstone.javabox.csit228.utils;

import javafx.scene.media.AudioClip;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class SoundManager {

    private SoundManager() {
        throw new UnsupportedOperationException("Hey this is kinda like an autoload in Godot. Please don't instantiate this!");
    }

    private static final String SFX_FOLDER = "/com/capstone/javabox/csit228/audio/sfx/";
    private static final String MUSIC_FOLDER = "/com/capstone/javabox/csit228/audio/music/";

    //Cache so that it loads faster the next time you play the sound effect.
    private static final Map<String, AudioClip> audioCache = new HashMap<>();

    public static void playSFX(String fileName) {
        try {
            String fullFileName = fileName;

            AudioClip clip = audioCache.get(fullFileName);

            if (clip == null) {
                URL resource = SoundManager.class.getResource(SFX_FOLDER + fullFileName);

                if (resource == null) {
                    System.err.println("You dyslexic FAILURE! The sound effect doesn't exist! -> " + fullFileName);
                    return;
                }

                clip = new AudioClip(resource.toExternalForm());
                audioCache.put(fullFileName, clip);
            }

            //The pitch variation for sound effects is hard coded for now. This is for ease of use because
            //Usually, you want a sound effect not to be repetitive.
            double minPitch = 0.6;
            double maxPitch = 1.4;
            double randomPitch = minPitch + (Math.random() * (maxPitch - minPitch));

            clip.setRate(randomPitch);
            clip.play();

        } catch (Exception e) {
            System.err.println("Failed to play sound: " + e.getMessage());
        }
    }

    public static void playMusic(String fileName){
        try {
            String fullFileName = fileName;

            AudioClip clip = audioCache.get(fullFileName);

            if (clip == null) {
                URL resource = SoundManager.class.getResource(MUSIC_FOLDER + fullFileName);

                if (resource == null) {
                    System.err.println("You dyslexic FAILURE! The music track doesn't exist! -> " + fullFileName + " at " + MUSIC_FOLDER);
                    return;
                }

                clip = new AudioClip(resource.toExternalForm());
                audioCache.put(fullFileName, clip);
            }
            clip.play();

        } catch (Exception e) {
            System.err.println("Failed to play sound: " + e.getMessage());
        }
    }
}