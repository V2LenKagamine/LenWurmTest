package lensrandoms.wurm.lensrandoms;

import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.gotti.wurmunlimited.modloader.interfaces.Configurable;
import org.gotti.wurmunlimited.modloader.interfaces.Initable;
import org.gotti.wurmunlimited.modloader.interfaces.PreInitable;
import org.gotti.wurmunlimited.modloader.interfaces.ServerStartedListener;
import org.gotti.wurmunlimited.modloader.interfaces.WurmServerMod;
import org.gotti.wurmunlimited.modsupport.actions.ModActions;

public class LensRandoms implements WurmServerMod, Configurable, Initable, PreInitable, ServerStartedListener  {

	private Logger logger = Logger.getLogger(this.getClass().getName());
	
	@Override
	public void configure(Properties Properties) {
		
	}
	
	@Override
	public void preInit() {
		ModActions.init();
	}
	
	@Override
	public void init() {
		
	}

	@Override
	public void onServerStarted() {
		logger.log(Level.INFO,"LensRandoms actions registering...");
		ModActions.registerAction(new FunnyAction());
	}
	
}
