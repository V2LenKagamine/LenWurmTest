package lensrandoms.wurm.lensrandoms;

import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.gotti.wurmunlimited.modloader.classhooks.CodeReplacer;
import org.gotti.wurmunlimited.modloader.classhooks.HookManager;
import org.gotti.wurmunlimited.modloader.classhooks.LocalNameLookup;
import org.gotti.wurmunlimited.modloader.interfaces.Configurable;
import org.gotti.wurmunlimited.modloader.interfaces.Initable;
import org.gotti.wurmunlimited.modloader.interfaces.PreInitable;
import org.gotti.wurmunlimited.modloader.interfaces.ServerStartedListener;
import org.gotti.wurmunlimited.modloader.interfaces.WurmServerMod;
import org.gotti.wurmunlimited.modsupport.actions.ModActions;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.CtPrimitiveType;
import javassist.NotFoundException;
import javassist.bytecode.BadBytecode;
import javassist.bytecode.Bytecode;
import javassist.bytecode.CodeAttribute;
import javassist.bytecode.Descriptor;
import javassist.bytecode.LocalVariableAttribute;
import javassist.bytecode.MethodInfo;

public class LensRandoms implements WurmServerMod, Configurable, Initable, PreInitable, ServerStartedListener  {

	private Logger logger = Logger.getLogger(this.getClass().getName());
	
	private boolean noDecayOnDeeds= true;
	private boolean enableBonsaiTrees= true;
	
	
	public static boolean doDecayCode(Boolean decay,Boolean onDeed,Boolean decaytimeql,Boolean isBulk,Integer isRandTick) {
		return (decay && !onDeed && (decaytimeql || isBulk || isRandTick == 0));
	}
	/* Future Features 
	 *  	Rename animals?
	 * 
	 * 
	 * 
	 * 
	 * 
	 */
	
	
	@Override
	public void configure(Properties properties) {
		noDecayOnDeeds = Boolean.valueOf(properties.getProperty("noDecayOnDeeds", Boolean.toString(noDecayOnDeeds)));
		enableBonsaiTrees = Boolean.valueOf(properties.getProperty("enableBonsaiTrees",Boolean.toString(enableBonsaiTrees)));
		logger.log(Level.INFO,"Items do not decay while on a deed: "+ noDecayOnDeeds);
	}
	
	@Override
	public void preInit() {
		ModActions.init();
	}
	
