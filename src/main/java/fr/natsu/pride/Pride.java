package fr.natsu.pride;

import com.llamalad7.mixinextras.MixinExtrasBootstrap;

import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(Pride.MODID)
public class Pride {
	public static final String MODID = "pride";

    public Pride() {
    	IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
    	MixinExtrasBootstrap.init();
    }
    
    @SubscribeEvent
    public static void onCommonSetup(FMLCommonSetupEvent event) {
    	
    }
}
