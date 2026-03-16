package fr.natsu.pride;

import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(WarriorsPride.MODID)
public class WarriorsPride {
	public static final String MODID = "pride";

    public WarriorsPride() {
    	IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
    	
    }
    
    @SubscribeEvent
    public static void onCommonSetup(FMLCommonSetupEvent event) {
    	
    }
}