	@Override
	public void init() { 
		/*if (variable) {
			try {
				CtClass[] paramTypes = {
						CtPrimitiveType.longType,
						HookManager.getInstance().getClassPool().get("com.wurmonline.items.DbStrings")
				};
				
				HookManager.getInstance().registerHook("com.wurmonline.server.items", "decay", Descriptor.ofMethod(CtPrimitiveType.voidType, paramTypes), new InvocationHandlerFactory() {
					
					@Override
					public InvocationHandler createInvocationHandler() {
						return new InvocationHandler() {
							
							@Override
							public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
								
								return null;
							}
						};
					}
				});
				
				
			} catch (NotFoundException e) {
  				throw new HookException(e);
  			}
		} */

		if (noDecayOnDeeds) {
			ClassPool cPool = HookManager.getInstance().getClassPool();
			CtClass jrand = null;
			CtClass ctItem = null;
			CtClass serverRand = null;
			try {
			 ctItem = cPool.get("com.wurmonline.server.items.Item");
			 serverRand = cPool.get("com.wurmonline.server.Server");
			 jrand = cPool.get("java.util.Random");
			} catch (NotFoundException e) {
				logger.log(Level.SEVERE,"Cant find Item,Server, or Random class! Somethings gonna be Null!");
			}
			CtClass[] paramTypes = {
					ctItem,
					CtPrimitiveType.intType,
					CtPrimitiveType.booleanType,
					CtPrimitiveType.booleanType,
					CtPrimitiveType.booleanType,
					CtPrimitiveType.booleanType,
					CtPrimitiveType.booleanType
			};
			
			CtMethod method = null;
			try {
				method = ctItem.getMethod("poll", Descriptor.ofMethod(CtPrimitiveType.booleanType, paramTypes));
			} catch (NotFoundException e1) {
				logger.log(Level.SEVERE,"Cant find Poll method!");
				e1.printStackTrace();
			}
			
			MethodInfo methodInfo = method.getMethodInfo();
			CodeAttribute codeAttribute= methodInfo.getCodeAttribute();
			
			LocalNameLookup localNames = new LocalNameLookup((LocalVariableAttribute) codeAttribute.getAttribute(LocalVariableAttribute.tag));
			
			//Now for the Bytecode we're looking for...
			//Line 20626 in com.wurmonline.server.items.Item
		
			
			Bytecode bytecode = new Bytecode(methodInfo.getConstPool());
			
			//try {
			try {
				bytecode.addIload(localNames.get("decay"));
			} catch (NotFoundException e1) {
				logger.log(Level.SEVERE,"Cant find decay!");
				e1.printStackTrace();
			}
			bytecode.add(bytecode.IFEQ);
			bytecode.add(0,304);
		
			try {
				bytecode.addIload(localNames.get("decaytimeql"));
			} catch (NotFoundException e1) {
				logger.log(Level.SEVERE,"Cant find decaytimeql!");
				e1.printStackTrace();
			}
			bytecode.add(bytecode.IFNE);
			bytecode.add(0,21);
			bytecode.add(bytecode.ALOAD_0);
			bytecode.addInvokevirtual(ctItem,"isBulkItem","()Z");
			bytecode.add(bytecode.IFNE);
			bytecode.add(0,14);
			bytecode.addGetstatic(serverRand,"rand", "Ljava/util/Random;");
			try {
				bytecode.addIload(localNames.get("num"));
			} catch (NotFoundException e1) {
				logger.log(Level.SEVERE,"Cant find num!");
				e1.printStackTrace();
			}
			bytecode.addInvokevirtual(jrand,"nextInt", "(I)I");
			bytecode.add(bytecode.IFNE);
			bytecode.add(0,281);
			/*
			} catch (NotFoundException e) {
				logger.log(Level.SEVERE, "Error! Can't find Bytecode to replace!");
				e.printStackTrace();
			}
			*/
			
			byte[] search = bytecode.get();
			
			logger.log(Level.INFO, "Im trying to look for: " + bytecode );
			
			
			//Bytecode to replace it
			bytecode = new Bytecode(methodInfo.getConstPool());
	
			try {
			bytecode.addAload(localNames.get("decay"));
			bytecode.addAload(localNames.get("deeded"));
			bytecode.addAload(localNames.get("decaytimeql"));
			} catch (NotFoundException e) {
				logger.log(Level.SEVERE, "Cant find local name for decay,deeded, or decaytimeql!");
				e.printStackTrace();
			}
			bytecode.add(bytecode.ALOAD_0);
			bytecode.addInvokevirtual(ctItem,"isBulkItem", "()B");
			bytecode.addGetstatic(serverRand,"rand", "Ljava/util/Random;");
			try {
				bytecode.addInvokestatic(cPool.get(this.getClass().getName()), "doDecayCode", Descriptor.ofMethod(CtPrimitiveType.booleanType, new CtClass[] {CtPrimitiveType.booleanType,CtPrimitiveType.booleanType,CtPrimitiveType.booleanType,CtPrimitiveType.booleanType,CtPrimitiveType.intType}));
			} catch (NotFoundException e) {
				logger.log(Level.SEVERE, "Dear god, I can't find my own mods name.");
				e.printStackTrace();
			}
			bytecode.addGap(search.length - bytecode.length() - 3);
			bytecode.add(bytecode.IFNE);
			bytecode.add(0,281);
			byte[] replacement = bytecode.get();
			//Actually replace code
			
			try {
				new CodeReplacer(codeAttribute).replaceCode(search, replacement);
			} catch (NotFoundException | BadBytecode e) {
				logger.log(Level.SEVERE, "Either the bytecode is bad, or it cant find the bytecode to replace! " + e);
				e.printStackTrace();
			}
		
			
		}
	}

	@Override
	public void onServerStarted() {
		logger.log(Level.INFO,"LensRandoms actions registering...");
		if (enableBonsaiTrees) {
			ModActions.registerAction(new PlantBonsaiAction());
			ModActions.registerAction(new PickBonsaiAction());
			logger.log(Level.INFO, "Bonsai trees enabled!");
		}
	}
	
}
