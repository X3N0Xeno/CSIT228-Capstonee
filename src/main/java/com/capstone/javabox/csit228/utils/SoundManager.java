package com.capstone.javabox.csit228.utils;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.scene.media.AudioClip;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class SoundManager {

    private SoundManager() {
        throw new UnsupportedOperationException("Hey this is kinda like an autoload in Godot. Please don't instantiate this!");
    }
    private static final String SFX_FOLDER = "/com/capstone/javabox/csit228/audio/sfx/";
    private static final String MUSIC_FOLDER = "/com/capstone/javabox/csit228/audio/music/";

    private static final Map<String, AudioClip> audioCache = new HashMap<>();

    private static MediaPlayer currentMusicPlayer;
    private static String currentMusicName;

    public static void playSFX(String fileName) {
        try {
            AudioClip clip = audioCache.get(fileName);

            if (clip == null) {
                URL resource = SoundManager.class.getResource(SFX_FOLDER + fileName);

                if (resource == null) {
                    System.err.println("You dyslexic FAILURE! The sound effect doesn't exist! -> " + fileName);
                    return;
                }

                clip = new AudioClip(resource.toExternalForm());
                audioCache.put(fileName, clip);
            }

            //Random pitch for sound effects
            double minPitch = 0.6;
            double maxPitch = 1.4;
            double randomPitch = minPitch + (Math.random() * (maxPitch - minPitch));

            clip.setRate(randomPitch);
            clip.play();

        } catch (Exception e) {
            System.err.println("Failed to play sound effect: " + e.getMessage());
        }
    }

    public static void playMusic(String fileName) {
        try {
            //If requested track is already playing do nothing
            if (fileName.equals(currentMusicName) && currentMusicPlayer != null) {
                return;
            }

            URL resource = SoundManager.class.getResource(MUSIC_FOLDER + fileName);
            if (resource == null) {
                System.err.println("You dyslexic FAILURE! The music track doesn't exist! -> " + fileName + " at " + MUSIC_FOLDER);
                return;
            }

            Media media = new Media(resource.toExternalForm());
            MediaPlayer newPlayer = new MediaPlayer(media);
            newPlayer.setCycleCount(MediaPlayer.INDEFINITE); // Loop indefinitely!

            //If there is nothing playing...then play music
            if (currentMusicPlayer == null) {
                newPlayer.setVolume(1.0);
                newPlayer.play();

                currentMusicPlayer = newPlayer;
                currentMusicName = fileName;
            }
            //If there is something playing...then crossfade to new music
            else {
                MediaPlayer oldPlayer = currentMusicPlayer;

                currentMusicPlayer = newPlayer;
                currentMusicName = fileName;

                newPlayer.setVolume(0.0);
                newPlayer.play();

                Timeline crossfade = new Timeline(
                        new KeyFrame(Duration.ZERO,
                                new KeyValue(oldPlayer.volumeProperty(), oldPlayer.getVolume()),
                                new KeyValue(newPlayer.volumeProperty(), 0.0)
                        ),
                        new KeyFrame(Duration.seconds(1.5), // 1.5 second crossfade
                                new KeyValue(oldPlayer.volumeProperty(), 0.0),
                                new KeyValue(newPlayer.volumeProperty(), 1.0)
                        )
                );

                crossfade.setOnFinished(e -> {
                    oldPlayer.stop();
                    oldPlayer.dispose();
                });

                crossfade.play();
            }

        } catch (Exception e) {
            System.err.println("Failed to play music: " + e.getMessage());
        }
    }

    public static void stopMusic() {
        if (currentMusicPlayer != null) {
            MediaPlayer playerToStop = currentMusicPlayer;
            currentMusicPlayer = null;
            currentMusicName = null;
            Timeline fadeOut = new Timeline(
                    new KeyFrame(Duration.ZERO,
                            new KeyValue(playerToStop.volumeProperty(), playerToStop.getVolume())
                    ),
                    new KeyFrame(Duration.seconds(1.0),
                            new KeyValue(playerToStop.volumeProperty(), 0.0)
                    )
            );
            fadeOut.setOnFinished(e -> {
                playerToStop.stop();
                playerToStop.dispose();
            });
            fadeOut.play();
        }
    }
}