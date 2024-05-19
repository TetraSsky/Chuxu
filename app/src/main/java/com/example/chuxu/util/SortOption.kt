package com.example.chuxu.util

/**
 * L'énumération `SortOption` est utilisée pour déterminer l'ordre de tri et les filtres des éléments dans le `RecyclerView` de `GameViewModelAdapter`
 * Les différentes options de tri et de filtre sont sélectionnées par l'utilisateur à partir du menu de l'interface de recherche
 * - `NAME_ASC`: Tri par nom de jeu en ordre croissant (A à Z)
 * - `NAME_DESC`: Tri par nom de jeu en ordre décroissant (Z à A)
 * - `PRICE_ASC`: Tri par prix en ordre croissant
 * - `PRICE_DESC`: Tri par prix en ordre décroissant
 * - `TYPE`: Tri par type de jeu
 * - `GAME`: Filtre pour afficher uniquement les jeux
 * - `DLC`: Filtre pour afficher uniquement les DLC
 * - `DEMO`: Filtre pour afficher uniquement les démos
 * - `MUSIC`: Filtre pour afficher uniquement la musique
 * - `RESET`: Réinitialise les filtres et affiche tous les éléments
 */
enum class SortOption {
    NAME_ASC,
    NAME_DESC,
    PRICE_ASC,
    PRICE_DESC,
    TYPE,
    GAME,
    DLC,
    DEMO,
    MUSIC,
    RESET
}