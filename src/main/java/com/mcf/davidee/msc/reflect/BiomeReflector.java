package com.mcf.davidee.msc.reflect;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.EnumCreatureType;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.biome.BiomeGenBase.SpawnListEntry;

import com.mcf.davidee.msc.BiomeNameHelper;
import com.mcf.davidee.msc.MobSpawnControls;

public class BiomeReflector {

    // TODO Cache
    public static List<SpawnListEntry> reflectList(BiomeGenBase biome, EnumCreatureType type) {
        try {
            int ordin = (type == EnumCreatureType.waterCreature) ? 2
                : (type == EnumCreatureType.ambient) ? 3 : type.ordinal();
            return reflect(biome, ordin);
        } catch (Exception e) {
            MobSpawnControls.getLogger()
                .severe("Unable to reflect list for biome " + BiomeNameHelper.getBiomeName(biome) + " of type " + type);
            MobSpawnControls.getLogger()
                .throwing("BiomeReflector", "reflectList", e);
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    private static List<SpawnListEntry> reflect(BiomeGenBase biome, int ordinal) throws Exception {
        Field[] all = BiomeGenBase.class.getDeclaredFields();
        String name = ordinalToFieldName(ordinal);

        if (name != null) {
            for (Field f : all) {
                if (name != null && f.getName() == name) {
                    f.setAccessible(true);
                    Object value = f.get(biome);
                    if (value != null && value instanceof List) {
                        return (List<SpawnListEntry>) value;
                    }
                }
            }
        }

        return new ArrayList<SpawnListEntry>();
    }

    private static String ordinalToFieldName(int ordinal) {
        if (ordinal == 0) return "spawnableMonsterList";
        else if (ordinal == 1) return "spawnableCreatureList";
        else if (ordinal == 2) return "spawnableWaterCreatureList";
        else if (ordinal == 3) return "spawnableCaveCreatureList";
        return "spawnableMonsterList"; // TODO: Make fully dynamically
    }
}
