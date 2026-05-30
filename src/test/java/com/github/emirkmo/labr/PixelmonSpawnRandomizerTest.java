package com.github.emirkmo.labr;

import com.pixelmonmod.pixelmon.api.pokemon.species.Pokedex;
import com.pixelmonmod.pixelmon.api.pokemon.species.Species;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class PixelmonSpawnRandomizerTest {
    private static Species species(final String name, final int dex) {
        return new Species(name, dex);
    }

    private Species[] originalPokedex;

    @Before
    public void storeOriginalPokedex() {
        originalPokedex = Pokedex.actualPokedex;
    }

    @After
    public void restoreOriginalPokedex() {
        Pokedex.actualPokedex = originalPokedex;
    }

    @Test
    public void getSpeciesPoolReadsNonNullSpeciesFromPixelmonPokedex() {
        final Species bulbasaur = species("Bulbasaur", 1);
        final Species mew = species("Mew", 151);
        Pokedex.actualPokedex = new Species[] {bulbasaur, null, mew};

        final List<Species> pool = new PixelmonSpawnRandomizer().getSpeciesPool();

        assertEquals(Arrays.asList(bulbasaur, mew), pool);
        assertSame(bulbasaur, pool.get(0));
        assertSame(mew, pool.get(1));
        assertUnmodifiable(pool);
    }

    @Test
    public void getSpeciesPoolRetriesAfterEmptyStartupRead() {
        final PixelmonSpawnRandomizer randomizer = new PixelmonSpawnRandomizer();
        Pokedex.actualPokedex = null;

        assertTrue(randomizer.getSpeciesPool().isEmpty());

        final Species eevee = species("Eevee", 133);
        Pokedex.actualPokedex = new Species[] {eevee};

        assertEquals(Collections.singletonList(eevee), randomizer.getSpeciesPool());
    }

    @Test
    public void getSpeciesPoolCachesFirstNonEmptyPokedexRead() {
        final PixelmonSpawnRandomizer randomizer = new PixelmonSpawnRandomizer();
        final Species eevee = species("Eevee", 133);
        Pokedex.actualPokedex = new Species[] {eevee};

        final List<Species> firstPool = randomizer.getSpeciesPool();
        Pokedex.actualPokedex = new Species[] {species("Mew", 151)};

        assertSame(firstPool, randomizer.getSpeciesPool());
        assertEquals(Collections.singletonList(eevee), randomizer.getSpeciesPool());
    }

    private static void assertUnmodifiable(final List<Species> pool) {
        try {
            pool.add(species("Mewtwo", 150));
            fail("Expected species pool to be unmodifiable");
        } catch (UnsupportedOperationException expected) {
            // Expected: cached pool callers should not be able to mutate future random choices.
        }
    }
}
