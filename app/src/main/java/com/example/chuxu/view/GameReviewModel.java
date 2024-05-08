package com.example.chuxu.view;

import java.time.LocalDate;

/**
 * Modèle de données pour représenter un avis d'utilisateur sur un jeu spécifique.
 */
public class GameReviewModel {
    private Integer userID;
    private String userName;
    private String gameName;
    private String reviewMessage;
    private LocalDate reviewDate;
    private Integer reviewID;

    public GameReviewModel(Integer userID, String userName, String gameName, String reviewMessage, LocalDate reviewDate, Integer reviewID) {
        this.userID = userID;
        this.userName = userName;
        this.gameName = gameName;
        this.reviewMessage = reviewMessage;
        this.reviewDate = reviewDate;
        this.reviewID = reviewID;
    }


    public Integer getUserID() { return userID; }

    public String getUserName() { return userName; }

    public String getGameName() { return gameName; }

    public String getReviewMessage() { return reviewMessage; }

    public LocalDate getReviewDate() { return reviewDate; }

    public Integer getReviewID() { return reviewID; }
}