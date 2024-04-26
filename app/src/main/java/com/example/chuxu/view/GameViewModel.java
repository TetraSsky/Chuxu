package com.example.chuxu.view;

public class GameViewModel {
    String gameNameTextView;
    String gameTypeTextView;
    String gamePrixTextView;
    String gameDescTextView;
    String gameImgImageView;


    public GameViewModel(String gameNameTextView, String gameTypeTextView, String gamePrixTextView, String gameDescTextView, String gameImgImageView) {
        this.gameNameTextView = gameNameTextView;
        this.gameTypeTextView = gameTypeTextView;
        this.gamePrixTextView = gamePrixTextView;
        this.gameDescTextView = gameDescTextView;
        this.gameImgImageView = gameImgImageView;
    }

    public String getGameNameTextView() {
        return gameNameTextView;
    }

    public String getGameTypeTextView() {
        return gameTypeTextView;
    }

    public String getGamePrixTextView() {
        return gamePrixTextView;
    }

    public String getGameDescTextView() {
        return gameDescTextView;
    }

    public String getGameImgImageView() {
        return gameImgImageView;
    }
}
