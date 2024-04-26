package com.example.chuxu.view;

/*
Cette classe représente un modèle de données pour UN jeu spécifique
- Contient des chaînes de caractères pour le nom du jeu, le type, le prix, la description et l'URL de l'image
- Encapsule les données d'un jeu. Chaque instance de GameViewModel représente UN JEU INDIVIDUEL
 */

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
