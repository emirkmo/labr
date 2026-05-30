package com.github.emirkmo.labr;

import com.pixelmonmod.api.pokemon.PokemonSpecificationProxy;
import com.pixelmonmod.pixelmon.api.events.spawning.SpawnEvent;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.api.pokemon.species.Pokedex;
import com.pixelmonmod.pixelmon.api.pokemon.species.Species;
import com.pixelmonmod.pixelmon.api.spawning.archetypes.entities.pokemon.SpawnActionPokemon;
import com.pixelmonmod.pixelmon.entities.pixelmon.PixelmonEntity;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

final class PixelmonSpawnRandomizer {
    private volatile List<Species> speciesPool;

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onSpawn(final SpawnEvent event) {
        if (!(event.action instanceof SpawnActionPokemon)) {
            return;
        }

        final SpawnActionPokemon action = (SpawnActionPokemon) event.action;
        final PixelmonEntity entity = action.getOrCreateEntity();
        if (entity == null) {
            Labr.LOGGER.warn("Pixelmon spawn action had no entity; leaving spawn unchanged");
            return;
        }

        final Pokemon original = entity.getPokemon();
        final Species replacementSpecies = pickRandomSpecies();

        if (replacementSpecies == null) {
            Labr.LOGGER.warn("Pixelmon species pool is empty; leaving spawn unchanged");
            return;
        }

        final Pokemon replacement = PokemonSpecificationProxy.create(replacementSpecies.getName()).create();
        if (replacement == null) {
            Labr.LOGGER.warn("Could not create replacement Pokemon for {}", replacementSpecies.getName());
            return;
        }

        if (original != null) {
            replacement.setLevel(original.getPokemonLevel());
            replacement.setShiny(original.isShiny());
        }

        action.pokemon = replacement;
        entity.setPokemon(replacement);

        Labr.LOGGER.info("Randomized Pixelmon spawn: {} -> {}", getSpeciesName(original), replacementSpecies.getName());
    }

    private Species pickRandomSpecies() {
        final List<Species> pool = getSpeciesPool();
        if (pool.isEmpty()) {
            return null;
        }

        return pool.get(ThreadLocalRandom.current().nextInt(pool.size()));
    }

    List<Species> getSpeciesPool() {
        // Fast path after the first successful load. The volatile field makes this safe without locking.
        List<Species> pool = speciesPool;
        if (pool != null) {
            return pool;
        }

        synchronized (this) {
            // Another spawn thread may have populated the cache while this thread was waiting for the lock.
            pool = speciesPool;
            if (pool != null) {
                return pool;
            }

            pool = buildSpeciesPool(Pokedex.actualPokedex);
            if (pool.isEmpty()) {
                // Pixelmon fills actualPokedex during startup/config loading. Do not cache an empty
                // early read, otherwise this randomizer would stay disabled until the next game restart.
                return pool;
            }

            speciesPool = pool;
            Labr.LOGGER.info("Loaded {} Pixelmon species for random spawning", speciesPool.size());
            return speciesPool;
        }
    }

    static List<Species> buildSpeciesPool(final Species[] pokedex) {
        final List<Species> builtPool = new ArrayList<>();
        if (pokedex != null) {
            for (Species species : pokedex) {
                if (species != null) {
                    builtPool.add(species);
                }
            }
        }

        // Keep the cached pool stable even if Pixelmon later mutates/replaces its backing array.
        return Collections.unmodifiableList(builtPool);
    }

    private static String getSpeciesName(final Pokemon pokemon) {
        if (pokemon == null || pokemon.getSpecies() == null) {
            return "unknown";
        }

        return pokemon.getSpecies().getName();
    }
}
