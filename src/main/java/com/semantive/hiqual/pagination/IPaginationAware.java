package com.semantive.hiqual.pagination;

/**
 * Interfejs dla kolekcji paginowalnych.
 *
 * @author Jacek Lewandowski
 */
public interface IPaginationAware {
    /**
     * @return zwraca całkowitą liczbę wierszy w źródle danych.
     */
    int totalSize();

    /**
     * @return zwraca numer pierwszego elementu w tej kolekcji
     */
    int offset();
}
