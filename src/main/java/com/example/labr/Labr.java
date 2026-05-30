package com.example.labr;

import com.pixelmonmod.pixelmon.Pixelmon;
import net.minecraftforge.fml.ExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.network.FMLNetworkConstants;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(Labr.MOD_ID)
public final class Labr
{
    public static final String MOD_ID = "labr";

    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    public Labr() {
        ModLoadingContext.get().registerExtensionPoint(
            ExtensionPoint.DISPLAYTEST,
            () -> Pair.of(
                () -> FMLNetworkConstants.IGNORESERVERONLY,
                (remoteVersion, isNetworkConnection) -> true
            )
        );

        Pixelmon.EVENT_BUS.register(new PixelmonSpawnRandomizer());
        LOGGER.info("Registered Pixelmon spawn randomizer");
    }
}
