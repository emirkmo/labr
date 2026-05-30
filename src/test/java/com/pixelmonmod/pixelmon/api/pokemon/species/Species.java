package com.pixelmonmod.pixelmon.api.pokemon.species;

public class Species {
    private final String name;
    private final int dex;

    public Species(final String name, final int dex) {
        this.name = name;
        this.dex = dex;
    }

    public String getName() {
        return name;
    }

    public int getDex() {
        return dex;
    }
}
